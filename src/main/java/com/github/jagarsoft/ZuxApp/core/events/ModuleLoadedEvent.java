package com.github.jagarsoft.ZuxApp.core.events;

import com.github.jagarsoft.ZuxApp.core.bus.Event;

public class ModuleLoadedEvent implements Event {
    private final String moduleName;

    public ModuleLoadedEvent(String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public String getEventName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getMessage() {
        return moduleName;
    }
}
