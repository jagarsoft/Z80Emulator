package com.github.jagarsoft.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

class MainFrame {
    final double xM = 0.8750;
    final double yM = 0.2916;
    int width = 256;
    int height = 192;
    int xMargin = 48; //(int)(width * xM);
    int yMargin = 56; //(int)(height * yM);
	JFrame frame = new JFrame("Z80Emulator");
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	
	// Create the main frame
	public void init(){
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width + 2 * xMargin, height + 2 * yMargin);
		
		/*Image icon = new javax.swing.ImageIcon("images/android.png").getImage();
		frame.setIconImage(icon);

		String iconPath = "/net/codejava/swing/jframe/android.png";
		Image icon = new ImageIcon(getClass().getResource(iconPath)).getImage();
		frame.setIconImage(icon);*/
	}

    // Create a menu bar
	public void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem exitItem = new JMenuItem("Exit");

        openItem.addActionListener(new FileOpen(frame));
        exitItem.addActionListener(new FileExit(frame));

        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
		
		// Set layout for the main frame
        frame.setLayout(new BorderLayout());
        frame.setJMenuBar(menuBar);

        frame.setVisible(true);
	}

    public void createPanels() {
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
                int width = frame.getWidth();
                int height = frame.getHeight();
                int xMargin = 48; //(int) (width * xM);
                int yMargin = 56; //(int) (height * yM);
                // Ajustar tamaño del panel inferior
                bottomPanel.setBounds(0, 0, width, height);
System.out.printf("width = " + width + " height = " + height + "\n");
System.out.printf("width - 2 * " + xMargin + " = " + (width - 2 * xMargin) + "\n");
System.out.printf("height - 2 * " + yMargin + " = " + (height - 2 * yMargin) + "\n");
                // Ajustar tamaño del panel superior con un margen dinámico
                topPanel.setBounds(xMargin, yMargin, width - 2 * xMargin, height - 2 * yMargin);

                topPanel.updateImageBounds(width - 2 * xMargin, height - 2 * yMargin);

                // Revalidar y repintar el layeredPane
                layeredPane.revalidate();
                layeredPane.repaint();
            }
        });

        // Crear un patrón o línea (ejemplo adicional)
        for (int y = 0; y < frame.getHeight(); y++) {
            drawPixel(y, y, Color.GREEN); // Línea diagonal verde
        }
    }

    private void drawPixel(int x, int y, Color color) {
        if (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight()) {
            image.setRGB(x, y, color.getRGB());
        }
    }

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
            g.drawImage(image, 0, 0, width - 2 * xMargin, height - 2 * yMargin,null);
        }

        public void updateImageBounds(int width, int height) {
            this.width = width;
            this.height = height;
            repaint(); // Redibujar el panel con la nueva imagen
        }
    }
}

