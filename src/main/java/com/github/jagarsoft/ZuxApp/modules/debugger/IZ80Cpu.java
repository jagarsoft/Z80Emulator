package com.github.jagarsoft.ZuxApp.modules.debugger;

import com.github.jagarsoft.Computer;

public interface IZ80Cpu {
    void step(); // exec 1 instruction
    Z80State snapshot();
    int getPC();
    boolean isHalted();
    void reset();
    int getReg(String name);
    Computer getComputer();
}
