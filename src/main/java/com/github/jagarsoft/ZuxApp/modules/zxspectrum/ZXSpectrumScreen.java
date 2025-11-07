package com.github.jagarsoft.ZuxApp.modules.zxspectrum;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * ZXSpectrumScreen
 *
 * - Bitmap 256x192 con mapping VRAM (interleaving real).
 * - Atributos 32x24 (INK/PAPER/BRIGHT/FLASH).
 * - Flash animado por Swing Timer.
 * - Borde de 48 px alrededor (pantalla completa 352x288).
 *
 * Métodos útiles:
 * - pokeBitmap(int addr, byte value)
 * - pokeAttribute(int index, byte value)
 * - pokeAttributeCell(int col, int row, byte value)
 * - setBorderColor(int a)  // cambia color del borde (bits 0-2)
 * - updateScreen()
 */
public class ZXSpectrumScreen extends JPanel {
    // pantalla principal
    public static final int SCREEN_W = 256;
    public static final int SCREEN_H = 192;
    public static final int BORDER = 48;
    public static final int FULL_W = SCREEN_W + BORDER * 2;   // 352
    public static final int FULL_H = SCREEN_H + BORDER * 2;   // 288

    // VRAM sizes
    public static final int VRAM_BYTES = 6144; // 256*192/8
    public static final int ATTR_BYTES = 768;  // 32*24

    // data
    private final byte[] bitmap = new byte[VRAM_BYTES];
    private final byte[] attributes = new byte[ATTR_BYTES];

    // rendered image (borde + pantalla)
    private final BufferedImage image = new BufferedImage(FULL_W, FULL_H, BufferedImage.TYPE_INT_RGB);

    // flash phase
    private boolean flashPhase = false;

    // border color index (0-7) and bright flag
    private int borderColorIndex = 0;
    //private boolean borderBright = false;

    // Timer period (ms) for FLASH toggle
    private static final int FLASH_PERIOD_MS = 500;

    public ZXSpectrumScreen() {
        setPreferredSize(new Dimension(FULL_W * 2, FULL_H * 2)); // escala por defecto 2x para verlo mejor
        clearAll();
        startFlashTimer();
        updateScreen();
    }

    // -------------------------------------------------------------------------
    // PUBLIC API
    // -------------------------------------------------------------------------

    /** Escribe un byte en la VRAM (bitmap). Dirección 0..6143 según el layout del Spectrum. */
    public void pokeBitmap(int addr, byte value) {
        if (addr < 0 || addr >= VRAM_BYTES) {
            System.err.println("Invalid address pokeBitmap(" + Integer.toHexString(addr) + ", " + Integer.toHexString(value) + ")");
            return;
        }
        bitmap[addr] = value;
        updateByte(addr);
        repaint();
    }

    public byte peekBitmap(int addr) {
        if (addr < 0 || addr >= VRAM_BYTES) {
            System.err.println("Invalid address peekBitmap(" + Integer.toHexString(addr) + ")");
            return 0;
        }
        return bitmap[addr];
    }

    /** Escribe un byte en la tabla de atributos (índice 0..767). */
    public void pokeAttribute(int index, byte value) {
        if (index < 0 || index >= ATTR_BYTES) {
            System.err.println("Invalid address pokeAttribute(" + Integer.toHexString(index) + ", " + Integer.toHexString(value) + ")");
            return;
        }
        attributes[index] = value;
        updateAttributeBlock(index);
        repaint();
    }

    public byte peekAttribute(int index) {
        if (index < 0 || index >= ATTR_BYTES) {
            System.err.println("Invalid address peekAttribute(" + Integer.toHexString(index) + ")");
            return 0;
        }
        return attributes[index];
    }

    /** Escribe un byte de atributo por coordenadas de celda (col 0..31, row 0..23). */
    public void pokeAttributeCell(int col, int row, byte value) {
        if (col < 0 || col >= 32 || row < 0 || row >= 24)
            return;
        int index = row * 32 + col;
        pokeAttribute(index, value);
    }

    /** Simula OUT (254), A: cambia color del borde. Se usan bits 0-2 => color index 0..7.
     *  Si ajustas, podrías usar otro bit para BRIGHT; aquí permitimos usar bit 3 para bright si quisieras.
     */
    public void setBorderColor(int a) {
        borderColorIndex = a & 0x07;
        // opcional: usar bit 3 para bright si lo deseas:
        //borderBright = ((a & 0x08) != 0);
        fillBorder(); // repinta borde en buffer
        repaint();
    }

    /** Fuerza actualización de toda la pantalla desde bitmap+attributes. */
    public void updateScreen() {
        // primero rellenar borde
        fillBorder();
        // luego todo el bitmap
        for (int addr = 0; addr < VRAM_BYTES; addr++) updateByte(addr);
        repaint();
    }

    /** Limpia bitmap y atributos, pone borde negro */
    public void clearAll() {
        Arrays.fill(bitmap, (byte) 0);
        Arrays.fill(attributes, (byte) 0);
        borderColorIndex = 0;
        //borderBright = false;
        fillBorder();
        // clear visible area to black
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(BORDER, BORDER, SCREEN_W, SCREEN_H);
        g.dispose();
    }

    // -------------------------------------------------------------------------
    // RENDER internals
    // -------------------------------------------------------------------------

    private void startFlashTimer() {
        new Timer(FLASH_PERIOD_MS, e -> {
            flashPhase = !flashPhase;
            // Only need to re-evaluate attribute-driven pixels (faster to update all)
            updateScreen();
        }).start();
    }

    private void fillBorder() {
        Color borderColor = ZXPalette.getColor(borderColorIndex, false); // borderBright
        Graphics2D g = image.createGraphics();
        g.setColor(borderColor);
        g.fillRect(0, 0, FULL_W, FULL_H);
        g.dispose();
    }

    /** Actualiza los 8 píxeles correspondientes a un byte de VRAM */
    private void updateByte(int addr) {
        // Cálculo del y real desde la dirección VRAM (mapping real del Spectrum)
        int y = addressToY(addr);
        int xByte = addr % 32;
        int x = xByte * 8;

        // determinar índice de atributo de la celda 8x8 que contiene (x,y)
        int charRow = y / 8;
        int charCol = x / 8;
        int attrIndex = charRow * 32 + charCol;
        byte attr = attributes[attrIndex];

        boolean bright = ((attr >> 6) & 0x01) == 1;
        boolean flash = ((attr >> 7) & 0x01) == 1;
        int paper = (attr >> 3) & 0x07;
        int ink = attr & 0x07;

        // aplicar FLASH (si está activo y fase lo indica), intercambia ink/paper
        if (flash && flashPhase) {
            int tmp = ink; ink = paper; paper = tmp;
        }

        Color cPaper = ZXPalette.getColor(paper, bright);
        Color cInk = ZXPalette.getColor(ink, bright);

        byte b = bitmap[addr];

        int baseX = x + BORDER;
        int baseY = y + BORDER;

        // poner los 8 píxeles
        for (int bit = 0; bit < 8; bit++) {
            boolean on = ((b >> (7 - bit)) & 1) == 1;
            int px = baseX + bit;
            int py = baseY;
            image.setRGB(px, py, (on ? cInk : cPaper).getRGB());
        }
    }

    /** Cuando cambia un atributo en la celda, hay que actualizar las 8 filas de esa celda */
    private void updateAttributeBlock(int attrIndex) {
        int charRow = attrIndex / 32;
        int charCol = attrIndex % 32;
        int xByte = charCol;
        // para cada una de las 8 líneas en esa celda, calcular la VRAM address base y actualizar el byte
        for (int rowInChar = 0; rowInChar < 8; rowInChar++) {
            int y = charRow * 8 + rowInChar;
            int addrBase = yToAddress(y);
            int addr = addrBase + xByte;
            // addr es la dirección del byte en VRAM correspondiente a (xByte,y)
            if (addr >= 0 && addr < VRAM_BYTES) updateByte(addr);
        }
    }

    // -------------------------------------------------------------------------
    // VRAM address <-> Y mapping (Spectrum interleaving)
    // yToAddress: línea y -> VRAM address base para la columna 0 (i.e., addr of byte at x=0)
    // addressToY: VRAM address -> y
    // -------------------------------------------------------------------------

    /** Convierte una línea Y (0..191) -> dirección base de VRAM para esa línea (multiplo de 32). */
    private static int yToAddress(int y) {
        // ((y & 0b00000111) << 8) | ((y & 0b00111000) << 2) | ((y & 0b11000000) << 5)
        return ((y & 0x07) << 8) | ((y & 0x38) << 2) | ((y & 0xC0) << 5); // TODO fix!!
    }

    /** Convierte una dirección VRAM -> Y */
    /*private static int addressToY(int addr) {
        // y0 = (addr & 0x0700) >> 8;
        // y1 = (addr & 0x00F0) >> 2;
        // y2 = (addr & 0x1800) >> 5;
        int y0 = (addr & 0x0700) >> 8;
        int y1 = (addr & 0x00F0) >> 2;
        int y2 = (addr & 0x1800) >> 5;
        return (y2 | y1 | y0) & 0xFF;
    }*/

    private static int addressToY(int addr) {
        int y;
        byte h, l;

        h = (byte) ((addr & 0xFF00) >> 8);
        l = (byte) (addr & 0x00FF);

        y = ((h & 0b0001_1000) << 3)
                | ((l & 0b1110_0000) >> 2)
                |  (h & 0b0000_0111);
        return y;
    }

    // -------------------------------------------------------------------------
    // Paint
    // -------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // dibujar la imagen escalada para que sea más visible; mantener aspecto
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
    }

    // -------------------------------------------------------------------------
    // ZX Palette (8 colors + bright)
    // -------------------------------------------------------------------------
    private static class ZXPalette {
        private static final Color[][] COLORS = {
                { new Color(0,0,0),         new Color(0,0,0)        }, // black (bright same)
                { new Color(0,0,205),       new Color(0,0,255)      }, // blue
                { new Color(205,0,0),       new Color(255,0,0)      }, // red
                { new Color(205,0,205),     new Color(255,0,255)    }, // magenta
                { new Color(0,205,0),       new Color(0,255,0)      }, // green
                { new Color(0,205,205),     new Color(0,255,255)    }, // cyan
                { new Color(205,205,0),     new Color(255,255,0)    }, // yellow
                { new Color(205,205,205),   new Color(255,255,255)  }  // white
        };

        static Color getColor(int index, boolean bright) {
            index = index & 0x07;
            return COLORS[index][bright ? 1 : 0];
        }
    }

    // -------------------------------------------------------------------------
    // Demo main: muestra el panel dentro de un JInternalFrame en un JDesktopPane
    // -------------------------------------------------------------------------
/*    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("ZX Spectrum: bitmap + atributos + border + flash");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);

            JDesktopPane desktop = new JDesktopPane();
            frame.setContentPane(desktop);

            // crear internal frame y añadir nuestro panel
            JInternalFrame internal = new JInternalFrame("Pantalla Spectrum", true, true, true, true);
            com.github.jagarsoft.example.zxspectrum.ZXSpectrumScreen screen = new com.github.jagarsoft.example.zxspectrum.ZXSpectrumScreen();
            internal.setContentPane(screen);
            internal.pack();
            internal.setVisible(true);
            internal.setLocation(20, 20);
            desktop.add(internal);

            frame.setVisible(true);

            // Demo: pintar patrón y atributos con flash/bright aleatorio
            new Thread(() -> {
                try {
                    // llenar bitmap con un patrón (cada byte alternado)
                    for (int addr = 0; addr < VRAM_BYTES; addr++) {
                        byte pattern = (byte) (((addr % 2) == 0) ? 0xAA : 0x55);
                        screen.pokeBitmap(addr, pattern);
                    }
                    // llenar atributos
                    for (int r = 0; r < 24; r++) {
                        for (int c = 0; c < 32; c++) {
                            int idx = r * 32 + c;
                            // crear varios atributos para que se aprecie flash/bright
                            int ink = (c + r) % 8;
                            int paper = (7 - ink);
                            boolean bright = ((r + c) % 2) == 0;
                            boolean flash = ((r + c) % 5) == 0;
                            byte attr = (byte) ((flash ? 0x80 : 0) | (bright ? 0x40 : 0) | ((paper & 0x07) << 3) | (ink & 0x07));
                            screen.pokeAttribute(idx, attr);
                        }
                    }
                    // cambiar borde alternando colores
                    int idx = 0;
                    while (true) {
                        screen.out254(idx & 0x07);
                        idx++;
                        Thread.sleep(300);
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
    }
 */

    public void loadSCRFile(File file) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {
            if (in.read(bitmap) != 6144) throw new IOException("Error leyendo bitmap");
            if (in.read(attributes) != 768) throw new IOException("Error leyendo atributos");
        }
        updateScreen();
    }
}
