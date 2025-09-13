package com.github.jagarsoft.ZuxApp.modules.debugger.events;

import com.github.jagarsoft.Computer;
import com.github.jagarsoft.ZuxApp.core.bus.Event;
import com.github.jagarsoft.ZuxApp.modules.computer.commands.GetComputerCommand;

public class ImageLoadedEvent implements Event {
    private final Computer currentComputer;
    private final long length;

    public ImageLoadedEvent(Computer currentComputer, long length) {
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
