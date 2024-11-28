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
    static byte x;
    static byte y;
    static byte z;

    // Array to store reference to methods of different implementations
    static OpCode[][][] opCodes = new OpCode[4][8][8];

    private void dispatcher(Z80OpCode opC) {
        // According to http://www.z80.info/decoding.htm
        //     [x][z][y]
        opCodes[0][0][0] = opC::NOP;
        opCodes[0][0][1] = opC::EX_AF_AF_;
        opCodes[0][0][2] = opC::DJNZ;
        opCodes[0][0][3] = opC::JR;
        opCodes[0][0][4] = opC::JR_cc;
        opCodes[0][0][5] = opC::JR_cc;
        opCodes[0][0][6] = opC::JR_cc;
        opCodes[0][0][7] = opC::JR_cc;
        // z = 1 skipped
        // z = 2
        opCodes[0][2][0b000] = opC::LD_BC_A;
        opCodes[0][2][0b010] = opC::LD_DE_A;
        opCodes[0][2][0b100] = opC::LD_nn_HL;
        opCodes[0][2][0b110] = opC::LD_nn_A;
        opCodes[0][2][0b001] = opC::LD_A_BC;
        opCodes[0][2][0b011] = opC::LD_A_DE;
        opCodes[0][2][0b101] = opC::LD_HL_nn;
        opCodes[0][2][0b111] = opC::LD_A_nn;

    }

    public void fetch(byte opC){
        x = (byte) ((opC & (byte)0b11000000) >> 6);
        y = (byte) ((opC & (byte)0b00111000) >> 3);
        z = (byte) (opC & (byte)0b111);

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
    protected short IX;
    protected short IY;
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
    public short getHL(){ return (short)((short)(H << 8) | L); }
    public short getBC(){ return (short)((short)(B << 8) | C); }
    public short getDE(){ return (short)((short)(D << 8) | E); }

    private short getWZ(){ return (short)((short)(W << 8) | Z); }
    
    public void setHL(short hl){ H = (byte)((hl &0xFF00) >> 8); L = (byte)(hl & 0x00FF); }
    public void setBC(short bc){ B = (byte)((bc &0xFF00) >> 8); C = (byte)(bc & 0x00FF); }
    public void setDE(short de){ D = (byte)((de &0xFF00) >> 8); E = (byte)(de & 0x00FF); }

    public String getWord(byte h, byte l) { return Integer.toHexString((short)((short)(h << 8) | (l & 0xFF))); }
    
    public int getPC() { return PC++; }


    /*
     *
     *      Instructions
     *
     */

    public void NOP() {
        /* No Operation */ ;
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

        if( --B != 0 )
            PC += (short)d;
    }

    public void JR() {
        byte d = currentComp.peek(PC++);

        PC += (short)d;
    }

    public void JR_cc() {
        boolean ccSet = false;
        byte d = currentComp.peek(PC++);

        switch (cc[y-4]) {
            case "NZ":  ccSet = ! getZF(); break;
            case "Z":   ccSet = getZF(); break;
            case "NC":  ccSet = ! getCF(); break;
            case "C":   ccSet = getCF(); break;
        }

        if( ccSet )
            PC += (short)d;
    }
    
    public void LD_BC_A() {
        currentComp.poke(getBC(), A);
    }

    public void LD_DE_A(){
        currentComp.poke(getDE(), A);
    }

    public void LD_nn_HL() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        currentComp.poke(getWZ(), L);
        currentComp.poke(getWZ()+1, H);
    }

    public void LD_nn_A(){
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        currentComp.poke(getWZ(), A);
    }

    public void LD_A_BC(){
        A = currentComp.peek(getBC());
    }
    public void LD_A_DE(){
        A = currentComp.peek(getDE());
    }

    public void LD_HL_nn() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        L = currentComp.peek(getWZ());
        H = currentComp.peek(getWZ()+1);
    }

    public void LD_A_nn(){
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        A = currentComp.peek(getWZ());
    }
}
