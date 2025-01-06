package com.github.jagarsoft.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;

class ZXSpectrumScreen implements Screen {
    final double xM = 0.1875/2;
    final double yM = 0.3334/2;
    int width = 256;
    int height = 192;
    final double marginHorizontalRatio = 0.09375; // 9.375% del ancho
    final double marginVerticalRatio = 0.1667; // 16.67% del alto
    JFrame frame;
    JLayeredPane layeredPane;
    JPanel bottomPanel;
    TopPanel topPanel;

    // This image object is Spectrum's VRAM.
    private BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    static private Color[] attr = new Color[8];

    static {
        attr[0] = Color.BLACK;
        attr[1] = Color.BLUE;
        attr[2] = Color.RED;
        attr[3] = Color.MAGENTA;
        attr[4] = Color.GREEN;
        attr[5] = Color.CYAN;
        attr[6] = Color.YELLOW;
        attr[7] = Color.WHITE;
     }

    public void drawPixel(int x, int y, Color color) {
        if (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight()) {
            //image.setRGB(x, y, attr[color].getRGB());
            image.setRGB(x, y, color.getRGB());
            System.out.println("BufferedImage actualizado.");
        }
    }

    //@Override
    /* public void drawPixel(int x, int y) { this.drawPixel(x, y, Color.RED); } */

    @Override
    public void drawAttr(byte data) {

    }

    @Override
    public void repaint(Rectangle rect) {
System.out.println("repaint rect: "+rect.toString());
        // Obtener las decoraciones del marco
        Insets insets = frame.getInsets();
        int originalWidth = 256;
        int originalHeight = 192;
        int usableWidth = frame.getWidth() - insets.left - insets.right;
        int usableHeight = frame.getHeight() - insets.top - insets.bottom - frame.getJMenuBar().getHeight();;
        int newWidth = usableWidth;
        int newHeight = usableHeight;

        Rectangle adjusted = adjustRectangle(rect, originalWidth, originalHeight, newWidth, newHeight);

        topPanel.revalidate();
        topPanel.repaint(adjusted);
        //layeredPane.revalidate();
        //layeredPane.repaint(rect);

        System.out.println("Rectángulo original: " + rect);
        System.out.println("Rectángulo ajustado: " + adjusted);
    }

    public Rectangle adjustRectangle(Rectangle original,
                                     int originalWidth, int originalHeight,
                                     int newWidth, int newHeight) {
        int adjustedX = original.x * newWidth / originalWidth;
        int adjustedY = original.y * newHeight / originalHeight;
        int adjustedWidth = original.width * newWidth / originalWidth;
        int adjustedHeight = original.height * newHeight / originalHeight;

        return new Rectangle(adjustedX, adjustedY, adjustedWidth, adjustedHeight);
    }

    public void createScreen(JFrame frame) {
        // Screen is made of two panes: bottom for the border and top for the main
        this.frame = frame;
        this.layeredPane = new JLayeredPane();
        this.bottomPanel = new JPanel();
        this.bottomPanel.setBackground(Color.BLUE);
        bottomPanel.setLayout(null);
        this.bottomPanel.setBounds(0, 0, frame.getWidth(), frame.getHeight());

        this.topPanel = new TopPanel(image);

        //topPanel.setBackground(Color.RED);
        //topPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)); // Borde negro para destacar
        //width = frame.getWidth();
        //height = frame.getHeight();
        //topPanel.setBounds(margin, margin, width - 2 * margin, height - 2 * margin);

        // add panes at JLayeredPane
        this.layeredPane.add(bottomPanel, Integer.valueOf(0)); // bottom layer
        this.layeredPane.add(topPanel, Integer.valueOf(1));   // top layer

        this.frame.add(this.layeredPane);

        // Listener to dynamically adjust both panes size together
        this.frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                topPanel.resized();
            }
        });

        topPanel.resized();

        System.out.println(Arrays.toString(frame.getComponents())); // Verifica los contenidos del JFrame
        System.out.println("LayeredPane parent: " + layeredPane.getParent());
        System.out.println("LayeredPane size: " + layeredPane.getWidth() + " x " + layeredPane.getHeight());
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
            resized();
        }

        public void resized() {
            /*
            int width = frame.getWidth() - 16;
            int height = frame.getHeight() - 55;
            int xMargin = (int) (width * xM) / 2;
            int yMargin = (int) (height * yM) / 2;
            // Ajustar tamaño del panel inferior
            bottomPanel.setBounds(0, 0, width, height);
            */
            // Obtener las decoraciones del marco
            Insets insets = frame.getInsets();
            int width = frame.getWidth() - insets.left - insets.right;
            int height = frame.getHeight() - insets.top - insets.bottom -
                    frame.getJMenuBar().getHeight();
            // Márgenes proporcionales
            int xMargin = (int) (width * marginHorizontalRatio);
            int yMargin = (int) (height * marginVerticalRatio);
//System.out.printf("width = " + width + " height = " + height + "\n");
//System.out.printf("width - 2 * " + xMargin + " = " + (width - 2 * xMargin) + "\n");
//System.out.printf("height - 2 * " + yMargin + " = " + (height - 2 * yMargin) + "\n");
            // Calcular dimensiones del topPanel
            int calculatedTopWidth = width - 2 * xMargin;
            int calculatedTopHeight = height - 2 * yMargin;

            // Ajustar tamaño del panel superior con un margen dinámico
            //this.setBounds(xMargin, yMargin, width - 2 * xMargin, height - 2 * yMargin);
            // Configurar los paneles
            bottomPanel.setBounds(0, 0, width, height);
            setBounds(
                    xMargin, yMargin, // Posición dentro de los márgenes
                    calculatedTopWidth, calculatedTopHeight // Dimensiones calculadas
            );

            //this.updateImageBounds(width - 2 * xMargin, height - 2 * yMargin);
            this.updateImageBounds(
                    calculatedTopWidth, calculatedTopHeight // Dimensiones calculadas
            );

            // Revalidar y repintar el layeredPane
            layeredPane.revalidate();
            layeredPane.repaint();
            //frame.revalidate();
            //frame.repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, width, height,null);
            System.out.println("Redibujando en paintComponent.");
            Rectangle clipping = g.getClipBounds();
            System.out.println("Área de repintado: " + clipping);
        }

        public void updateImageBounds(int width, int height) {
            this.width = width;
            this.height = height;
            repaint(); // Redraw panel with new image
        }
    }
}