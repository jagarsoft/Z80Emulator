package com.github.jagarsoft.ZuxApp.modules.ports.events;

import com.github.jagarsoft.ZuxApp.core.bus.Event;

public class PortWriteEvent implements Event {
    private final int addr;
    private final byte data;

    public PortWriteEvent(int addr, byte data) {
        this.addr = addr;
        this.data = data;
    }

    @Override
    public String getEventName() {
        return getClass().getSimpleName();
    }

    @Override
    public String getMessage() {
        return String.format("%04X", getAddr())
                + " " + (char) data + " (" + String.format("%02X", getData() ) + ")";
    }

    public int getAddr() {
        return addr;
    }

    public byte getData() {
        return data;
    }
}
