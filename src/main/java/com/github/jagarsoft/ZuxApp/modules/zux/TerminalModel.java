package com.github.jagarsoft.ZuxApp.modules.zux;

import java.util.Arrays;

import static java.lang.Character.isDigit;

public class TerminalModel {
	private final int ESC = 0x1B;
    private final int TAB_SIZE = 8;

    private final int rows;
    private final int cols;

    private final char[][] chars;

    private int firstRow = 0;

    private int cursorRow = 0;
    private int cursorCol = 0;

	private boolean isEscape = false;
    private int inEscape = 0;
    private int paramESC1 = 0;
    private int paramESC2 = 0;
    private int paramESC3 = 0;
    private int storedCursorCol = 0;
    private int storedCursorRow = 0;

    private TerminalPanel panel;

    private void debugPrint(String dbgMsg) {
        System.out.printf(dbgMsg);
    }

    public TerminalModel(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;

        chars = new char[rows][cols];

        clear();
    }

    public void setPanel(TerminalPanel panel) {
        this.panel = panel;
    }

    public void test() {
        for (int r = 0; r < rows; r++) {
            print(String.format("%2d:", r));
            print("0000000");
            print("1111111111");
            print("2222222222");
            print("3333333333");
            print("4444444444");
            print("5555555555");
            print("6666666666");
            print("7777777777\n");
        }

        firstRow = 0;
        cursorRow = 0;
        cursorCol = 0;
    }

    public void clear() {
        for (int r = 0; r < rows; r++) {
            Arrays.fill(chars[r], ' ');
        }

        firstRow = 0;
        cursorRow = 0;
        cursorCol = 0;
    }

    public int getRows() { return rows; }

    public int getCols() {
        return cols;
    }

    public int getCursorRow() {
        return cursorRow;
    }

    public int getCursorCol() {
        return cursorCol;
    }

    public void setCursor(int x, int y) {
        cursorCol = x;
        cursorRow = y;
    }

    public void cursorLeft() {
        cursorCol--;
        if(cursorCol < 0) {
            if(cursorRow > 0) {
                cursorCol = cols - 1;
                cursorUp();
            } else
                cursorCol = 0;

        }
debugPrint(String.format("cursorRow=%d cursorCol=%d\r", cursorRow, cursorCol));
    }

    public void cursorUp() {
        cursorRow--;
        if(cursorRow < 0) {
            cursorRow = 0;
        }
debugPrint(String.format("cursorRow=%d cursorCol=%d\r", cursorRow, cursorCol));
    }

    public void cursorDown() {
        cursorRow++;
        if(cursorRow > rows - 1) {
            cursorRow = rows - 1;
        }
debugPrint(String.format("cursorRow=%d cursorCol=%d\r", cursorRow, cursorCol));
    }

    public void cursorRight() {
        cursorCol++;
        if (cursorCol > cols - 1) {
            //newLine();
            cursorCol = 0;
            cursorDown();
        }
debugPrint(String.format("cursorRow=%d cursorCol=%d\r", cursorRow, cursorCol));
    }

    public char getChar(int logicalRow, int col) {

        int physicalRow =
                (firstRow + logicalRow) % rows;

        return chars[physicalRow][col];
    }

    public void putChar(char c) {

        switch (c) {

            case '\n' -> {
                newLine();
                return;
                }
                case '\r' -> {
                cursorCol = 0;
                return;
        }
                case '\t' -> {
                    cursorCol += (cursorCol+TAB_SIZE) % TAB_SIZE;
                    return;
                }
                case '\b' -> {
                    cursorLeft();
                    return;
                }
                case 7 -> { // '\a'
                    //System.out.printf("%c", c);
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    return;
                }
            }

            if( isEscape || c == ESC) {
                isEscape = escape(c);
                return;
            }

        int physicalRow =
                (firstRow + cursorRow) % rows;

        chars[physicalRow][cursorCol] = c;

        cursorRight();
    }

    public void print(String text) {
        for (char c : text.toCharArray()) {
            putChar(c);
            Thread.yield();
        }
    }

    public void newLine() {

        cursorCol = 0;

        if (cursorRow < rows - 1) {
            cursorDown();
            return;
        }

        scroll();
    }

    private void scroll() {

        firstRow = (firstRow + 1) % rows;

        int bottomPhysicalRow =
                (firstRow + rows - 1) % rows;

        Arrays.fill(chars[bottomPhysicalRow], ' ');
    }

        /*
                             c
                             ESC  [     isDigit         ;        Letter
                inEscape 0    1
                         1        2
                         2             paramESC1        3        action(0)
                         3             paramESC2        4        action(0)
                         4             paramESC3   (5)abort(0)
         */
        /*
            https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797
            https://invisible-island.net/xterm/ctlseqs/ctlseqs.html
         */
        private boolean escape(char c) {
            if( inEscape == 0 && c == ESC) {
                inEscape++; // start sequence
                return true;
            }
            if( inEscape == 1 ) {
                if (c == '[') {
                    inEscape++;
                    return true; // still in sequence
                } else {
                    inEscape = 0; // abort sequence
                    return false;
                }
            }
            if( c == ';' ) {
                inEscape++; // next param
                if (inEscape >= 5) {
                    inEscape = 0; // abort sequence
                    paramESC1 = paramESC2 = paramESC3 = 0;
                    return false;
                }
                return true;
            }
            if( isDigit(c)) { // read params by turn
                if( inEscape == 2 ) {
                    paramESC1 = paramESC1*10 + (c - '0');
                    return true;
                }
                if( inEscape == 3 ) {
                    paramESC2 = paramESC2*10 + (c - '0');
                    return true;
                }
                if( inEscape == 4 ) {
                    paramESC3 = paramESC3 * 10 + (c - '0');
                    return true;
                }
            }
            switch (c) {
                case 'A' -> {
                    do
                        cursorUp();
                    while(--paramESC1 > 0);
                }
                case 'B' -> {
                    do
                        cursorDown();
                    while(--paramESC1 > 0);
                }
                case 'C' -> {
                    do
                        cursorRight();
                    while(--paramESC1 > 0);
                }
                case 'D' -> {
                    do
                        cursorLeft();
                    while(--paramESC1 > 0);
                }
                case 'f', 'H' -> { // set cursor position
                    setCursor(paramESC2,  paramESC1);
                }
                case 'J' -> { // clear screen
                    if(paramESC1 == 2) {
                        clear();
                    }
                }
                case 'K' -> { // clear until the end of line
                    int old_cursorCol =  cursorCol;
                    int old_cursorRow =  cursorRow;
                    inEscape=0;
                    for(int x = cursorCol; x < getCols(); x++) {
                        putChar(' ');
                        Thread.yield();
                    }
                    setCursor(old_cursorCol, old_cursorRow);
                }
                case 'p' -> {

                }
                case 's' -> { // strore current cursor position
                    storedCursorCol = cursorCol;
                    storedCursorRow = cursorRow;
                }
                case 'u' -> { // restore old cursor position
                    setCursor(storedCursorCol, storedCursorRow);
                }
                case 'm' -> {

                }
                case 'q' -> { // DECTCUSR
                    switch (paramESC1) {
                        case 0, 1 -> { // Default cursor or Blinking block
                            panel.setCursorStyle(TerminalPanel.CURSOR_STYLE.BLOCK);
                            panel.setCursorBlink(true);
                            panel.setCursorEnable(true);
                        }
                        case 2 -> { // Steady block
                            panel.setCursorStyle(TerminalPanel.CURSOR_STYLE.BLOCK);
                            panel.setCursorBlink(false);
                            panel.setCursorEnable(true);
                        }
                        case 3 -> { // Blinking underline
                            panel.setCursorStyle(TerminalPanel.CURSOR_STYLE.UNDERLINE);
                            panel.setCursorBlink(true);
                            panel.setCursorEnable(true);
                        }
                        case 4 -> { // Steady underline
                            panel.setCursorStyle(TerminalPanel.CURSOR_STYLE.UNDERLINE);
                            panel.setCursorBlink(false);
                            panel.setCursorEnable(true);
                        }
                        case 5 -> { // Blinking bar
                            panel.setCursorStyle(TerminalPanel.CURSOR_STYLE.VERTICAL);
                            panel.setCursorBlink(true);
                            panel.setCursorEnable(true);
                        }
                        case 6 -> { // Steady bar
                            panel.setCursorStyle(TerminalPanel.CURSOR_STYLE.VERTICAL);
                            panel.setCursorBlink(false);
                            panel.setCursorEnable(true);
                        }
                    }
                }
                case 'l' -> { // hide cursor
                    if(paramESC1 == 25) {
                        panel.setCursorEnable(false);
                    }
                }
                case 'h' -> { // show cursor
                    if(paramESC1 == 25) {
                        panel.setCursorEnable(true);
                    }
                }
            }
            inEscape = 0;
            paramESC1 = paramESC2 = paramESC3 = 0;
            return false;
        }
    }