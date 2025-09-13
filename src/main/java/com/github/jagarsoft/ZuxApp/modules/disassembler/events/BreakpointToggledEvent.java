package com.github.jagarsoft.ZuxApp.modules.disassembler.events;

import com.github.jagarsoft.ZuxApp.core.bus.Event;

public class BreakpointToggledEvent implements Event {
    int address;

    public BreakpointToggledEvent(int addr) {
        this.address = addr;
    }

    @Override
    public String getEventName() {
        return getClass().getName();
    }

    @Override
    public String getMessage() {
        return " at " + String.format("%04X", getAddress());
    }

    public int getAddress() {
        return address;
    }
}
