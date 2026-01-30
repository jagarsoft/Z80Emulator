package com.github.jagarsoft.ZuxApp.modules.disassembler.events;

import com.github.jagarsoft.ZuxApp.core.bus.Event;

public class StepEvent implements Event {
    private final int pc;

    public StepEvent(int pc) {
        this.pc = pc;
    }

    public int getPC() {
        return pc;
    }

    @Override
    public String getEventName() {
        return getClass().getSimpleName();
    }

    @Override
    public String getMessage() {
        return String.format("%04X", pc);
    }
}
