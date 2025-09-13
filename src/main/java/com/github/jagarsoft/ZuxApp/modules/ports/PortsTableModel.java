package com.github.jagarsoft.ZuxApp.modules.ports;

import javax.swing.table.AbstractTableModel;
import java.util.*;

public class PortsTableModel extends AbstractTableModel {
    private final List<Integer> ports = new ArrayList<>();
    private final Map<Integer, Deque<Integer>> values = new HashMap<>();
    private final int HISTORY = 16;

    @Override
    public int getRowCount() {
        return ports.size();
    }

    @Override
    public int getColumnCount() {
        return 1 + 2*HISTORY;
    }

    @Override
    public String getColumnName(int col) {
        if (col == 0) return "Port";
        if (col <= HISTORY) return String.format("%X", col - 1); // 0..F en HEX
        return String.format("A%X", col - HISTORY - 1); // column ASCII A0..AF
    }

    @Override
    public Object getValueAt(int row, int col) {
        int port = ports.get(row);
        if (col == 0) return String.format("%02X", port);

        Deque<Integer> history = values.get(port);
        if (history == null) return "";

        int idx;
        if (col <= HISTORY) {
            // Bloque HEX
            idx = col - 1;
            return history.stream().skip(idx).findFirst()
                    .map(v -> String.format("%02X", v))
                    .orElse("");
        } else {
            // Bloque ASCII
            idx = col - HISTORY - 1;
            return history.stream().skip(idx).findFirst()
                    .map(v -> {
                        char c = (char) (v & 0xFF);
                        return (c >= 32 && c < 127) ? String.valueOf(c) : ".";
                    })
                    .orElse("");
        }
    }

    public void addPort(int port) {
        if (!ports.contains(port)) {
            ports.add(port);
            values.put(port, new ArrayDeque<>());
            fireTableDataChanged();
        }
    }

    public void addValue(int port, int value) {
        Deque<Integer> history = values.computeIfAbsent(port, p -> new ArrayDeque<>());
        if (history.size() >= HISTORY) {
            history.removeFirst(); // discard the oldest one
        }
        history.addLast(value);
        int row = ports.indexOf(port);
        fireTableRowsUpdated(row, row);
    }
}