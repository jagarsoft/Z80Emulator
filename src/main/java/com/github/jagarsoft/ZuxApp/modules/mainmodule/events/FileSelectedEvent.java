package com.github.jagarsoft.ZuxApp.modules.mainmodule.events;

import com.github.jagarsoft.ZuxApp.core.bus.Event;

import java.io.File;

public class FileSelectedEvent implements Event {
    File selectedFile;

    public FileSelectedEvent(File selectedFile) {
        this.selectedFile = selectedFile;
    }

    @Override
    public String getEventName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getMessage() {
        return selectedFile.getAbsolutePath();
    }
}
