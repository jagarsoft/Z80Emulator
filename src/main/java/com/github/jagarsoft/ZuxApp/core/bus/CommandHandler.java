package com.github.jagarsoft.ZuxApp.core.bus;

/**
 * Interface genérica para los manejadores de comandos.
 */
public interface CommandHandler<T extends Command> {
    void handle(T command);
}
