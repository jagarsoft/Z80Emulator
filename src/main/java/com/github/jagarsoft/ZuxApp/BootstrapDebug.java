package com.github.jagarsoft.ZuxApp;

import com.github.jagarsoft.ZuxApp.core.bus.CommandBus;
import com.github.jagarsoft.ZuxApp.core.bus.CommandHandler;
import com.github.jagarsoft.ZuxApp.core.bus.EventBus;
import com.github.jagarsoft.ZuxApp.core.module.Module;
import com.github.jagarsoft.ZuxApp.infrastructure.bus.SimpleCommandBus;
import com.github.jagarsoft.ZuxApp.infrastructure.bus.AsyncEventBus;
import com.github.jagarsoft.ZuxApp.infrastructure.commands.GetBootstrapCommand;
import com.github.jagarsoft.ZuxApp.modules.computer.ComputerModule;
import com.github.jagarsoft.ZuxApp.modules.console.ConsoleModule;
import com.github.jagarsoft.ZuxApp.modules.dataregion.DataRegionModule;
import com.github.jagarsoft.ZuxApp.modules.debugger.DebuggerModule;
import com.github.jagarsoft.ZuxApp.modules.disassembler.DisassemblerModule;
import com.github.jagarsoft.ZuxApp.modules.memoryviewer.MemoryViewModule;
import com.github.jagarsoft.ZuxApp.modules.memoryconfig.MemoryConfigModule;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.MainModule;
import com.github.jagarsoft.ZuxApp.modules.logger.LoggerModule;
import com.github.jagarsoft.ZuxApp.modules.ports.PortsViewModule;
import com.github.jagarsoft.ZuxApp.modules.registers.RegistersViewModule;
import com.github.jagarsoft.ZuxApp.modules.symboltable.SymbolTableModule;
import com.github.jagarsoft.ZuxApp.modules.zxspectrum.ZXSpectrumModule;

import java.util.Arrays;
import java.util.List;

public class BootstrapDebug implements Bootstrap {
    private String image = null;
    private String dataRegion = null;
    private final Bootstrap that = this;

    private EventBus eventBus;
    private CommandBus commandBus;

    /*
     *  Startup
     */
    public void initialize() {
        //this.eventBus = new SimpleEventBus();
        this.eventBus = new AsyncEventBus();
        this.commandBus = new SimpleCommandBus();

        //this.mainModule = new MainModule(commandBus, eventBus);
        List<Module> modules = Arrays.asList(
                  new MainModule()
                , new LoggerModule()
                , new ConsoleModule()
                //, new HelloWorldModule()
                , new MemoryConfigModule(64, 1) // TODO Create with default values from commandline options or property file
                , new ZXSpectrumModule()
                , new ComputerModule(image,  dataRegion)
                , new RegistersViewModule()
                , new MemoryViewModule()
                , new PortsViewModule()
                , new SymbolTableModule()
                , new DataRegionModule()
                , new DisassemblerModule()
                , new DebuggerModule()
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

    /*
     * Shutdown
     */

    public void terminate() {
    }

    public void withdraw() {
        //mainModule.dispose(); // TODO who calls it?
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
