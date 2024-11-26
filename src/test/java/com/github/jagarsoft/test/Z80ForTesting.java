package com.github.jagarsoft.test;

import com.github.jagarsoft.Z80;

/**
 * Support class to access to Alternative Register Set for testing purpose only
 */
class Z80ForTesting extends Z80 {
    public byte getA_() { return alternative.A; }
    public void setA_(byte a) { alternative.A = a; }
    
    public byte getB_() { return alternative.B; }
    public void setB_(byte b) { alternative.B = b; }
    
    public byte getC_() { return alternative.C; }
    public void setC_(byte c) { alternative.C = c; }
    
    public byte getD_() { return alternative.D; }
    public void setD_(byte d) { alternative.D = d; }
    
    public byte getE_() { return alternative.E; }
    public void setE_(byte e) { alternative.E = e; }
    
    public byte getH_() { return alternative.H; }
    public void setH_(byte h) { alternative.H = h; }
    
    public byte getL_() { return alternative.L; }
    public void setL_(byte l) { alternative.L = l; }
    
    public short getHL_(){ return (short)((short)(alternative.H << 8) | alternative.L); }
    public short getBC_(){ return (short)((short)(alternative.B << 8) | alternative.C); }
    public short getDE_(){ return (short)((short)(alternative.D << 8) | alternative.E); }
    
    public void setHL_(short hl){ alternative.H = (byte)((hl & 0xFF00) >> 8); alternative.L = (byte)(hl & 0x00FF); }
    public void setBC_(short bc){ alternative.B = (byte)((bc & 0xFF00) >> 8); alternative.C = (byte)(bc & 0x00FF); }
    public void setDE_(short de){ alternative.D = (byte)((de & 0xFF00) >> 8); alternative.E = (byte)(de & 0x00FF); }

    public int getPC() { return PC; }
    protected void setPC(int pc) { PC = pc; }
}