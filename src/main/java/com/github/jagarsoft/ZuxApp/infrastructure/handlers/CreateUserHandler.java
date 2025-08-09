package com.github.jagarsoft.ZuxApp.infrastructure.handlers;

import com.github.jagarsoft.ZuxApp.core.bus.CommandHandler;
import com.github.jagarsoft.ZuxApp.core.commands.CreateUserCommand;
import com.github.jagarsoft.ZuxApp.core.bus.EventBus;
import com.github.jagarsoft.ZuxApp.core.events.UserCreatedEvent;

public class CreateUserHandler implements CommandHandler<CreateUserCommand> {

    private final EventBus eventBus;

    public CreateUserHandler(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void handle(CreateUserCommand command) {
        // Lógica simulada para crear un usuario
        System.out.println("Usuario creado en base de datos: " + command.getUsername());

        // Dispara evento
        eventBus.publish(new UserCreatedEvent(command.getUsername()));
    }
}
