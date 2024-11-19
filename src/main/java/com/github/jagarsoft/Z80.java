package com.github.jagarsoft;

import static java.lang.Boolean.TRUE;
import static java.lang.Boolean.FALSE;

public class Z80 implements Z80OpCode {
    protected int PC;
    protected byte A, A_;
    protected boolean SF, ZF, xF ,HF, yF, PF, NF, CF;
    protected boolean SF_, ZF_, xF_ ,HF_, yF_, PF_, NF_, CF_;
    protected byte H, H_;
    protected byte L, L_;
    protected byte D, D_;
    protected byte E, E_;
    protected byte B, B_;
    protected byte C, C_;

    static int offset = 0;
    public void reset() { this.PC = 0; }

    // Getters
    protected int getPC() {
        return this.PC++;
    }
    public byte getA_() {
        return this.A_;
    }
    public byte getA() {
        return this.A;
    }
    public byte getB() {
        return this.B;
    }
    public byte getB_() {
        return this.B_;
    }
    public byte getC() {
        return this.C;
    }
    public byte getC_() {
        return this.C_;
    }
    public byte getD() {
        return this.D;
    }
    public byte getE_() {
        return this.E_;
    }
    public byte getH() {
        return this.H;
    }
    public byte getH_() {
        return this.H_;
    }
    public byte getL() {
        return this.L;
    }
    public byte getL_() {
        return this.L_;
    }

    public byte getF() {
        int f;

        f  = this.SF ? 0b10000000 : 0;
        f |= this.ZF ? 0b01000000 : 0;
        f |= this.xF ? 0b00100000 : 0;
        f |= this.HF ? 0b00010000 : 0;
        f |= this.yF ? 0b00001000 : 0;
        f |= this.PF ? 0b00000100 : 0;
        f |= this.NF ? 0b00000010 : 0;
        f |= this.CF ? 0b00000001 : 0;

        return (byte)f;
    }

    public byte getF_() {
        int f_;

        f_  = this.SF_ ? 0b10000000 : 0;
        f_ |= this.ZF_ ? 0b01000000 : 0;
        f_ |= this.xF_ ? 0b00100000 : 0;
        f_ |= this.HF_ ? 0b00010000 : 0;
        f_ |= this.yF_ ? 0b00001000 : 0;
        f_ |= this.PF_ ? 0b00000100 : 0;
        f_ |= this.NF_ ? 0b00000010 : 0;
        f_ |= this.CF_ ? 0b00000001 : 0;

        return (byte)f_;
    }

    // Setters
    public void setPC(int pc) { this.PC = pc; }
    public void setA(byte a) { this.A = a; }
    public void setA_(byte a_) {
        this.A_ = a_;
    }
    public void setB(byte b) {
        this.B = b;
    }
    public void setB_(byte b_) {
        this.B_ = b_;
    }
    public void setC(byte c) {
        this.C = c;
    }
    public void setC_(byte c_) {
        this.C_ = c_;
    }
    public void setD(byte d) {
        this.D = d;
    }
    public void setE_(byte e_) {
        this.E_ = e_;
    }
    public void setH(byte h) {
        this.H = h;
    }
    public void setH_(byte h_) {
        this.H_ = h_;
    }
    public void setL(byte l) {
        this.L = l;
    }
    public void setL_(byte l_) {
        this.L_ = l_;
    }

    public void setF(byte f) {
        this.SF = ((int)f & 0b10000000) != 0 ? TRUE : FALSE;
        this.ZF = ((int)f & 0b01000000) != 0 ? TRUE : FALSE;
        this.xF = ((int)f & 0b00100000) != 0 ? TRUE : FALSE;
        this.HF = ((int)f & 0b00010000) != 0 ? TRUE : FALSE;
        this.yF = ((int)f & 0b00001000) != 0 ? TRUE : FALSE;
        this.PF = ((int)f & 0b00000100) != 0 ? TRUE : FALSE;
        this.NF = ((int)f & 0b00000010) != 0 ? TRUE : FALSE;
        this.CF = ((int)f & 0b00000001) != 0 ? TRUE : FALSE;
    }

    public void setF_(byte f_) {
        this.SF_ = ((int)f_ & 0b10000000) != 0 ? TRUE : FALSE;
        this.ZF_ = ((int)f_ & 0b01000000) != 0 ? TRUE : FALSE;
        this.xF_ = ((int)f_ & 0b00100000) != 0 ? TRUE : FALSE;
        this.HF_ = ((int)f_ & 0b00010000) != 0 ? TRUE : FALSE;
        this.yF_ = ((int)f_ & 0b00001000) != 0 ? TRUE : FALSE;
        this.PF_ = ((int)f_ & 0b00000100) != 0 ? TRUE : FALSE;
        this.NF_ = ((int)f_ & 0b00000010) != 0 ? TRUE : FALSE;
        this.CF_ = ((int)f_ & 0b00000001) != 0 ? TRUE : FALSE;
    }

    public int NOP(byte z, byte y) {
        return 0;
    }

    public int EX_AF_AF_(byte z, byte y) {
        z = this.A;
        this.A = this.A_;
        this.A_ = z;

        y = this.getF();
        this.setF(this.getF_());
        this.setF_(y);
        return 0;
    }

    public int DJNZ(byte y, byte d) {
        if( offset == 0 ){
            return ++offset;
        }

        if( --this.B != 0 )
            this.PC += d;

        return offset = 0;
    }

    public int JR(byte y, byte d) {
        if( offset == 0 ){
            return ++offset;
        }

        this.PC += d;

        return offset = 0;
    }

    public int JR_cc(byte y, byte d) {
        boolean t = false; // True?
        if( offset == 0 ){
            return ++offset;
        }

        switch (cc[y-4]) {
            case "NZ":  t = ! this.ZF; break;
            case "Z":   t = this.ZF; break;
            case "NC":  t = ! this.CF; break;
            case "C":   t = this.CF; break;
        }

        if( t ) this.PC += d;

        return offset = 0;
    }
}
