package com.github.jagarsoft.ZuxApp.modules.debugger.events;

import com.github.jagarsoft.ZuxApp.modules.debugger.IZ80Cpu;
import com.github.jagarsoft.ZuxApp.modules.debugger.Z80State;

public class CpuStateUpdatedEvent {
    private final IZ80Cpu cpu;
    private final Z80State state;
    public CpuStateUpdatedEvent(IZ80Cpu cpu,  Z80State s) {
        this.cpu = cpu;
        this.state = s;
    }
    public IZ80Cpu getCpu() { return cpu; }
    public Z80State getState() { return state; }
}
