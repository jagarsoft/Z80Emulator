package com.github.jagarsoft.ZuxApp.core.bus;

public interface IAsyncEventBus extends EventBus {
    <T> void subscribe(Class<T> eventType, EventHandler<T> handler);

    <T> void subscribe(Class<T> eventType, UIEventHandler<T> handler);

    <T> void subscribeToAll(UIEventHandler<T> handler);
}
