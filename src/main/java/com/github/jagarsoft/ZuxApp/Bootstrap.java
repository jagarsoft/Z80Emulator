package com.github.jagarsoft.ZuxApp;

import com.github.jagarsoft.ZuxApp.core.bus.CommandBus;
import com.github.jagarsoft.ZuxApp.core.bus.EventBus;
import com.github.jagarsoft.ZuxApp.core.module.Module;
import com.github.jagarsoft.ZuxApp.infrastructure.bus.SimpleCommandBus;
import com.github.jagarsoft.ZuxApp.infrastructure.bus.SimpleEventBus;
import com.github.jagarsoft.ZuxApp.infrastructure.bus.AsyncEventBus;
import com.github.jagarsoft.ZuxApp.ui.MainFrame;

import com.github.jagarsoft.ZuxApp.modules.helloworld.HelloWorldModule;
import com.github.jagarsoft.ZuxApp.modules.logger.LoggerModule;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class Bootstrap {

    private EventBus eventBus;
    private CommandBus commandBus;
    private MainFrame mainFrame;

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

        this.mainFrame = new MainFrame(commandBus, eventBus);
        List<Module> modules = Arrays.asList(
                //new MainModule(),
                new LoggerModule(),
                new HelloWorldModule()
        );

        for (Module module : modules) {
            module.register(commandBus, eventBus);
            module.initUI();
        }
    }

    public void launchMainWindow() {
        javax.swing.SwingUtilities.invokeLater(() -> mainFrame.setVisible(true));
    }

    /*
     * Shutdown
     */

    public void terminate() {
    }

    public void withdraw() {
        mainFrame.dispose();
    }
}
