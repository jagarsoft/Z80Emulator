package com.github.jagarsoft.ZuxApp.modules.disassembler;

import com.github.jagarsoft.Instruction;
import com.sun.source.tree.SynchronizedTree;

import java.util.*;

public class InstructionList<T> {
    private final TreeMap<Integer, T> treeMap_async = new TreeMap<>();
    Map<Integer, T> treeMap = Collections.synchronizedMap(treeMap_async);
    private ArrayList<T> instructions = new ArrayList<>();

    public void add(int clave, T valor) {
        synchronized(treeMap) {
            treeMap.put(clave, valor);
            instructions.add(valor);
        }
    }

    public void remove(int clave) {
        //clave = treeMap.floorKey(clave); TODO estas funciones han desaparecido al haber sincronizado el arbol
        //treeMap.tailMap(clave).clear();
        instructions.remove(clave);
    }

    public T get(int index) {
        return instructions.get(index);
    }

    /*public T get(int index) { // Used by getValueAt from DisassemblyTableModel
        List<T> values = new ArrayList<>(treeMap.values());
        / *if (index >= 0 && index < values.size()) {
            return values.get(index);
        }* /
        for(T i : values) {
            if( ((Instruction)i).getIndex() >= index )
                return i; // TODO usar una lista indexada por index que devuelva addr. Actualizada al llamar a add()
        }
        System.out.println("getByAddress no fue null pero no encontro index "+index);
        return null;
    }*/

    public T getByAddress(int address) {
        T instruction = treeMap.get(address);
        //assert(instruction != null);
        if( instruction == null ) {
            System.out.println(STR."Instruction not found at \{address}!!!");
            return get(0);
        }
        return instruction;
    }

    public List<T> getInstructions() {
        return new ArrayList<>(treeMap.values());
    }

    public int size() {
        return treeMap.size();
    }

    public int getLastProcessed() {
        return instructions.size();
    }

    public void clear() {
        treeMap.clear();
        instructions.clear();
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