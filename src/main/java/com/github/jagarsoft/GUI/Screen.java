package com.github.jagarsoft.GUI;

import javax.swing.*;
import java.awt.*;

public interface Screen {
    // void drawPixel(int x, int y);
    void drawPixel(int x, int y, Color color);
    void createScreen(JFrame frame);
    void drawAttr(byte data);
    void repaint(Rectangle rect);
}
