package com.github.jagarsoft.ZuxApp.modules.memoryconfig.events;

import com.github.jagarsoft.ZuxApp.core.bus.Event;

public class MemoryConfigCancelledEvent implements Event {
    //private final String moduleName;

    /*public MemoryConfigAcceptedEvent(String moduleName) {
        this.moduleName = moduleName;
    }*/

    @Override
    public String getEventName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getMessage() {
        return getEventName();
    }
}
