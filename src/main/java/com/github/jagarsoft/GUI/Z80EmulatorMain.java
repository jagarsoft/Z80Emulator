package com.github.jagarsoft.GUI;

import javax.swing.*;

public class Z80EmulatorMain {
		
    public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame mf = new MainFrame();

                mf.init("ZX Spectrum Emulator v0.1");
                mf.createMenuBar();
                mf.createPanels();
            }
        });
    }
}