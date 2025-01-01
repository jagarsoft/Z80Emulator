package com.github.jagarsoft;

import java.util.BitSet;

public class Z80 implements Z80OpCode {

    private boolean CB_prefix_seen = false;
    private boolean DD_prefix_seen = false;
    private boolean ED_prefix_seen = false;
    private boolean FD_prefix_seen = false;

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
    static int x;
    static int y;
    static int z;
    static int p;
    static int q;

    // Array to store reference to methods of different implementations
    static OpCode[][][] opCodes = new OpCode[4][8][8];
    static OpCode[][][] CBopCodes = new OpCode[4][8][8];
    static OpCode[][][] DDopCodes = new OpCode[4][8][8];
    static OpCode[][][] EDopCodes = new OpCode[4][8][8];
    static OpCode[][][] FDopCodes = new OpCode[4][8][8];

    private void dispatcher(Z80OpCode opC) {
        // According to http://www.z80.info/decoding.htm
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
        //opCodes[0][7][4] = opC::DAA;  TODO
        opCodes[0][7][5] = opC::CPL;
        opCodes[0][7][6] = opC::SCF;
        opCodes[0][7][7] = opC::CCF;
        // x = 1
        // Exception: 7 * 7 combinations managed in fetch
        // z=6 [x][z][y]
        opCodes[1][0][0] = opC::LD_r_y_r_z;

        // x = 2, z = 0..7
        // Exception: iterate over z instead of y are 7 * 7 combinations managed in fetch,
        // but still opC::PTR is needed
        //     [x][z][y]
        opCodes[2][0][0] = opC::ADD_A;
        opCodes[2][0][1] = opC::ADC_A;
        opCodes[2][0][2] = opC::SUB;
        opCodes[2][0][3] = opC::SBC_A;
        opCodes[2][0][4] = opC::AND;
        opCodes[2][0][5] = opC::XOR;
        opCodes[2][0][6] = opC::OR;
        opCodes[2][0][7] = opC::CP;

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
        // z=1 [x][z][y]
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
        opCodes[3][3][1] = opC::CB_prefix;
        opCodes[3][3][2] = opC::OUT_n_A;
        opCodes[3][3][3] = opC::IN_A_n;
        opCodes[3][3][4] = opC::EX_SP_HL;
        opCodes[3][3][5] = opC::EX_DE_HL;
        opCodes[3][3][6] = opC::DI; // TODO
        opCodes[3][3][7] = opC::EI; // TODO
        // z=4 [x][z][y]
        opCodes[3][4][0] = opC::CALL_cc_y_nn;
        opCodes[3][4][1] = opC::CALL_cc_y_nn;
        opCodes[3][4][2] = opC::CALL_cc_y_nn;
        opCodes[3][4][3] = opC::CALL_cc_y_nn;
        opCodes[3][4][4] = opC::CALL_cc_y_nn;
        opCodes[3][4][5] = opC::CALL_cc_y_nn;
        opCodes[3][4][6] = opC::CALL_cc_y_nn;
        opCodes[3][4][7] = opC::CALL_cc_y_nn;
        // z=5 [x][z][y]
        opCodes[3][5][0b000] = opC::PUSH_rp2_p;
        opCodes[3][5][0b010] = opC::PUSH_rp2_p;
        opCodes[3][5][0b100] = opC::PUSH_rp2_p;
        opCodes[3][5][0b110] = opC::PUSH_rp2_p;
        opCodes[3][5][0b001] = opC::CALL_nn;
//        opCodes[3][3][0x011] = opC::DD_prefix; TODO
//        opCodes[3][3][0x101] = opC::ED_prefix;
//        opCodes[3][3][0x111] = opC::FD_prefix;
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

/*
        // x = 0, z = 0..7
        // Exception: iterate over z instead of y are 7 * 7 combinations managed in fetch,
        // but still opC::PTR is needed
        //       [x][z][y]
        CBopCodes[0][0][0] = opC::RLC;
        CBopCodes[0][0][1] = opC::RRC;
        CBopCodes[0][0][2] = opC::RL;
        CBopCodes[0][0][3] = opC::RR;
        CBopCodes[0][0][4] = opC::SLA;
        CBopCodes[0][0][5] = opC::SRA;
        CBopCodes[0][0][6] = opC::SLL;
        CBopCodes[0][0][7] = opC::SRL;
        */
    }

    public void CB_prefix() { fetchCB(currentComp.peek(getPC())); }
/*
    public void DD_prefix() { fetchCB(currentComp.peek(getPC())); }
    public void ED_prefix() { ED_prefix_seen = true; }
    public void FD_prefix() { FD_prefix_seen = true; }
*/
    private static void opCMasked(byte opC) {
        x = ((opC & 0b11000000) >> 6);
        y = ((opC & 0b00111000) >> 3);
        z = (opC & 0b111);
        p = ((y & 0b110) >> 1);
        q = (y & 1);
    }

    public void fetch() { fetch(currentComp.peek(getPC())); }

    public void fetch(byte opC) {
        opCMasked(opC);

        if( x == 1 ) {
            if( z == 6 && y == 6)
                ;// HALT(); // TODO
            else
                opCodes[x][0][0].execute();// LD_r_y_r_z
            return;
        }

        if( x == 2 ) {
            opCodes[x][0][y].execute(); // Arithmetic & Logical instructions
            return;
        }
        
        if (opCodes[x][z][y] != null) {
            opCodes[x][z][y].execute();
        } else {
            throw new IllegalArgumentException("OpCode not implemented yet: " + Integer.toHexString(opC));
        }
    }

    private void fetchCB(byte opC) {
        opCMasked(opC);

        if (CBopCodes[x][z][y] != null) {
            CBopCodes[x][z][y].execute();
        } else {
            throw new IllegalArgumentException("CB+OpCode not implemented yet: " + Integer.toHexString(opC));
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

    protected int PC;

    protected short SP;

    protected short IX;
    protected short IY;

    protected byte I;
    protected byte R;

    private byte W;
    private byte Z;

    protected Register alternative = new Register();

    protected Computer currentComp;

    public Z80() {
        dispatcher(this);
    }

    public Z80(Z80OpCode opC) {
        dispatcher(opC);
    }

    public void reset() { PC = 0; SP = (short) 0xC000; }

    public void setComputer(Computer theComp) { currentComp = theComp; }

    // Getters / Setters
    public byte getA() { return A; }
    public void setA(byte a) { A = a; }

    public byte getB() { return B; }
    public void setB(byte b) { B = b; }

    public byte getC() { return C; }
    public void setC(byte c) { C = c; }

    public byte getD() {return D; }
    public void setD(byte d) { D = d; }

    public byte getE() { return E; }
    public void setE(byte e) { E = e; }

    public byte getH() { return H; }
    public void setH(byte h) { H = h; }

    public byte getL() { return L; }
    public void setL(byte l) { L = l; }

    public byte getF() { return F.toByteArray()[0]; }
    public void setF(byte f) { F = BitSet.valueOf(new byte[]{f}); }

    public byte getF_() { return alternative.F.toByteArray()[0]; }
    public void setF_(byte f) { alternative.F = BitSet.valueOf(new byte[]{f}); }

    public boolean getSF(){ return F.get(7); }
    public boolean getZF(){ return F.get(6); }
    public boolean getxF(){ return F.get(5); }
    public boolean getHF(){ return F.get(4); }
    public boolean getyF(){ return F.get(3); }
    public boolean getPF(){ return F.get(2); }
    public boolean getNF(){ return F.get(1); }
    public boolean getCF(){ return F.get(0); }

    public void setSF(){ F.set(7); }
    public void setZF(){ F.set(6); }
    public void setxF(){ F.set(5); }
    public void setHF(){ F.set(4); }
    public void setyF(){ F.set(3); }
    public void setPF(){ F.set(2); }
    public void setNF(){ F.set(1); }
    public void setCF(){ F.set(0); }

    public void resSF(){ F.clear(7); }
    public void resZF(){ F.clear(6); }
    public void resxF(){ F.clear(5); }
    public void resHF(){ F.clear(4); }
    public void resyF(){ F.clear(3); }
    public void resPF(){ F.clear(2); }
    public void resNF(){ F.clear(1); }
    public void resCF(){ F.clear(0); }

    // Words
    public short getBC() { return (short) ((short) (B << 8) | (C & 0xFF)); }
    public short getDE() { return (short) ((short) (D << 8) | (E & 0xFF)); }
    public short getHL() { return (short) ((short) (H << 8) | (L & 0xFF)); }
    public short getSP() { return SP; }

    private short getWZ() { return (short) ((short) (W << 8) | (Z & 0xFF)); }

    public void setBC(short bc) {
        B = (byte) ((bc & 0xFF00) >> 8);
        C = (byte) (bc & 0x00FF);
    }

    public void setDE(short de) {
        D = (byte) ((de & 0xFF00) >> 8);
        E = (byte) (de & 0x00FF);
    }

    public void setHL(short hl) {
        H = (byte) ((hl & 0xFF00) >> 8);
        L = (byte) (hl & 0x00FF);
    }

    public void setSP(short sp) {
        SP = sp;
    }

    public String getWord(byte h, byte l) {
        return Integer.toHexString((short) ((short) (h << 8) | (l & 0xFF)));
    }

    public int getPC() {
        return PC++;
    }


    /*
     *
     *      Instructions
     *
     */

    public void NOP() {
        /* No Operation */
    }

    public void EX_AF_AF_() {
        byte tmp = A;
        A = alternative.A;
        alternative.A = tmp;

        tmp = getF();
        setF(getF_());
        setF_(tmp);
    }

    public void DJNZ() {
        byte d = currentComp.peek(PC++);

        if (--B != 0)
            PC += (short) d;
    }

    public void JR() {
        byte d = currentComp.peek(PC++);

        PC += (short) d;
    }

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

    public void LD_rp_p_nn() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        switch (rp[p]) {
            case "BC":
                B = W;
                C = Z;
                break;
            case "DE":
                D = W;
                E = Z;
                break;
            case "HL":
                H = W;
                L = Z;
                break;
            case "SP":
                setSP(getWZ());
        }
    }

    @Override
    public void ADD_HL_rp_p() {
        short l;
        short c;

        switch (rp[p]) {
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
            case "SP":
                W = (byte) ((getSP() & 0xFF00) >> 8);
                Z = (byte) (getSP() & 0x00FF);
        }

        l = (short) ((L & 0x00FF) + (Z & 0x00FF));
        c = (short) (l & 0x0100); // pre-carry
        L = (byte) (l & 0xFF);
        H = (byte) (H + W + (c >> 8));
    }

    @Override
    public void LD_BC_A() {
        currentComp.poke(getBC(), A);
    }

    public void LD_DE_A() {
        currentComp.poke(getDE(), A);
    }

    public void LD_nn_HL() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        currentComp.poke(getWZ(), L);
        currentComp.poke(getWZ() + 1, H);
    }

    public void LD_nn_A() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        currentComp.poke(getWZ(), A);
    }

    public void LD_A_BC() {
        A = currentComp.peek(getBC());
    }

    public void LD_A_DE() {
        A = currentComp.peek(getDE());
    }

    public void LD_HL_nn() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        L = currentComp.peek(getWZ());
        H = currentComp.peek(getWZ() + 1);
    }

    public void LD_A_nn() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        A = currentComp.peek(getWZ());
    }

    public void INC_rp_p() {
        short z;
        short c;

        switch (rp[p]) {
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
            case "SP":
                W = (byte) ((getSP() & 0xFF00) >> 8);
                Z = (byte) (getSP() & 0x00FF);
        }

        z = (short) ((((short) Z) & 0xFF) + 1);
        c = (short) (z & 0x0100); // pre-carry
        Z = (byte) (z & 0xFF);
        W = (byte) (W + (c >> 8));

        switch (rp[p]) {
            case "BC":
                B = W;
                C = Z;
                break;
            case "DE":
                D = W;
                E = Z;
                break;
            case "HL":
                H = W;
                L = Z;
                break;
            case "SP":
                setSP(getWZ());
        }
    }

    public void DEC_rp_p() {
        short z;
        short c;

        switch (rp[p]) {
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
            case "SP":
                W = (byte) ((getSP() & 0xFF00) >> 8);
                Z = (byte) (getSP() & 0x00FF);
        }

        z = (short) ((((short) Z) & 0xFF) - 1);
        c = (short) ((z == -1) ? 1 : 0); // pre-carry
        Z = (byte) (z & 0xFF);
        W = (byte) (W - c);

        switch (rp[p]) {
            case "BC":
                B = W;
                C = Z;
                break;
            case "DE":
                D = W;
                E = Z;
                break;
            case "HL":
                H = W;
                L = Z;
                break;
            case "SP":
                setSP(getWZ());
        }
    }
    
    public void INC_r_y(){
        switch(r[y]){
            case "B": B++; break;
            case "C": C++; break;
            case "D": D++; break;
            case "E": E++; break;
            case "H": H++; break;
            case "L": L++; break;
            case "(HL)":
                    currentComp.poke(getHL(), (byte)(currentComp.peek(getHL()) + 1) );
                    break;
            case "A": A++;
        }
    }
    
    public void DEC_r_y(){
        switch(r[y]){
            case "B": B--; break;
            case "C": C--; break;
            case "D": D--; break;
            case "E": E--; break;
            case "H": H--; break;
            case "L": L--; break;
            case "(HL)":
                currentComp.poke(getHL(), (byte)(currentComp.peek(getHL()) - 1) );
                break;
            case "A": A--;
        }
    }

    public void LD_r_y_n() {
        Z = currentComp.peek(PC++);

        switch (r[y]){
            case "B": setB(Z); break;
            case "C": setC(Z); break;
            case "D": setD(Z); break;
            case "E": setE(Z); break;
            case "H": setH(Z); break;
            case "L": setL(Z); break;
            case "(HL)": currentComp.poke(getHL(), Z); break;
            case "A": setA(Z);
        }
    }

    public void RLCA() {
        byte c = (byte) (A & 0x80); // pre-carry

        A <<= 1;

        if( c != 0 ) {
            A |= 1;
            setCF();
        } else {
            resCF();
        }
    }

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
    }

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
    }

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
    }

    public void DAA() { // TODO

    }

    public void CPL() {
        A = (byte) ~A;
    }

    public void SCF() {
        setCF();
    }

    public void CCF() {
        F.flip(0);
    }

    public void LD_r_y_r_z() {
        switch (r[z]){
            case "B": Z = getB(); break;
            case "C": Z = getC(); break;
            case "D": Z = getD(); break;
            case "E": Z = getE(); break;
            case "H": Z = getH(); break;
            case "L": Z = getL(); break;
            case "(HL)": Z = currentComp.peek(getHL()); break;
            case "A": Z = getA();
        }
        
        switch (r[y]){
            case "B": setB(Z); break;
            case "C": setC(Z); break;
            case "D": setD(Z); break;
            case "E": setE(Z); break;
            case "H": setH(Z); break;
            case "L": setL(Z); break;
            case "(HL)": currentComp.poke(getHL(), Z); break;
            case "A": setA(Z);
        }
    }

    public void HALT() { // TODO

    }

    public void ADD_A() {
        switch (r[z]){
            case "B": Z = getB(); break;
            case "C": Z = getC(); break;
            case "D": Z = getD(); break;
            case "E": Z = getE(); break;
            case "H": Z = getH(); break;
            case "L": Z = getL(); break;
            case "(HL)": Z = currentComp.peek(getHL()); break;
            case "A": Z = getA();
        }

        A += Z;
    }

    public void ADC_A() {
        switch (r[z]){
            case "B": Z = getB(); break;
            case "C": Z = getC(); break;
            case "D": Z = getD(); break;
            case "E": Z = getE(); break;
            case "H": Z = getH(); break;
            case "L": Z = getL(); break;
            case "(HL)": Z = currentComp.peek(getHL()); break;
            case "A": Z = getA();
        }

        A += Z;

        if( getCF() ) A++;
    }

    public void SUB() {
        switch (r[z]){
            case "B": Z = getB(); break;
            case "C": Z = getC(); break;
            case "D": Z = getD(); break;
            case "E": Z = getE(); break;
            case "H": Z = getH(); break;
            case "L": Z = getL(); break;
            case "(HL)": Z = currentComp.peek(getHL()); break;
            case "A": Z = getA();
        }

        A -= Z;
    }

    public void SBC_A() {
        switch (r[z]){
            case "B": Z = getB(); break;
            case "C": Z = getC(); break;
            case "D": Z = getD(); break;
            case "E": Z = getE(); break;
            case "H": Z = getH(); break;
            case "L": Z = getL(); break;
            case "(HL)": Z = currentComp.peek(getHL()); break;
            case "A": Z = getA();
        }

        A -= Z;

        if( getCF() ) A--;
    }

    public void AND() {
        switch (r[z]){
            case "B": Z = getB(); break;
            case "C": Z = getC(); break;
            case "D": Z = getD(); break;
            case "E": Z = getE(); break;
            case "H": Z = getH(); break;
            case "L": Z = getL(); break;
            case "(HL)": Z = currentComp.peek(getHL()); break;
            case "A": Z = getA();
        }

        A &= Z;
    }

    public void XOR() {
        switch (r[z]){
            case "B": Z = getB(); break;
            case "C": Z = getC(); break;
            case "D": Z = getD(); break;
            case "E": Z = getE(); break;
            case "H": Z = getH(); break;
            case "L": Z = getL(); break;
            case "(HL)": Z = currentComp.peek(getHL()); break;
            case "A": Z = getA();
        }

        A ^= Z;
    }

    public void OR() {
        switch (r[z]){
            case "B": Z = getB(); break;
            case "C": Z = getC(); break;
            case "D": Z = getD(); break;
            case "E": Z = getE(); break;
            case "H": Z = getH(); break;
            case "L": Z = getL(); break;
            case "(HL)": Z = currentComp.peek(getHL()); break;
            case "A": Z = getA();
        }

        A |= Z;

        if( A == 0 )
            setZF();
        else
            resZF();
    }

    public void CP() {
        switch (r[z]){
            case "B": Z = getB(); break;
            case "C": Z = getC(); break;
            case "D": Z = getD(); break;
            case "E": Z = getE(); break;
            case "H": Z = getH(); break;
            case "L": Z = getL(); break;
            case "(HL)": Z = currentComp.peek(getHL()); break;
            case "A": Z = getA();
        }

        if( A == Z )
            setZF();
        else
            resZF();
    }
    
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
        
    public void POP_rp2_p() {
        Z = currentComp.peek(SP++);
        W = currentComp.peek(SP++);
        
        switch (rp2[p]) {
            case "BC":
                B = W;
                C = Z;
                break;
            case "DE":
                D = W;
                E = Z;
                break;
            case "HL":
                H = W;
                L = Z;
                break;
            case "AF":
                A = W;
                setF(Z);
        }
    }
    
    public void RET() {
        Z = currentComp.peek(SP++);
        W = currentComp.peek(SP++);
        
        PC = getWZ();
    }
    
    public void EXX() {
        byte tmp;
        
        tmp = getB();
        setB(alternative.B);
        alternative.B = tmp;
        tmp = getC();
        setC(alternative.C);
        alternative.C = tmp;
        
        tmp = getH();
        setH(alternative.H);
        alternative.H = tmp;
        tmp = getL();
        setL(alternative.L);
        alternative.L = tmp;
        
        tmp = getD();
        setD(alternative.D);
        alternative.D = tmp;
        tmp = getE();
        setE(alternative.E);
        alternative.E = tmp;
    }
    
    public void JP_HL() {
        PC = getHL();
    }
    
    public void LD_SP_HL() { SP = getHL(); }

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

    public void JP_nn() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        PC = getWZ();
    }

    public void OUT_n_A() {
        Z = currentComp.peek(PC++);
        W = A;
        currentComp.write(getWZ(), A);
    }

    public void IN_A_n() { // TODO test pending
        Z = currentComp.peek(PC++);
        W = A;
//System.out.println("IN port:"+Integer.toHexString(getWZ()));
        A = currentComp.read(getWZ());
//System.out.println("IN A:"+Integer.toHexString(A));
    }

    public void EX_SP_HL() {
        Z = currentComp.peek(SP);
        currentComp.poke(SP, L);
        L = Z;

        W = currentComp.peek(SP+1);
        currentComp.poke(SP+1, H);
        H = W;
    }

    public void EX_DE_HL() {
        Z = E;
        E = L;
        L = Z;

        W = D;
        D = H;
        H = W;
    }

    public void DI() {} // TODO
    public void EI() {} // TODO

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
        }
    }

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
                Z = 0; //getF();    // FIX TODO
        }

        currentComp.poke(--SP, W);
        currentComp.poke(--SP, Z);
    }

    public void CALL_nn() {
//System.out.println("CALL_nn");
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

//System.out.println("byte alto:"+Integer.toHexString(SP));
        currentComp.poke(--SP, (byte)((PC & 0xFF00)>>8));
//System.out.println("byte bajo:"+Integer.toHexString(SP));
        currentComp.poke(--SP, (byte)(PC & 0x00FF));
//System.out.println("CALL "+Integer.toHexString(getWZ())+ " RET "+Integer.toHexString(PC));

        PC = getWZ();
    }

    public void ADD_A_n() {
        Z = currentComp.peek(PC++);

        A += Z;
    }

    public void ADC_A_n() {
        Z = currentComp.peek(PC++);

        A += Z;

        if( getCF() ) A++;
    }

    public void SUB_n() {
        Z = currentComp.peek(PC++);

        A -= Z;
    }

    public void SBC_A_n() {
        Z = currentComp.peek(PC++);

        A -= Z;

        if( getCF() ) A--;
    }

    public void AND_n() {
        Z = currentComp.peek(PC++);

        A &= Z;

        if (A == 0)
            setZF();
        else
            resZF();

        if (A < 0)
            setSF();
        else
            resSF();
    }

    public void XOR_n() {
        Z = currentComp.peek(PC++);

        A ^= Z;
    }

    public void OR_n() {
        Z = currentComp.peek(PC++);

        A |= Z;
    }

    public void CP_n() {
        Z = currentComp.peek(PC++);

        if( A == Z )
            setZF();
        else
            resZF();
    }

    public void RST_y_8() {
        Z = (byte) (y * 8);
        W = 0x00;

        currentComp.poke(--SP, (byte)((PC & 0xFF00)>>8));
        currentComp.poke(--SP, (byte)(PC & 0x00FF));

        PC = getWZ();
    }
}
