package com.github.jagarsoft.ZuxApp.modules.mainmodule.commands;

import com.github.jagarsoft.ZuxApp.core.bus.Command;

import javax.swing.*;

public class AddJMenuItemToMenuCommand implements Command {
    private final String menuName;
    private final String menuItemName;
    private final JMenuItem openFileItem;

    public AddJMenuItemToMenuCommand(String menuName, String menuItemName, JMenuItem openFileItem) {
        this.menuName = menuName;
        this.menuItemName = menuItemName;
        this.openFileItem = openFileItem;
    }

    public String getMenuName() {
        return menuName;
    }

    public JMenuItem getJMenuItem() {
        return openFileItem;
    }

    public String getMenuItemName() {
        return menuItemName;
    }
}
