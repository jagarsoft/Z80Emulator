package com.github.jagarsoft.ZuxApp.modules.symboltable;

import javax.swing.*;
import java.io.File;

public class SymbolTableModule2 {
    private final SymbolTableView2 view;
    private SymbolTable2 model;

    public SymbolTableModule2() {
        this.model = new SymbolTable2();
        this.view = new SymbolTableView2();
    }

    public JInternalFrame getFrame() {
        return view.getFrame();
    }

    public JMenuItem createLoadMenuItem() {
        JMenuItem loadItem = new JMenuItem("Load Symbol Table...");
        loadItem.addActionListener(e -> onLoadSymbolTable());
        return loadItem;
    }

    private void onLoadSymbolTable() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open .sym or .map file");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Z88DK Symbol Files", "sym", "map"));

        if (chooser.showOpenDialog(view.getFrame()) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                this.model = SymbolTableLoader.load(file);
                view.updateTable(model);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        view.getFrame(),
                        "Failed to load symbol file: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}
