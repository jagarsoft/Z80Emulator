package com.github.jagarsoft.ZuxApp.modules.disassembler;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class BreakpointCellRenderer extends DefaultTableCellRenderer {
    private final Icon bpIcon = UIManager.getIcon("OptionPane.errorIcon");
    private final DisassemblyTableModel model;

    public BreakpointCellRenderer(DisassemblyTableModel model) {
        this.model = model;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

        JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        int addr = model.get(row).getAddress();
        if (model.isBreakpoint(addr)) {
            label.setIcon(bpIcon);
            label.setText(""); // sólo icono
        } else {
            label.setIcon(null);
            label.setText("");
        }

        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
}

