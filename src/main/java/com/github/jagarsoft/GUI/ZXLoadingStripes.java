package com.github.jagarsoft.GUI;

import javax.swing.*;
import java.awt.*;

public class ZXLoadingStripes extends JPanel {
    private int stripeIndex = 0; // Índice para alternar colores
    private final Color[] colors = {Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA}; // Colores de las franjas

    public ZXLoadingStripes() {
        // Temporizador para actualizar las franjas periódicamente
        Timer timer = new Timer(100, e -> {
            stripeIndex = (stripeIndex + 1) % colors.length; // Cambiar al siguiente color
            repaint(); // Redibujar las franjas
        });
        timer.start(); // Iniciar el temporizador
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Calcular la altura de cada franja
        int stripeHeight = getHeight() / colors.length;

        // Dibujar las franjas
        for (int i = 0; i < colors.length; i++) {
            g.setColor(colors[(i + stripeIndex) % colors.length]); // Alternar colores dinámicamente
            g.fillRect(0, i * stripeHeight, getWidth(), stripeHeight);
        }
    }

    public static void main(String[] args) {
        // Crear el marco principal
        JFrame frame = new JFrame("ZX Spectrum Loading Stripes");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);

        // Añadir el panel de franjas
        ZXLoadingStripes stripesPanel = new ZXLoadingStripes();
        frame.add(stripesPanel);

        // Mostrar el marco
        frame.setVisible(true);
    }
}
