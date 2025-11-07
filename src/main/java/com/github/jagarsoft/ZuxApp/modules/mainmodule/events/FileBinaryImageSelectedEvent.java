package com.github.jagarsoft.ZuxApp.modules.mainmodule.events;

import com.github.jagarsoft.ZuxApp.core.bus.Event;
import com.github.jagarsoft.ZuxApp.core.events.SelectedFile;

import java.io.File;

public class FileBinaryImageSelectedEvent implements Event, SelectedFile {
    private File selectedFile;

    @Override
    public String getEventName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getMessage() {
        return selectedFile.getAbsolutePath();
    }

    @Override
    public void setSelectedFile(File selectedFile) {
        this.selectedFile =  selectedFile;
    }

    @Override
    public File getSelectedFile() {
        return selectedFile;
    }
}
