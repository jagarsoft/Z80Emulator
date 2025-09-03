package com.github.jagarsoft.ZuxApp.modules.debugger.events;

import com.github.jagarsoft.ZuxApp.core.bus.Event;
import com.github.jagarsoft.ZuxApp.modules.debugger.Z80State;

public class BreakpointHitEvent implements Event {
    private final int address; private final Z80State state;
    public BreakpointHitEvent(int address, Z80State state) { this.address = address; this.state = state; }
    public int getAddress() { return address; }
    public Z80State getState() { return state; }

    @Override
    public String getEventName() {
        return ""; // TODO
    }

    @Override
    public String getMessage() {
        return ""; // TODO
    }
}
