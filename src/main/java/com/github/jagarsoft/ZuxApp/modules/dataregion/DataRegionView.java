package com.github.jagarsoft.ZuxApp.modules.dataregion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class DataRegionView extends JPanel {
    private DataRegion dataRegion;
    private JTextArea displayArea;
    private JTextField addressField;
    private JLabel resultLabel;

    public DataRegionView(DataRegion dataRegion) {
        this.dataRegion = dataRegion;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        JButton loadButton = new JButton("Load File");
        loadButton.addActionListener(new LoadFileListener());
        topPanel.add(loadButton);

        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.add(new JLabel("Address:"));
        addressField = new JTextField(8);
        searchPanel.add(addressField);
        JButton searchButton = new JButton("Check");
        searchButton.addActionListener(new SearchListener());
        searchPanel.add(searchButton);
        resultLabel = new JLabel(" ");
        searchPanel.add(resultLabel);
        
        topPanel.add(searchPanel);
        add(topPanel, BorderLayout.NORTH);

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(displayArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    private class LoadFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            
            if (fileChooser.showOpenDialog(DataRegionView.this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    dataRegion.load(selectedFile.getAbsolutePath());
                    updateDisplay();
                    JOptionPane.showMessageDialog(DataRegionView.this, 
                        "File loaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DataRegionView.this, 
                        "Error loading file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private class SearchListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String addressText = addressField.getText().trim();
                int address;
                
                if (addressText.startsWith("0x") || addressText.startsWith("0X")) {
                    address = Integer.parseInt(addressText.substring(2), 16);
                } else if (addressText.endsWith("h") || addressText.endsWith("H")) {
                    address = Integer.parseInt(addressText.substring(0, addressText.length() - 1), 16);
                } else {
                    address = Integer.parseInt(addressText);
                }
                
                boolean isDataRegion = dataRegion.isDataRegion(address);
                resultLabel.setText(isDataRegion ? "YES - Data Region" : "NO - Not Data Region");
                resultLabel.setForeground(isDataRegion ? Color.GREEN.darker() : Color.RED);
                
            } catch (NumberFormatException ex) {
                resultLabel.setText("Invalid address format");
                resultLabel.setForeground(Color.RED);
            }
        }
    }

    private void updateDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("Data Regions:\n");
        sb.append("=============\n\n");
        
        for (String region : dataRegion.getAllRegions()) {
            sb.append(region).append("\n");
        }
        
        displayArea.setText(sb.toString());
    }
}