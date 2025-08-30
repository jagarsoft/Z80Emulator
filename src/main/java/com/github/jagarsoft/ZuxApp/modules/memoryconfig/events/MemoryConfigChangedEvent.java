package com.github.jagarsoft.ZuxApp.modules.memoryconfig.events;

import com.github.jagarsoft.ZuxApp.core.bus.Event;

public class MemoryConfigChangedEvent implements Event {
    private int pageSize;
    private long numberPages;

    public MemoryConfigChangedEvent(int pageSize, long numberPages) {
        this.pageSize = pageSize;
        this.numberPages = numberPages;
    }

    @Override
    public String getEventName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getMessage() {
        return pageSize + " * " + numberPages;
    }
}
