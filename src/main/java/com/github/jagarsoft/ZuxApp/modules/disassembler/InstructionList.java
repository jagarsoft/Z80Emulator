package com.github.jagarsoft.ZuxApp.modules.disassembler;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

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

    /*public List<Integer> obtenerClavesOrdenadas() {
        return new ArrayList<>(mapa.keySet());
    }*/

    public void clear() {
        treeMap.clear();
    }
}