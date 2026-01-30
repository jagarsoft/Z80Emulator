package com.github.jagarsoft.ZuxApp.infrastructure.bus;

import com.github.jagarsoft.IODevice;
import com.github.jagarsoft.ZuxApp.core.bus.EventBus;
import com.github.jagarsoft.ZuxApp.modules.ports.events.PortReadEvent;
import com.github.jagarsoft.ZuxApp.modules.ports.events.PortWriteEvent;

public class BroadCastEvents implements IODevice {
    EventBus eventBus;
    IODevice ioDev;

    public BroadCastEvents(EventBus eventBus, IODevice ioDev) {
        this.eventBus = eventBus;
        this.ioDev = ioDev;
    }

    @Override
    public void write(int addr, byte data) {
        ioDev.write(addr, data);
        eventBus.publish(new PortWriteEvent(addr, data));
    }

    @Override
    public void write(int addr, byte data, int tstate) {
        ioDev.write(addr, data, tstate);
        eventBus.publish(new PortWriteEvent(addr, data));
    }

    @Override
    public byte read(int addr) {
        byte data = ioDev.read(addr);
        eventBus.publish(new PortReadEvent(addr, data));
        return data;
    }
}
