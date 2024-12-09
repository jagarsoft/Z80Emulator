package com.github.jagarsoft.GUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FileExit implements ActionListener {
    private JFrame frame;

    FileExit(JFrame frame){
        this.frame = frame;
    }
        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0); // Cierra la aplicación
        }
}
