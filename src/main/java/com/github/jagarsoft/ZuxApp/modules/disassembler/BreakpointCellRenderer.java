package com.github.jagarsoft.ZuxApp.modules.disassembler;

import com.github.jagarsoft.Instruction;

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

        Instruction inst = model.getInstruction(row);
        if( inst != null ) {
            int bank = inst.bank;
            int addr = inst.getAddress();
            if (model.isBreakpoint(bank*0x1_0000+addr)) {
                label.setIcon(bpIcon);
                label.setText(""); // icon only
            } else {
                label.setIcon(null);
                label.setText("");
            }
        } else
            System.out.println("Fila "+row+" no encontrada");


        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
}

