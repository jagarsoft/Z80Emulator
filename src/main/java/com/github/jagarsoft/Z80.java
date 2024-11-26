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

    Computer currentComp;
    
    public void reset() { PC = 0; }
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
     * Instructions
     */

    public void NOP(byte z, byte y) {
        /* No Operation */ ;
    }

    public void EX_AF_AF_(byte z, byte y) {
        z = A;
        A = alternative.A;
        alternative.A = z;

        y = getF();
        setF(getF_());
        setF_(y);
    }

    public void DJNZ(byte y, byte z) {
        byte d = currentComp.peek(PC++);

        if( --B != 0 )
            PC += (short)d;
    }

    public void JR(byte y, byte z) {
        byte d = currentComp.peek(PC++);

        PC += (short)d;
    }

    public void JR_cc(byte y, byte z) {
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
    
    public void LD_BC_A(byte z, byte y){
        currentComp.poke(getBC(), A);
    }

    public void LD_DE_A(byte z, byte y){
        currentComp.poke(getDE(), A);
    }

    public void LD_nn_HL(byte z, byte y){
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        currentComp.poke(getWZ(), L);
        currentComp.poke(getWZ()+1, H);
    }

    public void LD_nn_A(byte z, byte y){
        return;
    }
    public void LD_A_BC(byte z, byte y){
        return;
    }
    public void LD_A_DE(byte z, byte y){
        return;
    }
    public void LD_HL_nn(byte z, byte y){
        return;
    }
    public void LD_A_nn(byte z, byte y) {
        return;
    }
}
