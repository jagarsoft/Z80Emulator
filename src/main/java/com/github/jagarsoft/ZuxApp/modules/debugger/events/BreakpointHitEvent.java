package com.github.jagarsoft.ZuxApp.modules.debugger.events;

import com.github.jagarsoft.ZuxApp.core.bus.Event;
import com.github.jagarsoft.ZuxApp.modules.debugger.Z80State;

public class BreakpointHitEvent implements Event {
    private final int address;
    public BreakpointHitEvent(int address) { this.address = address;  }
    public int getAddress() { return address; }

    @Override
    public String getEventName() { return getClass().getName(); }

    @Override
    public String getMessage() {
        return " at " + String.format("%04X", getAddress());
    }
}
