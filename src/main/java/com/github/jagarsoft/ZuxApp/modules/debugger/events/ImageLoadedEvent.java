package com.github.jagarsoft.ZuxApp.modules.debugger.events;

import com.github.jagarsoft.ZuxApp.core.bus.Event;

public class ImageLoadedEvent implements Event {
    @Override
    public String getEventName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getMessage() {
        return getEventName();
    }
}
