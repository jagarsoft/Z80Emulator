package com.github.jagarsoft.GUI;

import javax.swing.*;
import java.awt.*;

class MainFrame {
    
	JFrame frame = new JFrame();
    Screen screen;
    
    MainFrame(Screen screen) {
        this.screen = screen;
    }        

	// Create the main frame
	public void init(String title){
        final double xM = 0.1875;
        final double yM = 0.3334;
        int width = 256;
        int height = 192;
        int xMargin = (int)(width * xM);
        int yMargin = (int)(height * yM);
        frame.setTitle(title);
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
/*
 * This method must be refactorings to a specific class ZXSpectrumScreen (TODO)
 * Need image object
 */
    public void createPanels() {
        screen.createScreen(frame);
    }   
}
