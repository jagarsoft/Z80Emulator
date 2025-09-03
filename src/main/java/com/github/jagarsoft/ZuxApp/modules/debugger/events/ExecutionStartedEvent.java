package com.github.jagarsoft.ZuxApp.modules.debugger.events;

import com.github.jagarsoft.ZuxApp.core.bus.Event;

public class ExecutionStartedEvent implements Event {
    private final String mode;

    public ExecutionStartedEvent(String mode) { this.mode = mode; }
    public String getMode() { return mode; }

    @Override
    public String getEventName() {
        return "ExecutionStartedEvent";
    }

    public String getMessage() {
        return mode;
    }
}
