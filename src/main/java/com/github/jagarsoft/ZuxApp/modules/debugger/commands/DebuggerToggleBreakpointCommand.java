package com.github.jagarsoft.ZuxApp.modules.debugger.commands;

import com.github.jagarsoft.ZuxApp.core.bus.Command;

public class DebuggerToggleBreakpointCommand implements Command {
    private final int address;
    public DebuggerToggleBreakpointCommand(int address) { this.address = address; }
    public int getAddress() { return address; }
}
