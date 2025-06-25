package com.github.jagarsoft.Zux;

import com.github.jagarsoft.GUI.Screen;
import com.github.jagarsoft.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class ZuxScreen implements Screen {
    final double marginHorizontalRatio = 0.09375; // 9.375% del ancho
    final double marginVerticalRatio = 0.1667; // 16.67% del alto
    JFrame frame;
    JLayeredPane layeredPane;
    JPanel bottomPanel;
    TopPanel topPanel;

    int cursor_x = 0;
    int cursor_y = 0;

    // This image object is Spectrum's VRAM.
    private BufferedImage image = new BufferedImage(Screen.WIDTH, Screen.HEIGHT, BufferedImage.TYPE_INT_RGB);
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

    private Color pendingColor;

    public void setBorder(byte color) {
        pendingColor = attr[color];
        if (this.bottomPanel == null) {
            return;
        }
        this.bottomPanel.setBackground(pendingColor);
    }

    public void drawPixel(int x, int y, Color color) {
        if (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight()) {
            image.setRGB(x, y, color.getRGB());
            Logger.info("BufferedImage actualizado.");
        }
    }

    public void drawPixel(int x, int y, int color) {
        if (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight()) {
            image.setRGB(x, y, attr[color].getRGB());
            Logger.info("BufferedImage actualizado.");
        }
    }

    //@Override
    /* public void drawPixel(int x, int y) { this.drawPixel(x, y, Color.RED); } */

    /*@Override
    public void drawAttr(byte data) {

    }*/

    // Obtener las decoraciones del marco
    private Dimension usableDimension() {
        Insets insets = frame.getInsets();
        return new Dimension(frame.getWidth() - insets.left - insets.right,
                frame.getHeight() - insets.top - insets.bottom - frame.getJMenuBar().getHeight()
        );
    }

    @Override
    public void repaint(Rectangle rect) {
        Logger.info("repaint rect: "+rect.toString());
        Dimension usableDimension = usableDimension();

        //Fix TODO: shifts an unpainted gap
        //Rectangle adjusted = adjustRectangle(rect, Screen.WIDTH, Screen.HEIGHT, usableDimension.width, usableDimension.height);

        this.bottomPanel.setBackground(pendingColor);

        topPanel.revalidate();
        //topPanel.repaint(adjusted);
        topPanel.repaint();
        //layeredPane.revalidate();
        //layeredPane.repaint(rect);

        Logger.info("usable Dimension: " + usableDimension);
        Logger.info("Rectángulo original: " + rect);
        //Logger.info("Rectángulo ajustado: " + adjusted);
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
        this.bottomPanel.setBackground(pendingColor);
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

        Logger.info(Arrays.toString(frame.getComponents())); // Verifica los contenidos del JFrame
        Logger.info("LayeredPane parent: " + layeredPane.getParent());
        Logger.info("LayeredPane size: " + layeredPane.getWidth() + " x " + layeredPane.getHeight());
    }

    public void putchar(byte data) {
        switch(data) {
            case '\r': cursor_x = 0; return;
            case '\n': cursor_y++; return;
        }

        //System.out.print('*');
        //System.out.printf("%c",data);
        //System.out.print('*');

        if( ! (data == '\n' || data == '\r' || (0x20<=data && data<=0x7f) ) ) {
            return;
        }

        byte[] c = ZXSpectrumCharset.getChar(data);
        int pos_x = cursor_x * 8;
        int pos_y = cursor_y * 8;
        //System.out.printf("pos_x=%d, pos_y=%d\n", pos_x, pos_y);
        for(int y = 0; y < c.length; y++) {
            data = c[y];
            for(int x = 0; x<8; x++, data<<=1) {
                if( ((data & 0x1000_0000) != 0) )
                    drawPixel(pos_x+x, pos_y+y, attr[7]);//attr[1]); // set pixel to Ink Color.RED
                else
                    drawPixel(pos_x+x, pos_y+y, attr[0]); //attr[0]); // res pixel to Paper c[j]
            }
        }
        cursor_x++;
    }

    /*
     * Private Inner Class
     */
    private class TopPanel extends JPanel {
        //private BufferedImage image;
        private int width;
        private int height;

        TopPanel(BufferedImage image) {
            //this.image = image;
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
            Dimension usableDimension = usableDimension();

            // Márgenes proporcionales
            int xMargin = (int) (usableDimension.width * marginHorizontalRatio);
            int yMargin = (int) (usableDimension.height * marginVerticalRatio);
//System.out.printf("width = " + width + " height = " + height + "\n");
//System.out.printf("width - 2 * " + xMargin + " = " + (width - 2 * xMargin) + "\n");
//System.out.printf("height - 2 * " + yMargin + " = " + (height - 2 * yMargin) + "\n");
            // Calcular dimensiones del topPanel
            int calculatedTopWidth = usableDimension.width - 2 * xMargin;
            int calculatedTopHeight = usableDimension.height - 2 * yMargin;

            // Ajustar tamaño del panel superior con un margen dinámico
            //this.setBounds(xMargin, yMargin, width - 2 * xMargin, height - 2 * yMargin);
            // Configurar los paneles
            bottomPanel.setBounds(0, 0, usableDimension.width, usableDimension.height);
            this.setBounds(
                    xMargin, yMargin, // Posición dentro de los márgenes
                    calculatedTopWidth, calculatedTopHeight // Dimensiones calculadas
            );

            //this.updateImageBounds(width - 2 * xMargin, height - 2 * yMargin);
            this.updateImageBounds(
                    calculatedTopWidth, calculatedTopHeight // Dimensiones calculadas
            );

            //image = this.resizeImage(image, calculatedTopWidth, calculatedTopHeight);

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
            Logger.info("Redibujando en paintComponent.");
            Rectangle clipping = g.getClipBounds();
            Logger.info("Área de repintado: " + clipping);
        }

        private void updateImageBounds(int width, int height) {
            this.width = width;
            this.height = height;
            //repaint(); // Redraw panel with new image
        }

        /*private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
            Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            BufferedImage resizedBufferedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            this.width = targetWidth;
            this.height = targetHeight;
            Graphics2D g2d = resizedBufferedImage.createGraphics();
            g2d.drawImage(scaledImage, 0, 0, null);
            g2d.dispose();
            repaint();
            return resizedBufferedImage;
        }*/
    }
}