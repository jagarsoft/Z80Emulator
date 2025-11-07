package com.github.jagarsoft.ZuxApp.modules.symboltable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class SymbolTableView extends JPanel {
    private final SymbolTable symbolTable;
    private final JTextField searchField;
    private final JTable table;
    private List<Map.Entry<Integer, String>> searchResults;
    private int currentSearchIndex = -1;

    public SymbolTableView(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        setLayout(new BorderLayout());

        // Campo de búsqueda
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
        searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        JButton prevButton = new JButton("Prev");
        JButton nextButton = new JButton("Next");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);

        searchPanel.add(buttonPanel, BorderLayout.EAST);
        add(searchPanel, BorderLayout.NORTH);

        // Tabla
        table = new JTable(new DefaultTableModel(new Object[]{"Address", "Label"}, 0));
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Eventos
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updateSearch(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updateSearch(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateSearch(); }
        });

        nextButton.addActionListener(e -> showNextMatch());
        prevButton.addActionListener(e -> showPreviousMatch());
    }

    public void loadData() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (Map.Entry<Integer, String> entry : symbolTable.getAllSymbols().entrySet()) {
            model.addRow(new Object[]{String.format("%04X", entry.getKey()), entry.getValue()});
        }
    }

    private void updateSearch() {
        searchResults = symbolTable.search(searchField.getText());
        currentSearchIndex = searchResults.isEmpty() ? -1 : 0;
        highlightCurrentMatch();
    }

    private void showNextMatch() {
        if (searchResults == null || searchResults.isEmpty()) return;
        currentSearchIndex = (currentSearchIndex + 1) % searchResults.size();
        highlightCurrentMatch();
    }

    private void showPreviousMatch() {
        if (searchResults == null || searchResults.isEmpty()) return;
        currentSearchIndex = (currentSearchIndex - 1 + searchResults.size()) % searchResults.size();
        highlightCurrentMatch();
    }

    private void highlightCurrentMatch() {
        if (currentSearchIndex < 0 || searchResults == null || searchResults.isEmpty()) return;
        int address = searchResults.get(currentSearchIndex).getKey();
        for (int i = 0; i < table.getRowCount(); i++) {
            String rowAddress = (String) table.getValueAt(i, 0);
            if (rowAddress.equals(String.format("%04X", address))) {
                table.setRowSelectionInterval(i, i);
                table.scrollRectToVisible(table.getCellRect(i, 0, true));
                break;
            }
        }
    }
}
