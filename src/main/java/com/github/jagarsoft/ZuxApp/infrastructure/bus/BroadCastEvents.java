package com.github.jagarsoft.ZuxApp.infrastructure.bus;

import com.github.jagarsoft.IODevice;
import com.github.jagarsoft.Zux.ZuxIO;
import com.github.jagarsoft.ZuxApp.core.bus.EventBus;
import com.github.jagarsoft.ZuxApp.modules.ports.events.PortWriteEvent;

public class BroadCastEvents implements IODevice {
    EventBus eventBus;
    ZuxIO io;

    public BroadCastEvents(EventBus eventBus, ZuxIO zuxIO) {
        this.eventBus = eventBus;
        this.io = zuxIO;
    }

    @Override
    public void write(int addr, byte data) {
        io.write(addr, data);
        eventBus.publish(new PortWriteEvent(addr, data));
    }

    @Override
    public byte read(int addr) {
        byte data = io.read(addr);
        //eventBus.publish(new PortReadEvent(addr, data));
        return data;
    }
}
