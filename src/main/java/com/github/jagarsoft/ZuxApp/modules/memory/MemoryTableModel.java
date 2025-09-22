package com.github.jagarsoft.ZuxApp.modules.memory;

import com.github.jagarsoft.Computer;

import javax.swing.table.AbstractTableModel;

public class MemoryTableModel extends AbstractTableModel {
    private Computer computer;
    private int startAddress;
    private int length;

    @Override
    public int getRowCount() {
        //return (length > 0 && length < 16) ? 1 : length / 16;
        return (length + 15) / 16;
    }

    @Override
    public int getColumnCount() {
        return 3; // Dirección, Hex, ASCII
    }

    @Override
    public Object getValueAt(int row, int col) {
        int offset = row * 16;
        return switch (col) {
            case 0 -> String.format("%04X", startAddress + offset);
            case 1 -> {
                StringBuilder hex = new StringBuilder();
                for (int i = 0; i < 16; i++) {
                    if (startAddress + offset + i < this.length)
                        hex.append(String.format("%02X ", computer.peek(startAddress + offset + i)));
                    else
                        hex.append("   ");
                }
                yield hex.toString().trim();
            }
            case 2 -> {
                StringBuilder ascii = new StringBuilder();
                for (int i = 0; i < 16; i++) {
                    int index = startAddress + offset + i;
                    if (index < this.length) {
                        byte b = computer.peek(index);
                        ascii.append((b >= 32 && b <= 126) ? (char) b : '.');
                    } else {
                        ascii.append(' ');
                    }
                }
                yield ascii.toString();
            }
            default -> "";
        };
    }

    @Override
    public String getColumnName(int col) {
        return switch (col) {
            case 0 -> "Addr";
            case 1 -> "Hex";
            case 2 -> "ASCII";
            default -> "";
        };
    }

    public void dump(Computer computer, int org, int size) {
        this.computer = computer;
        this.startAddress = org;
        this.length = size;
        this.fireTableDataChanged();
    }
}
