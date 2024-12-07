package com.github.jagarsoft.GUI;

import javax.swing.*;
import java.awt.*;

class MainFrame {
	JFrame frame = new JFrame("Z80Emulator");
	
	// Create the main frame
	public void init(){
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
		
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
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
		
		// Set layout for the main frame
        frame.setLayout(new BorderLayout());
        frame.setJMenuBar(menuBar);
	}
	
	public void setVisible(boolean status) {
		frame.setVisible(status);
	}
}