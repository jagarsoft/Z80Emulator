package com.github.jagarsoft.ZuxApp.infrastructure.commands;

import com.github.jagarsoft.ZuxApp.Bootstrap;
import com.github.jagarsoft.ZuxApp.core.bus.Command;
import com.github.jagarsoft.ZuxApp.core.bus.CommandHandler;

public class GetBootstrapCommand implements Command {
    private Bootstrap bootstrap;

    public void setBootstrap(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public Bootstrap getBootstrap() {
        return this.bootstrap;
    }
}
