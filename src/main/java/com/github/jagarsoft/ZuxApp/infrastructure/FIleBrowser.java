package com.github.jagarsoft.ZuxApp.infrastructure;

import com.github.jagarsoft.ZuxApp.core.bus.Event;
import com.github.jagarsoft.ZuxApp.core.bus.EventBus;
import com.github.jagarsoft.ZuxApp.core.events.SelectedFile;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class FIleBrowser implements ActionListener {
    private final EventBus eventBus;
    private final Event event;
    
    public FIleBrowser(EventBus eventBus, Event event) {
        this.eventBus = eventBus;
        this.event = event;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        openFileBrowser();
    }

    private void openFileBrowser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a file");

        fileChooser.setCurrentDirectory(new java.io.File(System.getProperty("user.home")));

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            ((SelectedFile)event).setSelectedFile(selectedFile);
            eventBus.publish(event);
        }
    }
}
