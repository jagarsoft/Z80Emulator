package com.github.jagarsoft.ZuxApp.core.bus;

/*public interface CommandBus {
    void dispatch(Command command);
}*/

/**
 * CommandBus: responsable de despachar comandos a sus manejadores correspondientes.
 */
public interface CommandBus {

    /**
     * Registra un handler para un tipo de comando.
     *
     * @param commandType Tipo de comando
     * @param handler     Implementación del handler
     * @param <T>         Tipo del comando
     */
    <T extends Command> void registerHandler(Class<T> commandType, CommandHandler<T> handler);

    /**
     * Ejecuta un comando enviándolo a su handler registrado.
     *
     * @param command Instancia del comando
     * @param <T>     Tipo del comando
     */
    <T extends Command> void execute(T command);
}
