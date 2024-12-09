package com.github.jagarsoft.GUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FileOpen implements ActionListener {
    private JFrame frame;

    FileOpen(JFrame frame){
        this.frame = frame;
    }
        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(frame, "Open selected!");
        }
}
