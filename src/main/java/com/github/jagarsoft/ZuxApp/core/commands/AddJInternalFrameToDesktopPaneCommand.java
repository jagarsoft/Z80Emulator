package com.github.jagarsoft.ZuxApp.core.commands;

import com.github.jagarsoft.ZuxApp.core.bus.Command;

import javax.swing.*;

public class AddJInternalFrameToDesktopPaneCommand implements Command {
    private final JInternalFrame frame;

    public AddJInternalFrameToDesktopPaneCommand(JInternalFrame frame) {
        this.frame = frame;
    }

    public JInternalFrame getFrame() {
        return this.frame;
    }
}
