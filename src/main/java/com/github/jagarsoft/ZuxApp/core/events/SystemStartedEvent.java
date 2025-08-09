package com.github.jagarsoft.ZuxApp.core.events;

public class SystemStartedEvent {
    private final long timestamp;

    public SystemStartedEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }
}
