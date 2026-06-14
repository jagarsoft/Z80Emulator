package com.github.jagarsoft.ZuxApp.modules.mainmodule.commands;

import com.github.jagarsoft.ZuxApp.core.bus.Command;

import javax.swing.*;

/*
    MainModule exposes this Command in order to other modules can join their InternalFrames
    to DesktopPane
 */
public class AddJInternalFrameToDesktopPaneCommand implements Command {
    private final JInternalFrame frame;

    public AddJInternalFrameToDesktopPaneCommand(JInternalFrame frame) {
        this.frame = frame;
    }

    public JInternalFrame getFrame() {
        return this.frame;
    }
}
