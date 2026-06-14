package com.github.jagarsoft.ZuxApp.modules.zux;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;

public class TerminalPanel extends JPanel {
    public static final int SCREEN_W = 256;
    public static final int SCREEN_H = 192;
    public static final int BORDER = 48;
    public static final int FULL_W = SCREEN_W + BORDER * 2;   // 352
    public static final int FULL_H = SCREEN_H + BORDER * 2;   // 288

    public enum CURSOR_STYLE {
        UNDERLINE,
        BLOCK,
        VERTICAL
    }

    public enum PHOSPHOR_COLOR {
        GREEN,
        AMBER,
        WHITE
    }

    private final TerminalModel model;

    private Font terminalFont;

    private int cellWidth = 9;
    private int cellHeight = cellWidth*2;
    private int baseline = 0;

    private int offsetX = BORDER/2;
    private int offsetY = BORDER/2;

    private boolean isCursorEnabled = true;
    private boolean isCursorVisible = true;
    private boolean isCursorBlinking = true;
    private boolean isGrid = false;

    private int cursorWidth = 4;
    private int cursorHeight = 4;
    private CURSOR_STYLE currentCursorStyle = CURSOR_STYLE.BLOCK;
    //private CURSOR_STYLE currentCursorStyle = CURSOR_STYLE.UNDERLINE;
    //private CURSOR_STYLE currentCursorStyle = CURSOR_STYLE.VERTICAL;

    private final Timer cursorTimer;
    private final Timer repaintTimer;

    private final Color backgroundColor = new Color(5, 11, 20);
    // White
    private Color phosphorWhite = new Color(168, 216, 255); // Azul CRT clásico
    // Amber
    private Color phosphorAmber = new Color(0xFF, 0xA5, 0); // fósforo ambar
    private Color glowAmber = new Color(120, 190, 255, 40);
    // Green
    private Color phosphorGreen = new Color(0,255,120);
    private Color glowGreen = new Color(120,255,120,60);

    private Color phosphorColor;
    private Color glowColor;

    private float ghostingAlpha = 0.15f;
    private boolean showScanlines = true;

    // Cut&Paste feature
    private boolean selecting = false;

    private int selStartRow = -1;
    private int selStartCol = -1;

    private int selEndRow = -1;
    private int selEndCol = -1;
    //private final Color selectedText = new Color(120,150,255,120);
    private final Color selectedText = new Color(50, 100, 200,120);

    public TerminalPanel(TerminalModel model) {

        this.model = model;
        model.setPanel(this);

        setBackground(backgroundColor);

        setPhosphorColor(PHOSPHOR_COLOR.AMBER);

        cursorTimer =
                new Timer(500, e -> {
                        isCursorVisible = !isCursorVisible;
                    repaint();
                        /*repaint(colToPixel(model.getCursorCol()), rowToPixel(model.getCursorRow()),
                                cursorWidth, cursorHeight);*/ // TODO repintar solo el propio cursor
                });
        cursorTimer.start();
		setCursorStyle(currentCursorStyle);
        setCursorEnable(isCursorEnabled);
        setCursorBlink(isCursorBlinking);

        // Repaint ~50 FPS
        repaintTimer = new Timer(20, e -> {
            repaint();
        });
        repaintTimer.start();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                computeFontMetrics4();
        }});

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {

                requestFocusInWindow();

                selStartRow = pixelToRow(e.getY());
                selStartCol = pixelToCol(e.getX());

                selEndRow = selStartRow;
                selEndCol = selStartCol;

                selecting = true;

                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {

                selecting = false;

                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {

                selEndRow = pixelToRow(e.getY());
                selEndCol = pixelToCol(e.getX());

                repaint();
            }
        });

        InputMap im = getInputMap(WHEN_FOCUSED);

        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), "copy");

        am.put("copy",new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copySelection();
            }
        });

        setFocusable(true);

        //computeFontMetrics4();

            /*System.out.println("cellHeight = " + cellHeight);
            System.out.println("cellWidth = " + cellWidth);
            System.out.println("panel height = " + getHeight());
            System.out.println("panel width = " + getWidth());
            System.out.println("computed rows = " + (getHeight() / cellHeight));
            System.out.println("computed cols = " + (getWidth() / cellWidth));
            System.out.println("offsetX = " + offsetX);
            System.out.println("offsetY = " + offsetY);
            System.out.println("baseline = " + baseline);*/
    }

    /*
     * Start Selection feature
     */

        private int rowToPixel(int row) {
            return offsetY + (row * cellHeight);// + baseline;
        }

        private int colToPixel(int col) {
            return offsetX + (col * cellWidth);
        }


    private int pixelToRow(int y) {

        int row = (y-offsetY) / cellHeight;

        return Math.max(0, Math.min( model.getRows() - 1,row));
        //return Math.clamp(model.getRows() - 1, 0, row);
    }

    private int pixelToCol(int x) {

        int col = (x-offsetX) / cellWidth;

        return Math.max(0, Math.min(model.getCols() - 1, col));
        //return Math.clamp(model.getCols() - 1, 0,col);
    }

    private int position(int row, int col) {
        return row * model.getCols() + col;
    }

    private boolean isSelected(int row, int col) {

        if (selStartRow < 0) {
            return false;
        }

            int start = position(selStartRow, selStartCol);
        int end = position(selEndRow, selEndCol);

        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }

        int current = position(row, col);

        return current >= start
                && current <= end;
    }

    public String getSelectedText() {

        if (selStartRow < 0) {
            return "";
        }

        int start = position(selStartRow, selStartCol);
        int end   = position(selEndRow, selEndCol);

        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }

        StringBuilder sb = new StringBuilder();

        for (int p = start; p <= end; p++) {

            int row = p / model.getCols();
            int col = p % model.getCols();

            sb.append(model.getChar(row, col));

            if (col == model.getCols() - 1) {
                sb.append('\n');
            }
        }

        return sb.toString();
    }

    private void copySelection() {

        String text = getSelectedText();

        StringSelection data = new StringSelection(text);

        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(data, null);
    }

    /*
     * End Selection feature
     */

    public void setGrid(boolean status) {
        isGrid = status;
    }

    /*
     * status = true -> Shown
     * status = false -> Hide
     */
    public void setCursorEnable(boolean status) {
            isCursorEnabled = status;
            setBlinkTimer(isCursorEnabled && isCursorBlinking);
            isCursorVisible = status;
        }

        /*
         * status = true -> Blink
         * status = false -> Steady
         */
        public void setCursorBlink(boolean status) {
            isCursorBlinking = status;
            setBlinkTimer(isCursorEnabled && isCursorBlinking);
        }

        private void setBlinkTimer(boolean status) {
            if(status) {
            //cursorTimer.restart();
            if(!cursorTimer.isRunning())
                cursorTimer.start();
        } else
        if(cursorTimer.isRunning())
            cursorTimer.stop();
    }

    @Override
    public Dimension getPreferredSize() {
        // cellWidth=9, cellHeight=18 por defecto → 768 x 498
        return new Dimension(
                cellWidth * model.getCols() + BORDER*2,
                cellHeight * model.getRows() + BORDER/2
        );
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        // Mejor calidad visual
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (terminalFont == null && getWidth() > 0 && getHeight() > 0) {
            computeFontMetrics4();
        }

        // Ghosting effect
        g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, ghostingAlpha));
        g2.setColor(backgroundColor);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setComposite(AlphaComposite.SrcOver);

        g2.setFont(terminalFont);

        // Grid
        if( isGrid ) {
            for (int r = 0; r <= model.getRows(); r++)
                for (int c = 0; c <= model.getCols(); c++) {

                    int x = offsetX + c * cellWidth;
                    int y = offsetY + r * cellHeight;

                    g2.setColor(Color.RED);
                        g2.drawLine(0, y, getWidth(), y); // horizontal
                        g2.drawLine(x, 0, x, getHeight());// vertical
                }
        }

        /*
         * Dump buffer
         */
        for (int row = 0; row < model.getRows(); row++) {

            int y = offsetY + (row * cellHeight) + baseline;

            for (int col = 0; col < model.getCols(); col++) {

                char c = model.getChar(row, col);

                    /*if (c == ' ') {
                        continue;
                    }*/

                int x = offsetX + (col * cellWidth);

                // Selection
                if (isSelected(row, col)) {

                    g2.setColor(selectedText);
                        g2.fillRect(offsetX + col * cellWidth,
                            offsetY + row * cellHeight,
                            cellWidth,
                            cellHeight);
                }

                // Glow effect
                g2.setColor(glowColor);
                g2.drawString(
                        String.valueOf(c),
                        x + 1,
                        y + 1);

                // Main color
                g2.setColor(phosphorColor);
                g2.drawString(
                        String.valueOf(c),
                        x,
                        y);
            }
        }

        drawCursor(g2);

        // Scanlines
        if (showScanlines) {
            g2.setColor(new Color(0, 0, 0, 25));
            for (int y = 0; y < getHeight(); y += 4) {
                g2.drawLine(0, y, getWidth(), y);
            }
        }

        g2.dispose();
    }

    public void Hmas() {
        cellHeight++;
System.out.println("cellHeight = " + cellHeight);
System.out.println("cellWidth = " + cellWidth);
        setSize(cellWidth*model.getCols()+BORDER,
                cellHeight*model.getRows()+BORDER);
        this.pack();
    }

    public void Hmenos() {
        cellHeight--;
System.out.println("cellHeight = " + cellHeight);
System.out.println("cellWidth = " + cellWidth);
            /*setSize(cellWidth*model.getCols()+BORDER,
                    cellHeight*model.getRows()+BORDER);*/
        setPreferredSize(
                new Dimension(
                        cellWidth * model.getCols() + BORDER,
                        cellHeight * model.getRows() + BORDER
                )
        );
        this.pack();
    }

    public void Wmas() {
        cellWidth++;
System.out.println("cellHeight = " + cellHeight);
System.out.println("cellWidth = " + cellWidth);
            /*setSize(cellWidth*model.getCols()+BORDER,
                    cellHeight*model.getRows()+BORDER);*/
        setPreferredSize(
                new Dimension(
                        cellWidth * model.getCols() + BORDER,
                        cellHeight * model.getRows() + BORDER
                )
        );
        this.pack();
    }

    public void Wmenos() {
        cellWidth--;
System.out.println("cellHeight = " + cellHeight);
System.out.println("cellWidth = " + cellWidth);
            /*setSize(cellWidth*model.getCols()+BORDER,
                    cellHeight*model.getRows()+BORDER);*/
        setPreferredSize(
                new Dimension(
                        cellWidth * model.getCols() + BORDER,
                        cellHeight * model.getRows() + BORDER
                )
        );
        this.pack();
    }

    private void pack() {
        Window window = SwingUtilities.getWindowAncestor(this);

        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
                /*Insets insets = frame.getInsets();
                frame.setSize(this.getWidth() + insets.left + insets.right,
                        this.getHeight() + insets.top + insets.bottom + BORDER/2*/ /*- frame.getJMenuBar().getHeight()*/
            //);
            frame.pack();

        } else {
            // El panel aún no está en un JFrame (o está en un JDialog, o no está en pantalla)
        }
    }

    private void computeFontMetrics(Graphics2D g2) {

        int targetHeight = //16;
                Math.max(
                        8,
                        getHeight() /
                                model.getRows());

        terminalFont =
                new Font(
                        "Monospaced",
                        Font.BOLD,
                        targetHeight);

        FontMetrics fm =
                g2.getFontMetrics(
                        terminalFont);

        cellWidth = 12;
        //fm.charWidth('W');

        cellHeight = 26;
        fm.getHeight();

        baseline =
                fm.getAscent();

/*System.out.println("cellHeight = " + cellHeight);
System.out.println("cellWidth = " + cellWidth);
System.out.println("panel height = " + getHeight());
System.out.println("panel width = " + getWidth());
System.out.println("computed rows = " + (getHeight() / cellHeight));
System.out.println("computed cols = " + (getWidth() / cellWidth));*/
    }

    private void computeFontMetrics3(Graphics2D g2d) {
        //Graphics2D g2d = (Graphics2D) g;

        // 1. Activar renderizado de alta calidad para texto (Antialiasing)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 2. Calcular el espacio disponible real en el componente
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        // 3. Calcular el tamaño máximo teórico de cada celda de carácter
        double cellWidth = (double) panelWidth / model.getCols();
        double cellHeight = (double) panelHeight / model.getRows();

        // Para que la fuente no se deforme y mantenga una proporción fija (ej: 1:2 o similar),
        // calculamos el font size limitándolo por el lado que antes se sature.
        // En una fuente monoespaciada estándar, la altura suele ser el doble del ancho.
        int fontSize = (int) Math.min(cellWidth * 2.0, cellHeight);

        if (fontSize < 1) fontSize = 1; // Evitar tamaños inválidos

        // 4. Instanciar la fuente monospaced con el tamaño dinámico
        // Usamos "Monospaced" para que Java busque la fuente de ancho fijo nativa del sistema (Courier, JetBrains, etc.)
        Font terminalFont = new Font(Font.MONOSPACED, Font.PLAIN, fontSize);
        g2d.setFont(terminalFont);
        g2d.setColor(Color.GREEN); // Verde fósforo clásico de terminal

        // 5. Obtener las métricas reales de la fuente calculada por el sistema
        FontMetrics metrics = g2d.getFontMetrics(terminalFont);
        int actualCharWidth = metrics.charWidth(' ');
        int actualCharHeight = metrics.getHeight();

        this.cellWidth = actualCharWidth;
        this.cellHeight = actualCharHeight;

        // Centrar la rejilla del terminal en el panel si sobra espacio por redondeos
        offsetX = (panelWidth - (actualCharWidth * model.getCols())) / 2;
        offsetY = (panelHeight - (actualCharHeight * model.getRows())) / 2;

        if (offsetX < 0) {
            offsetX = BORDER / 2;
        }
        if (offsetY < 0) {
            offsetY = BORDER / 4;
        }

        // 6. Dibujar la rejilla de caracteres
        baseline = metrics.getAscent(); // Línea de base interna para dibujar el texto correctamente

/*System.out.println("cellHeight = " + cellHeight);
System.out.println("cellWidth = " + cellWidth);
System.out.println("panel height = " + getHeight());
System.out.println("panel width = " + getWidth());
System.out.println("computed rows = " + (getHeight() / cellHeight));
System.out.println("computed cols = " + (getWidth() / cellWidth));
System.out.println("offsetX = " + offsetX);
System.out.println("offsetY = " + offsetY);
System.out.println("baseline = " + baseline);*/
    }

    public void computeFontMetrics4() {
        // Paso 1 — Determinar el área útil real
        Insets insets = getInsets(); // del JPanel o JFrame
        int availW = getWidth()  - insets.left - insets.right;
        int availH = getHeight() - insets.top  - insets.bottom;

        //Paso 2 — Calcular dimensiones de celda objetivo
        float targetCellW = (float) availW / model.getCols();
        float targetCellH = (float) availH / model.getRows();

        //Paso 3 — Encontrar el font size que produce esas dimensiones
        // 1. Mide el carácter de referencia a un tamaño conocido
        Font refFont = new Font("Courier New", Font.PLAIN, 12);
        //Font refFont = new Font("Monospaced", Font.BOLD, 20);
        //Font refFont = new Font("Monospaced", Font.PLAIN, 12);
        FontMetrics refMetrics = getFontMetrics(refFont);

        float refCharW = refMetrics.charWidth('W');   // fuente monoespaciada: todos iguales
        float refCharH = refMetrics.getHeight();      // ascent + descent + leading

        // 2. Calcula el factor de escala en cada eje
        float scaleX = targetCellW / refCharW;
        float scaleY = targetCellH / refCharH;

        // 3. Usa el mínimo para no salir de los límites
        float scale = Math.min(scaleX, scaleY);

        // 4. Nuevo tamaño de fuente
        float newFontSize = 12f * scale;
        terminalFont = refFont.deriveFont(newFontSize);

        //Paso 4 — Recalcular las celdas reales a partir del font definitivo
        FontMetrics fm = getFontMetrics(terminalFont);
        cellWidth = fm.charWidth('W');
        cellHeight = fm.getHeight();
            baseline = fm.getAscent(); // Línea base imaginaria sobre la que se asientan los caracteres

        //Paso 5 — Centrar la grilla en el componente (el borde que describes)
        int totalGridW = cellWidth * model.getCols();
        int totalGridH = cellHeight * model.getRows();
        offsetX = (availW - totalGridW) / 2;
        offsetY = (availH - totalGridH) / 2;

        if (offsetX < 0) {
            offsetX = BORDER / 2;
        }
        if (offsetY < 0) {
            offsetY = BORDER / 2;
        }

            // 6. Redimensiona el cursor de acuerdo al nuevo tamaño de la fuente
        setCursorDimensions(currentCursorStyle);

System.out.println("cellHeight = " + cellHeight);
System.out.println("cellWidth = " + cellWidth);
System.out.println("panel height = " + availH);
System.out.println("panel width = " + availW);
System.out.println("computed rows = " + (availH / cellHeight));
System.out.println("computed cols = " + (availW / cellWidth));
System.out.println("offsetX = " + offsetX);
System.out.println("offsetY = " + offsetY);
System.out.println("baseline = " + baseline);
System.out.println();
    }

    private void drawCursor(Graphics2D g2) {

        if (!isCursorVisible) {
            return;
        }

        int x = offsetX + (model.getCursorCol() * cellWidth);
        int y = offsetY + (model.getCursorRow() * cellHeight);

        //g2.setColor(glowColor);
        //g2.fillRect(x + 1, y + cellHeight - (cursorHeight-1), cursorWidth, cursorHeight+1);
        g2.fillRect(x + 1, y + cellHeight-cursorHeight+1, cursorWidth, cursorHeight);

        g2.setColor(phosphorColor);
        //g2.fillRect(x, y + cellHeight - (cursorHeight), cursorWidth, cursorHeight+1);
        //g2.fillRect(x, y + cellHeight - (cursorHeight) + 1, cursorWidth, cursorHeight-1);
        g2.fillRect(x, y + cellHeight-cursorHeight, cursorWidth, cursorHeight);
        /*g2.fillRect(
                x,
                y + cellHeight - 3,
                cellWidth,
                2);*/
    }

    public void setCursorStyle(CURSOR_STYLE style) {
        currentCursorStyle = style;
        setCursorDimensions(style);
    }

    public void setPhosphorColor(PHOSPHOR_COLOR color) {
        switch (color) {
            case GREEN -> {
                phosphorColor = phosphorGreen;
                glowColor = glowGreen;
            }
            case AMBER -> {
                phosphorColor = phosphorAmber;
                glowColor = glowAmber;
            }
            case WHITE -> {
                phosphorColor = phosphorWhite;
                glowColor = glowAmber;
            }
        }
    }

    private void setCursorDimensions(CURSOR_STYLE style) {
        switch (style) {
            case UNDERLINE -> {
                cursorWidth = cellWidth;
                cursorHeight = 3;
            }
            case BLOCK ->  {
                cursorWidth = cellWidth;
                cursorHeight = cellHeight;
            }
            case VERTICAL -> {
                cursorWidth = 1;
                cursorHeight = cellHeight;
            }
        }
    }
}