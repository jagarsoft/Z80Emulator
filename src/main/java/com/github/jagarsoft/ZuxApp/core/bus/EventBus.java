package com.github.jagarsoft.ZuxApp.core.bus;

/*import java.util.function.Consumer;

public interface EventBus {
    <T extends Event> void publish(T event);
    <T extends Event> void subscribe(Class<T> type, Consumer<T> handler);
}
*/

import com.github.jagarsoft.ZuxApp.modules.logger.events.LogEvent;

import java.util.function.Consumer;

public interface EventBus {

    /**
     * Suscribe un handler a un tipo de evento específico.
     *
     * @param eventType Clase del evento
     * @param handler   Función que maneja el evento
     * @param <T>       Tipo del evento
     */
    <T> void subscribe(Class<T> eventType, Consumer<T> handler);

    <T> void subscribe(Class<T> eventType, EventHandler<T> handler);

    <T> void subscribeToAll(Consumer<T> handler);

    <T> void subscribeToAll(EventHandler<T> handler);

    /**
     * Publica un evento en el bus.
     *
     * @param event Evento a publicar
     * @param <T>   Tipo del evento
     */
    <T> void publish(T event);

    /**
     * Elimina todos los handlers registrados para un tipo de evento.
     */
    <T> void unsubscribeAll(Class<T> eventType);

    void unsubscribeAll();

    /**
     * Limpia todos los listeners del bus.
     */
    void clear();
}
