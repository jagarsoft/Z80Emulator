package com.github.jagarsoft.ZuxApp.modules.debugger.events;

import com.github.jagarsoft.Computer;
import com.github.jagarsoft.ZuxApp.core.bus.Event;

public class BinaryImageLoadedEvent implements Event {
    private final Computer currentComputer;
    private final long length;

    public BinaryImageLoadedEvent(Computer currentComputer, long length) {
        this.currentComputer = currentComputer;
        this.length = length;
    }

    public Computer getComputer() { return currentComputer; }

    public long getLength() {
        return length;
    }

    @Override
    public String getEventName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getMessage() {
        return getEventName();
    }
}
