package com.github.jagarsoft.ZuxApp.infrastructure.bus;

import com.github.jagarsoft.ZuxApp.core.bus.EventHandler;
import com.github.jagarsoft.ZuxApp.core.bus.IAsyncEventBus;
import com.github.jagarsoft.ZuxApp.core.bus.UIEventHandler;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.concurrent.CopyOnWriteArrayList;

public class AsyncEventBus implements IAsyncEventBus {

    private final Map<Class<?>, List<Object>> listeners = new ConcurrentHashMap<>();
    private final List<Consumer<?>> globalListeners = new CopyOnWriteArrayList<>();
    private final List<EventHandler<?>> globalAsyncListeners = new CopyOnWriteArrayList<>();
    private final List<UIEventHandler<?>> globalUIListeners = new CopyOnWriteArrayList<>();

    private final ExecutorService executor;

    public AsyncEventBus() {
        this.executor = Executors.newCachedThreadPool();
    }

    @Override
    public <T> void subscribe(Class<T> eventType, Consumer<T> handler) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    @Override
    public <T> void subscribe(Class<T> eventType, EventHandler<T> handler) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    @Override
    public <T> void subscribe(Class<T> eventType, UIEventHandler<T> handler) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    @Override
    public <T> void subscribeToAll(Consumer<T> handler) {
        globalListeners.add(handler);
    }

    @Override
    public <T> void subscribeToAll(EventHandler<T> handler) {
        globalAsyncListeners.add(handler);
    }

    @Override
    public <T> void subscribeToAll(UIEventHandler<T> handler) {
        globalUIListeners.add(handler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {
        List<Object> handlers = listeners.get(event.getClass());
        if (handlers != null) {
            for (Object handler : handlers) {
                if (handler instanceof Consumer) {
                    executor.submit(() -> ((Consumer<T>) handler).accept(event));
                } else if (handler instanceof EventHandler) {
                    executor.submit(() -> invokeHandler((EventHandler<T>) handler, event));
                }
            }
        }

        // execute async handlers before sync ones below in order to not block the publisher.
        for (EventHandler<?> handler : globalAsyncListeners) {
            executor.submit(() -> invokeHandler((EventHandler<T>) handler, event)); // pool
        }

        for (UIEventHandler<?> h : globalUIListeners)
            executor.submit(() -> invokeHandler((EventHandler<T>) h, event)); // invokeLater → EDT

        for (Consumer<?> handler : globalListeners) {
            executor.submit(() -> ((Consumer<T>) handler).accept(event));
        }

    }

    private <T> void invokeHandler(EventHandler<T> handler, T event) {
        if (handler instanceof UIEventHandler) {
            SwingUtilities.invokeLater(() -> handler.handle(event));
        } else {
            handler.handle(event);
        }
    }

    @Override
    public <T> void unsubscribeAll(Class<T> eventType) {
        listeners.remove(eventType);
    }

    @Override
    public void unsubscribeAll() {
        globalListeners.clear();
        globalAsyncListeners.clear();
    }

    @Override
    public void clear() {
        listeners.clear();
    }

    public void shutdown() {
        executor.shutdown();
    }
}
