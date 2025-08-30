package com.github.jagarsoft.ZuxApp.modules.mainmodule.commands;

import com.github.jagarsoft.ZuxApp.core.bus.Command;

import javax.swing.*;

public class AddJMenuToMenuBarCommand implements Command {
    private final JMenu menu;

    public AddJMenuToMenuBarCommand(JMenu menu) {
        this.menu = menu;
    }

    public JMenu getMenu() { return this.menu; }
}
