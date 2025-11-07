package com.github.jagarsoft.ZuxApp.modules.dataregion.events;

import com.github.jagarsoft.ZuxApp.core.bus.Event;
import com.github.jagarsoft.ZuxApp.modules.dataregion.DataRegion;

public class DataBlockMapLoadedEvent implements Event {
    private final DataRegion dataRegion;

    public DataBlockMapLoadedEvent(DataRegion dataRegion) {
        this.dataRegion = dataRegion;
    }

    @Override
    public String getEventName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getMessage() {
        return "dataRegion.getMessage() was called";
    }

    public DataRegion getDataRegion() {
        return dataRegion;
    }

}
