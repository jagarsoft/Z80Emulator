package com.github.jagarsoft.ZuxApp.modules.memoryconfig.commands;

import com.github.jagarsoft.ZuxApp.core.bus.Command;

public class GetMemoryConfiguration implements Command {
    public int pageSize;
    public long numberPages;
}
