package com.github.jagarsoft.Zux;

import com.github.jagarsoft.Computer;
import com.github.jagarsoft.GUI.Screen;
import com.github.jagarsoft.Z80;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class ZuxDebuggingRunner
       extends SwingWorker<Void, Rectangle>
       implements Observable {

    Screen screen;
    private Z80 cpu;
    volatile String order = null;
    LinkedList<String> messages = new LinkedList<String>();

    ZuxDebuggingRunner(Computer zux, Screen screen) {
        this.cpu = zux.getCPU();
        this.screen = screen;
    }

    @Override
    protected Void doInBackground() {
        while (true) {
            order = messages.poll();

            if (order != null) {

            }
        }
    }

    @Override
    protected void process(List<Rectangle> chunks) {
        for (Rectangle rect : chunks) {
            screen.repaint(rect);
        }
    }

    @Override
    synchronized public void notify(String order) {
        messages.addLast(order);
    }
}
