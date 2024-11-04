package com.github.jagarsoft;

public class Z80cpu {
    private int pc;
    public void reset() {
        this.pc = 0;
    }

    public void exec(byte code) {
        System.out.printf("%d: %d\n", this.pc, code);
    }

    public int getPC() {
        return this.pc++;
    }
}
