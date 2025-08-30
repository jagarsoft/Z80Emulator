package com.github.jagarsoft.ZuxApp.infrastructure.bus;

import com.github.jagarsoft.ZuxApp.core.bus.Command;
import com.github.jagarsoft.ZuxApp.core.bus.CommandHandler;
import com.github.jagarsoft.ZuxApp.core.bus.CommandBus;

/*public class SimpleCommandBus implements CommandBus {
    @Override
    public void dispatch(Command command) {
        // Versión inicial: solo loguea
        System.out.println("[CommandBus] Dispatched: " + command.getClass().getSimpleName());
    }
}*/

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleCommandBus implements CommandBus {

    private final Map<Class<?>, CommandHandler<?>> handlers = new ConcurrentHashMap<>();

    @Override
    public <T extends Command> void registerHandler(Class<T> commandType, CommandHandler<T> handler) {
        if (handlers.containsKey(commandType)) {
            throw new IllegalStateException("Ya existe un handler registrado para el comando: " + commandType.getName());
        }
        handlers.put(commandType, handler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Command> void execute(T command) {
        CommandHandler<T> handler = (CommandHandler<T>) handlers.get(command.getClass());
        if (handler == null) {
            throw new IllegalArgumentException("No hay handler registrado para el comando: " + command.getClass().getName());
        }
        handler.handle(command);
    }
}
