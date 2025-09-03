package com.github.jagarsoft.ZuxApp.modules.debugger.events;

import com.github.jagarsoft.ZuxApp.core.bus.Event;

public class ExecutionPausedEvent implements Event {
    private final String mode;

    public ExecutionPausedEvent(String mode) { this.mode = mode; }
    public String getMode() { return mode; }

    @Override
    public String getEventName() {
        return "ExecutionPausedEvent";
    }

    public String getMessage() {
        return mode;
    }
}
