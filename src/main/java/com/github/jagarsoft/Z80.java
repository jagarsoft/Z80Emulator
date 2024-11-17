package com.github.jagarsoft;

public class Z80 {
    protected int pc;
    public void reset() {
        this.pc = 0;
    }

    protected int getPC() {
        return this.pc++;
    }
}
