package com.github.jagarsoft.ZuxApp.infrastructure;

import javax.swing.*;

public class MenuUtils {

    // ==== Inserción por referencia ====
    public static boolean insertBefore(JMenu menu, JMenuItem reference, JMenuItem newItem) {
        return insertRelative(menu, reference, newItem, true);
    }

    public static boolean insertAfter(JMenu menu, JMenuItem reference, JMenuItem newItem) {
        return insertRelative(menu, reference, newItem, false);
    }

    // ==== Inserción por texto ====
    public static boolean insertBefore(JMenu menu, String referenceText, JMenuItem newItem) {
        JMenuItem ref = findItemByText(menu, referenceText);
        if (ref == null) return false;
        return insertBefore(menu, ref, newItem);
    }

    public static boolean insertAfter(JMenu menu, String referenceText, JMenuItem newItem) {
        JMenuItem ref = findItemByText(menu, referenceText);
        if (ref == null) return false;
        return insertAfter(menu, ref, newItem);
    }

    // ==== Búsqueda recursiva por texto ====
    private static JMenuItem findItemByText(JMenu menu, String text) {
        for (int i = 0; i < menu.getMenuComponentCount(); i++) {
            if (menu.getMenuComponent(i) instanceof JMenuItem) {
                JMenuItem item = (JMenuItem) menu.getMenuComponent(i);
                if (text.equals(item.getText())) {
                    return item;
                }
                if (item instanceof JMenu) {
                    JMenuItem found = findItemByText((JMenu) item, text);
                    if (found != null) return found;
                }
            }
        }
        return null;
    }

    // ==== Inserción recursiva interna ====
    private static boolean insertRelative(JMenu menu, JMenuItem reference, JMenuItem newItem, boolean before) {
        int index = menu.getPopupMenu().getComponentIndex(reference);
        if (index != -1) {
            menu.insert(newItem, before ? index : index + 1);
            return true;
        }
        for (int i = 0; i < menu.getMenuComponentCount(); i++) {
            if (menu.getMenuComponent(i) instanceof JMenu) {
                JMenu subMenu = (JMenu) menu.getMenuComponent(i);
                boolean inserted = insertRelative(subMenu, reference, newItem, before);
                if (inserted) return true;
            }
        }
        return false;
    }
}
