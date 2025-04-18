package com.github.jagarsoft.GUI;

import javax.swing.*;
import java.awt.*;

public interface Screen {
    int WIDTH = 256;
    int HEIGHT = 192;
    // void drawPixel(int x, int y);
    void drawPixel(int x, int y, Color color);
    void drawPixel(int x, int y, int color);
    void createScreen(JFrame frame);
    //void drawAttr(byte data);
    void repaint(Rectangle rect);
}
