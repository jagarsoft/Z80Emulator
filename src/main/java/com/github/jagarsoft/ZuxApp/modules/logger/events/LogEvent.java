package com.github.jagarsoft.ZuxApp.modules.logger.events;

import com.github.jagarsoft.ZuxApp.core.bus.Event;

public class LogEvent implements Event {
    private final String message;

    public LogEvent(String message) {
        this.message = message;
    }

    @Override
    public String getEventName() {
        return "LogEvent";
    }

    public String getMessage() {
        return message;
    }
}
