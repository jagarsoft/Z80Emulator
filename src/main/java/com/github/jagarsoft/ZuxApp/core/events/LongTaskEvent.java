package com.github.jagarsoft.ZuxApp.core.events;

import com.github.jagarsoft.ZuxApp.core.bus.Event;
import com.github.jagarsoft.ZuxApp.core.bus.EventHandler;

public class LongTaskEvent/*<T> implements EventHandler<T>, Event*/ {
    private final String message;

    public LongTaskEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
