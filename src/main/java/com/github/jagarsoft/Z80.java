package com.github.jagarsoft;

public class Z80 {
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

    public void reset() { this.PC = 0; }

    // Getters
    protected int getPC() {
        return this.PC++;
    }
    protected byte getA() {
        return this.A;
    }
    protected byte getA_() {
        return this.A_;
    }
    protected byte getB() {
        return this.B;
    }
    protected byte getB_() {
        return this.B_;
    }
    protected byte getC() {
        return this.C;
    }
    protected byte getC_() {
        return this.C_;
    }
    protected byte getD() {
        return this.D;
    }
    protected byte getE_() {
        return this.E_;
    }
    protected byte getH() {
        return this.H;
    }
    protected byte getH_() {
        return this.H_;
    }
    protected byte getL() {
        return this.L;
    }
    protected byte getL_() {
        return this.L_;
    }
    /*protected byte getF() {
        return 0; /#*this.SF << 7
             | ZF << 6
             | xF << 5
             | HF << 4
             | yF << 3
             | PF << 2
             | NF << 1
             | CF;*#/
    }*/

    protected byte getF_() {
        return 0;
    }

    // Setters
    protected void setPC(int pc) { this.PC = pc; }
    protected void setA(byte a) { this.A = a; }
    protected void setA_(byte a_) {
        this.A_ = a_;
    }
    protected void setB(byte b) {
        this.B = b;
    }
    protected void setB_(byte b_) {
        this.B_ = b_;
    }
    protected void setC(byte c) {
        this.C = c;
    }
    protected void setC_(byte c_) {
        this.C_ = c_;
    }
    protected void setD(byte d) {
        this.D = d;
    }
    protected void setE_(byte e_) {
        this.E_ = e_;
    }
    protected void setH(byte h) {
        this.H = h;
    }
    protected void setH_(byte h_) {
        this.H_ = h_;
    }
    protected void setL(byte l) {
        this.L = l;
    }
    protected void setL_(byte l_) {
        this.L_ = l_;
    }

}
