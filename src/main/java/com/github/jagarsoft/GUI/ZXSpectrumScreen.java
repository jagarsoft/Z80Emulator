package com.github.jagarsoft.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

class ZXSpectrumScreen implements Screen {
    final double xM = 0.1875;
    final double yM = 0.3334;
    int width = 256;
    int height = 192;

/*
     * TODO: This image object is Spectrum's VRAM. Must be refactoring along with drawPixel method below.
     * Must be accessible from RAMMemory implementation in order to translate poke into drawPixel
     */
    private BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    
    
    public void drawPixel(int x, int y, Color color) {
        if (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight()) {
            image.setRGB(x, y, color.getRGB());
        }
    }

    @Override
    public void drawPixel(int x, int y) {
        this.drawPixel(x, y, Color.RED);
    }

    public void createScreen(JFrame frame) {
        // Crear el JLayeredPane
        JLayeredPane layeredPane = new JLayeredPane();
        frame.add(layeredPane);

        // Crear el panel inferior
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.BLUE);
        bottomPanel.setBounds(0, 0, frame.getWidth(), frame.getHeight());

        // Crear el panel superior
        TopPanel topPanel = new TopPanel(image);

        //topPanel.setBackground(Color.RED);
        //topPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)); // Borde negro para destacar
        //width = frame.getWidth();
        //height = frame.getHeight();
        //topPanel.setBounds(margin, margin, width - 2 * margin, height - 2 * margin);

        // Añadir los paneles al JLayeredPane
        layeredPane.add(bottomPanel, Integer.valueOf(0)); // Capa inferior
        layeredPane.add(topPanel, Integer.valueOf(1));   // Capa superior

        // Listener para ajustar el tamaño de los paneles dinámicamente
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int width = frame.getWidth() - 16;
                int height = frame.getHeight() - 55;
                int xMargin = (int) (width * xM) / 2;
                int yMargin = (int) (height * yM) / 2;
                // Ajustar tamaño del panel inferior
                bottomPanel.setBounds(0, 0, width, height);
//System.out.printf("width = " + width + " height = " + height + "\n");
//System.out.printf("width - 2 * " + xMargin + " = " + (width - 2 * xMargin) + "\n");
//System.out.printf("height - 2 * " + yMargin + " = " + (height - 2 * yMargin) + "\n");
                // Ajustar tamaño del panel superior con un margen dinámico
                topPanel.setBounds(xMargin, yMargin, width - 2 * xMargin, height - 2 * yMargin);

                topPanel.updateImageBounds(width - 2 * xMargin, height - 2 * yMargin);

                // Revalidar y repintar el layeredPane
                layeredPane.revalidate();
                layeredPane.repaint();
            }
        });

        // Crear un patrón o línea (ejemplo adicional)
        /*for (int y = 0; y < frame.getHeight(); y++) {
            drawPixel(y, y, Color.GREEN); // Línea diagonal verde
        }*/
        /*for(int y = 0; y < 192; y++)
            for(int x = 0; x < 256; x++) {
                drawPixel(x, y, Color.GREEN);
            }
        */
    }
    
    /*
     * Private Inner Class
     */
    private class TopPanel extends JPanel {
        private Image image;
        private int width;
        private int height;

        TopPanel(Image image) {
            this.image = image;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, width, height,null);
        }

        public void updateImageBounds(int width, int height) {
            this.width = width;
            this.height = height;
            repaint(); // Redibujar el panel con la nueva imagen
        }
    }
}