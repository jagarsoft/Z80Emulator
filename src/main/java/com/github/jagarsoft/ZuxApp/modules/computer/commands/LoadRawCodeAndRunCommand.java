package com.github.jagarsoft.ZuxApp.modules.computer.commands;

import com.github.jagarsoft.ZuxApp.core.bus.Command;

public class LoadRawCodeAndRunCommand implements Command {
    public final String init;
    public final String binName;

    public LoadRawCodeAndRunCommand(String init, String binName ) {
        this.init = init;
        this.binName = binName;
    }
}
