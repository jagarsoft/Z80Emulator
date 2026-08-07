package com.github.jagarsoft.ZuxApp.modules.debugger.events;

import com.github.jagarsoft.Computer;
import com.github.jagarsoft.ZuxApp.core.bus.Event;
import com.github.jagarsoft.ZuxApp.modules.debugger.DebuggerModule;
import com.github.jagarsoft.ZuxApp.modules.disassembler.DisassemblerModule;
import com.github.jagarsoft.ZuxApp.modules.memoryviewer.MemoryViewModule;

/*
    A binary image file had been loaded. Three modules are subscribe to this event:
        * DisassemblerModule
        * DebuggerModule
        * MemoryViewModule

    They listen to this event in order to update their own data model
 */
public class BinaryImageLoadedEvent implements Event {
    private final Computer currentComputer;
    private final int bank;
    private final int origin;
    private final long length;

    public BinaryImageLoadedEvent(Computer currentComputer, int origin, long length) {
        this.currentComputer = currentComputer;
        this.bank = 0;
        this.origin = origin;
        this.length = length;
    }

    public BinaryImageLoadedEvent(Computer currentComputer, int bank, int offset, long length) {
        this.currentComputer = currentComputer;
        this.bank = bank;
        this.origin = offset;
        this.length = length;
    }

    public Computer getComputer() { return currentComputer; }

    public int getBank() {
        return bank;
    }

    public int getOrigin() {
        return origin;
    }

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
