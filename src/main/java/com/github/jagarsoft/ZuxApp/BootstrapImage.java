package com.github.jagarsoft.ZuxApp;

import com.github.jagarsoft.ZuxApp.core.bus.CommandBus;
import com.github.jagarsoft.ZuxApp.core.bus.CommandHandler;
import com.github.jagarsoft.ZuxApp.core.bus.EventBus;
import com.github.jagarsoft.ZuxApp.core.module.Module;
import com.github.jagarsoft.ZuxApp.infrastructure.bus.SimpleCommandBus;
import com.github.jagarsoft.ZuxApp.infrastructure.bus.SimpleEventBus;
import com.github.jagarsoft.ZuxApp.infrastructure.commands.GetBootstrapCommand;
import com.github.jagarsoft.ZuxApp.modules.computer.ComputerModule;
import com.github.jagarsoft.ZuxApp.modules.computer.commands.GetComputerCommand;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.MainModule;
import com.github.jagarsoft.ZuxApp.modules.memoryconfig.MemoryConfigModule;
import com.github.jagarsoft.ZuxApp.modules.zxspectrum.ZXSpectrumModule;

import java.util.Arrays;
import java.util.List;

public class BootstrapImage implements Bootstrap {
    private String image = null;
    private String dataRegion = null;

    private EventBus eventBus;
    private CommandBus commandBus;
    private final Bootstrap that = this;

    @Override
    public void initialize() {
        this.eventBus = new SimpleEventBus();
        this.commandBus = new SimpleCommandBus();

        List<Module> modules = Arrays.asList(
                new MainModule()
                , new MemoryConfigModule(64, 1) // TODO Create with default values from commandline options or property file
                , new ZXSpectrumModule()
                , new ComputerModule(image, dataRegion)
        );

        commandBus.registerHandler(GetBootstrapCommand.class, new CommandHandler<GetBootstrapCommand>() {
            @Override
            public void handle(GetBootstrapCommand command) {
                command.setBootstrap(that);
            }
        });

        for (Module module : modules) {
            module.register(commandBus, eventBus);
            module.initUI();
        }
    }

    @Override
    public void terminate() {

    }

    @Override
    public void withdraw() {

    }

    @Override
    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public void setDataRegion(String dataRegion) {
        this.dataRegion = dataRegion;
    }
}
