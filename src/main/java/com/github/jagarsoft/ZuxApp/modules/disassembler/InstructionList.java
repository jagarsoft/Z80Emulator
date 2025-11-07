package com.github.jagarsoft.ZuxApp.modules.disassembler;

import java.util.*;

public class InstructionList<T> {
    private TreeMap<Integer, T> treeMap = new TreeMap<>();

    public void add(Integer clave, T valor) {
        treeMap.put(clave, valor);
    }

    /*public T get(Integer clave) {
        return mapa.get(clave);
    }*/

    public T get(int index) { // Used by getValueAt from DisassemblyTableModel
        List<T> values = new ArrayList<>(treeMap.values());
        if (index >= 0 && index < values.size()) {
            return values.get(index);
        }
        return null;
    }

    public T getByAddress(Integer address) {
        return treeMap.get(address);
    }

    public List<T> getInstructions() {
        return new ArrayList<>(treeMap.values());
    }

    public int size() {
        return treeMap.size();
    }

    public void clear() {
        treeMap.clear();
    }

    public List<Map.Entry<Integer, T>> search(String query) {
        String q = query.toLowerCase(Locale.ROOT);
        List<Map.Entry<Integer, T>> result = new ArrayList<>();
        for (Map.Entry<Integer, T> entry : treeMap.entrySet()) {
            //String hexValue = String.format("%04X", entry.getKey());
            if (/*hexValue.toLowerCase(Locale.ROOT).contains(q) ||*/
                    entry.getValue().toString().toLowerCase(Locale.ROOT).contains(q)) {
                result.add(entry);
            }
        }
        return result;
    }
}