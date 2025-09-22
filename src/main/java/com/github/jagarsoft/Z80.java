package com.github.jagarsoft;

import java.util.BitSet;

public class Z80 implements Z80OpCode {

    protected class Register {
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
    protected int d; // displacement for IX and IY

    protected int prefix; // DD or FD

    // Array to store reference to methods of different implementations
    protected OpCode[][][] opCodes   = new OpCode[4][8][8];
    protected OpCode[][][] CBopCodes = new OpCode[4][8][8];
    protected OpCode[][][] EDopCodes = new OpCode[4][8][8];
    protected OpCode[][][] DDopCodes = new OpCode[4][8][8];
    protected OpCode[][][] FDopCodes = new OpCode[4][8][8];
    protected OpCode[][][] FDCBopCodes=new OpCode[4][8][8];

    private int tstate = 0;

    protected void dispatcher(Z80OpCode opC) {
        // According to http://www.z80.info/decoding.htm
        // and http://www.z80.info/z80code.txt
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
        opCodes[0][2][0b100] = opC::LD_nn_HL;
        opCodes[0][2][0b110] = opC::LD_nn_A;
        opCodes[0][2][0b001] = opC::LD_A_BC;
        opCodes[0][2][0b011] = opC::LD_A_DE;
        opCodes[0][2][0b101] = opC::LD_HL_nn;
        opCodes[0][2][0b111] = opC::LD_A_nn;
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
        opCodes[0][6][6] = opC::LD_r_y_n;
        opCodes[0][6][7] = opC::LD_r_y_n;
        // z=7 [x][z][y]
        opCodes[0][7][0] = opC::RLCA;
        opCodes[0][7][1] = opC::RRCA;
        opCodes[0][7][2] = opC::RLA;
        opCodes[0][7][3] = opC::RRA;
        opCodes[0][7][4] = opC::DAA;  // TODO
        opCodes[0][7][5] = opC::CPL;
        opCodes[0][7][6] = opC::SCF;
        opCodes[0][7][7] = opC::CCF;
        // x = 1
        // Exception: 7 * 7 combinations managed in fetch
        //     [x][z][y] (z=y=0 are dummies))
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
        opCodes[3][7][0] = opC::RST_y_8;
        opCodes[3][7][1] = opC::RST_y_8;
        opCodes[3][7][2] = opC::RST_y_8;
        opCodes[3][7][3] = opC::RST_y_8;
        opCodes[3][7][4] = opC::RST_y_8;
        opCodes[3][7][5] = opC::RST_y_8;
        opCodes[3][7][6] = opC::RST_y_8;
        opCodes[3][7][7] = opC::RST_y_8;

        /* Table CB Prefix */

        // x = 0, z = 0..7
        // Exception: iterates over z instead of y are 7 * 7 combinations managed in fetch,
        // but still opC::PTR is needed
        //       [x][z][y] (z=0 is dummy)
        CBopCodes[0][0][0] = opC::RLC_r_z;
        CBopCodes[0][0][1] = opC::RRC_r_z;
        CBopCodes[0][0][2] = opC::RL_r_z;
        CBopCodes[0][0][3] = opC::RR_r_z;
        CBopCodes[0][0][4] = opC::SLA_r_z;
        CBopCodes[0][0][5] = opC::SRA_r_z;
        CBopCodes[0][0][6] = opC::SLL_r_z;
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
        EDopCodes[1][3][0b100] = opC::LD_mm_rp_p;
        EDopCodes[1][3][0b110] = opC::LD_mm_rp_p;
        EDopCodes[1][3][0b001] = opC::LD_rp_p_mm; // LD rp[p], (mm)
        EDopCodes[1][3][0b011] = opC::LD_rp_p_mm;
        EDopCodes[1][3][0b101] = opC::LD_rp_p_mm;
        EDopCodes[1][3][0b111] = opC::LD_rp_p_mm;
        // z=4   [x][z][y]
        EDopCodes[1][4][0] = opC::NEG;
        // z=5   [x][z][y]
        EDopCodes[1][5][0] = opC::RETN; // y != 1
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
        //EDopCodes[1][7][4] = opC::RRD;
        //EDopCodes[1][7][5] = opC::RLD;
        EDopCodes[1][7][6] = opC::NOP;
        EDopCodes[1][7][7] = opC::NOP;

        // x=2
        // z<=3 and y>=4
        //       [x][z][y]
        EDopCodes[2][0][0b100] = opC::LDI; // TODO
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

        /* Table DD Prefix */

        // x = 0
        // z=1   [x][z][y] (q=0)
        DDopCodes[0][1][0b100] = opC::LD_IX_nn;
        //                 (q=1)
        DDopCodes[0][1][0b001] = opC::ADD_IX_rp_p;
        DDopCodes[0][1][0b011] = opC::ADD_IX_rp_p;
        DDopCodes[0][1][0b101] = opC::ADD_IX_rp_p;
        DDopCodes[0][1][0b111] = opC::ADD_IX_rp_p;

        // z=3   [x][z][y]
//1        DDopCodes[0][3][0b100] = opC::INC_IX;
        DDopCodes[0][3][0b101] = opC::DEC_IX;
//1        DDopCodes[0][4][0b110] = opC::INC_IX_d;
//1        DDopCodes[0][5][0b110] = opC::DEC_IX_d;

        // z=0..7[x][z][y]
//1        DDopCodes[0][0b000][6] = opC::LD_IX_d_r_z;
//1        DDopCodes[0][0b001][6] = opC::LD_IX_d_r_z;
//1        DDopCodes[0][0b010][6] = opC::LD_IX_d_r_z;
//1        DDopCodes[0][0b011][6] = opC::LD_IX_d_r_z;
//1        DDopCodes[0][0b100][6] = opC::LD_IX_d_r_z;
//1        DDopCodes[0][0b101][6] = opC::LD_IX_d_r_z;
//1        DDopCodes[0][0b110][6] = opC::LD_IX_d_n;
//1        DDopCodes[0][0b111][6] = opC::LD_IX_d_r_z;

        // x = 1
        // z=2   [x][z][y]
//1        DDopCodes[1][2][4] = opC::LD_mm_IX;
//1        DDopCodes[1][2][5] = opC::LD_IX_mm;

        // z=6   [x][z][y]       LD r, (IX+d)
        DDopCodes[1][6][0b000] = opC::LD_r_y_IX_d;
        DDopCodes[1][6][0b001] = opC::LD_r_y_IX_d;
        DDopCodes[1][6][0b010] = opC::LD_r_y_IX_d;
        DDopCodes[1][6][0b011] = opC::LD_r_y_IX_d;
        DDopCodes[1][6][0b100] = opC::LD_r_y_IX_d;
        DDopCodes[1][6][0b101] = opC::LD_r_y_IX_d;
//1        DDopCodes[1][6][0b110] = opC::NOPI;
        DDopCodes[1][6][0b111] = opC::LD_r_y_IX_d;

        // x = 2
        // z=1   [x][z][y]
//1        DDopCodes[2][6][0] = opC::ADD_A_IX_d;

        // x = 3
        // z=1
        DDopCodes[3][1][0b100] = opC::POP_IX;
        DDopCodes[3][1][0b101] = opC::JP_IX;
        // z=5 [x][z][y]
        DDopCodes[3][5][0b100] = opC::PUSH_IX;

        /* Table FD Prefix */

        /* FDCB Prefix */
        FDopCodes[3][3][1] = opC::FDCB_prefix;

        //         [x][z][y]
//      ADD_IY_rp_p 0  1  pp1 ADD IY, rp[p]
//      INC_IY      0  3  4
//      DEC_IY      0  3  5
//      LD_mm_IY    1  4  2
//      LD_IY_mm    1  5  2
//      ADC_A_IY_d  2  6  1   ADC A, (IY+d)

        // x=0
        //       [x][z][y]
        FDopCodes[0][1][0b100] = opC::LD_IY_nn;
        FDopCodes[0][4][0b110] = opC::INC_IY_d;
        FDopCodes[0][5][0b110] = opC::DEC_IY_d;
        FDopCodes[0][6][0b110] = opC::LD_IY_d_n; // TODO LD_IY_mm

        // x=1
        // y=6   [x][z][y]       LD (IY+d), r
        FDopCodes[1][0b000][6] = opC::LD_IY_d_r_z;
        FDopCodes[1][0b001][6] = opC::LD_IY_d_r_z;
        FDopCodes[1][0b010][6] = opC::LD_IY_d_r_z;
        FDopCodes[1][0b011][6] = opC::LD_IY_d_r_z;
        FDopCodes[1][0b100][6] = opC::LD_IY_d_r_z;
        FDopCodes[1][0b101][6] = opC::LD_IY_d_r_z;
        FDopCodes[1][0b110][6] = opC::LD_IY_d_r_z;
        FDopCodes[1][0b111][6] = opC::LD_IY_d_r_z;
        // z=5 [x][z][y]
        FDopCodes[3][5][0b100] = opC::PUSH_IY;
        // z=6   [x][z][y]       LD r, (IY+d)
        FDopCodes[1][6][0b000] = opC::LD_r_y_IY_d;
        FDopCodes[1][6][0b001] = opC::LD_r_y_IY_d;
        FDopCodes[1][6][0b010] = opC::LD_r_y_IY_d;
        FDopCodes[1][6][0b011] = opC::LD_r_y_IY_d;
        FDopCodes[1][6][0b100] = opC::LD_r_y_IY_d;
        FDopCodes[1][6][0b101] = opC::LD_r_y_IY_d;
        FDopCodes[1][6][0b110] = opC::LD_r_y_IY_d;
        FDopCodes[1][6][0b111] = opC::LD_r_y_IY_d;

        // x=2
        // z=6   [x][z][y]
        FDopCodes[2][6][0] = opC::ADD_A_IY_d;
        FDopCodes[2][6][7] = opC::CP_IY_d;
        // x=3
        // z=1
        FDopCodes[3][1][0b100] = opC::POP_IY;
        FDopCodes[3][1][0b101] = opC::JP_IY;
        // z=6
        FDopCodes[2][6][2] = opC::SUB_IY_d;

        /* FDCB Prefix */

        // x=1
        // z=6     [x][z][y]
        FDCBopCodes[1][6][0b000] = opC::BIT_y_IY_d;
        FDCBopCodes[1][6][0b001] = opC::BIT_y_IY_d;
        FDCBopCodes[1][6][0b010] = opC::BIT_y_IY_d;
        FDCBopCodes[1][6][0b011] = opC::BIT_y_IY_d;
        FDCBopCodes[1][6][0b100] = opC::BIT_y_IY_d;
        FDCBopCodes[1][6][0b101] = opC::BIT_y_IY_d;
        FDCBopCodes[1][6][0b110] = opC::BIT_y_IY_d;
        FDCBopCodes[1][6][0b111] = opC::BIT_y_IY_d;

        // x=2
        // z=6     [x][z][y]
        FDCBopCodes[2][6][0b000] = opC::RES_y_IY_d;
        FDCBopCodes[2][6][0b001] = opC::RES_y_IY_d;
        FDCBopCodes[2][6][0b010] = opC::RES_y_IY_d;
        FDCBopCodes[2][6][0b011] = opC::RES_y_IY_d;
        FDCBopCodes[2][6][0b100] = opC::RES_y_IY_d;
        FDCBopCodes[2][6][0b101] = opC::RES_y_IY_d;
        FDCBopCodes[2][6][0b110] = opC::RES_y_IY_d;
        FDCBopCodes[2][6][0b111] = opC::RES_y_IY_d;

        // x=3
        // z=6     [x][z][y]
        FDCBopCodes[3][6][0b000] = opC::SET_y_IY_d;
        FDCBopCodes[3][6][0b001] = opC::SET_y_IY_d;
        FDCBopCodes[3][6][0b010] = opC::SET_y_IY_d;
        FDCBopCodes[3][6][0b011] = opC::SET_y_IY_d;
        FDCBopCodes[3][6][0b100] = opC::SET_y_IY_d;
        FDCBopCodes[3][6][0b101] = opC::SET_y_IY_d;
        FDCBopCodes[3][6][0b110] = opC::SET_y_IY_d;
        FDCBopCodes[3][6][0b111] = opC::SET_y_IY_d;
    }

    public void interrupt() {
        // interrupted disabled?
        if( !IFF1 )
            return;

        // instruction behind EI can't be interrupted
        if( IFF3 != 0 )
            return;

        // ISR runs with implicit DI
        IFF1 = IFF2 = false;

        /*switch (currentIM) {
            case 0: y = 7; break;
            case 1: y = 7; break;
            case 2: y = 7; break;
        }*/
        y = 7; // RST 38h
        RST_y_8();

        isHalted = false;
    }

    public void interruptNMI() {
        IFF2 = IFF1;
        IFF1 = false;

        Z = (byte) 0x66;
        W = 0x00;

        currentComp.poke(--SP, (byte)((PC & 0xFF00)>>8));
        currentComp.poke(--SP, (byte)(PC & 0x00FF));

        PC = getWZ();
        regTouched(RegTouched.SP);
    }

    public void CB_prefix() { fetchCB(currentComp.peek(PC++)); }
    public void ED_prefix() { fetchED(currentComp.peek(PC++)); }
    public void DD_prefix() { fetchDD(currentComp.peek(PC++)); }
    public void FD_prefix() { fetchFD(currentComp.peek(PC++)); }
    public void FDCB_prefix() { fetchFDCB(currentComp.peek(PC++)); }
    // DDCB

    protected void opCMasked(byte opC) {
        x = ((opC & 0b11000000) >> 6);
        y = ((opC & 0b00111000) >> 3);
        z = (opC & 0b111);
        p = ((y & 0b110) >> 1);
        q = (y & 1);
    }

    public void fetch() { fetch(currentComp.peek(PC++)); }

    public void fetch(byte opC) {
        /*if( PC == 0 /#* 0x07e2 0x47CD 0x11DC PC == 0x16D2 || PC == 0x1297 || PC == 0x0D7F*#/ ) {
            IFF1 = false;
            setA(getA());
        }*/

        if( isHalted ) {
            PC--;
            NOP();
            Logger.halted();
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
                System.out.println("OpCode not implemented yet: " + Integer.toHexString(opC));;
                //throw new IllegalArgumentException("OpCode not implemented yet: " + Integer.toHexString(opC));
            }
        }

        if( IFF1 && IFF3 > 0 )
            IFF3--;

        if( ++tstate > 400 ) {
            interrupt();
            tstate = 0;
        }
    }

    protected void fetchCB(byte opC) {
        opCMasked(opC);

        if( x == 0 ) {
            CBopCodes[x][0][y].execute(); // Roll/Shift
            return;
        }

        // x > 0
        if (CBopCodes[x][0][0] != null) {
            CBopCodes[x][0][0].execute(); // Bitwise y, r[z]
        } else {
            System.out.println("OpCode not implemented yet: " + Integer.toHexString(opC));
            //throw new IllegalArgumentException("CB+OpCode not implemented yet: " + Integer.toHexString(opC));
        }
    }

    protected void fetchED(byte opC) {
        opCMasked(opC);

        if( x == 0 || x == 3
         /*|| z > 4  || y < 4 */) {
            NONI();
            return;
        }

        if(x == 1){
            if(z == 5){
                if(y != 1)
                    EDopCodes[x][z][0].execute(); // RETN
            }
        }

        if (EDopCodes[x][z][y] != null) {
            EDopCodes[x][z][y].execute();
        } else {
            System.out.println("OpCode not implemented yet: " + Integer.toHexString(opC));
            //throw new IllegalArgumentException("ED+OpCode not implemented yet: " + Integer.toHexString(opC));
        }
    }

    protected void fetchDD(byte opC) {
        opCMasked(opC);
        Logger.info("AQUI DD:"+Integer.toHexString(opC));
        if (DDopCodes[x][z][y] != null) {
            DDopCodes[x][z][y].execute();
        } else {
            System.out.println("DD+OpCode not implemented yet: " + Integer.toHexString(opC));
            //throw new IllegalArgumentException("DD+OpCode not implemented yet: " + Integer.toHexString(opC));
        }
    }

    protected void fetchFD(byte opC) {
        opCMasked(opC);
Logger.info("AQUI FD:"+Integer.toHexString(opC));
        if (FDopCodes[x][z][y] != null) {
            FDopCodes[x][z][y].execute();
        } else {
            System.out.println("FD+OpCode not implemented yet: " + Integer.toHexString(opC));
            //throw new IllegalArgumentException("FD+OpCode not implemented yet: " + Integer.toHexString(opC));
        }
    }

    protected void fetchFDCB(byte opC) {
Logger.info("AQUI FDCB disp:"+Integer.toHexString(opC));
        d = opC; // skip displacement

        opC = currentComp.peek(PC++);
Logger.info("AQUI FDCB opC:"+Integer.toHexString(opC));

        opCMasked(opC);

        if (FDCBopCodes[x][z][y] != null) {
            FDCBopCodes[x][z][y].execute();
        } else {
            System.out.println("FDCB+OpCode not implemented yet: " + Integer.toHexString(opC));
            //throw new IllegalArgumentException("FDCB+OpCode not implemented yet: " + Integer.toHexString(opC));
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
    protected BitSet F = new BitSet(8);

    public enum RegTouched {
        A, B, C, D, E, F, H, L, SP, IX, IY, I, R,
        // 0  1  2  3  4  5  6  7  8   9   10 11 12
        A_, B_, C_, D_, E_, F_, H_, L_;
        // 13  14  15  16  17  18  19  20
    };
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
    }

    public Z80(Z80OpCode opC) {
        dispatcher(opC);
    }

    // According to Reset Timing at http://www.z80.info/interrup.htm
    public void reset() {
        regTouched.clear();
        PC = 0; // 0x4C2;
        setI((byte) 0);
        setR((byte) 0); // will forget Flags affectation
        setA((byte) 0xFF);
        setF((byte) 0xFF);
        SP = 0xFFFF;
        regTouched(RegTouched.SP);
        IFF1 = IFF2 = false;
        IFF3 = 2;
        currentIM = 0;
        isHalted = false;
    }

    public void setComputer(Computer theComp) { currentComp = theComp; }

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

    public boolean getSF(){ return F.get(7); }
    public boolean getZF(){ return F.get(6); }
    public boolean getxF(){ return F.get(5); }
    public boolean getHF(){ return F.get(4); }
    public boolean getyF(){ return F.get(3); }
    public boolean getPF(){ return F.get(2); }
    public boolean getVF(){ return F.get(2); } // same getP()
    public boolean getNF(){ return F.get(1); }
    public boolean getCF(){ return F.get(0); }

    public void setSF(){ F.set(7); regTouched(RegTouched.F); }
    public void setZF(){ F.set(6); regTouched(RegTouched.F); }
    public void setxF(){ F.set(5); regTouched(RegTouched.F); }
    public void setHF(){ F.set(4); regTouched(RegTouched.F); }
    public void setyF(){ F.set(3); regTouched(RegTouched.F); }
    public void setPF(){ F.set(2); regTouched(RegTouched.F); }
    public void setVF(){ F.set(2); regTouched(RegTouched.F); } // same setPF()
    public void setNF(){ F.set(1); regTouched(RegTouched.F); }
    public void setCF(){ F.set(0); regTouched(RegTouched.F); }

    public void resSF(){ F.clear(7); regTouched(RegTouched.F); }
    public void resZF(){ F.clear(6); regTouched(RegTouched.F); }
    public void resxF(){ F.clear(5); regTouched(RegTouched.F); }
    public void resHF(){ F.clear(4); regTouched(RegTouched.F); }
    public void resyF(){ F.clear(3); regTouched(RegTouched.F); }
    public void resPF(){ F.clear(2); regTouched(RegTouched.F); }
    public void resVF(){ F.clear(2); regTouched(RegTouched.F); } // same resPF()
    public void resNF(){ F.clear(1); regTouched(RegTouched.F); }
    public void resCF(){ F.clear(0); regTouched(RegTouched.F); }

    // Words
    public short getBC() { return (short) ((short) (B << 8) | (C & 0xFF)); }
    public short getDE() { return (short) ((short) (D << 8) | (E & 0xFF)); }
    public short getHL() { return (short) ((short) (H << 8) | (L & 0xFF)); }
    public int   getSP() { return SP; }
    public short getIX() { return IX; }
    public short getIY() { return IY; }

    private short getWZ() { return (short) ((short) (W << 8) | (Z & 0xFF)); }

    public void setBC(short bc) {
        setB((byte) ((bc & 0xFF00) >> 8));
        setC((byte) (bc & 0x00FF));
    }

    public void setDE(short de) {
        setD((byte) ((de & 0xFF00) >> 8));
        setE( (byte) (de & 0x00FF));
    }

    public void setHL(short hl) {
        setH((byte) ((hl & 0xFF00) >> 8));
        setL( (byte) (hl & 0x00FF));
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
        int nop = 0;
        ++nop;
        --nop;
    }

    private void NONI() {
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
    }

	@Override
    public void DJNZ() {
        byte d = currentComp.peek(PC++);

        if (--B != 0)
            PC += (short) d;
        regTouched(RegTouched.B);
    }

	@Override
    public void JR() {
        byte d = currentComp.peek(PC++);

        PC += (short) d;
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

        if (ccSet)
            PC += (short) d;
    }

    @Override
    public void LD_rp_p_nn() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        set_rp_p();
    }

    @Override
    public void ADD_HL_rp_p() {
        int hl = getHL() & 0xFFFF;
        int wz;

        get_rp_p();
        wz = getWZ() & 0xFFFF;

        hl += wz;

        boolean isHalfCarry = (((getHL() & 0x0FFF) + (getWZ() & 0x0FFF)) & 0xF000) != 0;

        if( (hl & 0xFFFF0000) != 0 )
            setCF();
        else
            resCF();

        if( hl == 0)
            setZF();
        else
            resZF();

        if( iSOverflowADD16(getHL(), getWZ(), hl))
            setVF();
        else
            resVF();

        if(isHalfCarry)
            setHF();
        else
            resHF();

        resNF();

        setHL((short) (hl & 0xFFFF));
    }

    @Override
    public void LD_BC_A() {
        currentComp.poke(getBC(), A);
    }

	@Override
    public void LD_DE_A() {
        currentComp.poke(getDE(), A);
    }

	@Override
    public void LD_nn_HL() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        currentComp.poke(getWZ(), L);
        currentComp.poke(getWZ() + 1, H);
    }

	@Override
    public void LD_nn_A() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        currentComp.poke(getWZ(), A);
    }

	@Override
    public void LD_A_BC() {
        setA(currentComp.peek(getBC()));
    }

	@Override
    public void LD_A_DE() {
        setA(A = currentComp.peek(getDE()));
    }

	@Override
    public void LD_HL_nn() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        setL(currentComp.peek(getWZ()));
        setH(currentComp.peek(getWZ() + 1));
    }

	@Override
    public void LD_A_nn() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        setA(currentComp.peek(getWZ()));
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

        boolean isHalfCarry = (((x & 0x0F) + (1 & 0x0F)) & 0xF0) != 0;

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

        boolean isHalfCarry = ((x & 0x0F) - (1 & 0x0F)) < 0;

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
    }

	@Override
    public void LD_r_y_n() {
        Z = currentComp.peek(PC++);

        set_r_(y);
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

        regTouched(RegTouched.A);
    }

	@Override
    public void RRCA() {
        byte c = (byte) (A & 0x01); // pre-carry

        A >>= 1;

        if( c != 0 ) {
            A |= 0x80;
            setCF();
        } else {
            A &= 0x7F;
            resCF();
        }

        resNF();
        resHF();

        regTouched(RegTouched.A);
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

        regTouched(RegTouched.A);
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

        regTouched(RegTouched.A);
    }

    // https://worldofspectrum.org/faq/reference/z80reference.htm#DAA
	@Override
    public void DAA() { // TODO
        System.out.println("DAA TODO ERROR");
    }

	@Override
    public void CPL() {
        int a = A;
        A = (byte) ~A;

        if( A == 0 )
            setZF();
        else
            resZF();

        if( (A & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( iSOverflowADD(a, 0, A) )
            setVF();
        else
            resVF();

        setNF();
        setHF();

        regTouched(RegTouched.A);
    }

	@Override
    public void SCF() {
        setCF();

        resNF();
        resHF();
    }

	@Override
    public void CCF() {
        F.flip(0);

        resNF();
    }

	@Override
    public void LD_r_y_r_z() {
        get_r_(z);

        set_r_(y);
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
        Logger.halt();
    }

    /*
        https://web.archive.org/web/20170121033813/http://www.cs.umd.edu:80/class/spring2003/cmsc311/Notes/Comb/overflow.html

        https://www.cs.umd.edu/~meesh/cmsc311/clin-cmsc311/Lectures/lecture22/overflow.htm

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

	@Override
    public void ADD_A_r_z() {
        int x = A & 0xFF;
        int result;

        get_r_(z);
        int y = Z & 0xFF;

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

        resNF();

        setA((byte) (result & 0xFF));
    }

	@Override
    public void ADC_A_r_z() {
        int x = A & 0xFF;

        get_r_(z);
        int y = Z & 0xFF;

        int result = x + y;

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
    }

	@Override
    public void SUB_r_z() {
        int x = A & 0xFF;

        get_r_(z);
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

        setA((byte) (result & 0xFF));
    }

	@Override
    public void SBC_A_r_z() {
        int x = A & 0xFF;

        get_r_(z);
        int y = Z & 0xFF;

        int result = x - y;

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

        setA((byte) (result & 0xFF));
    }

	@Override
    public void AND_r_z() {
        get_r_(z);

        A &= Z; regTouched(RegTouched.A);

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
        setHF();
    }

	@Override
    public void XOR_r_z() {
        get_r_(z);

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

        if (A < 0)
            setSF();
        else
            resSF();

        resCF();
        resNF();
        resHF();
    }

	@Override
    public void OR_r_z() {
        get_r_(z);

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

        if (A < 0)
            setSF();
        else
            resSF();

        resCF();
        resNF();
        resHF();
    }

	@Override
    public void CP_r_z() {
        int x = A & 0xFF;

        get_r_(z);

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

            PC = getWZ();
        }
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

        PC = getWZ();
        regTouched(RegTouched.SP);
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
    }

	@Override    
    public void LD_SP_HL() {
        setSP(getHL());
    }

	@Override
    public void JP_cc_y_nn() {
        boolean ccSet = false;

        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

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

        if (ccSet)
            PC = getWZ();
    }

	@Override
    public void JP_nn() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        PC = getWZ();
    }

	@Override
    public void OUT_n_A() {
        Z = currentComp.peek(PC++);
        W = A;
        currentComp.write(getWZ(), A);
    }

	@Override
    public void IN_A_n() { // TODO
        Z = currentComp.peek(PC++);
        W = A;
//Logger.info("IN port:"+Integer.toHexString(getWZ()));
        setA(currentComp.read(getWZ()));
//Logger.info("IN A:"+Integer.toHexString(A));
    }

	@Override
    public void EX_SP_HL() {
        Z = currentComp.peek(SP);
        currentComp.poke(SP, L);
        setL(Z);

        W = currentComp.peek(SP+1);
        currentComp.poke(SP+1, H);
        setH(W);
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
        // opcodes countdown to effectively enabled:
        // this one (EI) and next one
        IFF3 = 2;
    }

	@Override
    public void CALL_cc_y_nn() {
        boolean ccSet = false;
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

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

            PC = getWZ();
            regTouched(RegTouched.SP);
        }
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

        PC = getWZ();
        regTouched(RegTouched.SP);
    }

	@Override
    public void ADD_A_n() {
        Z = currentComp.peek(PC++);

        A += Z;
        regTouched(RegTouched.A);
    }

	@Override
    public void ADC_A_n() {
        Z = currentComp.peek(PC++);

        A += Z;

        if( getCF() ) A++;

        regTouched(RegTouched.A);
    }

	@Override
    public void SUB_n() {
        Z = currentComp.peek(PC++);

        A -= Z;

        regTouched(RegTouched.A);
    }

	@Override
    public void SBC_A_n() {
        Z = currentComp.peek(PC++);

        A -= Z;

        if( getCF() ) A--;

        regTouched(RegTouched.A);
    }

	@Override
    public void AND_n() {
        Z = currentComp.peek(PC++);

        A &= Z; regTouched(RegTouched.A);

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
        setHF();
    }

	@Override
    public void XOR_n() {
        Z = currentComp.peek(PC++);

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

        if (A < 0)
            setSF();
        else
            resSF();

        resCF();
        resNF();
        resHF();
    }

	@Override
    public void OR_n() {
        Z = currentComp.peek(PC++);

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

        if (A < 0)
            setSF();
        else
            resSF();

        resCF();
        resNF();
        resHF();
    }

	@Override
    public void CP_n() {
        int x = A & 0xFF;

        Z = currentComp.peek(PC++);
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

	@Override
    public void RST_y_8() {
        Z = (byte) (y * 8);
        W = 0x00;

        currentComp.poke(--SP, (byte)((PC & 0xFF00)>>8));
        currentComp.poke(--SP, (byte)(PC & 0x00FF));

        PC = getWZ();
        regTouched(RegTouched.SP);
    }

    /* CB prefix */

    @Override
    public void RLC_r_z() {
        get_r_(z);

        byte c = (byte) (Z & 0x80); // pre-carry

        Z <<= 1;
        BitSet pZ = BitSet.valueOf(new byte[]{Z});

        if( (Z & 0x80) != 0 )
            setSF();
        else
            resSF();

        if( Z == 0 )
            setZF();
        else
            resZF();

        if( (pZ.cardinality() % 2) == 0 )
            setPF();
        else
            resPF();

        if( c != 0 ) {
            Z |= 1;
            setCF();
        } else {
            resCF();
        }

        if (Z == 0) // Must be tested after pre-carry!!
            setZF();
        else
            resZF();

        resNF();
        resHF();

        set_r_(z);
    }

    public void RRC_r_z() {
        System.out.println("RRC_r_z"); // TODO
    }

    public void RL_r_z() {
        get_r_(z);

        byte c = (byte) (Z & 0x80); // pre-carry
        boolean oc = getCF(); // old-carry

        Z <<= 1;

        if( c != 0 ) {
            setCF();
        } else {
            resCF();
        }

        if( oc )
            Z |= 1;
        else
            Z &= 0xFE;

        resNF();
        resHF();

        set_r_(z);
    }

/*      CBopCodes[0][0][3] = opC::RR_r_z;
        CBopCodes[0][0][4] = opC::SLA_r_z;
        CBopCodes[0][0][5] = opC::SRA_r_z;
        CBopCodes[0][0][6] = opC::SLL_r_z;*/
    public void RR_r_z() { System.out.println("RR_r_z ERROR"); }
    public void SRA_r_z() { System.out.println("SRA_r_z ERROR"); }

    public void SLA_r_z() {
        get_r_(z);

        byte c = (byte) (Z & 0x80); // pre-carry

        Z <<= 1;

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

        resHF();
        resNF();

        set_r_(z);
    }

    public void SLL_r_z() { System.out.println("SLL_r_z ERROR"); }

    public void SRL_r_z() {
        get_r_(z);

        byte c = (byte) (Z & 0x01); // pre-carry

        Z >>= 1;

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

        set_r_(z);
    }

    /* ED prefix */

	@Override
    public void SBC_HL_rp_p() {
        int carry = getCF() ? 1 : 0;
        int hl = getHL() & 0xFFFF;

        get_rp_p();
        int wz = getWZ() & 0xFFFF;
        if( wz == 0x7FFF )
            System.out.println("BREAKPOINT");

        int result = hl - wz - carry;

        if( isOverflowSUB16(hl, wz, result) )
            setVF();
        else
            resVF();

        if ( (result & 0x10000) != 0 )
            setCF();
        else
            resCF();

        if ((result & 0xFFFF) == 0)
            setZF();
        else
            resZF();

        if ((result & 0x8000) != 0)
            setSF();
        else
            resSF();

        setNF();

        setHL((short)(result & 0xFFFF));
    }

	@Override
    public void ADC_HL_rp_p() {
        int hl;

        get_rp_p();

        hl = getHL();
        hl += getWZ();

        boolean isHalfCarry = (((getHL() & 0x0FFF) + ((getCF()?getWZ()+1:getWZ()) & 0x0FFF)) & 0xF000) != 0;

        if( (hl & 0xFFFF0000) != 0 )
            setCF();
        else
            resCF();

        if(isHalfCarry)
            setHF();
        else
            resHF();

        if( iSOverflowADD16(getHL(), getWZ(), hl) )
            setVF();
        else
            resVF();

        resNF();

        setHL((short) (hl & 0xFFFF));
    }

	@Override
    public void LD_mm_rp_p() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);
        MEMPTR = getWZ();

        get_rp_p();

        currentComp.poke(MEMPTR, Z);
        currentComp.poke(MEMPTR + 1, W);
    }

	@Override
    public void LD_rp_p_mm() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);
        MEMPTR = getWZ();

        Z = currentComp.peek(MEMPTR);
        W = currentComp.peek(MEMPTR + 1);

        set_rp_p();
    }

    /* CB Prefix */

	@Override
    public void BIT_y_r_z() {
        W = (byte) (1 << y);

        get_r_(z);

        Z &= W;

        if( Z == 0 )
            setZF();
        else
            resZF();

        resNF();
        setHF();
    }

	@Override
    public void RES_y_r_z() {
        W = (byte) ~(1 << y);

        get_r_(z);

        Z &= W;

        set_r_(z);
    }

	@Override
    public void SET_y_r_z() {
        W = (byte) (1 << y);

        get_r_(z);

        Z |= W;

        set_r_(z);
    }

    /* ED Prefix */

    public void OUT_C_r_y() {
        Z = C;
        W = B;
        currentComp.write(getWZ(), A);
    }

	@Override
    public void IN_r_y_C() { // TODO
        Z = C;
        W = B;
//Logger.info("IN port:"+Integer.toHexString(getWZ()));
        setA(currentComp.read(getWZ()));
//Logger.info("IN A:"+Integer.toHexString(A));

        BitSet pA = BitSet.valueOf(new byte[]{A});

        if( (pA.cardinality() % 2) == 0 )
            setPF();
        else
            resPF();

        if( A == 0 )
            setZF();
        else
            resZF();

        if ((A & 0x80) != 0)
            setSF();
        else
            resSF();

        resNF();
        resHF();
    }

	@Override
    public void NEG() {
        boolean isHalfCarry = (-(A & 0x0F)) < 0;
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
    }

    @Override
    public void RETN() {
        IFF1 = IFF2;

Logger.info("RET SP:"+Integer.toHexString(SP));
        Z = currentComp.peek(SP++);
Logger.info("(SP):"+Integer.toHexString(Z));
Logger.info("RET SP:"+Integer.toHexString(SP));
        W = currentComp.peek(SP++);
Logger.info("(SP):"+Integer.toHexString(W));

        PC = getWZ();
        regTouched(RegTouched.SP);
    }

    @Override
    public void RETI() {
Logger.info("RET SP:"+Integer.toHexString(SP));
        Z = currentComp.peek(SP++);
Logger.info("(SP):"+Integer.toHexString(Z));
Logger.info("RET SP:"+Integer.toHexString(SP));
        W = currentComp.peek(SP++);
Logger.info("(SP):"+Integer.toHexString(W));

        PC = getWZ();
        regTouched(RegTouched.SP);
    }

    @Override
    public void IM_im_y() {
        switch (y) {
            case 0: currentIM = 0; break;
            case 2: currentIM = 1; break;
            case 3: currentIM = 2; break;
        }
    }

	@Override
    public void LD_I_A() {
        setI(A); // TODO flag affectation missing http://www.z80.info/z80sflag.htm
    }

	@Override
    public void LD_R_A() {
        setR(A);
    }

	@Override
    public void LD_A_I() {
        setA(I);

        if( A == 0 )
            setZF();
        else
            resZF();

        if( IFF2 )
            setVF();
        else
            resVF();

        if( IFF2 )
            setPF();
        else
            resPF();

        resNF();
        resHF();
    }

	@Override
    public void LD_A_R() {
        setA(R);

        if( A == 0 )
            setZF();
        else
            resZF();

        if( IFF2 )
            setVF();
        else
            resVF();

        if( IFF2 )
            setPF();
        else
            resPF();

        resNF();
        resHF();
    }

    //@Override
    //public void RRD() {} TODO
    //@Override
    //public void RLD() {}

	@Override
    public void LDI() {
        // TODO
    }

	@Override
    public void LDD() {
        // TODO
    }

	@Override
    public void LDIR() {
        short org = getHL();
        short dst = getDE();
        short count = getBC();

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
        short org = getHL();
        short dst = getDE();
        short count = getBC();

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

	@Override
    public void CPI() {
        // TODO
    }

	@Override
    public void CPD() {
        // TODO
    }

	@Override
    public void CPIR() {
        // TODO
    }

	@Override
    public void CPDR() {
        // TODO
    }

	@Override
    public void INI() {
        // TODO
    }

	@Override
    public void IND() {
        // TODO
    }

	@Override
    public void INIR() {
        // TODO
    }

	@Override
    public void INDR() {
        // TODO
    }

	@Override
    public void OUTI() {
        // TODO
    }

	@Override
    public void OUTD() {
        // TODO
    }

	@Override
    public void OTIR() {
        // TODO
    }

	@Override
    public void OTDR() {
        // TODO
    }

    /* DD prefix */

    @Override
    public void DEC_IX() {
        IX--;
        regTouched(RegTouched.IX);
    }

    @Override
    public void LD_r_y_IX_d() {
        d = currentComp.peek(PC++);

        Z = currentComp.peek(getIX()+d);

        set_r_(y);
    }

    @Override
    public void LD_IX_nn() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        setIX(getWZ());
    }

    @Override
    public void ADD_IX_rp_p() {
        int hl = getIX() & 0xFFFF;
        int wz;

        get_rp_p();
        wz = getWZ() & 0xFFFF;

        hl += wz;

        boolean isHalfCarry = (((getIX() & 0x0FFF) + (getWZ() & 0x0FFF)) & 0xF000) != 0;

        if( (hl & 0xFFFF0000) != 0 )
            setCF();
        else
            resCF();

        if( hl == 0)
            setZF();
        else
            resZF();

        if( iSOverflowADD16(getIX(), getWZ(), hl))
            setVF();
        else
            resVF();

        if(isHalfCarry)
            setHF();
        else
            resHF();

        resNF();

        setIX((short) (hl & 0xFFFF));
    }

    @Override
    public void JP_IX() {
        PC = getIX();
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

        setIX(getWZ());
        regTouched(RegTouched.SP);
    }

    /* FD prefix */

	@Override
    public void LD_IY_nn() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        setIY(getWZ());
    }

	@Override
    public void LD_IY_d_r_z() { // TODO revisar valor de Z
        d = currentComp.peek(PC++);

        get_r_(z);

        currentComp.poke(getIY()+d, Z);
    }

	@Override
    public void LD_r_y_IY_d() {
        d = currentComp.peek(PC++);

        Z = currentComp.peek(getIY()+d);

        set_r_(y);
    }

    @Override
    public void INC_IY_d() {
        byte d = currentComp.peek(PC++);
        int Z = getIY() + d;

        currentComp.poke(Z, (byte)(currentComp.peek(Z) + 1) );
    }

	@Override
    public void DEC_IY_d() {
        byte d = currentComp.peek(PC++);

        final int Z = getIY() + d;
        currentComp.poke(Z, (byte)(currentComp.peek(Z) - 1) );
    }

	@Override
    public void LD_IY_d_n() {
        byte d = currentComp.peek(PC++);
        Z = currentComp.peek(PC++);

        currentComp.poke(getIY()+d, Z);
    }

    /* FDCB prefix */

	@Override
    public void BIT_y_IY_d() {
        W = (byte) (1 << y);

        Z = currentComp.peek(getIY()+d);
        Z &= W;

        if( Z == 0 )
            setZF();
        else
            resZF();

        resNF();
        setHF();
    }

	@Override
    public void RES_y_IY_d() {
        W = (byte) ~(1 << y);

        Z = currentComp.peek(getIY()+d);
        Z &= W;
        currentComp.poke(getIY()+d, Z);
    }

	@Override
    public void SET_y_IY_d() {
        W = (byte) (1 << y);

        Z = currentComp.peek(getIY()+d);
        Z |= W;
        currentComp.poke(getIY()+d, Z);
    }

	@Override
    public void ADD_A_IY_d() {
        int a = A;
        byte d = currentComp.peek(PC++);

        Z = currentComp.peek(getIY()+d);

        a += Z;

        if( a == 0 )
            setZF();
        else
            resZF();

        if( a >= Byte.MAX_VALUE )
            setCF();
        else
            resCF();

        setA((byte) a);
    }

    @Override
    public void CP_IY_d() {
        byte d = currentComp.peek(PC++);
        int x = A & 0xFF;

        Z = currentComp.peek(getIY()+d);

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

    @Override
    public void SUB_IY_d() {
        byte d = currentComp.peek(PC++);
        int x = A & 0xFF;

        Z = currentComp.peek(getIY()+d);
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

        setA((byte) (result & 0xFF));
    }

    @Override
    public void JP_IY() {
        PC = getIY();
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

        setIY(getWZ());
        regTouched(RegTouched.SP);
    }
}
