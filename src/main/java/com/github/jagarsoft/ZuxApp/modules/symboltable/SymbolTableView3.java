package com.github.jagarsoft.ZuxApp.modules.symboltable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class SymbolTableView3 {
    private final JInternalFrame frame;
    private final JTable table;
    private final DefaultTableModel tableModel;

    public SymbolTableView3() {
        frame = new JInternalFrame("Symbol Table", true, true, true, true);
        frame.setSize(500, 400);

        tableModel = new DefaultTableModel(new Object[]{"Label(s)", "Value (hex)"}, 0);
        table = new JTable(tableModel);

        frame.add(new JScrollPane(table));
        frame.setVisible(true);
    }

    public JInternalFrame getFrame() {
        return frame;
    }

    public void updateTable(SymbolTable2 symbols) {
        tableModel.setRowCount(0); // limpiar
        symbols.getAll().forEach((val, labels) -> {
            tableModel.addRow(new Object[]{labels, String.format("%04X", val)});
        });
    }
}
