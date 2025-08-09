package com.github.jagarsoft.ZuxApp.infrastructure.bus;

import com.github.jagarsoft.ZuxApp.core.bus.EventBus;
import com.github.jagarsoft.ZuxApp.core.bus.EventHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class SimpleEventBus implements EventBus {

    private final Map<Class<?>, List<Consumer<?>>> listeners = new ConcurrentHashMap<>();
    private final List<Consumer<?>> globalListeners = new CopyOnWriteArrayList<>();

    @Override
    public <T> void subscribe(Class<T> eventType, Consumer<T> handler) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<Consumer<?>>()).add(handler);
    }

    @Override
    public <T> void subscribe(Class<T> eventType, EventHandler<T> handler) {
        // nothing
    }

    @Override
    public <T> void subscribeToAll(Consumer<T> handler) {
        globalListeners.add(handler);
    }

    @Override
    public <T> void subscribeToAll(EventHandler<T> handler) {
        // nothing
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {
        List<Consumer<?>> handlers = listeners.getOrDefault(event.getClass(), Collections.<Consumer<?>>emptyList());
        for (Consumer<?> handler : handlers) {
            ((Consumer<T>) handler).accept(event);
        }

        for (Consumer<?> handler : globalListeners) {
            ((Consumer<T>) handler).accept(event);
        }
    }

    @Override
    public <T> void unsubscribeAll(Class<T> eventType) {
        listeners.remove(eventType);
    }

    @Override
    public void unsubscribeAll() {
        globalListeners.clear();
    }

    @Override
    public void clear() {
        listeners.clear();
    }
}
