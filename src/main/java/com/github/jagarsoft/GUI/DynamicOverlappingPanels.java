package com.github.jagarsoft.GUI;

import javax.swing.*;
        import java.awt.*;
        import java.awt.event.ComponentAdapter;
        import java.awt.event.ComponentEvent;

public class DynamicOverlappingPanels {
    public static void main(String[] args) {
        int margin = 25; // Tamaño del borde visible del panel inferior
        int width;
        int height;
        // Crear el marco principal
        JFrame frame = new JFrame("Dynamic Overlapping Panels");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);


        // Crear el JLayeredPane
        JLayeredPane layeredPane = new JLayeredPane();
        frame.add(layeredPane);

        // Crear el panel inferior
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.BLUE);
        bottomPanel.setBounds(0, 0, frame.getWidth(), frame.getHeight());

        // Crear el panel superior
        JPanel topPanel = new JPanel();
        topPanel.setBackground(Color.RED);
        //topPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)); // Borde negro para destacar
        width = frame.getWidth();
        height = frame.getHeight();
        topPanel.setBounds(margin, margin, width - 2 * margin, height - 2 * margin);

        // Añadir los paneles al JLayeredPane
        layeredPane.add(bottomPanel, Integer.valueOf(0)); // Capa inferior
        layeredPane.add(topPanel, Integer.valueOf(1));   // Capa superior

        // Listener para ajustar el tamaño de los paneles dinámicamente
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int width = frame.getWidth();
                int height = frame.getHeight();
                // Ajustar tamaño del panel inferior
                bottomPanel.setBounds(0, 0, width, height);

                // Ajustar tamaño del panel superior con un margen dinámico
                topPanel.setBounds(margin, margin, width - 2 * margin, height - 2 * margin);

                // Revalidar y repintar el layeredPane
                layeredPane.revalidate();
                layeredPane.repaint();
            }
        });

        // Mostrar el marco
        frame.setVisible(true);
    }
}
