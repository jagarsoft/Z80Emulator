package com.github.jagarsoft.ZuxApp.modules.symboltable;

import java.util.*;

public class SymbolTable {
    private final Map<Integer, String> addressToLabels = new TreeMap<>();

    public void addSymbol(String label, int address) {
        addressToLabels.merge(address, label, (existing, newLabel) -> existing + "/" + newLabel);
    }

    public String getLabel(int address) {
        return addressToLabels.get(address);
    }

    public Map<Integer, String> getAllSymbols() {
        return Collections.unmodifiableMap(addressToLabels);
    }

    public List<Map.Entry<Integer, String>> search(String query) {
        String q = query.toLowerCase(Locale.ROOT);
        List<Map.Entry<Integer, String>> result = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : addressToLabels.entrySet()) {
            String hexValue = String.format("%04X", entry.getKey());
            if (hexValue.toLowerCase(Locale.ROOT).contains(q) || entry.getValue().toLowerCase(Locale.ROOT).contains(q)) {
                result.add(entry);
            }
        }
        return result;
    }
}
