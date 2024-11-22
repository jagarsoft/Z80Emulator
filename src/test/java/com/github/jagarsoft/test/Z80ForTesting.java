package com.github.jagarsoft.test;

import com.github.jagarsoft.Z80;

/**
 * Support class to access to Alternative Register Bank for testing purpose only
 */
class Z80ForTesting extends Z80 {
    public byte getA_() { return Alternative.A; }
    public void setA_(byte a) { Alternative.A = a; }
    
    public byte getB_() { return Alternative.B; }
    public void setB_(byte b) { Alternative.B = b; }
    
    public byte getC_() { return Alternative.C; }
    public void setC_(byte c) { Alternative.C = c; }
    
    public byte getD_() { return Alternative.D; }
    public void setD_(byte d) { Alternative.D = d; }
    
    public byte getE_() { return Alternative.E; }
    public void setE_(byte e) { Alternative.E = e; }
    
    public byte getH_() { return Alternative.H; }
    public void setH_(byte h) { Alternative.H = h; }
    
    public byte getL_() { return Alternative.L; }
    public void setL_(byte l) { Alternative.L = l; }
    
    public short getHL_(){ return (short)((short)(Alternative.H << 8) | Alternative.L); }
    public short getBC_(){ return (short)((short)(Alternative.B << 8) | Alternative.C); }
    public short getDE_(){ return (short)((short)(Alternative.D << 8) | Alternative.E); }
    
    public void setHL_(short hl){ Alternative.H = (byte)((hl &0xFF00) >> 8); Alternative.L = (byte)(hl & 0x00FF); }
    public void setBC_(short bc){ Alternative.B = (byte)((bc &0xFF00) >> 8); Alternative.C = (byte)(bc & 0x00FF); }
    public void setDE_(short de){ Alternative.D = (byte)((de &0xFF00) >> 8); Alternative.E = (byte)(de & 0x00FF); }
}