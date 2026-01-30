package com.github.jagarsoft.ZuxApp.modules.disassembler;

import com.github.jagarsoft.Instruction;

import java.util.*;

public class InstructionList<T> {
    private TreeMap<Integer, T> treeMap = new TreeMap<>();

    public void add(int clave, T valor) {
        treeMap.put(clave, valor);
    }

    /*public T get(Integer clave) {
        return mapa.get(clave);
    }*/

    public T get(int index) { // Used by getValueAt from DisassemblyTableModel
        List<T> values = new ArrayList<>(treeMap.values());
        /*if (index >= 0 && index < values.size()) {
            return values.get(index);
        }*/
        for(T i : values) {
            if( ((Instruction)i).getIndex() >= index )
                return i; // TODO usar una lista indexada por index que devuelva addr. Actualizada al llamar a add()
        }
        System.out.println("getByAddress no fue null pero no encontro index "+index);
        return null;
    }

    public T getByAddress(int address) {
        T instruction = treeMap.get(address);
        assert(instruction != null);
        return instruction;
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