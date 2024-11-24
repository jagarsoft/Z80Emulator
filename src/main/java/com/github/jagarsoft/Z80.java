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
    
    protected Register Alternative = new Register();

    static int offset = 0; // Minimalist State Machine
    
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

    public byte getF_() { return Alternative.F.toByteArray()[0]; }

    public void setF_(byte f) { Alternative.F = BitSet.valueOf(new byte[]{f}); }
    
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

    public int NOP(byte z, byte y) {
        return 0;
    }

    public int EX_AF_AF_(byte z, byte y) {
        z = A;
        A = Alternative.A;
        Alternative.A = z;

        y = getF();
        setF(getF_());
        setF_(y);
        return 0;
    }

    public int DJNZ(byte y, byte d) {
        if( offset == 0 ){
            return ++offset;
        }

        if( --B != 0 )
            PC += d;

        return offset = 0;
    }

    public int JR(byte y, byte d) {
        if( offset == 0 ){
            return ++offset;
        }

        PC += d;

        return offset = 0;
    }

    public int JR_cc(byte y, byte d) {
        boolean t = false; // True?
        if( offset == 0 ){
            return ++offset;
        }

        switch (cc[y-4]) {
            case "NZ":  t = ! getZF(); break;
            case "Z":   t = getZF(); break;
            case "NC":  t = ! getCF(); break;
            case "C":   t = getCF(); break;
        }

        if( t )
            PC += d;

        return offset = 0;
    }
    
    public int LD_BC_A(byte z, byte y){
        currentComp.poke(getBC(), A);
        
        return 0;
    }

    public int LD_DE_A(byte z, byte y){
        currentComp.poke(getDE(), A);
        
        return 0;
    }
    public int LD_nn_HL(byte z, byte y){
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        currentComp.poke(getWZ(), L);
        currentComp.poke(getWZ()+1, H);

        return 0;
    }
    public int LD_nn_A(byte z, byte y){
        return 0;
    }
    public int LD_A_BC(byte z, byte y){
        return 0;
    }
    public int LD_A_DE(byte z, byte y){
        return 0;
    }
    public int LD_HL_nn(byte z, byte y){
        return 0;
    }
    public int LD_A_nn(byte z, byte y) {
        return 0;
    }
}
