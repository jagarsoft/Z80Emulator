package com.github.jagarsoft.ZuxApp.modules.debugger;

public interface IZ80Cpu {
    void step(); // exec 1 instruction
    Z80State snapshot();
    int getPC();
    boolean isHalted();
    void reset();
    int getReg(String name);
}
