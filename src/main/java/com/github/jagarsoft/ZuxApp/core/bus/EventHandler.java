package com.github.jagarsoft.ZuxApp.core.bus;

public interface EventHandler<T> {
    void handle(T event);
}
