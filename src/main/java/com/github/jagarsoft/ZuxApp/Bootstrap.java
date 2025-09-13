package com.github.jagarsoft.ZuxApp;

import com.github.jagarsoft.ZuxApp.core.bus.CommandBus;
import com.github.jagarsoft.ZuxApp.core.bus.EventBus;
import com.github.jagarsoft.ZuxApp.core.module.Module;
import com.github.jagarsoft.ZuxApp.infrastructure.bus.SimpleCommandBus;
import com.github.jagarsoft.ZuxApp.infrastructure.bus.AsyncEventBus;
import com.github.jagarsoft.ZuxApp.modules.computer.ComputerModule;
import com.github.jagarsoft.ZuxApp.modules.console.ConsoleModule;
import com.github.jagarsoft.ZuxApp.modules.debugger.DebuggerModule;
import com.github.jagarsoft.ZuxApp.modules.disassembler.DisassemblerModule;
import com.github.jagarsoft.ZuxApp.modules.memory.MemoryViewModule;
import com.github.jagarsoft.ZuxApp.modules.memoryconfig.MemoryConfigModule;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.MainModule;

import com.github.jagarsoft.ZuxApp.modules.logger.LoggerModule;
import com.github.jagarsoft.ZuxApp.modules.ports.PortsViewModule;
import com.github.jagarsoft.ZuxApp.modules.registers.RegistersViewModule;

import java.util.Arrays;
import java.util.List;

public class Bootstrap {

    private EventBus eventBus;
    private CommandBus commandBus;
    private MainModule mainModule;

    /*
     *  Startup
     */
    public void initialize() {
        //this.eventBus = new SimpleEventBus();
        this.eventBus = new AsyncEventBus();
        this.commandBus = new SimpleCommandBus();

        /*
        import com.github.jagarsoft.ZuxApp.core.commands.CreateUserCommand;
        import com.github.jagarsoft.ZuxApp.core.events.UserCreatedEvent;
        import com.github.jagarsoft.ZuxApp.infrastructure.handlers.CreateUserHandler;

        // Listener para eventos de usuario creado
        eventBus.subscribe(UserCreatedEvent.class, event -> {
            System.out.println("Listener: Notificación enviada al usuario " + event.getUsername());
        });

        // Registro del handler
        commandBus.registerHandler(CreateUserCommand.class, new CreateUserHandler(eventBus));

        // Ejecución de un comando que dispara un evento
        commandBus.execute(new CreateUserCommand("Javier"));
         */

        //this.mainModule = new MainModule(commandBus, eventBus);
        List<Module> modules = Arrays.asList(
                new MainModule()
                , new LoggerModule()
                , new ConsoleModule()
                //, new HelloWorldModule()
                , new MemoryConfigModule(64, 1) // TODO Create with default values from commandline options or property file
                , new ComputerModule()
                , new RegistersViewModule()
                , new MemoryViewModule()
                , new PortsViewModule()
                , new DisassemblerModule()
                , new DebuggerModule()
        );

        for (Module module : modules) {
            module.register(commandBus, eventBus);
            module.initUI();
        }
    }

    public void launchMainWindow() {
        //javax.swing.SwingUtilities.invokeLater(() -> mainModule.setVisible(true));
    }

    /*
     * Shutdown
     */

    public void terminate() {
    }

    public void withdraw() {
        //mainModule.dispose(); // TODO who calls it?
    }
}
