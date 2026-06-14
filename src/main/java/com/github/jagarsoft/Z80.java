package com.github.jagarsoft;

import com.github.jagarsoft.ZuxApp.modules.tape.TAP;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.BitSet;

import static java.lang.Thread.sleep;

public class Z80 implements Z80OpCode, Cloneable {

    private final TAP tap;

    @Override
    public Z80 clone() {
        try {
            Z80 clone = (Z80) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    protected static class Register {
        public byte A;
        public byte B;
        public byte C;
        public byte D;
        public byte E;
        public byte H;
        public byte L;
        public BitSet F = new BitSet(8); // SF, ZF, xF ,HF, yF, PF, NF, CF;
    }

    //*** Begin Dispatcher Section
    protected int x; // xx-- ----
    protected int y; // --yy y---
    protected int z; // ---- -zzz
    protected int p; // --pp ----
    protected int q; // ---- q---
    protected byte d; // displacement for IX and IY

    protected int prefix; // DD or FD

    // Array to store reference to methods of different implementations
    protected OpCode[][][] opCodes   = new OpCode[4][8][8];
    protected OpCode[][][] CBopCodes = new OpCode[4][8][8];
    protected OpCode[][][] EDopCodes = new OpCode[4][8][8];
    protected OpCode[][][] DDopCodes = new OpCode[4][8][8];
    protected OpCode[][][] FDopCodes = new OpCode[4][8][8];
    protected OpCode[][][] DDCBopCodes= new OpCode[4][8][8];
    protected OpCode[][][] FDCBopCodes=new OpCode[4][8][8];

    private long tstate = 0;
    private long old_tstate = 0;
    private long old_tstate_out = 0;
    private long tstate_pseudo = 0; // TODO
    private long waiting_count = 0;
    private long unwaiting_count = 0;

    private static final int sz53n_addTable[] = new int[256];
    private static final int sz53pn_addTable[] = new int[256];
    private static final int sz53n_subTable[] = new int[256];
    private static final int sz53pn_subTable[] = new int[256];
    protected int sz5h3pnFlags;
    protected boolean carryFlag;

    // Posiciones de los flags
    private static final int CARRY_MASK = 0x01;
    private static final int ADDSUB_MASK = 0x02;
    private static final int PARITY_MASK = 0x04;
    private static final int OVERFLOW_MASK = 0x04; // alias de PARITY_MASK
    private static final int BIT3_MASK = 0x08;
    private static final int HALFCARRY_MASK = 0x10;
    private static final int BIT5_MASK = 0x20;
    private static final int ZERO_MASK = 0x40;
    private static final int SIGN_MASK = 0x80;
    // Máscaras de conveniencia
    private static final int FLAG_53_MASK = BIT5_MASK | BIT3_MASK;
    private static final int FLAG_SZ_MASK = SIGN_MASK | ZERO_MASK;
    private static final int FLAG_SZHN_MASK = FLAG_SZ_MASK | HALFCARRY_MASK | ADDSUB_MASK;
    private static final int FLAG_SZP_MASK = FLAG_SZ_MASK | PARITY_MASK;
    private static final int FLAG_SZHP_MASK = FLAG_SZP_MASK | HALFCARRY_MASK;

    static {
        boolean evenBits;

        for (int idx = 0; idx < 256; idx++) {
            if (idx > 0x7f) {
                sz53n_addTable[idx] |= SIGN_MASK;
            }

            evenBits = true;
            for (int mask = 0x01; mask < 0x100; mask <<= 1) {
                if ((idx & mask) != 0) {
                    evenBits = !evenBits;
                }
            }

            sz53n_addTable[idx] |= (idx & FLAG_53_MASK);
            sz53n_subTable[idx] = sz53n_addTable[idx] | ADDSUB_MASK;

            if (evenBits) {
                sz53pn_addTable[idx] = sz53n_addTable[idx] | PARITY_MASK;
                sz53pn_subTable[idx] = sz53n_subTable[idx] | PARITY_MASK;
            } else {
                sz53pn_addTable[idx] = sz53n_addTable[idx];
                sz53pn_subTable[idx] = sz53n_subTable[idx];
            }
        }

        sz53n_addTable[0] |= ZERO_MASK;
        sz53pn_addTable[0] |= ZERO_MASK;
        sz53n_subTable[0] |= ZERO_MASK;
        sz53pn_subTable[0] |= ZERO_MASK;
    }

    // Patch a hook for opCode
    public void dispatcherFor(byte prefix, byte opCode, Z80OpCode custom) {
        if( prefix == 0 ) {
            switch (opCode) {
                case (byte) 0xE7:
                    opCMasked(opCode);
                    opCodes[x][z][y] = custom::RST_y_8; // RST 20h
                    break;
            }
        } else {
            switch (prefix) {
                case (byte) 0xED:
                    opCMasked(opCode);
                    EDopCodes[x][z][y] = custom::RETI;
                    break;
            }
        }
    }

    protected void dispatcher(Z80OpCode opC) {
        // According to
        // http://www.z80.info/decoding.htm
        // http://www.z80.info/z80code.txt
        // https://floooh.github.io/2021/12/06/z80-instruction-timing.html
        // x = 0
        // z=0 [x][z][y]
        opCodes[0][0][0] = opC::NOP;
        opCodes[0][0][1] = opC::EX_AF_AF_;
        opCodes[0][0][2] = opC::DJNZ;
        opCodes[0][0][3] = opC::JR;
        opCodes[0][0][4] = opC::JR_cc;
        opCodes[0][0][5] = opC::JR_cc;
        opCodes[0][0][6] = opC::JR_cc;
        opCodes[0][0][7] = opC::JR_cc;
        // z=1 [x][z][y]
        opCodes[0][1][0b000] = opC::LD_rp_p_nn;
        opCodes[0][1][0b010] = opC::LD_rp_p_nn;
        opCodes[0][1][0b100] = opC::LD_rp_p_nn;
        opCodes[0][1][0b110] = opC::LD_rp_p_nn;
        opCodes[0][1][0b001] = opC::ADD_HL_rp_p;
        opCodes[0][1][0b011] = opC::ADD_HL_rp_p;
        opCodes[0][1][0b101] = opC::ADD_HL_rp_p;
        opCodes[0][1][0b111] = opC::ADD_HL_rp_p;
        // z=2 [x][z][y]
        opCodes[0][2][0b000] = opC::LD_BC_A;
        opCodes[0][2][0b010] = opC::LD_DE_A;
        opCodes[0][2][0b100] = opC::LD_mm_HL; // LD (mm), HL without ED prefix
        opCodes[0][2][0b110] = opC::LD_mm_A;
        opCodes[0][2][0b001] = opC::LD_A_BC;
        opCodes[0][2][0b011] = opC::LD_A_DE;
        opCodes[0][2][0b101] = opC::LD_HL_mm; // LD HL, (mm) without ED prefix
        opCodes[0][2][0b111] = opC::LD_A_mm;
        // z=3 [x][z][y]
        opCodes[0][3][0b000] = opC::INC_rp_p;
        opCodes[0][3][0b001] = opC::DEC_rp_p;
        opCodes[0][3][0b010] = opC::INC_rp_p;
        opCodes[0][3][0b011] = opC::DEC_rp_p;
        opCodes[0][3][0b100] = opC::INC_rp_p;
        opCodes[0][3][0b101] = opC::DEC_rp_p;
        opCodes[0][3][0b110] = opC::INC_rp_p;
        opCodes[0][3][0b111] = opC::DEC_rp_p;
        // z=4 [x][z][y]
        opCodes[0][4][0] = opC::INC_r_y;
        opCodes[0][4][1] = opC::INC_r_y;
        opCodes[0][4][2] = opC::INC_r_y;
        opCodes[0][4][3] = opC::INC_r_y;
        opCodes[0][4][4] = opC::INC_r_y;
        opCodes[0][4][5] = opC::INC_r_y;
        opCodes[0][4][6] = opC::INC_r_y;
        opCodes[0][4][7] = opC::INC_r_y;
        // z=5 [x][z][y]
        opCodes[0][5][0] = opC::DEC_r_y;
        opCodes[0][5][1] = opC::DEC_r_y;
        opCodes[0][5][2] = opC::DEC_r_y;
        opCodes[0][5][3] = opC::DEC_r_y;
        opCodes[0][5][4] = opC::DEC_r_y;
        opCodes[0][5][5] = opC::DEC_r_y;
        opCodes[0][5][6] = opC::DEC_r_y;
        opCodes[0][5][7] = opC::DEC_r_y;
        // z=6 [x][z][y]
        opCodes[0][6][0] = opC::LD_r_y_n;
        opCodes[0][6][1] = opC::LD_r_y_n;
        opCodes[0][6][2] = opC::LD_r_y_n;
        opCodes[0][6][3] = opC::LD_r_y_n;
        opCodes[0][6][4] = opC::LD_r_y_n;
        opCodes[0][6][5] = opC::LD_r_y_n;
        opCodes[0][6][6] = opC::LD_r_y_n; // LD (HL), n
        opCodes[0][6][7] = opC::LD_r_y_n;
        // z=7 [x][z][y]
        opCodes[0][7][0] = opC::RLCA;
        opCodes[0][7][1] = opC::RRCA;
        opCodes[0][7][2] = opC::RLA;
        opCodes[0][7][3] = opC::RRA;
        opCodes[0][7][4] = opC::DAA;
        opCodes[0][7][5] = opC::CPL;
        opCodes[0][7][6] = opC::SCF;
        opCodes[0][7][7] = opC::CCF;
        // x = 1
        // Exception: 7 * 7 combinations managed in fetch
        //     [x][z][y] (z=y=0 are dummies). Include LD (HL), r; LD r, (HL)
        opCodes[1][0][0] = opC::LD_r_y_r_z;
        opCodes[1][6][6] = opC::HALT;

        // x = 2, z = 0..7 Arithmetic & Logical instructions
        // Exception: iterates over z instead of y are 7 * 7 combinations managed in fetch,
        // but still opC::PTR is needed
        //     [x][z][y] (z=0 is dummy)
        opCodes[2][0][0] = opC::ADD_A_r_z;
        opCodes[2][0][1] = opC::ADC_A_r_z;
        opCodes[2][0][2] = opC::SUB_r_z;
        opCodes[2][0][3] = opC::SBC_A_r_z;
        opCodes[2][0][4] = opC::AND_r_z;
        opCodes[2][0][5] = opC::XOR_r_z;
        opCodes[2][0][6] = opC::OR_r_z;
        opCodes[2][0][7] = opC::CP_r_z;

        // x = 3
        // z=0 [x][z][y]
        opCodes[3][0][0] = opC::RET_cc_y;
        opCodes[3][0][1] = opC::RET_cc_y;
        opCodes[3][0][2] = opC::RET_cc_y;
        opCodes[3][0][3] = opC::RET_cc_y;
        opCodes[3][0][4] = opC::RET_cc_y;
        opCodes[3][0][5] = opC::RET_cc_y;
        opCodes[3][0][6] = opC::RET_cc_y;
        opCodes[3][0][7] = opC::RET_cc_y;
        // z=1 [x][z][y] (y=ppq)
        opCodes[3][1][0b000] = opC::POP_rp2_p;
        opCodes[3][1][0b010] = opC::POP_rp2_p;
        opCodes[3][1][0b100] = opC::POP_rp2_p;
        opCodes[3][1][0b110] = opC::POP_rp2_p;
        opCodes[3][1][0b001] = opC::RET;
        opCodes[3][1][0b011] = opC::EXX;
        opCodes[3][1][0b101] = opC::JP_HL;
        opCodes[3][1][0b111] = opC::LD_SP_HL;
        // z=2 [x][z][y]
        opCodes[3][2][0] = opC::JP_cc_y_nn;
        opCodes[3][2][1] = opC::JP_cc_y_nn;
        opCodes[3][2][2] = opC::JP_cc_y_nn;
        opCodes[3][2][3] = opC::JP_cc_y_nn;
        opCodes[3][2][4] = opC::JP_cc_y_nn;
        opCodes[3][2][5] = opC::JP_cc_y_nn;
        opCodes[3][2][6] = opC::JP_cc_y_nn;
        opCodes[3][2][7] = opC::JP_cc_y_nn;
        // z=3 [x][z][y]
        opCodes[3][3][0] = opC::JP_nn;

        /* CB Prefix */
        opCodes[3][3][1] = opC::CB_prefix;

        opCodes[3][3][2] = opC::OUT_n_A;
        opCodes[3][3][3] = opC::IN_A_n;
        opCodes[3][3][4] = opC::EX_SP_HL;
        opCodes[3][3][5] = opC::EX_DE_HL;
        opCodes[3][3][6] = opC::DI;
        opCodes[3][3][7] = opC::EI;
        // z=4 [x][z][y]
        opCodes[3][4][0] = opC::CALL_cc_y_nn;
        opCodes[3][4][1] = opC::CALL_cc_y_nn;
        opCodes[3][4][2] = opC::CALL_cc_y_nn;
        opCodes[3][4][3] = opC::CALL_cc_y_nn;
        opCodes[3][4][4] = opC::CALL_cc_y_nn;
        opCodes[3][4][5] = opC::CALL_cc_y_nn;
        opCodes[3][4][6] = opC::CALL_cc_y_nn;
        opCodes[3][4][7] = opC::CALL_cc_y_nn;
        // z=5 [x][z][y] (y=ppq)
        opCodes[3][5][0b000] = opC::PUSH_rp2_p;
        opCodes[3][5][0b010] = opC::PUSH_rp2_p;
        opCodes[3][5][0b100] = opC::PUSH_rp2_p;
        opCodes[3][5][0b110] = opC::PUSH_rp2_p;
        opCodes[3][5][0b001] = opC::CALL_nn;

        /* DD Prefix */
        opCodes[3][5][0b011] = opC::DD_prefix;

        /* ED Prefix */
        opCodes[3][5][0b101] = opC::ED_prefix;

        /* FD Prefix */
        opCodes[3][5][0b111] = opC::FD_prefix;

        // z=6 [x][z][y]
        opCodes[3][6][0] = opC::ADD_A_n;
        opCodes[3][6][1] = opC::ADC_A_n;
        opCodes[3][6][2] = opC::SUB_n;
        opCodes[3][6][3] = opC::SBC_A_n;
        opCodes[3][6][4] = opC::AND_n;
        opCodes[3][6][5] = opC::XOR_n;
        opCodes[3][6][6] = opC::OR_n;
        opCodes[3][6][7] = opC::CP_n;
        // z=7 [x][z][y]
        opCodes[3][7][0] = opC::RST_y_8; // RST 00h
        opCodes[3][7][1] = opC::RST_y_8; // RST 08h
        opCodes[3][7][2] = opC::RST_y_8; // RST 10h
        opCodes[3][7][3] = opC::RST_y_8; // RST 18h
        opCodes[3][7][4] = opC::RST_y_8; // RST 20h
        opCodes[3][7][5] = opC::RST_y_8; // RST 28h
        opCodes[3][7][6] = opC::RST_y_8; // RST 30h
        opCodes[3][7][7] = opC::RST_y_8; // RST 38h

        /* Table CB Prefix */

        // x = 0, y = 0..7
        // Exception: iterates over z instead of y are 7 * 7 combinations managed in fetch,
        // but still opC::PTR is needed
        //       [x][z][y] (z=0 is dummy)
        CBopCodes[0][0][0] = opC::RLC_r_z;
        CBopCodes[0][0][1] = opC::RRC_r_z;
        CBopCodes[0][0][2] = opC::RL_r_z;
        CBopCodes[0][0][3] = opC::RR_r_z;
        CBopCodes[0][0][4] = opC::SLA_r_z;
        CBopCodes[0][0][5] = opC::SRA_r_z;
        CBopCodes[0][0][6] = opC::SLL_r_z; // undocumented
        CBopCodes[0][0][7] = opC::SRL_r_z;

        // x = 1
        // Exception: 7 * 7 combinations managed in fetch
        //       [x][z][y] (z=y=0 are dummies)
        CBopCodes[1][0][0] = opC::BIT_y_r_z;
        // x = 2
        // Exception: 7 * 7 combinations managed in fetch
        //       [x][z][y]
        CBopCodes[2][0][0] = opC::RES_y_r_z;
        // x = 3
        // Exception: 7 * 7 combinations managed in fetch
        //       [x][z][y]
        CBopCodes[3][0][0] = opC::SET_y_r_z;

        /* Table ED Prefix */
        // x = 0 or x = 3 are invalid instructions, equivalent to NONI followed by NOP

        // x = 1
        // z=0   [x][z][y]
        EDopCodes[1][0][0b000] = opC::IN_r_y_C;
        EDopCodes[1][0][0b001] = opC::IN_r_y_C;
        EDopCodes[1][0][0b010] = opC::IN_r_y_C;
        EDopCodes[1][0][0b011] = opC::IN_r_y_C;
        EDopCodes[1][0][0b100] = opC::IN_r_y_C;
        EDopCodes[1][0][0b101] = opC::IN_r_y_C;
        EDopCodes[1][0][0b110] = opC::IN_r_y_C;
        EDopCodes[1][0][0b111] = opC::IN_r_y_C;
        // z=1   [x][z][y]
        EDopCodes[1][1][0b000] = opC::OUT_C_r_y;
        EDopCodes[1][1][0b001] = opC::OUT_C_r_y;
        EDopCodes[1][1][0b010] = opC::OUT_C_r_y;
        EDopCodes[1][1][0b011] = opC::OUT_C_r_y;
        EDopCodes[1][1][0b100] = opC::OUT_C_r_y;
        EDopCodes[1][1][0b101] = opC::OUT_C_r_y;
        EDopCodes[1][1][0b110] = opC::OUT_C_r_y;
        EDopCodes[1][1][0b111] = opC::OUT_C_r_y;
        // z=2   [x][z][y] (y=ppq)
        EDopCodes[1][2][0b000] = opC::SBC_HL_rp_p;
        EDopCodes[1][2][0b010] = opC::SBC_HL_rp_p;
        EDopCodes[1][2][0b100] = opC::SBC_HL_rp_p;
        EDopCodes[1][2][0b110] = opC::SBC_HL_rp_p;
        EDopCodes[1][2][0b001] = opC::ADC_HL_rp_p;
        EDopCodes[1][2][0b011] = opC::ADC_HL_rp_p;
        EDopCodes[1][2][0b101] = opC::ADC_HL_rp_p;
        EDopCodes[1][2][0b111] = opC::ADC_HL_rp_p;
        // z=3   [x][z][y] (y=ppq)
        EDopCodes[1][3][0b000] = opC::LD_mm_rp_p; // LD (mm), rp[p]
        EDopCodes[1][3][0b010] = opC::LD_mm_rp_p;
        EDopCodes[1][3][0b100] = opC::LD_mm_rp_p; // LD (mm), HL with ED prefix
        EDopCodes[1][3][0b110] = opC::LD_mm_rp_p;
        EDopCodes[1][3][0b001] = opC::LD_rp_p_mm; // LD rp[p], (mm)
        EDopCodes[1][3][0b011] = opC::LD_rp_p_mm;
        EDopCodes[1][3][0b101] = opC::LD_rp_p_mm; // LD HL, (mm) with ED prefix
        EDopCodes[1][3][0b111] = opC::LD_rp_p_mm;
        // z=4   [x][z][y]
        EDopCodes[1][4][0] = opC::NEG; // y=1..7 are undocumented managed in fetch
        // z=5   [x][z][y]
        EDopCodes[1][5][0] = opC::RETN; // except y != 1
        EDopCodes[1][5][1] = opC::RETI;

        // z=6   [x][z][y]
        EDopCodes[1][6][0b000] = opC::IM_im_y;
        EDopCodes[1][6][0b010] = opC::IM_im_y;
        EDopCodes[1][6][0b011] = opC::IM_im_y;
        // z=7   [x][z][y]
        EDopCodes[1][7][0] = opC::LD_I_A; // TODO flag affectation
        EDopCodes[1][7][1] = opC::LD_R_A;
        EDopCodes[1][7][2] = opC::LD_A_I;
        EDopCodes[1][7][3] = opC::LD_A_R;
        EDopCodes[1][7][4] = opC::RRD;
        EDopCodes[1][7][5] = opC::RLD;
        //EDopCodes[1][7][6] = opC::NONI; // TODO
        //EDopCodes[1][7][7] = opC::NONI;

        // x=2
        // z<=3 and y>=4
        //       [x][z][y]
        EDopCodes[2][0][0b100] = opC::LDI;
        EDopCodes[2][0][0b101] = opC::LDD;
        EDopCodes[2][0][0b110] = opC::LDIR;
        EDopCodes[2][0][0b111] = opC::LDDR;
        EDopCodes[2][1][0b100] = opC::CPI;
        EDopCodes[2][1][0b101] = opC::CPD;
        EDopCodes[2][1][0b110] = opC::CPIR;
        EDopCodes[2][1][0b111] = opC::CPDR;
        EDopCodes[2][2][0b100] = opC::INI;
        EDopCodes[2][2][0b101] = opC::IND;
        EDopCodes[2][2][0b110] = opC::INIR;
        EDopCodes[2][2][0b111] = opC::INDR;
        EDopCodes[2][3][0b100] = opC::OUTI;
        EDopCodes[2][3][0b101] = opC::OUTD;
        EDopCodes[2][3][0b110] = opC::OTIR;
        EDopCodes[2][3][0b111] = opC::OTDR;
        // x=3
        // z=y=5 [x][z][y
        EDopCodes[3][5][5] = opC::ED_prefix;

        /* Table DD Prefix */

        // x = 0
        // z=1   [x][z][y] (q=0)
        DDopCodes[0][1][0b100] = opC::LD_IX_nn;
        //                 (q=1)
        DDopCodes[0][1][0b001] = opC::ADD_IX_rp_p;
        DDopCodes[0][1][0b011] = opC::ADD_IX_rp_p;
        DDopCodes[0][1][0b101] = opC::ADD_IX_rp_p;
        DDopCodes[0][1][0b111] = opC::ADD_IX_rp_p;
        // z=2   [x][z][y]
        DDopCodes[0][2][4] = opC::LD_mm_IX;
        DDopCodes[0][2][5] = opC::LD_IX_mm;
        // z=3   [x][z][y]
        DDopCodes[0][3][0b100] = opC::INC_IX;
        DDopCodes[0][3][0b101] = opC::DEC_IX;
        // z=4   [x][z][y]
        DDopCodes[0][4][0] = opC::INC_r_y;
        DDopCodes[0][4][1] = opC::INC_r_y;
        DDopCodes[0][4][2] = opC::INC_r_y;
        DDopCodes[0][4][3] = opC::INC_r_y;
        DDopCodes[0][4][4] = opC::INC_IXH;
        DDopCodes[0][4][5] = opC::INC_IXL;
        DDopCodes[0][4][0b110] = opC::INC_IX_d;
        DDopCodes[0][4][7] = opC::INC_r_y;
        // z=5   [x][z][y]
        DDopCodes[0][5][0] = opC::DEC_r_y;
        DDopCodes[0][5][1] = opC::DEC_r_y;
        DDopCodes[0][5][2] = opC::DEC_r_y;
        DDopCodes[0][5][3] = opC::DEC_r_y;
        DDopCodes[0][5][4] = opC::DEC_IXH;
        DDopCodes[0][5][5] = opC::DEC_IXL;
        DDopCodes[0][5][0b110] = opC::DEC_IX_d;
        DDopCodes[0][5][7] = opC::DEC_r_y;
        // z=6   [x][z][y]
        DDopCodes[0][6][0] = opC::LD_r_y_n;
        DDopCodes[0][6][1] = opC::LD_r_y_n;
        DDopCodes[0][6][2] = opC::LD_r_y_n;
        DDopCodes[0][6][3] = opC::LD_r_y_n;
        DDopCodes[0][6][4] = opC::LD_IXH_n;
        DDopCodes[0][6][5] = opC::LD_IXL_n;
        DDopCodes[0][6][6] = opC::LD_IX_d_n; // LD (IX+d), n
        DDopCodes[0][6][7] = opC::LD_r_y_n;

        // x = 1
        // Exception: 7 * 7 combinations managed in fetch
        // z,y=4,5
        //       [x][z][y] (z=y=0 are dummies). Include LD (HL), r or LD r, (HL)
        DDopCodes[1][0][0] = opC::LD_r_y_r_z;
        DDopCodes[1][4][0] = opC::LD_r_y_IXH;
        DDopCodes[1][5][0] = opC::LD_r_y_IXL;
        DDopCodes[1][0][4] = opC::LD_IXH_r_z;
        DDopCodes[1][0][5] = opC::LD_IXL_r_z;
        DDopCodes[1][4][4] = opC::LD_IXH_IXH;
        DDopCodes[1][5][5] = opC::LD_IXL_IXL;
        DDopCodes[1][5][4] = opC::LD_IXH_IXL;
        DDopCodes[1][4][5] = opC::LD_IXL_IXH;
        // y=6   [x][z][y]       LD (IX+d), r
        DDopCodes[1][0][6] = opC::LD_IX_d_r_z;
        // z=6   [x][z][y]       LD r, (IX+d)
        DDopCodes[1][6][0] = opC::LD_r_y_IX_d;
        // z=y=6 [x][z][y]
        DDopCodes[1][6][6] = opC::NONI;

        // x = 2
        // z=4,5 [x][z][y]
        DDopCodes[2][4][0] = opC::ADD_A_IXH; // 0x84
        DDopCodes[2][5][0] = opC::ADD_A_IXL; // 0x85
        DDopCodes[2][4][1] = opC::ADC_A_IXH;
        DDopCodes[2][5][1] = opC::ADC_A_IXL;
        DDopCodes[2][4][2] = opC::SUB_IXH;
        DDopCodes[2][5][2] = opC::SUB_IXL;
        DDopCodes[2][4][3] = opC::SBC_IXH;
        DDopCodes[2][5][3] = opC::SBC_IXL;
        DDopCodes[2][4][4] = opC::AND_IXH;
        DDopCodes[2][5][4] = opC::AND_IXL;
        DDopCodes[2][4][5] = opC::XOR_IXH;
        DDopCodes[2][5][5] = opC::XOR_IXL;
        DDopCodes[2][4][6] = opC::OR_IXH;
        DDopCodes[2][5][6] = opC::OR_IXL;
        DDopCodes[2][4][7] = opC::CP_IXH;
        DDopCodes[2][5][7] = opC::CP_IXL;
        // z=6   [x][z][y]
        DDopCodes[2][6][0] = opC::ADD_A_IX_d;
        DDopCodes[2][6][1] = opC::ADC_A_IX_d;
        DDopCodes[2][6][2] = opC::SUB_IX_d;
        DDopCodes[2][6][3] = opC::SBC_A_IX_d;
        DDopCodes[2][6][4] = opC::AND_IX_d;
        DDopCodes[2][6][5] = opC::XOR_IX_d;
        DDopCodes[2][6][6] = opC::OR_IX_d;
        DDopCodes[2][6][7] = opC::CP_IX_d;

        // x = 3
        // z=1
        DDopCodes[3][1][0b100] = opC::POP_IX;
        DDopCodes[3][1][0b101] = opC::JP_IX;
        DDopCodes[3][1][0b111] = opC::LD_SP_IX;
        // z=3
        DDopCodes[3][3][0b100] = opC::EX_SP_IX;
        // z=5 [x][z][y]
        DDopCodes[3][5][0b100] = opC::PUSH_IX;

        /* DD Prefix */
        DDopCodes[3][5][0b011] = opC::DD_prefix;

        /* ED Prefix */
        //DDopCodes[3][5][0b101] = opC::ED_prefix;

        /* FD Prefix */
        DDopCodes[3][5][0b111] = opC::FD_prefix;

        /* Table FD Prefix */

        /* FDCB Prefix */
        FDopCodes[3][3][1] = opC::FDCB_prefix;

        // x = 0
        // z=1   [x][z][y] (q=0)
        FDopCodes[0][1][0b100] = opC::LD_IY_nn;
        //                 (q=1)
        FDopCodes[0][1][0b001] = opC::ADD_IY_rp_p;
        FDopCodes[0][1][0b011] = opC::ADD_IY_rp_p;
        FDopCodes[0][1][0b101] = opC::ADD_IY_rp_p;
        FDopCodes[0][1][0b111] = opC::ADD_IY_rp_p;
        // z=2   [x][z][y]
        FDopCodes[0][2][4] = opC::LD_mm_IY;
        FDopCodes[0][2][5] = opC::LD_IY_mm;
        // z=3   [x][z][y]
        FDopCodes[0][3][0b100] = opC::INC_IY;
        FDopCodes[0][3][0b101] = opC::DEC_IY;
        // z=4   [x][z][y]
        FDopCodes[0][4][0] = opC::INC_r_y;
        FDopCodes[0][4][1] = opC::INC_r_y;
        FDopCodes[0][4][2] = opC::INC_r_y;
        FDopCodes[0][4][3] = opC::INC_r_y;
        FDopCodes[0][4][4] = opC::INC_IYH;
        FDopCodes[0][4][5] = opC::INC_IYL;
        FDopCodes[0][4][0b110] = opC::INC_IY_d;
        FDopCodes[0][4][7] = opC::INC_r_y;
        // z=5   [x][z][y]
        FDopCodes[0][5][0] = opC::DEC_r_y;
        FDopCodes[0][5][1] = opC::DEC_r_y;
        FDopCodes[0][5][2] = opC::DEC_r_y;
        FDopCodes[0][5][3] = opC::DEC_r_y;
        FDopCodes[0][5][4] = opC::DEC_IYH;
        FDopCodes[0][5][5] = opC::DEC_IYL;
        FDopCodes[0][5][0b110] = opC::DEC_IY_d;
        FDopCodes[0][5][7] = opC::DEC_r_y;
        // z=6   [x][z][y]
        FDopCodes[0][6][0] = opC::LD_r_y_n;
        FDopCodes[0][6][1] = opC::LD_r_y_n;
        FDopCodes[0][6][2] = opC::LD_r_y_n;
        FDopCodes[0][6][3] = opC::LD_r_y_n;
        FDopCodes[0][6][4] = opC::LD_IYH_n;
        FDopCodes[0][6][5] = opC::LD_IYL_n;
        FDopCodes[0][6][6] = opC::LD_IY_d_n; // LD (IY+d), n
        FDopCodes[0][6][7] = opC::LD_r_y_n;

        // x = 1
        // Exception: 7 * 7 combinations managed in fetch
        // z,y=4,5
        //       [x][z][y] (z=y=0 are dummies). Include LD (HL), r or LD r, (HL)
        FDopCodes[1][0][0] = opC::LD_r_y_r_z;
        FDopCodes[1][4][0] = opC::LD_r_y_IYH;
        FDopCodes[1][5][0] = opC::LD_r_y_IYL;
        FDopCodes[1][0][4] = opC::LD_IYH_r_z;
        FDopCodes[1][0][5] = opC::LD_IYL_r_z;
        FDopCodes[1][4][4] = opC::LD_IYH_IYH;
        FDopCodes[1][5][5] = opC::LD_IYL_IYL;
        FDopCodes[1][5][4] = opC::LD_IYH_IYL;
        FDopCodes[1][4][5] = opC::LD_IYL_IYH;
        // y=6   [x][z][y]       LD (IY+d), r
        FDopCodes[1][0][6] = opC::LD_IY_d_r_z;
        // z=6   [x][z][y]       LD r, (IY+d)
        FDopCodes[1][6][0] = opC::LD_r_y_IY_d;
        // z=y=6 [x][z][y]
        FDopCodes[1][6][6] = opC::NONI;

        // x = 2
        // z=4,5   [x][z][y]
        FDopCodes[2][4][0] = opC::ADD_A_IYH; // 0x84
        FDopCodes[2][5][0] = opC::ADD_A_IYL; // 0x85
        FDopCodes[2][4][1] = opC::ADC_A_IYH;
        FDopCodes[2][5][1] = opC::ADC_A_IYL;
        FDopCodes[2][4][2] = opC::SUB_IYH;
        FDopCodes[2][5][2] = opC::SUB_IYL;
        FDopCodes[2][4][3] = opC::SBC_IYH;
        FDopCodes[2][5][3] = opC::SBC_IYL;
        FDopCodes[2][4][4] = opC::AND_IYH;
        FDopCodes[2][5][4] = opC::AND_IYL;
        FDopCodes[2][4][5] = opC::XOR_IYH;
        FDopCodes[2][5][5] = opC::XOR_IYL;
        FDopCodes[2][4][6] = opC::OR_IYH;
        FDopCodes[2][5][6] = opC::OR_IYL;
        FDopCodes[2][4][7] = opC::CP_IYH;
        FDopCodes[2][5][7] = opC::CP_IYL;
        // z=6   [x][z][y]
        FDopCodes[2][6][0b000] = opC::ADD_A_IY_d;
        FDopCodes[2][6][0b001] = opC::ADC_A_IY_d;
        FDopCodes[2][6][0b010] = opC::SUB_IY_d;
        FDopCodes[2][6][0b011] = opC::SBC_A_IY_d;
        FDopCodes[2][6][0b100] = opC::AND_IY_d;
        FDopCodes[2][6][0b101] = opC::XOR_IY_d;
        FDopCodes[2][6][0b110] = opC::OR_IY_d;
        FDopCodes[2][6][0b111] = opC::CP_IY_d;

        // x=3
        // z=1   [x][z][y]
        FDopCodes[3][1][0b100] = opC::POP_IY;
        FDopCodes[3][1][0b101] = opC::JP_IY;
        FDopCodes[3][1][0b111] = opC::LD_SP_IY;
        // z=3
        FDopCodes[3][3][0b100] = opC::EX_SP_IY;
        // z=5   [x][z][y]
        FDopCodes[3][5][0b100] = opC::PUSH_IY;

        /* DD Prefix */
        FDopCodes[3][5][0b011] = opC::DD_prefix;

        /* ED Prefix */
        //FDopCodes[3][5][0b101] = opC::ED_prefix;

        /* FD Prefix */
        FDopCodes[3][5][0b111] = opC::FD_prefix;

        /* DDCB Prefix */
        DDopCodes[3][3][1] = opC::DDCB_prefix;

        // x = 0, y = 0..7
        // Exception: iterates over z instead of y are 7 * 7 combinations managed in fetch,
        // but still opC::PTR is needed
        // z=0..7  [x][z][y] Only z=6 is official; rest are undocumented

        // solo hacen falta las entradas para z == 6, z == 0 asume el resto de valores

        DDCBopCodes[0][0][0] = opC::RLC_IX_d; // RLC (IX+d),B etc
        DDCBopCodes[0][0][1] = opC::RRC_IX_d;
        DDCBopCodes[0][0][2] = opC::RL_IX_d;
        DDCBopCodes[0][0][3] = opC::RR_IX_d;
        DDCBopCodes[0][0][4] = opC::SLA_IX_d;
        DDCBopCodes[0][0][5] = opC::SRA_IX_d;
        DDCBopCodes[0][0][6] = opC::SLL_IX_d; // undocumented
        DDCBopCodes[0][0][7] = opC::SRL_IX_d;

        // x=1
        // z=6     [x][z][y]
        DDCBopCodes[1][0][0] = opC::BIT_y_IX_d;

        // x=2
        // z=6     [x][z][y]
        DDCBopCodes[2][0][0] = opC::RES_y_IX_d;

        // x=3
        // z=6     [x][z][y]
        DDCBopCodes[3][0][0] = opC::SET_y_IX_d;

        /* FDCB Prefix */

        // x = 0, y = 0..7
        // Exception: iterates over z instead of y are 7 * 7 combinations managed in fetch,
        // but still opC::PTR is needed
        // z=0..7  [x][z][y] Only z=6 is official; rest are undocumented
        FDCBopCodes[0][0][0] = opC::RLC_IY_d; // RLC (IY+d),B etc
        FDCBopCodes[0][0][1] = opC::RRC_IY_d;
        FDCBopCodes[0][0][2] = opC::RL_IY_d;
        FDCBopCodes[0][0][3] = opC::RR_IY_d;
        FDCBopCodes[0][0][4] = opC::SLA_IY_d;
        FDCBopCodes[0][0][5] = opC::SRA_IY_d;
        FDCBopCodes[0][0][6] = opC::SLL_IY_d; // undocumented
        FDCBopCodes[0][0][7] = opC::SRL_IY_d;

        // x=1
        // z=6     [x][z][y]
        FDCBopCodes[1][0][0] = opC::BIT_y_IY_d;

        // x=2
        // z=6     [x][z][y]
        FDCBopCodes[2][0][0] = opC::RES_y_IY_d;

        // x=3
        // z=6     [x][z][y]
        FDCBopCodes[3][0][0] = opC::SET_y_IY_d;
    }

    private static final int TSTATES_PER_FRAME = 69888;

    protected long getTState() {
        return  tstate;
    }

    public void interrupt() {
        // interrupted disabled?
        if( !IFF1 )
            return;

        // instruction behind EI can't be interrupted
        if( IFF3 != 0 )
            return;

        // ISR runs with implicit DI
        /*
         * When an INT is accepted, both IFF1 and IFF2 are cleared, preventing another interrupt from
         * occurring which would end up as an infinite loop (and overflowing the stack).
         * Section 5.2 o.c. Undoc by Sean Young
         */
        IFF1 = IFF2 = false;

        switch (currentIM) {
            case 0: //y = 7; break;
            case 1:
                y = 7; // RST 38h
                RST_y_8();
                tstate+=13;
            break;
            case 2:
                W = I;
                Z = (byte) 0xFF; // TODO 0xFE bit 0 bajo direccion par ???
                // En teoría si, pero el Spectrum no está preparado para IM2
                // así que la ULA inserta 0xFF siempre
                // https://wiki.speccy.org/cursos/ensamblador/interrupciones
                int mL = currentComp.peek(getWZ());
                int mH = currentComp.peek(getWZ()+1);
                W = (byte) mH;
                Z = (byte) mL;
                currentComp.poke(--SP, (byte)((PC & 0xFF00)>>8));
                currentComp.poke(--SP, (byte)(PC & 0x00FF));
                PC = getWZ();
                regTouched(RegTouched.SP);
                tstate+=19;
                break;
        }
        // TODO la instruccion a ejecutar no tiene por que ser siempre una RST.
        // En Modo 1 sí es RST 38h (opCode = FFh) pero en el Modo 0 se le debe
        // consultar a opCode = Computer.getInstructionOnInterrupt() que devolverá
        // el resultado de Computer.setInstructionOnInterrupt(new InstructionOnInterrupt())
        // En Modo 2 se debe aplicar el procedimiento de obtención de I como indice
        // en una tabla

        isHalted = false;
        resQF();
    }

    public void interruptNMI() {
        IFF2 = IFF1; // Save current state of interruptions
        IFF1 = false;// Now disabled

        Z = (byte) 0x66;
        W = 0x00;

        currentComp.poke(--SP, (byte)((PC & 0xFF00)>>8));
        currentComp.poke(--SP, (byte)(PC & 0x00FF));

        PC = getWZ();
        regTouched(RegTouched.SP);
        tstate+=11;
    }

    public void CB_prefix() { fetchCB(currentComp.peek(PC++)); }
    public void ED_prefix() { fetchED(currentComp.peek(PC++)); }
    public void DD_prefix() { fetchDD(currentComp.peek(PC++)); }
    public void FD_prefix() { fetchFD(currentComp.peek(PC++)); }
    public void DDCB_prefix() { fetchDDCB(currentComp.peek(PC++)); }
    public void FDCB_prefix() { fetchFDCB(currentComp.peek(PC++)); }

    /****/
    // Optimized frame-based timing system
    private final Z80FrameTiming frameTiming = new Z80FrameTiming();
    
    // Performance measurement (kept for debugging)
    private long instrCount = 0;
    private long measureLastTime = System.nanoTime();

    private void measureSpeed() {
        instrCount++;

        long measureNow = System.nanoTime();
        //if (now - lastTime >= 1_000_000_000L) { // 1 second
        if (measureNow - measureLastTime >= 1_000_000_000L) { // 1 second
            System.out.printf("Instructions per second: %8d\n", instrCount);
            instrCount = 0;
            //lastTime = now;
            measureLastTime = measureNow;
        }
    }

    private boolean checkFrameComplete() {
        // Check if frame is complete and interrupt should be triggered
        if (frameTiming.executeFrame(this)) {
            interrupt();
            return true;
        }
        return false;
    }
    /****/

    protected void opCMasked(byte opC) {
        x = ((opC & 0b11000000) >> 6);
        y = ((opC & 0b00111000) >> 3);
        z = (opC & 0b111);
        p = ((y & 0b110) >> 1);
        q = (y & 1);
    }

    public void fetch() {
        if( PC == 0x0010 ) {outToScreen(getA());}
        /*if( PC == 0x04C2 ) {
            tap.SA_BYTES();
            PC = 0x053F; // SA_LD_RET, test for BREAK
        } else if( PC == 0x556 ) {
            tap.LD_BYTES(currentComp, getA()&0x00FF, getIX()&0x0000FFFF, getDE());
            PC = 0x053F;  // SA_LD_RET, test for BREAK
        }*/

        fetch(currentComp.peek(PC++));
    }

    String outToScreenOUT = "archivoOUT.txt";
    FileOutputStream fos = null;
    private void outToScreen(byte a) {
        int b = a & 0x00FF;

        if( fos == null ) {
            try {
                fos = new FileOutputStream(outToScreenOUT);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            fos.write(b);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //System.out.print(Character.toChars(b));
    }

    public void fetch(byte opC) {
        resQF();
        setR((byte)(((R + 1) & 0x7F) | (R & 0x80)));

        if( isHalted ) {
            PC--;
            //NOP();
            //Logger.halted((int)tstate_pseudo); // TODO
            frameTiming.addTStates(4); // NOP takes 4 T-states
            checkFrameComplete();
            return;
        }

        opCMasked(opC);

        if( x == 1 ) {
            if( z == 6 && y == 6)
                opCodes[1][6][6].execute();// HALT();
            else
                opCodes[x][0][0].execute();// LD_r_y_r_z
        }

        if( x == 2 ) {
            opCodes[x][0][y].execute(); // Arithmetic & Logical instructions
        }

        if ( x == 0 || x == 3 ) {
            if (opCodes[x][z][y] != null) {
                opCodes[x][z][y].execute();
            } else {
                System.out.println("OpCode not implemented yet: " + Integer.toHexString(opC & 0x00FF));
                //throw new IllegalArgumentException("OpCode not implemented yet: " + Integer.toHexString(opC));
            }
        }

        lastQ = getQF();

        if( IFF1 && IFF3 > 0 )
            IFF3--;

        // Add T-states for this instruction (will be set by each instruction)
        frameTiming.addTStates(tstate - old_tstate);
        old_tstate = tstate;
        
        // Check if frame is complete and handle timing
        checkFrameComplete();
        measureSpeed();
    }

    protected void fetchCB(byte opC) {
        opCMasked(opC);

        if( x == 0 ) {
            CBopCodes[x][0][y].execute(); // Shift/Rotate
            return;
        }

        // x > 0
        if (CBopCodes[x][0][0] != null) {
            CBopCodes[x][0][0].execute(); // Bitwise y, r[z]
        } else {
            System.out.println("OpCode not implemented yet: " + Integer.toHexString(opC & 0x00FF));
            //throw new IllegalArgumentException("CB+OpCode not implemented yet: " + Integer.toHexString(opC));
        }
    }

    protected void fetchED(byte opC) {
        opCMasked(opC);

        if( x == 0 || x == 3) {
            NONI();
            if( x==3 && z==5 && y==5 ) { // enchained 0xED
                fetch(opC);
            }
            return;
        }

        if(x == 1){
            if (z == 4) {
                EDopCodes[x][z][0].execute(); // NEG
                return;
            } else if(z == 5) {
                if (y == 1)
                    EDopCodes[x][z][1].execute(); // RETI
                else
                    EDopCodes[x][z][0].execute(); // RETN
                return;
            } else if (z == 6) {
                EDopCodes[x][z][0].execute(); // IM_im_y
                return;
            }
        }

        if (EDopCodes[x][z][y] != null) {
            EDopCodes[x][z][y].execute();
        } else {
            System.out.println("ED+OpCode not implemented yet: " + Integer.toHexString(opC & 0x00FF));
            //throw new IllegalArgumentException("ED+OpCode not implemented yet: " + Integer.toHexString(opC));
        }
    }

    protected void fetchDD(byte opC) {
        opCMasked(opC);

        if (x == 1) {
            if( z == 6 && y == 6 )
                DDopCodes[x][6][6].execute(); // NONI
            else if( z != 6 && y == 6 )
                DDopCodes[x][0][6].execute(); //LD_IX_d_r_z
            else if( z == 6 /*&& y != 6*/ )
                DDopCodes[x][6][0].execute(); //LD_r_y_IX_d
            else if( z == 4 && y == 4 )
                DDopCodes[x][4][4].execute(); //LD_IXH_IXH
            else if( z == 5 && y == 5 )
                DDopCodes[x][5][5].execute(); //LD_IXL_IXL
            else if( (z == 4 || z == 5) && (y != 4 && y != 5) )
                DDopCodes[x][z][0].execute(); // LD_r_y_IXH / LD_r_y_IXL
            else if( (y == 4 || y == 5) && (z != 4 && z != 5) )
                DDopCodes[x][0][y].execute(); //LD_IXH_r_z / LD_IXL_r_z
            else if( (z == 4 || z == 5) /*&& (y == 4 || y == 5)*/ )
                DDopCodes[x][z][y].execute(); // LD_IXH_IXL / LD_IXL_IXH
            else
                DDopCodes[x][0][0].execute();

            tstate += 4;
            return;
        }

        if (DDopCodes[x][z][y] != null) {
            //System.out.println("D");
            DDopCodes[x][z][y].execute();
        } else {
            System.out.println("DD+OpCode not implemented yet: " + Integer.toHexString(opC & 0x00FF) + " PC: " + Integer.toHexString(PC));
            //throw new IllegalArgumentException("DD+OpCode not implemented yet: " + Integer.toHexString(opC));
        }
    }

    protected void fetchFD(byte opC) {
        opCMasked(opC);
        
        if (x == 1) {
            if( z == 6 && y == 6 )
                FDopCodes[x][6][6].execute(); // NONI
            else if( z != 6 && y == 6 )
                FDopCodes[x][0][6].execute(); //LD_IY_d_r_z
            else if( z == 6 /*&& y != 6*/ )
                FDopCodes[x][6][0].execute(); //LD_r_y_IY_d
            else if( z == 4 && y == 4 )
                FDopCodes[x][4][4].execute(); //LD_IYH_IYH
            else if( z == 5 && y == 5 )
                FDopCodes[x][5][5].execute(); //LD_IYL_IYL
            else if( (z == 4 || z == 5) && (y != 4 && y != 5) )
                FDopCodes[x][z][0].execute(); // LD_r_y_IYH / LD_r_y_IYL
            else if( (y == 4 || y == 5) && (z != 4 && z != 5) )
                FDopCodes[x][0][y].execute(); //LD_IYH_r_z / LD_IYL_r_z
            else if( (z == 4 || z == 5) /*&& (y == 4 || y == 5)*/ ) // LD_IYH_IYL / LD_IYL_IYH
                FDopCodes[x][z][y].execute();
            else
                FDopCodes[x][0][0].execute();

            tstate += 4;
            return;
        }

        if (FDopCodes[x][z][y] != null) {
            //System.out.println("D");
            FDopCodes[x][z][y].execute();
        } else {
            System.out.println("FD+OpCode not implemented yet: " + Integer.toHexString(opC & 0x00FF) + " PC: " + Integer.toHexString(PC));
            //throw new IllegalArgumentException("FD+OpCode not implemented yet: " + Integer.toHexString(opC));
        }
    }

    protected void fetchDDCB(byte opC) {
        d = opC; // skip displacement
        opC = currentComp.peek(PC++);

        opCMasked(opC);

        if( x == 0 )
            DDCBopCodes[0][0][y].execute();
        else if (DDCBopCodes[x][0][0] != null)
            DDCBopCodes[x][0][0].execute();
        else { // No deberia de ocurrir
            System.out.println("DDCB+OpCode not implemented yet: " + Integer.toHexString(opC & 0x00FF));
            //throw new IllegalArgumentException("DDCB+OpCode not implemented yet: " + Integer.toUnsignedInt(opC));
        }
    }

    protected void fetchFDCB(byte opC) {
        d = opC; // skip displacement
        opC = currentComp.peek(PC++);

        opCMasked(opC);

        if( x == 0 )
            FDCBopCodes[0][0][y].execute();
        else if (FDCBopCodes[x][0][0] != null)
            FDCBopCodes[x][0][0].execute();
        else { // No deberia de ocurrir
            System.out.println("FDCB+OpCode not implemented yet: " + Integer.toHexString(opC & 0x00FF));
            //throw new IllegalArgumentException("FDCB+OpCode not implemented yet: " + Integer.toUnsignedInt(opC));
        }
    }

    //*** End Dispatcher Section

    protected byte A;
    protected byte B;
    protected byte C;
    protected byte D;
    protected byte E;
    protected byte H;
    protected byte L;
    protected BitSet F = new BitSet(9);
    protected boolean lastQ;

    public enum RegTouched {
           A, B, C, D, E, F, H, L, SP, IX, IY, I, R,
        // 0  1  2  3  4  5  6  7  8   9   10 11 12
        A_, B_, C_, D_, E_, F_, H_, L_
        // 13  14  15  16  17  18  19  20
    }
    private final BitSet regTouched = new BitSet(RegTouched.values().length);

    protected int PC;

    protected int SP;

    protected short IX;
    protected short IY;

    protected byte I;
    protected byte R;

    protected byte W;
    protected byte Z;

    protected int MEMPTR;

    protected int currentIM;
    protected boolean IFF1;
    protected boolean IFF2;
    private int IFF3;
    private boolean isHalted;

    protected Register alternative = new Register();

    protected Computer currentComp;

    public Z80() {
        dispatcher(this);
        tap = new TAP(currentComp);
    }

    public Z80(Z80OpCode opC) {
        dispatcher(opC);
        tap = new TAP(currentComp);
    }

    /*
     * According to Reset Timing at http://www.z80.info/interrup.htm
     * The Undocumented Z80 Documented by Sean Young
     * Version 0.91, 18th September, 2005
     * Section 2.4 Power on defaults
     */
    public void reset() {
        regTouched.clear();
        PC = 0;
        setI((byte) 0);
        setR((byte) 0); // will forget Flags affectation
        setA((byte) 0xFF);
        setF((byte) 0xFF);
        SP = 0xFFFF;
        regTouched(RegTouched.SP);
        IX = IY = (short) 0xFFFF;
        B = C = D = E = H = L = A;
        IFF1 = IFF2 = false;
        IFF3 = 0; // set to 2 in EI
        currentIM = 0;
        isHalted = false;
        
        // Reset timing system
        frameTiming.reset();
        tstate = 0;
        old_tstate = 0;
    }

    public void setComputer(Computer theComp) { currentComp = theComp; }
    public Computer getComputer() { return currentComp; }

    // Getters / Setters
    public byte getA() { return A; }
    public void setA(byte a) { A = a; regTouched(RegTouched.A); }

    public byte getB() { return B; }
    public void setB(byte b) { B = b; regTouched(RegTouched.B);}

    public byte getC() { return C; }
    public void setC(byte c) { C = c; regTouched(RegTouched.C); }

    public byte getD() {return D; }
    public void setD(byte d) { D = d; regTouched(RegTouched.D); }

    public byte getE() { return E; }
    public void setE(byte e) { E = e; regTouched(RegTouched.E); }

    public byte getH() { return H; }
    public void setH(byte h) { H = h; regTouched(RegTouched.H); }

    public byte getL() { return L; }
    public void setL(byte l) { L = l; regTouched(RegTouched.L); }

    public byte getI() { return I; }
    public void setI(byte i) { I = i; regTouched(RegTouched.I); }

    public byte getR() { return R; }
    public void setR(byte r) { R = r; regTouched(RegTouched.R); }

    public byte getF() {
        if( F.cardinality() == 0 ) // Hmmm... BitSet does not support returning 0 if all bits are clear ;-(
            return 0;
        return F.toByteArray()[0];
    }

    public void setF(byte f) { F = BitSet.valueOf(new byte[]{f}); regTouched(RegTouched.F); }

    public byte getF_() {
        if( alternative.F.cardinality() == 0 )
            return 0;
        return alternative.F.toByteArray()[0];
    }

    public void setF_(byte f) { alternative.F = BitSet.valueOf(new byte[]{f}); regTouched(RegTouched.F_); }

    public boolean getQF(){ return F.get(8); }
    public boolean getSF(){ return F.get(7); }
    public boolean getZF(){ return F.get(6); }
    public boolean getYF(){ return F.get(5); }
    public boolean getHF(){ return F.get(4); }
    public boolean getXF(){ return F.get(3); }
    public boolean getPF(){ return F.get(2); }
    public boolean getVF(){ return F.get(2); } // same getP()
    public boolean getNF(){ return F.get(1); }
    public boolean getCF(){ return F.get(0); }

    public void setQF(){ F.set(8); }
    public void setSF(){ setQF(); F.set(7); regTouched(RegTouched.F); }
    public void setZF(){ setQF(); F.set(6); regTouched(RegTouched.F); }
    public void setYF(){ setQF(); F.set(5); regTouched(RegTouched.F); }
    public void setHF(){ setQF(); F.set(4); regTouched(RegTouched.F); }
    public void setXF(){ setQF(); F.set(3); regTouched(RegTouched.F); }
    public void setPF(){ setQF(); F.set(2); regTouched(RegTouched.F); }
    public void setVF(){ setQF(); F.set(2); regTouched(RegTouched.F); } // same setPF()
    public void setNF(){ setQF(); F.set(1); regTouched(RegTouched.F); }
    public void setCF(){ setQF(); F.set(0); regTouched(RegTouched.F); }

    public void resQF(){ F.clear(8); }
    public void resSF(){ setQF(); F.clear(7); regTouched(RegTouched.F); }
    public void resZF(){ setQF(); F.clear(6); regTouched(RegTouched.F); }
    public void resYF(){ setQF(); F.clear(5); regTouched(RegTouched.F); }
    public void resHF(){ setQF(); F.clear(4); regTouched(RegTouched.F); }
    public void resXF(){ setQF(); F.clear(3); regTouched(RegTouched.F); }
    public void resPF(){ setQF(); F.clear(2); regTouched(RegTouched.F); }
    public void resVF(){ setQF(); F.clear(2); regTouched(RegTouched.F); } // same resPF()
    public void resNF(){ setQF(); F.clear(1); regTouched(RegTouched.F); }
    public void resCF(){ setQF(); F.clear(0); regTouched(RegTouched.F); }

    final int SFMASK = 0b1000_0000;
    //final int ZFMASK = 0b1111_1111; //setZF() iff (x & ZFMASK)==0 => x == 0
    final int YFMASK = 0b0010_0000;
    final int HFMASK = 0b0001_0000;
    final int XFMASK = 0b0000_1000;

    // undocumented
    private void affectXYFlags(int a) {
        if( (a & XFMASK) == XFMASK)
            setXF();
        else
            resXF();

        if( (a & YFMASK) == YFMASK)
            setYF();
        else
            resYF();
    }

    // Words
    public short getBC() { return (short) ((short) (B << 8) | (C & 0xFF)); }
    public short getDE() { return (short) ((short) (D << 8) | (E & 0xFF)); }
    //public short getHL() { return (short) ((short) ((H << 8) & 0xFF00) | (L & 0xFF)); }
    public short getHL() { return (short) ((short) ((H << 8) | (L & 0xFF))); }
    public int   getSP() { return SP; }
    public short getIX() { return IX; }
    public short getIY() { return IY; }

    public void setIXH(byte ixh) {
        setIX((short) ((ixh << 8) | (getIXL() & 0x00FF)));
    }

    public void setIXL(byte ixl) {
        setIX((short) ((IX & 0x0FF00) | (ixl & 0x00FF)));
    }

    public byte getIXH() {
        return (byte) ((IX & 0x0FF00) >>> 8);
    }

    public byte getIXL() {
        return (byte) (IX & 0x0FF);
    }

    public void setIYH(byte iyh) {
        setIY((short) ((iyh << 8) | (getIYL() & 0x00FF)));
    }

    public void setIYL(byte iyl) {
        setIY((short) ((IY & 0x0FF00) | (iyl & 0x00FF)));
    }

    public byte getIYH() {
        return (byte) ((IY & 0x0FF00) >>> 8);
    }

    public byte getIYL() {
        return (byte) (IY & 0x0FF);
    }

    protected void setWZ(int wz) { W = (byte) ((wz & 0x0FF00)>>>8); Z = (byte) (wz & 0x00FF); } // TODO pendiente de unificar en otros casos
    protected int getWZ() { return ((W << 8) | (Z & 0x00FF)) & 0x0000FFFF; }

    public void setBC(short bc) {
        setB((byte) ((bc & 0xFF00) >> 8));
        setC((byte) (bc & 0x00FF));
    }

    public void setDE(short de) {
        setD((byte) ((de & 0xFF00) >> 8));
        setE((byte) (de & 0x00FF));
    }

    public void setHL(short hl) {
        setH((byte) ((hl & 0x0FF00) >> 8));
        setL((byte) (hl & 0x00FF));
    }

    public void setSP(int sp) {
        SP = sp;
        regTouched(RegTouched.SP);
    }

    public void setIX(short ix) {
        IX = ix;
        regTouched(RegTouched.IX);
    }

    public void setIY(short iy) {
        IY = iy;
        regTouched(RegTouched.IY);
    }

    public String getWord(byte h, byte l) {
        return String.format("%04X", (short) ((h << 8) & 0xFF00 | (l & 0xFF)));
    }

    public int getPC() { return PC++; }
    public int getPCnonIncrement() { return PC; }

    private void get_rp_p() {
        switch (p) {
            case 0:
                W = B;
                Z = C;
                break;
            case 1:
                W = D;
                Z = E;
                break;
            case 2:
                W = H;
                Z = L;
                break;
            case 3:
                W = (byte) ((getSP() & 0xFF00) >> 8);
                Z = (byte) (getSP() & 0x00FF);
        }
    }

    private void set_rp_p() {
        switch (p) {
            case 0:
                setB(W);
                setC(Z);
                break;
            case 1:
                setD(W);
                setE(Z);
                break;
            case 2:
                setH(W);
                setL(Z);
                break;
            case 3:
                setSP(getWZ());
        }
    }

    public boolean isHalted() {
        return isHalted;
    }

    private void regTouched(RegTouched r) {
        regTouched.set(r.ordinal());
    }

    public BitSet getSnapshot() {
        //final long[] snapshot = regTouched.toLongArray();
        final BitSet snapshot = (BitSet)regTouched.clone();
        regTouched.clear();
        return snapshot;
    }

    /*
     *
     *      Instructions
     *
     */

	@Override
    public void NOP() {
        /* No Operation */
        //long old_tstate = tstate;
        //long start = System.nanoTime();
        //while( tstate - old_tstate < 4) tstate++;

        tstate+=4;

        //long end = System.nanoTime();
        //System.out.println("NOP TState: " + (end - start) + " nanos");
    }

    @Override
    public void NONI() {
// TODO
// Detrás de un DD/FD o varios en secuencia venía un código
// que no correspondía con una instrucción que involucra a
// IX o IY. Se trata como si fuera un código normal.
// Sin esto, además de emular mal, falla el test
// ld <bcdexya>,<bcdexya> de ZEXALL.
        NOP();
        IFF3 = 2;
    }

	@Override
    public void EX_AF_AF_() {
        W = A;
        setA(alternative.A);
        alternative.A = W;

        Z = getF();
        setF(getF_());
        setF_(Z);
        tstate+=4;
    }

	@Override
    public void DJNZ() {
        byte d = currentComp.peek(PC++);

        if (--B != 0) {
            PC += (short) d;
            MEMPTR = PC;
            tstate+=5;
        }
        tstate+=8;
        regTouched(RegTouched.B);
    }

	@Override
    public void JR() {
        byte d = currentComp.peek(PC++);

        PC += (short) d;

        MEMPTR = PC;

        tstate+=12;
    }

	@Override
    public void JR_cc() {
        boolean ccSet = false;
        byte d = currentComp.peek(PC++);

        switch (cc[y - 4]) {
            case "NZ":
                ccSet = !getZF();
                break;
            case "Z":
                ccSet = getZF();
                break;
            case "NC":
                ccSet = !getCF();
                break;
            case "C":
                ccSet = getCF();
        }

        if (ccSet) {
            PC += (short) d;
            MEMPTR = PC;
            tstate+=5;
        }

        tstate+=7;
    }

    @Override
    public void LD_rp_p_nn() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        set_rp_p();
        tstate+=10;
    }

    private int add16_common2(int reg16, int oper16) {
        oper16 += reg16;

        sz5h3pnFlags = 0xFF;
        carryFlag = oper16 > 0xffff;
        sz5h3pnFlags = (sz5h3pnFlags & FLAG_SZP_MASK) | ((oper16 >>> 8) & FLAG_53_MASK);
        oper16 &= 0x0000ffff;

        if ((oper16 & 0x0fff) < (reg16 & 0x0fff)) {
            sz5h3pnFlags |= HALFCARRY_MASK;
        }

        System.out.println(": " + oper16);
        return oper16;
    }

    @Override
    public void ADD_HL_rp_p() {
        int hl = getHL() & 0xFFFF;
        int wz;

        MEMPTR = hl+1;

        get_rp_p();
        wz = getWZ();
//System.out.print(getHL() + ", " + getWZ());
//add16_common2(getHL(), getWZ());
        hl += wz;

        // https://retrocomputing.stackexchange.com/questions/11262/can-someone-explain-this-algorithm-used-to-compute-the-auxiliary-carry-flag
        boolean isHalfCarry = (((getHL() & 0x0FFF) + (getWZ() & 0x0FFF)) & 0xF000) != 0;

        if( (hl & 0xFFFF0000) != 0 )
            setCF();
        else
            resCF();

        /*if( hl == 0)
            setZF();
        else
            resZF();*/

        /*if( iSOverflowADD16(getHL(), getWZ(), hl))
            setVF();
        else
            resVF();*/

        if(isHalfCarry)
            setHF();
        else
            resHF();

        resNF();

        setHL((short) (hl & 0xFFFF));

        affectXYFlags(H);

//System.out.println(" = " + getHL());
//verifyFlags();
        tstate+=11;
    }

    @Override
    public void ADD_IY_rp_p() {
        int iy = getIY() & 0xFFFF;
        int wz;

        MEMPTR = iy+1;

        get_rp_p();
        if( p == 2 ) {
            W = getIYH();
            Z = getIYL();
        }
        wz = getWZ();

        iy += wz;

        boolean isHalfCarry = (((getIY() & 0x0FFF) + (getWZ() & 0x0FFF)) & 0xF000) != 0;

        if( (iy & 0xFFFF0000) != 0 )
            setCF();
        else
            resCF();

        /*if( iy == 0)
            setZF();
        else
            resZF();

        if( iSOverflowADD16(getIY(), getWZ(), iy))
            setVF();
        else
            resVF();*/

        if(isHalfCarry)
            setHF();
        else
            resHF();

        resNF();

        setIY((short) (iy & 0x0FFFF));

        affectXYFlags(getIYH());

        tstate+=15;
    }

    @Override
    public void LD_BC_A() {
        MEMPTR = getBC();

        currentComp.poke(MEMPTR++, A);

        Z = (byte) (MEMPTR & 0x00FF);
        W = A;
        MEMPTR = getWZ();

        tstate+=7;
    }

	@Override
    public void LD_DE_A() {
        MEMPTR = getDE();

        currentComp.poke(MEMPTR++, A);

        Z = (byte) (MEMPTR & 0x00FF);
        W = A;
        MEMPTR = getWZ();

        tstate+=7;
    }

	@Override
    public void LD_mm_HL() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        currentComp.poke(getWZ(), L);
        currentComp.poke(getWZ() + 1, H);

        tstate+=16;
    }

	@Override
    public void LD_mm_A() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);
        MEMPTR = getWZ();

        currentComp.poke(MEMPTR++, A);

        Z = (byte) (MEMPTR & 0x00FF);
        W = A;
        MEMPTR = getWZ();

        tstate+=13;
    }

	@Override
    public void LD_A_BC() {
        MEMPTR = getBC();
        setA(currentComp.peek(MEMPTR++));
        tstate+=7;
    }

	@Override
    public void LD_A_DE() {
        MEMPTR = getDE();
        setA(currentComp.peek(MEMPTR++));
        tstate+=7;
    }

	@Override
    public void LD_HL_mm() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        setL(currentComp.peek(getWZ()));
        setH(currentComp.peek(getWZ() + 1));

        tstate+=16;
    }

	@Override
    public void LD_A_mm() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);
        MEMPTR = getWZ();

        setA(currentComp.peek(MEMPTR++));

        tstate+=13;
    }

	@Override
    public void INC_rp_p() {
        short z;
        short c;

        get_rp_p();

        z = (short) ((((short) Z) & 0xFF) + 1);
        c = (short) (z & 0x0100); // pre-carry
        Z = (byte) (z & 0xFF);
        W = (byte) (W + (c >> 8));

        set_rp_p();

        tstate+=6;
    }

    @Override
    public void INC_IY() {
        IY++;
        regTouched(RegTouched.IY);
        tstate+=10;
    }

    @Override
    public void DEC_IY() {
        IY--;
        regTouched(RegTouched.IY);
        tstate+=10;
    }

	@Override
    public void DEC_rp_p() {
        short z;
        short c;

        get_rp_p();

        z = (short) ((((short) Z) & 0xFF) - 1);
        c = (short) ((z == -1) ? 1 : 0); // pre-carry
        Z = (byte) (z & 0xFF);
        W = (byte) (W - c);

        set_rp_p();

        tstate+=6;
    }

	@Override
    public void INC_r_y(){
        int x;

        get_r_(y);
        x = Z;

        switch(y){
            case 0: Z = ++B; regTouched(RegTouched.B); break;
            case 1: Z = ++C; regTouched(RegTouched.C); break;
            case 2: Z = ++D; regTouched(RegTouched.D); break;
            case 3: Z = ++E; regTouched(RegTouched.E); break;
            case 4: Z = ++H; regTouched(RegTouched.H); break;
            case 5: Z = ++L; regTouched(RegTouched.L); break;
            case 6: currentComp.poke(getHL(), Z = (byte)(currentComp.peek(getHL()) + 1) );
                    break;
            case 7: Z = ++A; regTouched(RegTouched.A);
        }

        //ADD_A_common(x, 1, false, affectToCarry := false); // affectToCarry must be true for everyone else

        //boolean isHalfCarry = (((x & 0x0F) + (1 & 0x0F)) & 0xF0) != 0;
        //boolean isHalfCarry = (((x & 0x0F) + 1) & 0xF0) != 0;
        //boolean isHalfCarry = ((x ^ 1 ^ (x+1)) & 0x10) != 0;
        boolean isHalfCarry = (x & 0x0F) == 0x0F;

        if( (Z & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( Z == 0 )
            setZF();
        else
            resZF();

        if( isHalfCarry )
            setHF();
        else
            resHF();

        if( iSOverflowADD(x, 1, Z) )
            setVF();
        else
            resVF();

        resNF();

        affectXYFlags(Z);

        tstate+=4;
    }
    
    @Override
    public void DEC_r_y(){
        int x;

        get_r_(y);
        x = Z;

        switch(y){
            case 0: Z = --B; regTouched(RegTouched.B); break;
            case 1: Z = --C; regTouched(RegTouched.C); break;
            case 2: Z = --D; regTouched(RegTouched.D); break;
            case 3: Z = --E; regTouched(RegTouched.E); break;
            case 4: Z = --H; regTouched(RegTouched.H); break;
            case 5: Z = --L; regTouched(RegTouched.L); break;
            case 6: currentComp.poke(getHL(), Z = (byte)(currentComp.peek(getHL()) - 1) );
                    break;
            case 7: Z = --A; regTouched(RegTouched.A);
        }

        //boolean isHalfCarry = ((x & 0x0F) - (1 & 0x0F)) < 0;
        //boolean isHalfCarry = ((x ^ 1 ^ (x-1)) & 0x10) != 0;
        boolean isHalfCarry = (x & 0xF) == 0x00;

        if( (Z & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( Z == 0 )
            setZF();
        else
            resZF();

        if( isHalfCarry )
            setHF();
        else
            resHF();

        if( isOverflowSUB(x, 1, Z) )
            setVF();
        else
            resVF();

        setNF();

        affectXYFlags(Z);

        tstate+=4;
        if( y == 6 ) // (HL)
            tstate+=7;
    }

	@Override
    public void LD_r_y_n() {
        Z = currentComp.peek(PC++);

        set_r_(y);

        if( y == 6 )
            tstate+=3;
        tstate+=7;
    }

	@Override
    public void RLCA() {
        byte c = (byte) (A & 0x80); // pre-carry

        A <<= 1;

        if( c != 0 ) {
            A |= 1;
            setCF();
        } else {
            resCF();
        }

        resNF();
        resHF();

        affectXYFlags(A);

        regTouched(RegTouched.A);
        tstate+=4;
    }

	@Override
    public void RRCA() {
        byte c = (byte) (A & 0x01); // pre-carry

        A >>>= 1;

        if( c != 0 ) {
            A |= 0x80;
            setCF();
        } else {
            A &= 0x7F;
            resCF();
        }

        resNF();
        resHF();

        affectXYFlags(A);

        regTouched(RegTouched.A);
        tstate+=4;
    }

	@Override
    public void RLA() {
        byte c = (byte) (A & 0x80); // pre-carry
        boolean oc = getCF(); // old-carry

        A <<= 1;

        if( c != 0 ) {
            setCF();
        } else {
            resCF();
        }
        
        if( oc )
            A |= 1;
        else
            A &= 0xFE;

        resNF();
        resHF();

        affectXYFlags(A);

        regTouched(RegTouched.A);
        tstate+=4;
    }

	@Override
    public void RRA() {
        byte c = (byte) (A & 0x01); // pre-carry
        boolean oc = getCF(); // old-carry

        A >>= 1;

        if( c != 0 ) {
            setCF();
        } else {
            resCF();
        }

        if( oc )
            A |= 0x80;
        else
            A &= 0x7F;

        resNF();
        resHF();

        affectXYFlags(A);

        regTouched(RegTouched.A);
        tstate+=4;
    }

    // https://worldofspectrum.org/faq/reference/z80reference.htm#DAA
	// https://www.cpcwiki.eu/index.php/Some_features_of_the_Z80#Half-Carry_flag
    // http://www.z80.info/zip/z80-documented.pdf
    // https://stackoverflow.com/a/57837042/2928048
    // https://web.archive.org/web/20080216035732/http://www.geocities.com/SiliconValley/Peaks/3938/z80code.htm#DAA
    // https://jnz.dk/z80/daa.html
    @Override
    //public void DAA() { System.out.println("DAA TODO ERROR"); }
    public void DAA() {
        if(false)
            DAA_stackoverflow();
        else if(false)
            DAA_chatGPT();
        else if(true)
            DAA_chatGPT2();
        else if(false)
            DAA_ramsoft();
        else if(true)
            DAA_undoc(); // OK !!!
        else if(false)
            DAA_speccy(); // OK
        else if(false)
            DAA_perl();
        else
            DAA_mio();

        tstate+=4;
    }

    private void DAA_stackoverflow() {
            int t = 0;

            if(getHF() || ((A & 0x0F) > 9) )
                t++;

            if(getCF() || (A > 0x99) )
            {
                t += 2;
                setCF();
            }

            // builds final H flag
            if (getNF() && !getHF())
                resHF();
            else
            {
                if (getNF() && getHF())
                    if(((A & 0x0F)) < 6)
                        setHF();
                    else
                        getHF();
                else if ((A & 0x0F) >= 0x0A)
                    setHF();
                else
                    resHF();
            }

            switch(t)
            {
                case 1:
                    //A += (getNF())? (byte) 0x0FA :0x06; // -6..6
                    A += (getNF())? (byte) -6 : 0x06; // -6..6
                    break;
                case 2:
                    //A += (getNF())? (byte) 0x0A0 :0x60; // -0x60..0x60
                    A += (getNF())? (byte) -0x60 : 0x60; // -0x60..0x60
                    break;
                case 3:
                    //A += (getNF())? (byte) 0x9A :0x66; // -0x66..0x66
                    A += (getNF())? (byte) -0x66 : 0x66; // -0x66..0x66
                    break;
            }

            if( (A & 0x80) != 0 )
                setSF();
            else
                resSF();

            if( A == 0 )
                setZF();
            else
                resZF();

            BitSet pA = BitSet.valueOf(new byte[]{A});

            if( (pA.cardinality() % 2) == 0 )
                setPF();
            else
                resPF();

            A &= 0x0FF;
            regTouched(RegTouched.A);
            //flags.X = A & BIT_5;
            //flags.Y = A & BIT_3;
    }

    private void DAA_chatGPT() {
        int a = Byte.toUnsignedInt(A); // A es tu acumulador de 8 bits
        boolean n = getNF();                // Flag N (indica si fue resta)
        boolean h = getHF();                // Flag H (Half Carry)
        boolean c = getCF();                // Flag C (Carry)
        int correction = 0;
        boolean carryOut = c;

        if (!n) { // --- Operaciones de suma (ADD / ADC) ---
            if ((a & 0x0F) > 9 || h) {
                correction |= 0x06;
            }
            if (a > 0x99 || c) {
                correction |= 0x60;
                carryOut = true;
            }

        } else { // --- Operaciones de resta (SUB / SBC) ---
            if (h) correction |= 0x06;
            if (c) correction |= 0x60;
            //a = (a - correction) & 0xFF;
        }

        if(!n)
            a = (a + correction) & 0x00FF;
        else
            a = (a - correction) & 0x00FF;

        // Actualiza el acumulador
        A = (byte) a;
        regTouched(RegTouched.A);

        if( (A & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( A == 0 )
            setZF();
        else
            resZF();

        if(((correction << 2) & 0x10) != 0) // Half-carry (bit 4 de corrección)
            setHF();
        else
            resHF();

        //PF = Integer.bitCount(a & 0xFF) % 2 == 0; // Paridad (1 si par)
        BitSet pA = BitSet.valueOf(new byte[]{A});

        if( (pA.cardinality() % 2) == 0 )
            setPF();
        else
            resPF();

        if( carryOut )
            setCF();
        else
            resCF();
    }

    private void DAA_chatGPT2() {
        int a_before = A & 0xFF;
        int correction = 0;
        boolean carryOut = getCF();

        if ((a_before & 0x0F) > 9 || getHF())
            correction |= 0x06;

        if (a_before > 0x99 || getCF()) {
            correction |= 0x60;
            carryOut = true;
        }

        // --- Aplicar corrección ---
        int a_after;
        if (!getNF())
            a_after = (a_before + correction) & 0xFF;    // ADD/ADC
        else
            a_after = (a_before - correction) & 0xFF;   // SUB/SBC

        // CF actualizado
        // (en resta, normalmente se conserva si estaba activo)
        if( carryOut )
            setCF();
        else
            resCF();

        if( (a_after & SFMASK) != 0 )
            setSF();
        else
            resSF();

        if( a_after == 0 )
            setZF();
        else
            resZF();

        // Half carry real
        if( ((a_before ^ a_after ^ correction) & HFMASK) != 0 )
            setHF();
        else
            resHF();

        if( Integer.bitCount(a_after) % 2 == 0 )
            setPF();
        else
            resPF();

        affectXYFlags(a_after);

        setA((byte) a_after);
    }

    private void DAA_ramsoft() {

        int ah = A & 0x0F0;
        int al = A & 0x0F;
        byte correction = 0;

        if(!getNF()) { // ADD
            if(!getCF()) {
                if(!getHF()) {
                    if( 0 <= ah && ah <= 0x90 &&
                        0 <= al && al <= 9 ) {              // 1
                        correction = 0x00;
                    } else if( 0 <= ah && ah <= 0x80 &&
                            0x0A <= al && al <= 0xF ) {     // 2
                        correction = 0x06;
                        setHF();
                    } else if( 0xA0 <= ah && ah <= 0xF0 &&
                                  0 <= al && al <= 9 ) {    // 3
                        correction = 0x60;
                        setCF();
                    } else if( 0x90 <= ah && ah <= 0xF0 &&
                               0x0A <= al && al <= 0x0F) {  // 4
                        correction = 0x66;
                        setCF();
                    }
                } else { // H == 1
                    if( 0 <= ah && ah <= 0x90 &&
                        0 <= al && al <= 9 ) {   // al <= 3           // 5
                        correction = 0x06;
                        setHF();
                    } else if( 0xA0 <= ah && ah <= 0xF0 &&
                                  0 <= al && al <= 9 ) {   // al <= 3    // 6
                        correction = 0x66;
                        setHF();
                        setCF();
                    }
                }
            } else { // C = 1
                if(!getHF()) {
                    if( /*0 <= ah && ah <= 0x20 &&*/
                        0 <= al && al <= 9 ) {              // 7
                        correction = 0x60;
                        setCF();
                    } else if(/*0 <= ah && ah <= 0x20 &&*/
                            0x0A <= al && al <= 0x0F ) {    // 8
                        correction = 0x66;
                        setCF();
                    }
                } else {
                    if( 0 <= ah && ah <= 0x30 &&
                        0 <= al && al <= 3 ) {              // 9
                        correction = 0x66;
                        setCF();
                    }
                }
            }
        } else {      //SUB
            if(!getCF()) {
                if(!getHF()) {
                    if( 0 <= ah && ah <= 0x90 &&
                        0 <= al && al <= 9 ) {              // 10
                        correction = 0x00;
                    }
                } else {
                    if( 0xA0 <= ah && ah <= 0xF0 && // 0 <= ah && ah <= 0x80 &&
                        6 <= al && al <= 0x0F ) {           // 11
                        //correction = (byte) 0xFA;
                        correction = (byte) 6;
                    }
                }
            } else { // C = 1
                if(!getHF()) {
                    if( /*0x70 <= ah && ah <= 0xF0 &&*/
                           0 <= al && al <= 9 ) {              // 12
                        //correction = (byte) 0xA0;
                        correction = (byte) 0x60;
                        setCF();
                    }
                } else {
                    if( 0x60 <= ah && ah <= 0xF0 &&
                           6 <= al && al <= 0x0F ) {    // 13
                        correction = (byte) 0x9A;
                        //correction = (byte) 66;
                        setCF();
                    }
                }
            }
        }

        if( !getNF() )
            A += (byte) correction;
        else
            A -= (byte) correction;

        A &= 0x0FF;

        if( (A & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( A == 0 )
            setZF();
        else
            resZF();

        BitSet pA = BitSet.valueOf(new byte[]{A});

        if( (pA.cardinality() % 2) == 0 )
            setPF();
        else
            resPF();

        //flags.X = A & BIT_5;
        //flags.Y = A & BIT_3;
        regTouched(RegTouched.A);
    }

    private void DAA_undoc() {

        int ah = A & 0x0F0;
        int al = A & 0x0F;
        byte correction = 0;

        if(!getCF()) {
            if(!getHF()) {
                if( 0 <= ah && ah <= 0x90 &&
                    0 <= al && al <= 9 ) {              // 1
                    correction = 0x00;
                } else if( 0 <= ah && ah <= 0x80 &&
                        0x0A <= al && al <= 0x0F ) {    // 3
                    correction = 0x06;
                } else if(0xA0 <= ah && ah <= 0xF0 &&
                             0 <= al && al <= 0x09 ) {
                    correction = 0x60;                  // 4
                } else if(0x90 <= ah && ah <= 0xF0 &&
                          0x0A <= al && al <= 0x0F ) {  // 8
                    correction = 0x66;
                }
            } else {
                if( 0 <= ah && ah <= 0x90 &&
                    0 <= al && al <= 0x09 ) {           // 2
                    correction = 0x06;
                } else if( 0 <= ah && ah <= 0x80 &&
                        0x0A <= al && al <= 0x0F ) {    // 3
                    correction = 0x06;
                } else if(0x90 <= ah && ah <= 0xF0 &&
                        0x0A <= al && al <= 0x0F ) {    // 8
                    correction = 0x66;
                } else if (0xA0 <= ah && ah <= 0xF0 &&
                        0 <= al && al <= 9) {           // 9
                    correction = 0x66;
                }
            }
        } else { // C = 1
            if(!getHF()) {
                if( 0 <= al && al <= 0x9 ) {
                    correction = 0x60;                  // 5
                } else if( 0x0A <= al && al <= 0x0F ) { // 7
                    correction = 0x66;
                }
            } else {
                if( 0 <= al && al <= 0x9 ) {            // 6
                    correction = 0x66;
                } else if( 0x0A <= al && al <= 0x0F ) { // 7
                    correction = 0x66;
                }
            }
        }

        // Flags affected

        if(!getCF()) {
            if( 0 <= ah && ah <= 0x09 &&
             0x09 <= al && al <= 0x09 ) {           // 1
                resCF(); // !
            } if( 0 <= ah && ah <= 0x80 &&
               0x0A <= al && al <= 0x0F ) {         // 2
                resCF(); // !
            } else if( 0x90 <= ah && ah <= 0xF0 &&
                       0x0A <= al && al <= 0x0F ) { // 3
                setCF();
            } else if (0xA0 <= ah && ah <= 0xF0 &&
                          0 <= al && al <= 0x09 ) { // 4
                setCF();
            }
        } else { // C = 1
            setCF(); // !                           // 5
        }

        if(!getNF()) {
            if(!getHF()) {  // !
                if(0 <= al && al <= 0x09 ) {            // 1
                    resHF();
                } else if(0x0A <= al && al <= 0x0F ) {  // 2 // !
                    setHF();
                }
            } else {
                if(0 <= al && al <= 0x09 ) {            // 1
                    resHF();
                } else if(0x0A <= al && al <= 0x0F ) {  // 2 // !
                    setHF();
                }
            }
        } else { // N = 1
            if(!getHF()) {
                resHF();                                // 3 !
            } else {
                if( 0x06 <= al && al <= 0x0F ) {        // 4
                    resHF();
                } else if( 0 <= al && al <= 0x05 ) {    // 5
                    setHF();
                }
            }
        }

        if( !getNF() )
            A += (byte) correction;
        else
            A -= (byte) correction;

        A &= 0x0FF;

        if( (A & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( A == 0 )
            setZF();
        else
            resZF();

        BitSet pA = BitSet.valueOf(new byte[]{A});

        if( (pA.cardinality() % 2) == 0 )
            setPF();
        else
            resPF();

        //flags.X = A & BIT_5;
        //flags.Y = A & BIT_3;
        regTouched(RegTouched.A);
    }

    private void DAA_speccy() {
        int a = A & 0x0FF;
        int correction = 0;
        boolean oc = getCF(); // old carry

        if( getHF() || (a & 0x0F) > 0x09 )
            correction  |= 6;

        if( getCF() || a > 0x099 )
            correction |= 0x60;

        /*if( getNF() )
            correction = -correction;*/

        if( a > 0x099 )
            oc = true;

        if( !getNF() )
            DAA_ADD(correction);
        else
            DAA_SUB(correction);

        if( oc )
            setCF();
        else
            resCF();
    }

    private void DAA_perl() {
        int a = A & 0x0FF;
        int correction = 0;
        boolean oc = getCF(); // old carry

        if( getHF() || (a & 0x0F) > 0x09 )
            correction  |= 6;

        if( getCF() || a > 0x09F )
            correction |= 0x60;

        if( !getNF() ) {
            if ((a > 0x90) && ((a & 0x0f) > 0x09)) {
                correction |= 0x60;
            }
        }

        /*if( getNF() )
            correction = -correction;*/

        if( a > 0x099 )
            oc = true;

        if( !getNF() )
            DAA_ADD(correction);
        else
            DAA_SUB(correction);

        if( oc )
            setCF();
        else
            resCF();
    }

    private void DAA_ADD(int correction) {
        int x = A & 0xFF;
        int result;

        int y = (byte) correction;

        result = x + y;
        boolean isHalfCarry = (((x & 0x0F) + (y & 0x0F)) & 0xF0) != 0;

        if( (result & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( (result & 0x00FF) == 0 )
            setZF();
        else
            resZF();

        if( isHalfCarry )
            setHF();
        else
            resHF();

        if( iSOverflowADD(x, y, result) )
            setVF();
        else
            resVF();

        if( (result & 0xFF00) != 0 )
            setCF();
        else
            resCF();

        BitSet pA = BitSet.valueOf(new byte[]{(byte) (result & 0xFF)});

        if( (pA.cardinality() % 2) == 0 )
            setPF();
        else
            resPF();

        resNF();

        setA((byte) (result & 0xFF));
    }

    private void DAA_SUB(int correction) {
        int x = A & 0xFF;

        int y = (byte) correction;

        int result = x - y;
        boolean isHalfCarry = ((x & 0x0F) - (y & 0x0F)) < 0;

        if( (result & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( (result & 0x00FF) == 0 )
            setZF();
        else
            resZF();

        if( isHalfCarry )
            setHF();
        else
            resHF();

        if( isOverflowSUB(x, y, result) )
            setVF();
        else
            resVF();

        if( result < 0 )
            setCF();
        else
            resCF();

        BitSet pA = BitSet.valueOf(new byte[]{(byte) (result & 0xFF)});

        if( (pA.cardinality() % 2) == 0 )
            setPF();
        else
            resPF();

        setNF();

        setA((byte) (result & 0xFF));
    }

    private void DAA_mio() {
        int ah = A & 0x0F0;
        int al = A & 0x0F;
        byte correction = 0;

        if(!getHF()) { // H = 0 ADD
            if( 0 <= ah && ah <= 0x08 ) {
                if( 0x0A <= al && al <= 0x0F ) {
                    correction = 6;
                    setHF();
                }
            } else {
                if( 0x0A <= ah && ah <= 0x0F ) {
                    if( 0 <= al && al <= 0x09 ) {
                        correction = 0x60;
                        setCF();
                    } else if( 0x0A <= al && al <= 0x0F ) {
                        correction = 0x66;
                        setHF();
                        setCF();
                    }
                } // ah == 9
            }
        } else { // H = 1 SUB

        }

        if( !getNF() )
            A += (byte) correction;
        else
            A -= (byte) correction;

        A &= 0x0FF;
        regTouched(RegTouched.A);

        // Flags affected

        if( (A & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( A == 0 )
            setZF();
        else
            resZF();

        BitSet pA = BitSet.valueOf(new byte[]{A});

        if( (pA.cardinality() % 2) == 0 )
            setPF();
        else
            resPF();
    }

    @Override
    public void CPL() {
        A = (byte) ~A;

        setNF();
        setHF();

        affectXYFlags(A);

        regTouched(RegTouched.A);

        tstate+=4;
    }

    private void SCF_CCF_common(){
        int newXY;
        if (lastQ) {
            newXY = A & FLAG_53_MASK;
        } else {
            newXY = (getF() | A) & FLAG_53_MASK;
        }

        F.clear(5);
        F.clear(3);

        affectXYFlags(newXY);
    }

	@Override
    public void SCF() {
        SCF_CCF_common();

        setCF();

        resNF();
        resHF();

        tstate+=4;
    }

	@Override
    public void CCF() {
        boolean oldCF = getCF();
        SCF_CCF_common();

        if( oldCF ) // undocument
            setHF();
        else
            resHF();

        F.flip(0);

        resNF();

        tstate+=4;
    }

	@Override
    public void LD_r_y_r_z() {
        get_r_(z);

        set_r_(y);

        if( y == 6 || z == 6 )
            tstate+=3;
        tstate+=4;
    }

    private void get_r_(int yz) {
        switch (yz){
            case 0: Z = getB(); break;
            case 1: Z = getC(); break;
            case 2: Z = getD(); break;
            case 3: Z = getE(); break;
            case 4: Z = getH(); break;
            case 5: Z = getL(); break;
            case 6: Z = currentComp.peek(getHL()); break;
            case 7: Z = getA();
        }
    }

    private void set_r_(int yz) {
        switch (yz){
            case 0: setB(Z); break;
            case 1: setC(Z); break;
            case 2: setD(Z); break;
            case 3: setE(Z); break;
            case 4: setH(Z); break;
            case 5: setL(Z); break;
            case 6: currentComp.poke(getHL(), Z); break;
            case 7: setA(Z);
        }
    }

	@Override
    public void HALT() {
        isHalted = true;
        //Logger.halt();
        tstate+=4;
    }

    /*
        https://www.cs.umd.edu/~meesh/cmsc311/clin-cmsc311/Lectures/lecture22/overflow.htm

        https://web.archive.org/web/20170121033813/http://www.cs.umd.edu:80/class/spring2003/cmsc311/Notes/Comb/overflow.html

        https://stackoverflow.com/a/199668/2928048
     */
    private boolean iSOverflowADD(int x, int y, int result) {
        return ((~(x ^ y)) & (x ^ result) & 0x80) != 0;
    }

    private boolean iSOverflowADD16(int x, int y, int result) {
        return ((~(x ^ y)) & (x ^ result) & 0x8000) != 0;
    }

    private boolean isOverflowSUB(int x, int y, int result) {
        return (((x ^ result) & (x ^ y)) & 0x80) != 0;
    }

    private boolean isOverflowSUB16(int x, int y, int result) {
        return (((x ^ result) & (x ^ y)) & 0x8000) != 0;
    }

    private void verifyFlags() {
        /*System.out.println("S:"+(sz5h3pnFlags & SIGN_MASK) + ", " + getSF());
        System.out.println("Z:"+(sz5h3pnFlags & ZERO_MASK) + ", " + getZF());
        System.out.println("Y:"+(sz5h3pnFlags & BIT5_MASK) + ", " + getYF());
        System.out.println("H:"+(sz5h3pnFlags & HALFCARRY_MASK) + ", " + getHF());
        System.out.println("X:"+(sz5h3pnFlags & BIT3_MASK) + ", " + getXF());
        System.out.println("P:"+(sz5h3pnFlags & PARITY_MASK) + ", " + getPF());
        System.out.println("V:"+(sz5h3pnFlags & OVERFLOW_MASK) + ", " + getVF());
        System.out.println("N:"+(sz5h3pnFlags & ADDSUB_MASK) + ", " + getNF());
        System.out.println("C:"+(sz5h3pnFlags & CARRY_MASK) + ", " + getCF());
        System.out.println();*/
    }

    private void ADD_A_common2(int oper8) {
        int regA = A & 0x00FF;
        int res = regA + oper8;

        carryFlag = res > 0xff;
        res &= 0xff;
        sz5h3pnFlags = sz53n_addTable[res];

        /* El módulo 16 del resultado será menor que el módulo 16 del registro A
         * si ha habido HalfCarry. Sucede lo mismo para todos los métodos suma
         * SIN carry */
        if ((res & 0x0f) < (regA & 0x0f)) {
            sz5h3pnFlags |= HALFCARRY_MASK;
        }

        if (((regA ^ ~oper8) & (regA ^ res)) > 0x7f) {
            sz5h3pnFlags |= OVERFLOW_MASK;
        }

        if( carryFlag ) {
            sz5h3pnFlags |= CARRY_MASK;
        }

        System.out.println(": " + res);
    }

    private void ADC_A_common2(int oper8) {
        int regA = A & 0x00FF;
        int res = regA + oper8;

        carryFlag = getCF();

        if (carryFlag) {
            res++;
        }

        carryFlag = res > 0xff;
        res &= 0xff;
        sz5h3pnFlags = sz53n_addTable[res];

        if (((regA ^ oper8 ^ res) & 0x10) != 0) {
            sz5h3pnFlags |= HALFCARRY_MASK;
        }

        if (((regA ^ ~oper8) & (regA ^ res)) > 0x7f) {
            sz5h3pnFlags |= OVERFLOW_MASK;
        }

        if( carryFlag ) {
            sz5h3pnFlags |= CARRY_MASK;
        }

        System.out.println(": " + res);
    }

    private void ADD_A_common(int x, int y, boolean carry) {

        int result = x + y;

        if( carry ) result++;

        //boolean isHalfCarry = (((x & 0x0F) + (y & 0x0F)) & 0xF0) != 0;
        boolean isHalfCarry = ((x ^ y ^ result) & 0x10) != 0;

        if( (result & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( (result & 0x00FF) == 0 )
            setZF();
        else
            resZF();

        if( isHalfCarry )
            setHF();
        else
            resHF();

        if( iSOverflowADD(x, y, result) )
            setVF();
        else
            resVF();

        //if( affectToCarry ) // for INC r_z must be false
        if( (result & 0xFF00) != 0 )
            setCF();
        else
            resCF();

        resNF();

        affectXYFlags(result);

        setA((byte) (result & 0xFF));
    }

	@Override
    public void ADD_A_r_z() {
        int x = A & 0xFF;

        get_r_(z);
        int y = Z & 0x0FF;
//System.out.println(x + ", " + y);
//ADD_A_common2(y);
       ADD_A_common(x, y, false);
//System.out.println(" = " + A);
////verifyFlags();

        if(  z == 6 )
            tstate+=3;
       tstate+=4;
    }

    @Override
    public void ADD_A_IXH() {
        int x = A & 0xFF;

        Z = getIXH();
        int y = Z & 0x0FF;

        ADD_A_common(x, y, false);

        tstate+=8;
    }

    @Override
    public void ADD_A_IXL() {
        int x = A & 0xFF;

        Z = getIXL();
        int y = Z & 0x0FF;

        ADD_A_common(x, y, false);

        tstate+=8;
    }

    @Override
    public void ADD_A_IYH() {
        int x = A & 0xFF;

        Z = getIYH();
        int y = Z & 0x0FF;

        ADD_A_common(x, y, false);

        tstate+=8;
    }

    @Override
    public void ADD_A_IYL() {
        int x = A & 0xFF;

        Z = getIYL();
        int y = Z & 0x0FF;

        ADD_A_common(x, y, false);

        tstate+=8;
    }

	@Override
    public void ADC_A_r_z() {
        int x = A & 0xFF;

        get_r_(z);
        int y = Z & 0xFF;
//System.out.print(x + ", " + y);
//ADC_A_common2(y);
        ADD_A_common(x, y, getCF());
//System.out.println(" = " + A);
//verifyFlags();

        if( z == 6 )
            tstate+=3;
        tstate+=4;
    }

    @Override
    public void ADC_A_IXH() {
        int x = A & 0xFF;

        Z = getIXH();
        int y = Z & 0x0FF;

        ADD_A_common(x, y, getCF());

        tstate+=8;
    }

    @Override
    public void ADC_A_IXL() {
        int x = A & 0xFF;

        Z = getIXL();
        int y = Z & 0x0FF;

        ADD_A_common(x, y, getCF());

        tstate+=8;
    }

    @Override
    public void ADC_A_IYH() {
        int x = A & 0xFF;

        Z = getIYH();
        int y = Z & 0x0FF;

        ADD_A_common(x, y, getCF());

        tstate+=8;
    }

    @Override
    public void ADC_A_IYL() {
        int x = A & 0xFF;

        Z = getIYL();
        int y = Z & 0x0FF;

        ADD_A_common(x, y, getCF());

        tstate+=8;
    }

    @Override
    public void SUB_IX_d() {
        int x = A & 0x0FF;
        d = currentComp.peek(PC++);

        Z = currentComp.peek(MEMPTR = getIX() + d);
        int y = Z & 0x0FF;

        int result = x - y;
        boolean isHalfCarry = ((x & 0x0F) - (y & 0x0F)) < 0;

        if( (result & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( (result & 0x00FF) == 0 )
            setZF();
        else
            resZF();

        if( isHalfCarry )
            setHF();
        else
            resHF();

        if( isOverflowSUB(x, y, result) )
            setVF();
        else
            resVF();

        if( result < 0 )
            setCF();
        else
            resCF();

        setNF();

        affectXYFlags(result);

        setA((byte) (result & 0xFF));

        tstate+=19;
    }

    @Override
    public void SBC_A_IX_d() {
        d = currentComp.peek(PC++);
        Z = currentComp.peek(MEMPTR = getIX() + d);
        int x = A & 0xFF;
        int y = Z & 0xFF;

//System.out.print(x + ", " + y);
//SBC_A_common2(y);
        SUB_A_common(x, y, getCF());
//System.out.println(" = " + A);
//verifyFlags();
/*        int result = x - y;

        if( getCF() ) result--;

        if( result == 0 )
            setZF();
        else
            resZF();

        if( (result & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( (result & 0xFF00) != 0 )
            setCF();
        else
            resCF();

        if( iSOverflowADD(x, y, result) )
            setVF();
        else
            resVF();

        setNF();

        setA((byte) (result & 0xFF));*/
        tstate+=19;
    }

    @Override
    public void SBC_A_IY_d() {
        d = currentComp.peek(PC++);
        Z = currentComp.peek(getIY()+d);
        int x = A & 0xFF;
        int y = Z & 0xFF;

        SUB_A_common(x, y, getCF());
        /*int result = x - y;

        if( getCF() ) result--;

        if( result == 0 )
            setZF();
        else
            resZF();

        if( (result & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( (result & 0xFF00) != 0 )
            setCF();
        else
            resCF();

        if( iSOverflowADD(x, y, result) )
            setVF();
        else
            resVF();

        setNF();

        setA((byte) (result & 0xFF));*/

        tstate+=19;
    }

    @Override
    public void AND_IX_d() {
        d = currentComp.peek(PC++);
        Z = currentComp.peek(MEMPTR = getIX() + d);

        AND_common();
/*        A &= Z; regTouched(RegTouched.A);

        BitSet pA = BitSet.valueOf(new byte[]{A});

        if( (pA.cardinality() % 2) == 0 )
            setPF();
        else
            resPF();

        if (A == 0)
            setZF();
        else
            resZF();

        if (A < 0)
            setSF();
        else
            resSF();

        resCF();
        resNF();
        setHF();*/
        tstate+=19;
    }

    @Override
    public void AND_IY_d() {
        d = currentComp.peek(PC++);
        Z = currentComp.peek(getIY()+d);

        AND_common();
/*        A &= Z; regTouched(RegTouched.A);

        BitSet pA = BitSet.valueOf(new byte[]{A});

        if( (pA.cardinality() % 2) == 0 )
            setPF();
        else
            resPF();

        if (A == 0)
            setZF();
        else
            resZF();

        if (A < 0)
            setSF();
        else
            resSF();

        resCF();
        resNF();
        setHF();*/
        tstate+=19;
    }

    @Override
    public void XOR_IX_d() {
        d = currentComp.peek(PC++);
        Z = currentComp.peek(MEMPTR = getIX() + d);

        XOR_common();

        tstate+=19;
    }

    @Override
    public void XOR_IY_d() {
        d = currentComp.peek(PC++);
        Z = currentComp.peek(getIY()+d);

        XOR_common();

        tstate+=19;
    }

    @Override
    public void OR_IX_d() {
        d = currentComp.peek(PC++);
        Z = currentComp.peek(MEMPTR = getIX() + d);

        OR_common();

        tstate+=19;
    }

    @Override
    public void OR_IY_d() {
        d = currentComp.peek(PC++);
        Z = currentComp.peek(getIY()+d);

        OR_common();

        tstate+=19;
    }

    @Override
    public void CP_IX_d() {
        d = currentComp.peek(PC++);

        int x = A & 0xFF;

        Z = currentComp.peek(MEMPTR = getIX() + d);

        int y = Z & 0xFF;

        CP_common(x, y);

        tstate+=19;
    }

    private void SUB_A_common2(int oper8) {
        int regA = A & 0x00FF;
        int res = regA - oper8;

        carryFlag = res < 0;
        res &= 0xff;
        sz5h3pnFlags = sz53n_subTable[res];

        /* El módulo 16 del resultado será mayor que el módulo 16 del registro A
         * si ha habido HalfCarry. Sucede lo mismo para todos los métodos resta
         * SIN carry, incluido cp */
        if ((res & 0x0f) > (regA & 0x0f)) {
            sz5h3pnFlags |= HALFCARRY_MASK;
        }

        if (((regA ^ oper8) & (regA ^ res)) > 0x7f) {
            sz5h3pnFlags |= OVERFLOW_MASK;
        }

        if( carryFlag ) {
            sz5h3pnFlags |= CARRY_MASK;
        }

        System.out.println(": " + res);
    }

    private void SBC_A_common2(int oper8) {
        int regA = A & 0x00FF;
        int res = regA - oper8;

        carryFlag = getCF();

        if (carryFlag) {
            res--;
        }

        carryFlag = res < 0;
        res &= 0xff;
        sz5h3pnFlags = sz53n_subTable[res];

        if (((regA ^ oper8 ^ res) & 0x10) != 0) {
            sz5h3pnFlags |= HALFCARRY_MASK;
        }

        if (((regA ^ oper8) & (regA ^ res)) > 0x7f) {
            sz5h3pnFlags |= OVERFLOW_MASK;
        }

        if( carryFlag ) {
            sz5h3pnFlags |= CARRY_MASK;
        }

        System.out.println(": " + res);
    }

    private void SUB_A_common(int x, int y, boolean carry) {

        int result = x - y;

        if( carry ) result--;

        //boolean isHalfCarry = ((x & 0x0F) - (y & 0x0F)) < 0;
        boolean isHalfCarry = (x & 0x0F) < ((y & 0x0F) + (carry?1:0));
        //boolean isHalfCarry = ((x ^ y ^ result) & 0x10) != 0; // ANTES
        //boolean isHalfCarry = ((x ^ ~y ^ result) & 0x10) != 0; // DESPUES

        if( (result & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( (result & 0x00FF) == 0 )
            setZF();
        else
            resZF();

        if( isHalfCarry )
            setHF();
        else
            resHF();

        if( isOverflowSUB(x, y, result) )
            setVF();
        else
            resVF();

        if( result < 0 )
            setCF();
        else
            resCF();

        setNF();

        affectXYFlags(result);

        setA((byte) (result & 0xFF));
    }

	@Override
    public void SUB_r_z() {
        int x = A & 0xFF;

        get_r_(z);
        int y = Z & 0xFF;
    //System.out.print(x + ", " + y);
    //SUB_A_common2(y);
        SUB_A_common(x,y,false);
    //System.out.println(" = " + A);
    //verifyFlags();

        if( z == 6 )
            tstate+=3;
        tstate+=4;
    }

	@Override
    public void SBC_A_r_z() {
        int x = A & 0xFF;

        get_r_(z);
        int y = Z & 0xFF;
//System.out.print(x + ", " + y);
//SBC_A_common2(y);
        SUB_A_common(x, y, getCF());
//System.out.println(" = " + A);
//verifyFlags();
        /*if( getCF() ) result--;

        if( result == 0 )
            setZF();
        else
            resZF();

        if( (result & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( (result & 0xFF00) != 0 )
            setCF();
        else
            resCF();

        if( iSOverflowADD(x, y, result) )
            setVF();
        else
            resVF();

        setNF();

        setA((byte) (result & 0xFF));*/

        if( z == 6  )
            tstate+=3;
        tstate+=4;
    }

    @Override
    public void SUB_IXH() {
        int x = A & 0xFF;

        Z = getIXH();
        int y = Z & 0x0FF;

        SUB_A_common(x, y, false);

        tstate+=8;
    }

    @Override
    public void SUB_IXL() {
        int x = A & 0xFF;

        Z = getIXL();
        int y = Z & 0x0FF;

        SUB_A_common(x, y, false);

        tstate+=8;
    }

    @Override
    public void SUB_IYH() {
        int x = A & 0xFF;

        Z = getIYH();
        int y = Z & 0x0FF;

        SUB_A_common(x, y, false);

        tstate+=8;
    }

    @Override
    public void SUB_IYL() {
        int x = A & 0xFF;

        Z = getIYL();
        int y = Z & 0x0FF;

        SUB_A_common(x, y, false);

        tstate+=8;
    }

    @Override
    public void SBC_IXH() {
        int x = A & 0xFF;

        Z = getIXH();
        int y = Z & 0x0FF;

        SUB_A_common(x, y, getCF());

        tstate+=8;
    }

    @Override
    public void SBC_IXL() {
        int x = A & 0xFF;

        Z = getIXL();
        int y = Z & 0x0FF;

        SUB_A_common(x, y, getCF());

        tstate+=8;
    }

    @Override
    public void SBC_IYH() {
        int x = A & 0xFF;

        Z = getIYH();
        int y = Z & 0x0FF;

        SUB_A_common(x, y, getCF());

        tstate+=8;
    }

    @Override
    public void SBC_IYL() {
        int x = A & 0xFF;

        Z = getIYL();
        int y = Z & 0x0FF;

        SUB_A_common(x, y, getCF());

        tstate+=8;
    }

    private void AND_common() {
        A &= (Z & 0x00FF); regTouched(RegTouched.A);

        BitSet pA = BitSet.valueOf(new byte[]{A});

        if( (pA.cardinality() % 2) == 0 )
            setPF();
        else
            resPF();

        if (A == 0)
            setZF();
        else
            resZF();

        if( (A & 0x0080) != 0)
            setSF();
        else
            resSF();

        affectXYFlags(A);

        resCF();
        resNF();
        setHF();
    }

	@Override
    public void AND_r_z() {
        get_r_(z);

        AND_common();
        /*A &= Z; regTouched(RegTouched.A);

        BitSet pA = BitSet.valueOf(new byte[]{A});

        if( (pA.cardinality() % 2) == 0 )
            setPF();
        else
            resPF();

        if (A == 0)
            setZF();
        else
            resZF();

        if (A < 0)
            setSF();
        else
            resSF();

        resCF();
        resNF();
        setHF();*/

        if( z == 6 )
            tstate+=3;
        tstate+=4;
    }

    @Override
    public void AND_IXH() {
        Z = getIXH();

        AND_common();

        tstate+=8;
    }

    @Override
    public void AND_IXL() {
        Z = getIXL();

        AND_common();

        tstate+=8;
    }

    @Override
    public void AND_IYH() {
        Z = getIYH();

        AND_common();

        tstate+=8;
    }

    @Override
    public void AND_IYL() {
        Z = getIYL();

        AND_common();

        tstate+=8;
    }

    private void XOR_common() {
        A ^= Z; regTouched(RegTouched.A);

        BitSet pA = BitSet.valueOf(new byte[]{A});

        if( (pA.cardinality() % 2) == 0 )
            setPF();
        else
            resPF();

        if (A == 0)
            setZF();
        else
            resZF();

        if( (A & 0x0080) != 0 )
            setSF();
        else
            resSF();

        affectXYFlags(A);

        resCF();
        resNF();
        resHF();
    }

	@Override
    public void XOR_r_z() {
        get_r_(z);

        XOR_common();

        if( z== 6 )
            tstate+=3;
        tstate+=4;
    }

    @Override
    public void XOR_IXH() {
        Z = getIXH();

        XOR_common();

        tstate+=8;
    }

    @Override
    public void XOR_IXL() {
        Z = getIXL();

        XOR_common();

        tstate+=8;
    }

    @Override
    public void XOR_IYH() {
        Z = getIYH();

        XOR_common();

        tstate+=8;
    }

    @Override
    public void XOR_IYL() {
        Z = getIYL();

        XOR_common();

        tstate+=8;
    }

    private void OR_common() {
        A |= Z; regTouched(RegTouched.A);

        BitSet pA = BitSet.valueOf(new byte[]{A});

        if( (pA.cardinality() % 2) == 0 )
            setPF();
        else
            resPF();

        if (A == 0)
            setZF();
        else
            resZF();

        if( (A & 0x0080) != 0)
            setSF();
        else
            resSF();

        affectXYFlags(A);

        resCF();
        resNF();
        resHF();
    }

	@Override
    public void OR_r_z() {
        get_r_(z);

        OR_common();

        if( z == 6 )
            tstate+=3;
        tstate+=4;
    }

    @Override
    public void OR_IXH() {
        Z = getIXH();

        OR_common();
    }

    @Override
    public void OR_IXL() {
        Z = getIXL();

        OR_common();
    }

    @Override
    public void OR_IYH() {
        Z = getIYH();

        OR_common();
    }

    @Override
    public void OR_IYL() {
        Z = getIYL();

        OR_common();
    }

    private void CP_common(int x, int y) {
        int result = x - y;
        boolean isHalfCarry = ((x & 0x0F) - (y & 0x0F)) < 0;

        if( (result & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( (result & 0x00FF) == 0 )
            setZF();
        else
            resZF();

        if( isHalfCarry )
            setHF();
        else
            resHF();

        if( isOverflowSUB(x, y, result) )
            setVF();
        else
            resVF();

        if( result < 0 )
            setCF();
        else
            resCF();

        setNF();

        affectXYFlags(y);
    }

	@Override
    public void CP_r_z() {
        int x = A & 0xFF;

        get_r_(z);

        int y = Z & 0xFF;
//System.out.println(x + ", " + y);
//cp_common2(y);
        CP_common(x, y);
////verifyFlags();
        if( z == 6 )
            tstate+=3;
        tstate+=4;
    }

    @Override
    public void CP_IXH() {
        int x = A & 0xFF;

        Z = getIXH();

        int y = Z & 0xFF;

        CP_common(x,y);

        tstate+=8;
    }

    @Override
    public void CP_IXL() {
        int x = A & 0xFF;

        Z = getIXL();

        int y = Z & 0xFF;

        CP_common(x,y);

        tstate+=8;
    }

    @Override
    public void CP_IYH() {
        int x = A & 0xFF;

        Z = getIYH();

        int y = Z & 0xFF;

        CP_common(x,y);

        tstate+=8;
    }

    @Override
    public void CP_IYL() {
        int x = A & 0xFF;

        Z = getIYL();

        int y = Z & 0xFF;

        CP_common(x,y);

        tstate+=8;
    }

	@Override    
    public void RET_cc_y() {
        boolean ccSet = false;

        switch (cc[y]) {
            case "NZ":
                ccSet = ! getZF();        
                break;
            case "Z":
                ccSet = getZF();
                break;
            case "NC":
                ccSet = ! getCF();
                break;
            case "C":
                ccSet = getCF();
                break;
            case "PO":
                ccSet = ! getPF();
                break;
            case "PE":
                ccSet = getPF();
                break;
            case "P":
                ccSet = ! getSF();
                break;
            case "M":
                ccSet = getSF();
        }

        if (ccSet) {
            Z = currentComp.peek(SP++);
            W = currentComp.peek(SP++);

            PC = MEMPTR = getWZ();
            tstate+=6;
        }
        tstate+=5;
    }

	@Override        
    public void POP_rp2_p() {
        Z = currentComp.peek(SP++);
        W = currentComp.peek(SP++);
        
        switch (rp2[p]) {
            case "BC":
                setB(W);
                setC(Z);
                break;
            case "DE":
                setD(W);
                setE(Z);
                break;
            case "HL":
                setH(W);
                setL(Z);
                break;
            case "AF":
                setA(W);
                setF(Z);
        }
    }

	@Override    
    public void RET() {
        Z = currentComp.peek(SP++);
        W = currentComp.peek(SP++);

        PC = MEMPTR = getWZ();
        //PC &= 0x0000_FFFF; // TODO generalizar a todos los cambios de PC
        regTouched(RegTouched.SP);

        tstate+=10;
    }

	@Override    
    public void EXX() {
        Z = getB(); setB(alternative.B); alternative.B = Z; regTouched(RegTouched.B_);
        Z = getC(); setC(alternative.C); alternative.C = Z; regTouched(RegTouched.C_);

        Z = getH(); setH(alternative.H); alternative.H = Z; regTouched(RegTouched.H_);
        Z = getL(); setL(alternative.L); alternative.L = Z; regTouched(RegTouched.L_);
        
        Z = getD(); setD(alternative.D); alternative.D = Z; regTouched(RegTouched.D_);
        Z = getE(); setE(alternative.E); alternative.E = Z; regTouched(RegTouched.E_);
    }

	@Override    
    public void JP_HL() {
        PC = getHL();
        tstate+=4;
    }

	@Override    
    public void LD_SP_HL() {
        setSP(getHL());
        tstate+=6;
    }

    @Override
    public void LD_SP_IX() {
        setSP(getIX());
        tstate+=10;
    }

    @Override
    public void LD_SP_IY() {
        setSP(getIY());
        tstate+=10;
    }

	@Override
    public void JP_cc_y_nn() {
        boolean ccSet = false;

        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        MEMPTR = getWZ();

        switch (cc[y]) {
            case "NZ":
                ccSet = ! getZF();
                break;
            case "Z":
                ccSet = getZF();
                break;
            case "NC":
                ccSet = ! getCF();
                break;
            case "C":
                ccSet = getCF();
                break;
            case "PO":
                ccSet = ! getPF();
                break;
            case "PE":
                ccSet = getPF();
                break;
            case "P":
                ccSet = ! getSF();
                break;
            case "M":
                ccSet = getSF();
        }

        if (ccSet) {
            PC = MEMPTR;
            tstate+=9;
        }

        tstate+=1;
    }

	@Override
    public void JP_nn() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        PC = MEMPTR = getWZ();
        tstate+=10;
    }

	@Override
    public void OUT_n_A() {
        Z = currentComp.peek(PC++);
        W = 0; //A;
        tstate+=11;
        currentComp.write((short)getWZ(), A, (int)(tstate - old_tstate_out));
        Z++;
        MEMPTR = getWZ();
        old_tstate_out = tstate;
    }

	@Override
    public void IN_A_n() { // TODO
        Z = currentComp.peek(PC++);
        W = A;
        MEMPTR = (short)getWZ();

        setA(currentComp.read((short) MEMPTR++));

        tstate+=11;
    }

	@Override
    public void EX_SP_HL() {
        Z = currentComp.peek(SP);
        currentComp.poke(SP, L);
        setL(Z);

        W = currentComp.peek(SP+1);
        currentComp.poke(SP+1, H);
        setH(W);

        MEMPTR = getHL();
    }

    @Override
    public void EX_SP_IX() {
        Z = currentComp.peek(SP);
        currentComp.poke(SP, getIXL());
        setIXL(Z);

        W = currentComp.peek(SP+1);
        currentComp.poke(SP+1, getIXH());
        setIXH(W);

        MEMPTR = getIX();
    }

    @Override
    public void EX_SP_IY() {
        Z = currentComp.peek(SP);
        currentComp.poke(SP, getIYL());
        setIYL(Z);

        W = currentComp.peek(SP+1);
        currentComp.poke(SP+1, getIYH());
        setIYH(W);

        MEMPTR = getIY();
    }

	@Override
    public void EX_DE_HL() {
        Z = E;
        setE(L);
        setL(Z);

        W = D;
        setD(H);
        setH(W);
    }

	@Override
    public void DI() {
        IFF1 = IFF2 = false;
    }

    @Override
    public void EI() {
        IFF1 = IFF2 = true;
        // opcodes countdown to be interrupts effectively enabled:
        // this one (EI) and next one
        IFF3 = 2;

        tstate+=4;
    }

	@Override
    public void CALL_cc_y_nn() {
        boolean ccSet = false;
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        MEMPTR = getWZ();

        switch (cc[y]) {
            case "NZ":
                ccSet = ! getZF();
                break;
            case "Z":
                ccSet = getZF();
                break;
            case "NC":
                ccSet = ! getCF();
                break;
            case "C":
                ccSet = getCF();
                break;
            case "PO":
                ccSet = ! getPF();
                break;
            case "PE":
                ccSet = getPF();
                break;
            case "P":
                ccSet = ! getSF();
                break;
            case "M":
                ccSet = getSF();
        }

        if (ccSet) {
            currentComp.poke(--SP, (byte)((PC & 0xFF00)>>8));
            currentComp.poke(--SP, (byte)(PC & 0x00FF));

            PC = MEMPTR;
            regTouched(RegTouched.SP);

            tstate+=16;
        }
        tstate+=1;
    }

	@Override
    public void PUSH_rp2_p() {
        switch (rp2[p]) {
            case "BC":
                W = B;
                Z = C;
                break;
            case "DE":
                W = D;
                Z = E;
                break;
            case "HL":
                W = H;
                Z = L;
                break;
            case "AF":
                W = A;
                Z = getF();
        }

        currentComp.poke(--SP, W);
        currentComp.poke(--SP, Z);
        regTouched(RegTouched.SP);
    }

	@Override
    public void CALL_nn() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        currentComp.poke(--SP, (byte)((PC & 0xFF00)>>8));
        currentComp.poke(--SP, (byte)(PC & 0x00FF));

        PC = MEMPTR = getWZ();
        regTouched(RegTouched.SP);

        tstate+=17;
    }

	@Override
    public void ADD_A_n() {
        int x = A & 0x0FF;
        Z = currentComp.peek(PC++);
        int y = Z & 0x0FF;
    //System.out.print(x + ", " + y);
    //ADD_A_common2(y);
        ADD_A_common(x, y, false);
    //System.out.println(" = " + A);
    //verifyFlags();
/*        int result = x + y;
        boolean isHalfCarry = (((x & 0x0F) + (y & 0x0F)) & 0xF0) != 0;

        if( (result & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( (result & 0x00FF) == 0 )
            setZF();
        else
            resZF();

        if( isHalfCarry )
            setHF();
        else
            resHF();

        if( iSOverflowADD(x, y, result) )
            setVF();
        else
            resVF();

        if( (result & 0xFF00) != 0 )
            setCF();
        else
            resCF();

        resNF();

        setA((byte) (result & 0xFF));*/

        tstate+=7;
    }

	@Override
    public void ADC_A_n() {
        Z = currentComp.peek(PC++);
        int x = A & 0x0FF;
        int y = Z & 0x0FF;

        ADD_A_common(x, y, getCF());

        /*int result = x + y;

        if( getCF() ) result++;

        boolean isHalfCarry = (((x & 0x0F) + ((getCF()?y+1:y) & 0x0F)) & 0xF0) != 0;

        if( (result & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( (result & 0x00FF) == 0 )
            setZF();
        else
            resZF();

        if( isHalfCarry )
            setHF();
        else
            resHF();

        if( iSOverflowADD(x, y, result) )
            setVF();
        else
            resVF();

        if( (result & 0xFF00) != 0 )
            setCF();
        else
            resCF();

        resNF();

        setA((byte) (result & 0xFF));

        affectXYFlags(A);
*/
        tstate+=7;
    }

    @Override
    public void SUB_n() {
        Z = currentComp.peek(PC++);
        int x = A & 0xFF;
        int y = Z & 0xFF;

        SUB_A_common(x,y,false);
        /*int result = x - y;
        boolean isHalfCarry = ((x & 0x0F) - (y & 0x0F)) < 0;

        if( (result & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( (result & 0x00FF) == 0 )
            setZF();
        else
            resZF();

        if( isHalfCarry )
            setHF();
        else
            resHF();

        if( isOverflowSUB(x, y, result) )
            setVF();
        else
            resVF();

        if( result < 0 )
            setCF();
        else
            resCF();

        setNF();

        setA((byte) (result & 0xFF));
*/
        tstate+=7;
    }

	@Override
    public void SBC_A_n() {
        Z = currentComp.peek(PC++);
        int x = A & 0xFF;
        int y = Z & 0xFF;

        SUB_A_common(x, y, getCF());
        /*
        int result = x - y;

        if( getCF() ) result--;

        boolean isHalfCarry = (((x & 0x0F) - ((getCF()?y+1:y) & 0x0F)) & 0xF0) != 0;

        if( (result & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( (result & 0x00FF) == 0 )
            setZF();
        else
            resZF();

        if( isHalfCarry )
            setHF();
        else
            resHF();

        if( isOverflowSUB(x, y, result) )
            setVF();
        else
            resVF();

        if( result < 0 )
            setCF();
        else
            resCF();

        setNF();

        setA((byte) (result & 0xFF));
*/
        tstate+=7;
    }

	@Override
    public void AND_n() {
        Z = currentComp.peek(PC++);
//System.out.print(A + ", " + Z);
//AND_common2(Z);
        AND_common();
//System.out.println(" = " + A);
//verifyFlags();
        /*A &= Z; regTouched(RegTouched.A);

        BitSet pA = BitSet.valueOf(new byte[]{A});

        if( (pA.cardinality() % 2) == 0 )
            setPF();
        else
            resPF();

        if (A == 0)
            setZF();
        else
            resZF();

        if (A < 0)
            setSF();
        else
            resSF();

        resCF();
        resNF();
        setHF();*/

        tstate+=7;
    }

    private void AND_common2(int oper8) {
        int regA = (A & oper8 & 0x00FF);

        sz5h3pnFlags = sz53pn_addTable[regA] | HALFCARRY_MASK;
        sz5h3pnFlags &= ~CARRY_MASK;

        System.out.println(": " + regA);
    }

	@Override
    public void XOR_n() {
        Z = currentComp.peek(PC++);
//System.out.print(A + ", " + Z);
//XOR_common2(Z);
        XOR_common();
//System.out.println(" = " + A);
//verifyFlags();
        tstate+=7;
    }

    public final void XOR_common2(int oper8) {
        int regA = A & 0x00ff;
        oper8 &= 0x00ff;
        regA ^= oper8;
        //try {
            sz5h3pnFlags = sz53pn_addTable[regA];
        /*} catch (Exception e) {
            //throw new Exception(e);
            int x = 0;
        }*/
        sz5h3pnFlags &= ~CARRY_MASK;

        System.out.println(": " + regA);
    }

	@Override
    public void OR_n() {
        Z = currentComp.peek(PC++);
//System.out.print(A + ", " + Z);
//OR_common2(Z);
        OR_common();
//System.out.println(" = " + A);
//verifyFlags();
        tstate+=7;
    }

    private void OR_common2(int oper8) {
        int regA = A & 0x00ff;
        oper8 &= 0x00ff;
        regA |= oper8;
        //try {
            sz5h3pnFlags = sz53pn_addTable[regA];
        /*} catch(Exception e) {
            int y = 0;
        }*/
        sz5h3pnFlags &= ~CARRY_MASK;

        System.out.println(": " + regA);
    }

	@Override
    public void CP_n() {
        int x = A & 0xFF;

        Z = currentComp.peek(PC++);
        int y = Z & 0xFF;
//System.out.println(x + ", " + y);
//cp_common2(y);
        CP_common(x, y);
        /*int result = x - y;

        boolean isHalfCarry = ((x & 0x0F) - (y & 0x0F)) < 0;

        if( (result & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( (result & 0x00FF) == 0 )
            setZF();
        else
            resZF();

        if( isHalfCarry )
            setHF();
        else
            resHF();

        if( isOverflowSUB(x, y, result) )
            setVF();
        else
            resVF();

        if( result < 0 )
            setCF();
        else
            resCF();

        setNF();

        affectXYFlags(y);*/
////verifyFlags();

        tstate+=7;
    }

    public final void cp_common2(int oper8) {
        int regA = A & 0x00FF;
        int res = regA - (oper8 & 0xff);

        carryFlag = res < 0;
        res &= 0xff;

        sz5h3pnFlags = (sz53n_addTable[oper8] & FLAG_53_MASK)
                | // No necesito preservar H, pero está a 0 en la tabla de todas formas
                (sz53n_subTable[res] & FLAG_SZHN_MASK);

        if ((res & 0x0f) > (regA & 0x0f)) {
            sz5h3pnFlags |= HALFCARRY_MASK;
        }

        if (((regA ^ oper8) & (regA ^ res)) > 0x7f) {
            sz5h3pnFlags |= OVERFLOW_MASK;
        }

        if( carryFlag ) {
            sz5h3pnFlags |= CARRY_MASK;
        }
    }

	@Override
    public void RST_y_8() {
        Z = (byte) (y * 8);
        W = 0x00;

        currentComp.poke(--SP, (byte)((PC & 0xFF00)>>8));
        currentComp.poke(--SP, (byte)(PC & 0x00FF));

        PC = MEMPTR = getWZ();
        regTouched(RegTouched.SP);

        tstate+=11;
    }

    /* CB prefix */

    @Override
    public void RLC_r_z() {
        get_r_(z);

        RLC_common();

        set_r_(z);

        if( z == 6 )
            tstate+=7;
        tstate+=8;
    }

    @Override
    public void RLC_IX_d() {
        Z = currentComp.peek(MEMPTR = getIX() + d);
        RLC_common();
        currentComp.poke(getIX() + d, Z);
        if( z != 6)
            set_r_(z);
        tstate+=23;
    }

    @Override
    public void RLC_IY_d() {
        Z = currentComp.peek(MEMPTR = getIY() + d);
        RLC_common();
        currentComp.poke(getIY() + d, Z);
        if( z != 6)
            set_r_(z);
        tstate+=23;
    }

    private void RLC_common() {
        byte c = (byte) (Z & 0x80); // pre-carry

        Z <<= 1;

        if( c != 0 ) {  // Must test pre-carry first!!
            Z |= 1;
            setCF();
        } else {
            resCF();
        }

        if( (Z & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( Z == 0 )
            setZF();
        else
            resZF();

        BitSet pZ = BitSet.valueOf(new byte[]{Z});
        if( (pZ.cardinality() % 2) == 0 )
            setPF();
        else
            resPF();

        resNF();
        resHF();

        affectXYFlags(Z);
    }

    public void RRC_r_z() {
        get_r_(z);

        RRC_common();

        set_r_(z);

        if( z == 6 )
            tstate+=7;
        tstate+=8;
    }

    private void RRC_common() {
        byte c = (byte) (Z & 0x01); // pre-carry

        Z >>= 1;

        if( c != 0 ) { // Must test pre-carry first!!
            Z |= (byte) 0x80;
            setCF();
        } else {
            Z &= 0x7F;
            resCF();
        }

        if( (Z & 0x80) != 0 )
            setSF();
        else
            resSF();

        if (Z == 0)
            setZF();
        else
            resZF();

        BitSet pZ = BitSet.valueOf(new byte[]{Z});
        if( (pZ.cardinality() % 2) == 0 )
            setPF();
        else
            resPF();

        resNF();
        resHF();

        affectXYFlags(Z);
    }

    @Override
    public void RRC_IX_d() {
        Z = currentComp.peek(MEMPTR = getIX() + d);
        RRC_common();
        currentComp.poke(getIX() + d, Z);
        if( z != 6)
            set_r_(z);
        tstate+=23;
    }

    @Override
    public void RRC_IY_d() {
        Z = currentComp.peek(MEMPTR = getIY() + d);
        RRC_common();
        currentComp.poke(getIY() + d, Z);
        if( z != 6)
            set_r_(z);
        tstate+=23;
    }

    public void RL_r_z() {
        get_r_(z);

        RL_common();

        set_r_(z);

        if( z == 6 )
            tstate+=7;
        tstate+=8;
    }

    private void RL_common() {
        byte c = (byte) (Z & 0x80); // pre-carry
        boolean oc = getCF(); // old-carry

        Z <<= 1;

        if ((Z & 0x80) != 0)
            setSF();
        else
            resSF();

        if( c != 0 ) {
            setCF();
        } else {
            resCF();
        }

        if( oc )
            Z |= 1;
        else
            Z &= (byte) 0xFE;

        if( Z == 0 )
            setZF();
        else
            resZF();

        BitSet pZ = BitSet.valueOf(new byte[]{Z});
        if( (pZ.cardinality() % 2) == 0 )
            setPF();
        else
            resPF();

        resNF();
        resHF();

        affectXYFlags(Z);
    }

    @Override
    public void RL_IX_d() {
        Z = currentComp.peek(MEMPTR = getIX() + d);
        RL_common();
        currentComp.poke(getIX() + d, Z);
        if( z != 6)
            set_r_(z);
        tstate+=23;
    }

    @Override
    public void RL_IY_d() {
        Z = currentComp.peek(MEMPTR = getIY() + d);
        RL_common();
        currentComp.poke(getIY() + d, Z);
        if( z != 6)
            set_r_(z);
        tstate+=23;
    }
    
    public void RR_r_z() {
        get_r_(z);

        RR_common();

        set_r_(z);

        if( z == 6 )
            tstate+=7;
        tstate+=8;
    }

    private void RR_common() {
        byte c = (byte) (Z & 1); // pre-carry
        boolean oc = getCF();   // old carry

        Z >>>= 1;

        if( c != 0 ) {
            setCF();
        } else {
            resCF();
        }

        if( oc )
            Z |= (byte) 0x80;
        else
            Z &= (byte) 0x7F;

        if ((Z & 0x80) != 0)
            setSF();
        else
            resSF();

        if( Z == 0 )
            setZF();
        else
            resZF();

        BitSet pZ = BitSet.valueOf(new byte[]{Z});
        if( (pZ.cardinality() % 2) == 0 )
            setPF();
        else
            resPF();

        resNF();
        resHF();

        affectXYFlags(Z);
    }

    @Override
    public void RR_IX_d() {
        Z = currentComp.peek(MEMPTR = getIX() + d);
        RR_common();
        currentComp.poke(getIX() + d, Z);
        if( z != 6)
            set_r_(z);
        tstate+=23;
    }

    @Override
    public void RR_IY_d() {
        Z = currentComp.peek(MEMPTR = getIY() + d);
        RR_common();
        currentComp.poke(getIY() + d, Z);
        if( z != 6)
            set_r_(z);
        tstate+=23;
    }

    public void SRA_r_z() {
        get_r_(z);

        SRA_common();

        set_r_(z);

        if( z == 6 )
            tstate+=7;
        tstate+=8;
    }

    private void SRA_common() {
        byte c = (byte) (Z & 1);

        Z >>= 1; // sign bit is preserved

        if( (Z & 0x80) == 0x80 )
            setSF();
        else
            resSF();

        if( Z == 0 )
            setZF();
        else
            resZF();

        BitSet pZ = BitSet.valueOf(new byte[]{Z});

        if( (pZ.cardinality() % 2) == 0 )
            setPF();
        else
            resPF();

        if( c != 0 )
            setCF();
        else
            resCF();

        resNF();
        resHF();

        affectXYFlags(Z);
    }

    @Override
    public void SRA_IX_d() {
        Z = currentComp.peek(MEMPTR = getIX() + d);
        SRA_common();
        currentComp.poke(getIX() + d, Z);
        if( z != 6)
            set_r_(z);
        tstate+=23;
    }

    @Override
    public void SRA_IY_d() {
        Z = currentComp.peek(MEMPTR = getIY() + d);
        SRA_common();
        currentComp.poke(getIY() + d, Z);
        if( z != 6)
            set_r_(z);
        tstate+=23;
    }

    public void SLA_r_z() {
        get_r_(z);

        SLA_common();

        set_r_(z);

        if( z == 6 )
            tstate+=7;
        tstate+=8;
    }

    private void SLA_common() {
        byte c = (byte) (Z & 0x80); // pre-carry

        Z <<= 1; // bit 0 is preserved to 0

        if( (Z & 0x80) == 0x80 )
            setSF();
        else
            resSF();

        if( Z == 0 )
            setZF();
        else
            resZF();

        if( c != 0 )
            setCF();
        else
            resCF();

        BitSet pZ = BitSet.valueOf(new byte[]{Z});

        if( (pZ.cardinality() % 2) == 0 )
            setPF();
        else
            resPF();

        resHF();
        resNF();

        affectXYFlags(Z);
    }

    @Override
    public void SLA_IX_d() {
        Z = currentComp.peek(MEMPTR = getIX() + d);
        SLA_common();
        currentComp.poke(getIX() + d, Z);
        if( z != 6)
            set_r_(z);
        tstate+=23;
    }

    @Override
    public void SLA_IY_d() {
        Z = currentComp.peek(MEMPTR = getIY() + d);
        SLA_common();
        currentComp.poke(getIY() + d, Z);
        if( z != 6)
            set_r_(z);
        tstate+=23;
    }

    public void SLL_r_z() { // undocumented
        get_r_(z);

        SLL_common();

        set_r_(z);

        if( z == 6 )
            tstate+=7;
        tstate+=8;
    }

    private void SLL_common() {
        byte c = (byte) (Z & 0x80); // pre-carry

        Z <<= 1;
        Z |= 1;

        if( (Z & 0x80) == 0x80 )
            setSF();
        else
            resSF();

        if( c != 0 )
            setCF();
        else
            resCF();

        BitSet pZ = BitSet.valueOf(new byte[]{Z});

        if( (pZ.cardinality() % 2) == 0 )
            setPF();
        else
            resPF();

        resZF(); // never can be Zero
        resHF();
        resNF();

        affectXYFlags(Z);
    }

    @Override
    public void SLL_IX_d() { // undocumented
        Z = currentComp.peek(MEMPTR = getIX() + d);
        SLL_common();
        currentComp.poke(getIX() + d, Z);
        if( z != 6)
            set_r_(z);
        tstate+=23;
    }

    @Override
    public void SLL_IY_d() { // undocumented
        Z = currentComp.peek(MEMPTR = getIY() + d);
        SLL_common();
        currentComp.poke(getIY() + d, Z);
        if( z != 6)
            set_r_(z);
        tstate+=23;
    }

    public void SRL_r_z() {
        get_r_(z);

        SRL_common();

        set_r_(z);

        if( z == 6 )
            tstate+=7;
        tstate+=8;
    }

    private void SRL_common() {
        byte c = (byte) (Z & 0x01); // pre-carry

        Z >>= 1;

        Z &= 0x7F;
        resSF();

        if( Z == 0 )
            setZF();
        else
            resZF();

        BitSet pZ = BitSet.valueOf(new byte[]{Z});

        if( (pZ.cardinality() % 2) == 0 )
            setPF();
        else
            resPF();

        if( c != 0 )
            setCF();
        else
            resCF();

        resHF();
        resNF();

        affectXYFlags(Z);
    }

    @Override
    public void SRL_IX_d() {
        Z = currentComp.peek(MEMPTR = getIX() + d);
        SRL_common();
        currentComp.poke(getIX() + d, Z);
        if( z != 6)
            set_r_(z);
        tstate+=23;
    }

    @Override
    public void SRL_IY_d() {
        Z = currentComp.peek(MEMPTR = getIY() + d);
        SRL_common();
        currentComp.poke(getIY() + d, Z);
        if( z != 6)
            set_r_(z);
        tstate+=23;
    }

    /* ED prefix */

	@Override
    public void SBC_HL_rp_p() {
        int carry = getCF() ? 1 : 0;
        int hl = getHL() & 0xFFFF;

        MEMPTR = hl+1;

        get_rp_p();
        int wz = getWZ();

        int result = hl - wz - carry;

        boolean isHalfCarry = ((hl ^ wz ^ result) & 0x1000) != 0;

        if( isOverflowSUB16(hl, wz, result) )
            setVF();
        else
            resVF();

        if( isHalfCarry )
            setHF();
        else
            resHF();

        if ( (result & 0x10000) != 0 )
            setCF();
        else
            resCF();

        if ((result & 0x0FFFF) == 0)
            setZF();
        else
            resZF();

        if ((result & 0x8000) != 0)
            setSF();
        else
            resSF();

        setNF();

        setHL((short)(result & 0x0FFFF));

        affectXYFlags(H);

        tstate+=15;
    }

	@Override
    public void ADC_HL_rp_p() {
        int hl;

        get_rp_p();

        hl = getHL() & 0x0FFFF;
        MEMPTR = hl+1;
        hl += getWZ();

        if( getCF() ) hl++;

        boolean isHalfCarry = (((getHL() & 0x0FFF) + ((getCF()?getWZ()+1:getWZ()) & 0x0FFF)) & 0xF000) != 0;

        if( (hl & 0xFFFF0000) != 0 )
            setCF();
        else
            resCF();

        if(isHalfCarry)
            setHF();
        else
            resHF();

        if ((hl & 0x8000) != 0)
            setSF();
        else
            resSF();

        if ( (hl & 0x0FFFF) == 0 )
            setZF();
        else
            resZF();

        if( iSOverflowADD16(getHL(), getWZ(), hl) )
            setVF();
        else
            resVF();

        resNF();

        setHL((short) (hl & 0xFFFF));

        affectXYFlags(H);

        tstate+=15;
    }

	@Override
    public void LD_mm_rp_p() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);
        MEMPTR = getWZ();

        get_rp_p();

        currentComp.poke(MEMPTR, Z);
        currentComp.poke(++MEMPTR, W);

        tstate+=20;
    }

	@Override
    public void LD_rp_p_mm() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);
        MEMPTR = getWZ();

        Z = currentComp.peek(MEMPTR);
        W = currentComp.peek(++MEMPTR);

        set_rp_p();

        tstate+=20;
    }

    /* CB Prefix */

	@Override
    public void BIT_y_r_z() {
        W = (byte) (1 << y);

        get_r_(z);
        int oldZ = Z;

        BIT_common();

        if (z == 6) {
            setWZ(MEMPTR);
            affectXYFlags(W & 0x00FF);
            tstate += 4;
        } else {
            affectXYFlags(oldZ);
            tstate += 8;
        }
    }

    private void BIT_common() {
        Z &= W;

        if( Z == 0 ) {
            setZF();
            setPF(); // undocumented
        } else {
            resZF();
            resPF(); // undocumented
        }

        // undocumented
        if( (Z & 0x80) != 0 ) { // BIT 7
            setSF();
        } else {
            resSF();
        }

        resNF();
        setHF();
    }

	@Override
    public void RES_y_r_z() {
        W = (byte) ~(1 << y);

        get_r_(z);

        Z &= W;

        set_r_(z);

        if( z == 6 )
            tstate+=7;
        tstate+=8;
    }

	@Override
    public void SET_y_r_z() {
        W = (byte) (1 << y);

        get_r_(z);

        Z |= W;

        set_r_(z);

        if( z == 6 )
            tstate+=7;

        tstate+=8;
    }

    /* ED Prefix */

    public void OUT_C_r_y() {
        MEMPTR = getBC();
        if( y != 6)
            get_r_(y);
        else
            Z = 0;

        currentComp.write((short) MEMPTR++, Z, (int)(tstate - old_tstate_out));
        old_tstate_out = tstate;
    }

	@Override
    public void IN_r_y_C() {
        Z = C;
        W = B;
        MEMPTR = (short)getWZ();

        Z = currentComp.read((short) MEMPTR++);

        if( y != 6 )
            set_r_(y);

        BitSet pZ = BitSet.valueOf(new byte[]{Z});

        if( (pZ.cardinality() % 2) == 0 )
            setPF();
        else
            resPF();

        if( Z == 0 )
            setZF();
        else
            resZF();

        if ((Z & 0x80) != 0)
            setSF();
        else
            resSF();

        resNF();
        resHF();

        affectXYFlags(Z);
    }

	@Override
    public void NEG() {
        SUB_A_common(0, A&0x00FF, false);
/*        boolean isHalfCarry = (-(A & 0x0F)) < 0;
        int a = A;

        setA((byte) -A);

        if( (a & 0x80) != 0 ) {
            setSF();
            setPF();
        } else {
            resSF();
            resPF();
        }

        if( A == 0 )
            setZF();
        else
            resZF();

        if( isHalfCarry )
            setHF();
        else
            resHF();

        if ((A & 0x80) != 0)
            setSF();
        else
            resSF();

        if  ( a != 0 )
            setCF();
        else
            resCF();

        setNF();

        affectXYFlags(A);
*/
        tstate+=8;
    }

    @Override
    public void RETN() {
        IFF1 = IFF2; // Restore previous state of interruptions

        Z = currentComp.peek(SP++);
        W = currentComp.peek(SP++);

        PC = MEMPTR = getWZ();
        regTouched(RegTouched.SP);

        tstate+=14;
    }

    @Override
    public void RETI() {
        /*
         * Since every INT routine must end with EI followed by RETI
         * officially, it does not matter that RETI copies IFF2 into IFF1;
         * both are set anyway.
         * Section 5.3 o.c. Undoc by Sean Young
         */
        RETN();
        /*Z = currentComp.peek(SP++);
        W = currentComp.peek(SP++);

        PC = MEMPTR = getWZ();
        regTouched(RegTouched.SP);

        tstate+=14;*/
    }

    @Override
    public void IM_im_y() {
        switch (y) {
            case 0:
            case 1:
            case 4:
            case 5:
                currentIM = 0; break;
            case 2:
            case 6:
                currentIM = 1; break;
            case 3:
            case 7:
                currentIM = 2; break;
        }
        tstate+=8;
    }

	@Override
    public void LD_I_A() {
/* TODO
 * El par IR se pone en el bus de direcciones *antes*
 * de poner A en el registro I. Detalle importante.
 * Z80Ops::addressOnBus(getPairIR().word, 1);
 */
        setI(A); // TODO flag affectation missing http://www.z80.info/z80sflag.htm DONE?
        tstate+=9;
    }

	@Override
    public void LD_R_A() {
/* TODO
 * El par IR se pone en el bus de direcciones *antes*
 * de poner A en el registro R. Detalle importante.
 * Z80Ops::addressOnBus(getPairIR().word, 1);
 */
        setR(A);
        tstate+=9;
    }

	@Override
    public void LD_A_I() {
// TODO Z80Ops::addressOnBus(getPairIR().word, 1);
        setA(I);

        LD_A_IR_Common();
    }

    @Override
    public void LD_A_R() {
// TODO Z80Ops::addressOnBus(getPairIR().word, 1);
        setA(R);

        LD_A_IR_Common();
    }

    private void LD_A_IR_Common() {
        if( (A & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( A == 0 )
            setZF();
        else
            resZF();

        // Near to next interrupt means INT will occur during execution of this instruction
        //if( nextInterrupt-tstate>=9 && IFF2 )
        //if( frameTiming.targetTStates-tstate<=9 && IFF2 )
        if( IFF2 )
            setPF();
        else
            resPF();

        resNF();
        resHF();

        tstate+=9;
    }

    @Override
    public void RRD() {
        byte nibbleAL = (byte) (A & 0x0F);

        MEMPTR = getHL();

        Z = currentComp.peek(MEMPTR);
        byte nibbleZL = (byte) (Z & 0x0F);

        Z >>>= 4;
        Z &= 0x0F;
        Z |= (byte) (nibbleAL << 4);
        currentComp.poke(MEMPTR++, Z);

        A &= (byte) 0x0F0;
        A |= nibbleZL;

        RRDRLD_common();

        resHF();
        resNF();
    }

    private void RRDRLD_common() {
        boolean savedCF = getCF();          // preservar CF antes de tocar flags

        if (A == 0) setZF(); else resZF();
        if (A < 0)  setSF(); else resSF();
        affectXYFlags(A & 0xFF);            // XF/YF de A
        resHF();
        resNF();

        BitSet pA = BitSet.valueOf(new byte[]{A});
        if (pA.cardinality() % 2 == 0) setPF(); else resPF();
        if (savedCF) setCF(); else resCF();
    }

    @Override
    public void RLD() {
        byte nibbleAL = (byte) (A & 0x0F);

        MEMPTR = getHL();

        Z = currentComp.peek(MEMPTR);
        byte nibbleZH = (byte) (Z & 0xF0);

        Z <<= 4;
        //Z &= 0xF0;
        Z |= nibbleAL;
        currentComp.poke(MEMPTR++, Z);

        A &= (byte) 0x0F0;
        A |= (byte) ((nibbleZH >> 4) & 0x0F);

        RRDRLD_common();

        resHF();
        resNF();
    }

	@Override
    public void LDI() {
        short org = getHL();
        short dst = getDE();
        short count = getBC();

        assert org != dst : "LDI org = dst in " + (PC-2);

        currentComp.poke(dst++, Z = currentComp.peek(org++));
        count--;

        setHL(org);
        setDE(dst);
        setBC(count);

        LDX_common(count);

        tstate+=16;
    }

    private void LDX_common(int count) {
        W = (byte) ((Z & 0x00FF) + (A & 0x00FF));
        if ((W & 0x08) != 0) // bit 3 de n → XF
            setXF();
        else
            resXF();

        if ((W & 0x02) != 0) // bit 1 de n → YF (¡no bit 5!)
            setYF();
        else
            resYF();

        if( count == 0 ) {
            resPF();
        }
        else {
            setPF();
        }

        resNF();
        resHF();
    }

	@Override
    public void LDD() {
        short org = getHL();
        short dst = getDE();
        short count = getBC();

        assert org != dst : "LDD org = dst in " + (PC-2);

        currentComp.poke(dst--, Z = currentComp.peek(org--));
        count--;

        setHL(org);
        setDE(dst);
        setBC(count);

        LDX_common(count);
    }

    @Override
    public void LDIR() {
        LDI();                        // una sola transferencia (con n = mem_byte + A)
        LDXR_common();
    }

    private void LDXR_common() {
        if (getPF()) {                     // BC != 0
            PC -= 2;                       // retrocede a la instrucción LDDR
            MEMPTR = (PC + 1) & 0xFFFF;
            // XF/YF del byte alto de PC (sobreescribe los de n)
            resXF(); resYF();
            if ((PC & 0x0800) != 0) setXF();
            if ((PC & 0x2000) != 0) setYF();
            tstate += 5;
        }
    }

	//@Override
    public void LDIR_old() {
        short org = getHL();
        short dst = getDE();
        short count = getBC();

        assert org != dst : "LDIR org = dst in " + (PC-2);

        if( currentComp.isSameBank(org, dst, count) ) {
            currentComp.movemem(org, dst, count, Memory.MovememDirection.FORWARD);
            org += count;
            dst += count;
        } else {
            while( count-- > 0 ) {
                currentComp.poke(dst++, currentComp.peek(org++));
            }
        }

        setHL(org);
        setDE(dst);
        setBC((short) 0);

        resPF();
        resNF();
        resHF();
    }

    @Override
    public void LDDR() {
        LDD();                        // una sola transferencia (con n = mem_byte + A)
        LDXR_common();
    }

	//@Override
    public void LDDR_old() {
        short org = getHL();
        short dst = getDE();
        short count = getBC();

        assert org != dst : "LDDR org = dst in " + (PC-2);

        if (currentComp.isSameBank(org, dst, count)) {
            currentComp.movemem(org, dst, count, Memory.MovememDirection.REVERSE);
            org -= count;
            dst -= count;
        } else {
            while( count-- > 0 ) {
                currentComp.poke(dst--, currentComp.peek(org--));
            }
        }

        setHL(org);
        setDE(dst);
        setBC((short) 0);

        resPF();
        resNF();
        resHF();
    }

    // Paso base compartido por CPI, CPD, CPIR, CPDR
    private void CPX_common(int memVal) {
        int x   = A & 0xFF;
        int y   = memVal & 0xFF;
        int res = x - y;

        boolean savedCF  = getCF();                          // CF se preserva
        boolean hf       = ((x & 0x0F) - (y & 0x0F)) < 0;

        if ((res & 0x80) != 0) setSF(); else resSF();
        if ((res & 0xFF) == 0) setZF(); else resZF();
        if (hf) setHF(); else resHF();
        setNF();
        if (savedCF) setCF(); else resCF();                  // restaura CF

        // n = A - mem - HF  (usa el HF que acabamos de calcular)
        int n = (x - y - (hf ? 1 : 0)) & 0xFF;
        if ((n & 0x08) != 0) setXF(); else resXF();          // bit 3 → XF
        if ((n & 0x02) != 0) setYF(); else resYF();          // bit 1 → YF (no bit 5)

        // PF según BC (llamador actualiza BC antes de llamar a setPF/resPF)
        //setQF();
    }

    @Override
    public void CPI() {
        int mem = currentComp.peek(getHL() & 0xFFFF);
        setHL((short)(getHL() + 1));
        setBC((short)(getBC() - 1));

        CPX_common(mem);

        if (getBC() != 0) setPF(); else resPF();

        MEMPTR = (MEMPTR + 1) & 0xFFFF;                     // MEMPTR++
        tstate += 16;
    }

    @Override
    public void CPD() {
        int mem = currentComp.peek(getHL() & 0xFFFF);
        setHL((short)(getHL() - 1));
        setBC((short)(getBC() - 1));

        CPX_common(mem);

        if (getBC() != 0) setPF(); else resPF();

        MEMPTR = (MEMPTR - 1) & 0xFFFF;                     // MEMPTR--
        tstate += 16;
    }

    @Override
    public void CPIR() {
        int mem = currentComp.peek(getHL() & 0xFFFF);
        setHL((short)(getHL() + 1));
        setBC((short)(getBC() - 1));

        CPX_common(mem);

        boolean continua = (getBC() != 0) && !getZF();

        if (continua) {
            setPF();
            PC = (PC - 2) & 0xFFFF;                         // vuelve a CPIR
            MEMPTR = (PC + 1) & 0xFFFF;
            // XF/YF del byte alto de PC (sobreescribe los de n)
            if ((PC & 0x0800) != 0) setXF(); else resXF();
            if ((PC & 0x2000) != 0) setYF(); else resYF();
            tstate += 21;
        } else {
            if (getBC() != 0) setPF(); else resPF();
            MEMPTR = (MEMPTR + 1) & 0xFFFF;
            tstate += 16;
        }
    }

    @Override
    public void CPDR() {
        int mem = currentComp.peek(getHL() & 0xFFFF);
        setHL((short)(getHL() - 1));
        setBC((short)(getBC() - 1));

        CPX_common(mem);

        boolean continua = (getBC() != 0) && !getZF();

        if (continua) {
            setPF();
            PC = (PC - 2) & 0xFFFF;
            MEMPTR = (PC + 1) & 0xFFFF;
            if ((PC & 0x0800) != 0) setXF(); else resXF();
            if ((PC & 0x2000) != 0) setYF(); else resYF();
            tstate += 21;
        } else {
            if (getBC() != 0) setPF(); else resPF();
            MEMPTR = (MEMPTR - 1) & 0xFFFF;
            tstate += 16;
        }
    }

/*
    @Override
    public void CPI() {
        short org = getHL();
        short count = getBC();

        Z = currentComp.peek(org);
        CPX_common();

        --count;

        setHL(org);
        setBC(count);
    }

    private void CPX_common() {
        W = (byte) (A - Z);

        if( W < 0 )
            setCF();
        else
            resCF();
    }

	//@Override
    public void CPI_old() {
        short org = getHL();
        short count = getBC();

        CP_A(org++);
        --count;

        setHL(org);
        setBC(count);

        if( count == 0 )
            resPF();
        else
            setPF();
    }

	@Override
    public void CPD() {
        short org = getHL();
        short count = getBC();

        CP_A(org--);
        --count;

        setHL(org);
        setBC(count);

        if( count == 0 )
            resPF();
        else
            setPF();
    }

	@Override
    public void CPIR() {
        short org = getHL();
        short count = getBC();

        do {
            CP_A(org++);
        } while ( !getZF() && --count > 0);

        setHL(org);
        setBC(count);

        if( count == 0 )
            resPF();
        else
            setPF();
    }

	@Override
    public void CPDR() {
        short org = getHL();
        short count = getBC();

        do {
            CP_A(org--);
        } while ( !getZF() && --count > 0);

        setHL(org);
        setBC(count);

        if( count == 0 )
            resPF();
        else
            setPF();
    }

    // Used by block search instructions only
    private void CP_A(int org) {
        int x = A & 0xFF;

        Z = currentComp.peek(org);

        int y = Z & 0xFF;

        int result = x - y;
        boolean isHalfCarry = ((x & 0x0F) - (y & 0x0F)) < 0;

        if( (result & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( (result & 0x00FF) == 0 )
            setZF();
        else
            resZF();

        if( isHalfCarry )
            setHF();
        else
            resHF();

        if( isOverflowSUB(x, y, result) )
            setVF();
        else
            resVF();

        if( result < 0 )
            setCF();
        else
            resCF();

        setNF();
    }
*/
/*	@Override
    public void INI() {
        // TODO
        System.out.println("INI implementation pending");
    }

	@Override
    public void IND() {
        // TODO
        System.out.println("IND implementation pending");
    }

	@Override
    public void INIR() {
        // TODO
        System.out.println("INIR implementation pending");
    }

	@Override
    public void INDR() {
        // TODO
        System.out.println("INDR implementation pending");
    }

	@Override
    public void OUTI() {
        // TODO
        System.out.println("OUTI implementation pending");
    }

	@Override
    public void OUTD() {
        // TODO
        System.out.println("OUTD implementation pending");
    }

	@Override
    public void OTIR() {
        // TODO
        System.out.println("OTIR implementation pending");
    }

	@Override
    public void OTDR() {
        // TODO
        System.out.println("OTDR implementation pending");
    }*/

    // ─── Helper compartido para flags de INI/IND/INIR/INDR ──────────────────────
    private void ini_flags(int work8, int tmp) {
        int b = getB() & 0xFF;

        // S, Z, XF, YF de B nuevo
        if ((b & 0x80) != 0) setSF(); else resSF();
        if (b == 0)          setZF(); else resZF();
        affectXYFlags(b);

        // NF = bit 7 del byte leído del puerto
        if ((work8 & 0x80) != 0) setNF(); else resNF();

        // CF = HF = desbordamiento de tmp
        if (tmp > 0xFF) { setHF(); setCF(); } else { resHF(); resCF(); }

        // PF = paridad de ((tmp & 7) ^ B_nuevo)
        int parVal = (tmp & 0x07) ^ b;
        if (Integer.bitCount(parVal) % 2 == 0) setPF(); else resPF();

        setQF();
    }

    // ─── Helper compartido para flags de OUTI/OUTD/OTIR/OTDR ────────────────────
    private void outi_flags(int work8, int lAfter) {
        int b = getB() & 0xFF;
        int tmp = (lAfter & 0xFF) + (work8 & 0xFF);

        // NF = bit 7 del byte escrito en el puerto
        if ((work8 & 0x80) != 0) setNF(); else resNF();

        // S, Z, XF, YF de B nuevo
        if ((b & 0x80) != 0) setSF(); else resSF();
        if (b == 0)          setZF(); else resZF();
        affectXYFlags(b);

        // CF = HF = desbordamiento de (L_nuevo + work8)
        if (tmp > 0xFF) { setHF(); setCF(); } else { resHF(); resCF(); }

        // PF = paridad de ((tmp & 7) ^ B_nuevo)
        int parVal = (tmp & 0x07) ^ b;
        if (Integer.bitCount(parVal) % 2 == 0) setPF(); else resPF();

        setQF();
    }

    // ─── Helper para los repeating: ajusta XF/YF/PF/HF cuando B != 0 ────────────
    private void adjustINxROUTxRFlags() {
        // XF/YF del byte alto de PC (como LDIR/LDDR)
        if ((PC & 0x0800) != 0) setXF(); else resXF();
        if ((PC & 0x2000) != 0) setYF(); else resYF();

        boolean pf = getPF();
        int b      = getB() & 0xFF;
        // addsub: +1 si NF=0 (suma), -1 si NF=1 (resta)
        int addsub = getNF() ? -1 : 1;

        if (getCF()) {
            // Flip PF si paridad impar de ((B+addsub)&7)
            if (Integer.bitCount((b + addsub) & 0x07) % 2 != 0) pf = !pf;
            // HF: carry/borrow de bit 3 de B
            boolean hf = (addsub == 1) ? ((b & 0x0F) == 0x0F)
                    : ((b & 0x0F) == 0x00);
            if (hf) setHF(); else resHF();
        } else {
            if (Integer.bitCount(b & 0x07) % 2 != 0) pf = !pf;
            resHF();
        }
        if (pf) setPF(); else resPF();
    }

    // ─── INI ─────────────────────────────────────────────────────────────────────
    @Override
    public void INI() {
        MEMPTR = getBC() & 0xFFFF;               // MEMPTR = BC antes de decrementar B
        int work8 = currentComp.read((short) MEMPTR) & 0xFF;
        currentComp.poke((short) getHL(), (byte) work8);
        MEMPTR = (MEMPTR + 1) & 0xFFFF;

        int cPlus = (getC() & 0xFF) + 1;
        setB((byte)(getB() - 1));                // solo B
        setHL((short)(getHL() + 1));

        ini_flags(work8, work8 + (cPlus & 0xFF));
        tstate += 16;
    }

    // ─── IND ─────────────────────────────────────────────────────────────────────
    @Override
    public void IND() {
        MEMPTR = getBC() & 0xFFFF;
        int work8 = currentComp.read((short) MEMPTR) & 0xFF;
        currentComp.poke((short) getHL(), (byte) work8);
        MEMPTR = (MEMPTR - 1) & 0xFFFF;

        int cMinus = (getC() & 0xFF) - 1;
        setB((byte)(getB() - 1));
        setHL((short)(getHL() - 1));

        ini_flags(work8, work8 + (cMinus & 0xFF));
        tstate += 16;
    }

    // ─── INIR ────────────────────────────────────────────────────────────────────
    @Override
    public void INIR() {
        MEMPTR = getBC() & 0xFFFF;
        int work8 = currentComp.read((short) MEMPTR) & 0xFF;
        currentComp.poke((short) getHL(), (byte) work8);
        MEMPTR = (MEMPTR + 1) & 0xFFFF;

        int cPlus = (getC() & 0xFF) + 1;
        setB((byte)(getB() - 1));
        setHL((short)(getHL() + 1));

        ini_flags(work8, work8 + (cPlus & 0xFF));

        if ((getB() & 0xFF) != 0) {
            PC = (PC - 2) & 0xFFFF;
            MEMPTR = (PC + 1) & 0xFFFF;
            adjustINxROUTxRFlags();
            tstate += 21;
        } else {
            tstate += 16;
        }
    }

    // ─── INDR ────────────────────────────────────────────────────────────────────
    @Override
    public void INDR() {
        MEMPTR = getBC() & 0xFFFF;
        int work8 = currentComp.read((short) MEMPTR) & 0xFF;
        currentComp.poke((short) getHL(), (byte) work8);
        MEMPTR = (MEMPTR - 1) & 0xFFFF;

        int cMinus = (getC() & 0xFF) - 1;
        setB((byte)(getB() - 1));
        setHL((short)(getHL() - 1));

        ini_flags(work8, work8 + (cMinus & 0xFF));

        if ((getB() & 0xFF) != 0) {
            PC = (PC - 2) & 0xFFFF;
            MEMPTR = (PC + 1) & 0xFFFF;
            adjustINxROUTxRFlags();
            tstate += 21;
        } else {
            tstate += 16;
        }
    }

    // ─── OUTI ────────────────────────────────────────────────────────────────────
    @Override
    public void OUTI() {
        setB((byte)(getB() - 1));                // B-- PRIMERO
        MEMPTR = getBC() & 0xFFFF;               // MEMPTR con nuevo B
        int work8 = currentComp.peek((short) getHL()) & 0xFF;
        currentComp.write((short) MEMPTR, (byte) work8, (int)(tstate - old_tstate_out));
        MEMPTR = (MEMPTR + 1) & 0xFFFF;
        setHL((short)(getHL() + 1));             // HL++ después del out

        outi_flags(work8, getL());               // L ya incrementado
        tstate += 16;
    }

    // ─── OUTD ────────────────────────────────────────────────────────────────────
    @Override
    public void OUTD() {
        setB((byte)(getB() - 1));
        MEMPTR = getBC() & 0xFFFF;
        int work8 = currentComp.peek((short) getHL()) & 0xFF;
        currentComp.write((short) MEMPTR, (byte) work8, (int)(tstate - old_tstate_out));
        MEMPTR = (MEMPTR - 1) & 0xFFFF;
        setHL((short)(getHL() - 1));

        outi_flags(work8, getL());               // L ya decrementado
        tstate += 16;
    }

    // ─── OTIR ────────────────────────────────────────────────────────────────────
    @Override
    public void OTIR() {
        setB((byte)(getB() - 1));
        MEMPTR = getBC() & 0xFFFF;
        int work8 = currentComp.peek((short) getHL()) & 0xFF;
        currentComp.write((short) MEMPTR, (byte) work8, (int)(tstate - old_tstate_out));
        MEMPTR = (MEMPTR + 1) & 0xFFFF;
        setHL((short)(getHL() + 1));

        outi_flags(work8, getL());

        if ((getB() & 0xFF) != 0) {
            PC = (PC - 2) & 0xFFFF;
            MEMPTR = (PC + 1) & 0xFFFF;
            adjustINxROUTxRFlags();
            tstate += 21;
        } else {
            tstate += 16;
        }
    }

    // ─── OTDR ────────────────────────────────────────────────────────────────────
    @Override
    public void OTDR() {
        setB((byte)(getB() - 1));
        MEMPTR = getBC() & 0xFFFF;
        int work8 = currentComp.peek((short) getHL()) & 0xFF;
        currentComp.write((short) MEMPTR, (byte) work8, (int)(tstate - old_tstate_out));
        MEMPTR = (MEMPTR - 1) & 0xFFFF;
        setHL((short)(getHL() - 1));

        outi_flags(work8, getL());

        if ((getB() & 0xFF) != 0) {
            PC = (PC - 2) & 0xFFFF;
            MEMPTR = (PC + 1) & 0xFFFF;
            adjustINxROUTxRFlags();
            tstate += 21;
        } else {
            tstate += 16;
        }
    }

    /* DD prefix */

    @Override
    public void INC_IX() {
        IX++;
        regTouched(RegTouched.IX);
        tstate+=10;
    }

    @Override
    public void DEC_IX() {
        IX--;
        regTouched(RegTouched.IX);
        tstate+=10;
    }

    /*
        x is value before INC
        Z is value after  INC
     */
    private void INC_XY_common(int x) {
        boolean isHalfCarry = (x & 0x0F) == 0x0F;
        //boolean isHalfCarry = (((x & 0x0F) + 1) & 0xF0) != 0;

        if( (Z & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( Z == 0 )
            setZF();
        else
            resZF();

        if( isHalfCarry )
            setHF();
        else
            resHF();

        if( iSOverflowADD(x, 1, Z) )
            setVF();
        else
            resVF();

        resNF();

        affectXYFlags(Z);

        tstate+=8;
    }

    private void DEC_XY_common(int x) {
        boolean isHalfCarryA = ((x ^ 1 ^ (x-1)) & 0x10) != 0;
        boolean isHalfCarryB = x == 0x10;
        boolean isHalfCarry = (x & 0xF) == 0x00;

        if( (Z & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( Z == 0 )
            setZF();
        else
            resZF();

        if( isHalfCarry )
            setHF();
        else
            resHF();

        if( isOverflowSUB(x, 1, Z) )
            setVF();
        else
            resVF();

        setNF();

        affectXYFlags(Z);

        tstate+=8;
    }

    @Override
    public void INC_IXH() {
        byte x = getIXH();

        Z = (byte) (x + 1);

        INC_XY_common(x);

        setIXH(Z);
    }

    @Override
    public void INC_IXL() {
        byte x = getIXL();

        Z = (byte) (x + 1);

        INC_XY_common(x);

        setIXL(Z);
    }

    @Override
    public void DEC_IXH() {
        byte x = getIXH();

        Z = (byte) (x - 1);

        DEC_XY_common(x);

        setIXH(Z);
    }

    @Override
    public void DEC_IXL() {
        byte x = getIXL();

        Z = (byte) (x - 1);

        DEC_XY_common(x);

        setIXL(Z);
    }

    @Override
    public void LD_r_y_IX_d() {
        d = currentComp.peek(PC++);
        Z = currentComp.peek(MEMPTR = getIX() + d);

        set_r_(y);
        tstate+=19;
    }

    @Override
    public void LD_IX_nn() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        setIX((short)getWZ());
    }

    @Override
    public void ADD_IX_rp_p() {
        int ix = getIX() & 0xFFFF;
        int wz;

        MEMPTR = ix+1;

        get_rp_p();
        if( p == 2 ) {
            W = getIXH();
            Z = getIXL();
        }
        wz = getWZ();

        ix += wz;

        boolean isHalfCarry = (((getIX() & 0x0FFF) + (getWZ() & 0x0FFF)) & 0xF000) != 0;

        if( (ix & 0xFFFF0000) != 0 )
            setCF();
        else
            resCF();

        /*if( ix == 0)
            setZF();
        else
            resZF();

        if( iSOverflowADD16(getIX(), getWZ(), ix))
            setVF();
        else
            resVF();*/

        if(isHalfCarry)
            setHF();
        else
            resHF();

        resNF();

        setIX((short) (ix & 0x0FFFF));

        affectXYFlags(getIXH());

        tstate+=15;
    }

    @Override
    public void LD_mm_IX() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);
        MEMPTR = getWZ();

        short ix = getIX();
        Z = (byte) (ix & 0x00FF);
        W = (byte) ((ix & 0x0FF00) >> 8);

        currentComp.poke(MEMPTR, Z);
        currentComp.poke(++MEMPTR, W);

/* TODO test Alternative

        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        currentComp.poke(getWZ(), getIXL());
        currentComp.poke(getWZ() + 1, getIXH());
 */
    }

    @Override
    public void LD_IX_mm() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);
        MEMPTR = getWZ();

        Z = currentComp.peek(MEMPTR);
        W = currentComp.peek(++MEMPTR);

        setIX( (short) ((W << 8) | (Z & 0xFF)) );

/* TODO test alternative

        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        setIXL(currentComp.peek(getWZ());
        setIXH(currentComp.peek(getWZ() + 1);
 */
    }

    @Override
    public void INC_IX_d() {
        d = currentComp.peek(PC++);

        W = Z = currentComp.peek(MEMPTR = getIX() + d);
        Z++;
        currentComp.poke(MEMPTR, Z);

        //boolean isHalfCarry = (((W & 0x0F) + (1 & 0x0F)) & 0xF0) != 0;
        boolean isHalfCarry = (W & 0xF) == 0xF;

        if( (Z & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( Z == 0 )
            setZF();
        else
            resZF();

        if( isHalfCarry )
            setHF();
        else
            resHF();

        if( iSOverflowADD(W, 1, Z) )
            setVF();
        else
            resVF();

        resNF();

        affectXYFlags(Z);

        tstate+=23;
    }

    @Override
    public void DEC_IX_d() {
        d = currentComp.peek(PC++);

        W = Z = currentComp.peek(MEMPTR = getIX() + d);
        Z--;
        currentComp.poke(MEMPTR, Z);

        //boolean isHalfCarry = (((W & 0x0F) + (1 & 0x0F)) & 0xF0) != 0;
        boolean isHalfCarry = (W & 0xF) == 0x00;

        if( (Z & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( Z == 0 )
            setZF();
        else
            resZF();

        if( isHalfCarry )
            setHF();
        else
            resHF();

        if( isOverflowSUB(W, 1, Z) )
            setVF();
        else
            resVF();

        setNF();

        affectXYFlags(Z);

        tstate+=23;
    }

    @Override
    public void LD_IXH_n() {
        W = currentComp.peek(PC++);

        setIXH(W);
    }

    @Override
    public void LD_IXL_n() {
        Z = currentComp.peek(PC++);

        setIXL(Z);
    }

    @Override
    public void LD_IX_d_r_z() {
        d = currentComp.peek(PC++);

        get_r_(z);

        currentComp.poke(MEMPTR = getIX() + d, Z);

        tstate+=19;
    }

    @Override
    public void LD_IX_d_n() {
        byte d = currentComp.peek(PC++);
        Z = currentComp.peek(PC++);

        currentComp.poke(MEMPTR = getIX() + d, Z);

        tstate+=19;
    }

    @Override
    public void LD_r_y_IXH() {
        Z = getIXH();
        set_r_(y);
        tstate+=8;
    }

    @Override
    public void LD_r_y_IXL() {
        Z = getIXL();
        set_r_(y);
        tstate+=8;
    }

    @Override
    public void LD_IXH_r_z() {
        get_r_(z);
        setIXH(Z);
        tstate+=8;
    }

    @Override
    public void LD_IXL_r_z() {
        get_r_(z);
        setIXL(Z);
        tstate+=8;
    }

    @Override
    public void LD_IXH_IXH() {
        regTouched(RegTouched.IX); // TODO IXH only!
        tstate+=8;
    }

    @Override
    public void LD_IXL_IXL() {
        regTouched(RegTouched.IX); // TODO IXL only!
        tstate+=8;
    }

    @Override
    public void LD_IXH_IXL() {
        setIXH(getIXL());
        tstate+=8;
    }
    
    @Override
    public void LD_IXL_IXH() {
        setIXL(getIXH());
        tstate+=8;
    }

    @Override
    public void JP_IX() {
        PC = getIX();
        tstate+=8;
    }

    @Override
    public void PUSH_IX() {
        W = (byte) ((IX & 0xFF00) >> 8);
        Z = (byte) (IX & 0x00FF);

        currentComp.poke(--SP, W);
        currentComp.poke(--SP, Z);
        regTouched(RegTouched.SP);
    }

    @Override
    public void POP_IX() {
        Z = currentComp.peek(SP++);
        W = currentComp.peek(SP++);

        setIX((short)getWZ());
        regTouched(RegTouched.SP);
    }

    @Override
    public void ADD_A_IX_d() {
        int x = A & 0x0FF;
        byte d = currentComp.peek(PC++);

        Z = currentComp.peek(MEMPTR = getIX() + d);
        int y = Z & 0x0FF;

        ADD_A_common(x,y,false);

        tstate+=19;
    }

    @Override
    public void ADC_A_IX_d() {
        int x = A & 0x0FF;
        byte d = currentComp.peek(PC++);

        Z = currentComp.peek(MEMPTR = getIX() + d);
        int y = Z & 0x0FF;

        ADD_A_common(x,y,getCF());

        tstate+=19;
    }

    @Override
    public void ADC_A_IY_d() {
        int x = A & 0x0FF;
        byte d = currentComp.peek(PC++);

        Z = currentComp.peek(MEMPTR = getIY() + d);
        int y = Z & 0x0FF;

        ADD_A_common(x,y,getCF());

        tstate+=19;
    }

    /* DDCB prefix */

    @Override
    public void BIT_y_IX_d() {
        W = (byte) (1 << y);

        Z = currentComp.peek(MEMPTR = getIX() + d);

        BIT_common();

        setWZ(MEMPTR);
        affectXYFlags(W & 0x00FF);

        tstate+=20;
    }

    @Override
    public void RES_y_IX_d() {
        W = (byte) ~(1 << y);

        Z = currentComp.peek(MEMPTR = getIX() + d);
        Z &= W;
        currentComp.poke(MEMPTR, Z);
        if( z != 6)
            set_r_(z);
        tstate+=23;
    }

    @Override
    public void SET_y_IX_d() {
        W = (byte) (1 << y);

        Z = currentComp.peek(MEMPTR = getIX() + d);
        Z |= W;
        currentComp.poke(MEMPTR, Z);
        if( z != 6)
            set_r_(z);
        tstate+=23;
    }

    /* FD prefix */

	@Override
    public void LD_IY_nn() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        setIY((short)getWZ());
    }

    @Override
    public void LD_mm_IY() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);
        MEMPTR = getWZ();

        short iy = getIY();
        Z = (byte) (iy & 0x00FF);
        W = (byte) ((iy & 0x0FF00) >> 8);

        currentComp.poke(MEMPTR, Z);
        currentComp.poke(MEMPTR + 1, W);
    }

    @Override
    public void LD_IY_mm() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);
        MEMPTR = getWZ();

        Z = currentComp.peek(MEMPTR);
        W = currentComp.peek(MEMPTR + 1);

        setIY( (short) ((W << 8) | (Z & 0xFF)) );
    }

	@Override
    public void LD_IY_d_r_z() {
        d = currentComp.peek(PC++);

        get_r_(z);

        currentComp.poke(MEMPTR = getIY() + d, Z);

        tstate+=19;
    }

	@Override
    public void LD_r_y_IY_d() {
        d = currentComp.peek(PC++);

        Z = currentComp.peek(MEMPTR = getIY() + d);

        set_r_(y);

        tstate+=19;
    }

    @Override
    public void INC_IY_d() {
        d = currentComp.peek(PC++);

        W = Z = currentComp.peek(MEMPTR = getIY() + d);
        Z++;
        currentComp.poke(MEMPTR, Z);

        //boolean isHalfCarry = (((W & 0x0F) + (1 & 0x0F)) & 0xF0) != 0;
        boolean isHalfCarry = (W & 0xF) == 0xF;

        if( (Z & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( Z == 0 )
            setZF();
        else
            resZF();

        if( isHalfCarry )
            setHF();
        else
            resHF();

        if( iSOverflowADD(W, 1, Z) )
            setVF();
        else
            resVF();

        resNF();

        affectXYFlags(Z);

        tstate+=23;
    }

	@Override
    public void DEC_IY_d() {
        d = currentComp.peek(PC++);

        W = Z = currentComp.peek(MEMPTR = getIY() + d);
        Z--;
        currentComp.poke(MEMPTR, Z);

        //boolean isHalfCarry = (((W & 0x0F) + (1 & 0x0F)) & 0xF0) != 0;
        boolean isHalfCarry = (W & 0xF) == 0x00;

        if( (Z & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( Z == 0 )
            setZF();
        else
            resZF();

        if( isHalfCarry )
            setHF();
        else
            resHF();

        if( isOverflowSUB(W, 1, Z) )
            setVF();
        else
            resVF();

        setNF();

        affectXYFlags(Z);

        tstate+=23;
    }

    @Override
    public void INC_IYH() {
        byte x = getIYH();

        Z = (byte) (x + 1);

        INC_XY_common(x);

        setIYH(Z);
    }

    @Override
    public void INC_IYL() {
        byte x = getIYL();

        Z = (byte) (x + 1);

        INC_XY_common(x);

        setIYL(Z);
    }

    @Override
    public void DEC_IYH() {
        byte x = getIYH();

        Z = (byte) (x - 1);

        DEC_XY_common(x);

        setIYH(Z);
    }

    @Override
    public void DEC_IYL() {
        byte x = getIYL();

        Z = (byte) (x - 1);

        DEC_XY_common(x);

        setIYL(Z);
    }

	@Override
    public void LD_IY_d_n() {
        byte d = currentComp.peek(PC++);
        Z = currentComp.peek(PC++);

        currentComp.poke(MEMPTR = getIY() + d, Z);

        tstate+=19;
    }

    /* FDCB prefix */

	@Override
    public void BIT_y_IY_d() {
        W = (byte) (1 << y);

        Z = currentComp.peek(MEMPTR = getIY() + d);

        BIT_common();

        setWZ(MEMPTR);
        affectXYFlags(W & 0x00FF);

        tstate+=20;
    }

	@Override
    public void RES_y_IY_d() {
        W = (byte) ~(1 << y);

        Z = currentComp.peek(MEMPTR = getIY() + d);
        Z &= W;
        currentComp.poke(MEMPTR, Z);
        if( z != 6)
            set_r_(z);
        tstate+=23;
    }

	@Override
    public void SET_y_IY_d() {
        W = (byte) (1 << y);

        Z = currentComp.peek(MEMPTR = getIY() + d);
        Z |= W;
        currentComp.poke(MEMPTR, Z);
        if( z != 6)
            set_r_(z);
        tstate+=23;
    }

	@Override
    public void ADD_A_IY_d() {
        int x = A & 0x0FF;
        byte d = currentComp.peek(PC++);

        Z = currentComp.peek(MEMPTR = getIY() + d);
        int y = Z & 0x0FF;

        ADD_A_common(x, y, false);

        /*int result = x + y;
        boolean isHalfCarry = (((x & 0x0F) + (y & 0x0F)) & 0xF0) != 0;

        if( (result & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( (result & 0x00FF) == 0 )
            setZF();
        else
            resZF();

        if( isHalfCarry )
            setHF();
        else
            resHF();

        if( iSOverflowADD(x, y, result) )
            setVF();
        else
            resVF();

        if( (result & 0xFF00) != 0 )
            setCF();
        else
            resCF();

        resNF();

        setA((byte) (result & 0xFF));
*/
        tstate+=19;
    }

    @Override
    public void CP_IY_d() {
        byte d = currentComp.peek(PC++);
        int x = A & 0xFF;

        Z = currentComp.peek(MEMPTR = getIY() + d);

        int y = Z & 0xFF;

        CP_common(x, y);

        tstate+=19;
    }

    @Override
    public void SUB_IY_d() {
        int x = A & 0xFF;
        byte d = currentComp.peek(PC++);

        Z = currentComp.peek(MEMPTR = getIY() + d);
        int y = Z & 0xFF;

        int result = x - y;
        boolean isHalfCarry = ((x & 0x0F) - (y & 0x0F)) < 0;

        if( (result & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( (result & 0x00FF) == 0 )
            setZF();
        else
            resZF();

        if( isHalfCarry )
            setHF();
        else
            resHF();

        if( isOverflowSUB(x, y, result) )
            setVF();
        else
            resVF();

        if( result < 0 )
            setCF();
        else
            resCF();

        setNF();

        affectXYFlags(result);

        setA((byte) (result & 0xFF));

        tstate+=19;
    }

    @Override
    public void LD_IYH_n() {
        W = currentComp.peek(PC++);

        setIYH(W);
    }

    @Override
    public void LD_IYL_n() {
        Z = currentComp.peek(PC++);

        setIYL(Z);
    }

    @Override
    public void LD_r_y_IYH() {
        Z = getIYH();
        set_r_(y);
    }

    @Override
    public void LD_r_y_IYL() {
        Z = getIYL();
        set_r_(y);
        tstate+=8;
    }

    @Override
    public void LD_IYH_r_z() {
        get_r_(z);
        setIYH(Z);
        tstate+=8;
    }

    @Override
    public void LD_IYL_r_z() {
        get_r_(z);
        setIYL(Z);
    }

    @Override
    public void LD_IYH_IYH() {
        regTouched(RegTouched.IY); // TODO IYH only!
    }

    @Override
    public void LD_IYL_IYL() {
        regTouched(RegTouched.IY);
    }

    @Override
    public void LD_IYH_IYL() {
        setIYH(getIYL());
        tstate+=8;
    }

    @Override
    public void LD_IYL_IYH() {
        setIYL(getIYH());
        tstate+=8;
    }
    
    @Override
    public void JP_IY() {
        PC = getIY();
        tstate+=8;
    }

    @Override
    public void PUSH_IY() {
        W = (byte) ((IY & 0xFF00) >> 8);
        Z = (byte) (IY & 0x00FF);

        currentComp.poke(--SP, W);
        currentComp.poke(--SP, Z);
        regTouched(RegTouched.SP);
    }

    @Override
    public void POP_IY() {
        Z = currentComp.peek(SP++);
        W = currentComp.peek(SP++);

        setIY((short)getWZ());
        regTouched(RegTouched.SP);
    }
}
