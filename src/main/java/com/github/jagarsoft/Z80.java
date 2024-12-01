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
    static int x;
    static int y;
    static int z;
    static int p;
    static int q;

    // Array to store reference to methods of different implementations
    static OpCode[][][] opCodes = new OpCode[4][8][8];

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
        opCodes[0][1][0] = opC::LD_rp_p_nn;
        opCodes[0][1][1] = opC::ADD_HL_rp_p;
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

    }

    public void fetch(byte opC) {
        x = ((opC & 0b11000000) >> 6);
        y = ((opC & 0b00111000) >> 3);
        z = (opC & 0b111);
        p = ((y & 0b110) >> 1);
        q = (y & 1);

        if (opCodes[x][z][y] != null) {
            opCodes[x][z][y].execute();
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

    public void reset() { PC = 0; }

    public void setComputer(Computer theComp) { currentComp = theComp; }

    //public void fetch(byte opCode) { d.execute(opCode);}

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
                break;
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
                break;
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
                break;
        }

        l = (short) ((((short) L) & 0xFF) + Z);
        c = (short) (l & 0x0100); // carry
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
                break;
        }

        z = (short) ((((short) Z) & 0xFF) + 1);
        c = (short) (z & 0x0100); // carry
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
                break;
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
                break;
        }

        z = (short) ((((short) Z) & 0xFF) - 1);
        c = (short) ((z == -1) ? 1 : 0); // carry
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
                break;
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
            case "A": A++; break;
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
            case "A": A--; break;
        }
    }
}
