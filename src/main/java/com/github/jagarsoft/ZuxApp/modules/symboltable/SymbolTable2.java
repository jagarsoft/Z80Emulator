package com.github.jagarsoft.ZuxApp.modules.symboltable;

import java.util.*;

/**
 * Tabla de símbolos cargada desde ficheros .map / .sym de Z88DK.
 * Optimizada para búsqueda por dirección.
 */
public class SymbolTable2 {

    // Dirección → etiqueta(s)
    private final Map<Integer, String> addressToLabel = new HashMap<>();

    // También mantenemos etiqueta → dirección (para búsquedas rápidas en UI)
    private final Map<String, Integer> labelToAddress = new HashMap<>();

    /**
     * Añade un símbolo. Si la dirección ya existe, concatena con "/".
     */
    public void addSymbol(String label, int address) {
        // Normalizamos la etiqueta (trim por si acaso)
        label = label.trim();

        // Manejo address → label
        if (addressToLabel.containsKey(address)) {
            String existing = addressToLabel.get(address);
            if (!existing.contains(label)) {
                addressToLabel.put(address, existing + "/" + label);
            }
        } else {
            addressToLabel.put(address, label);
        }

        // Manejo label → address (último sobrescribe, es suficiente)
        labelToAddress.put(label, address);
    }

    /**
     * Devuelve la(s) etiqueta(s) asociada(s) a una dirección.
     */
    public Optional<String> getLabel(int address) {
        return Optional.ofNullable(addressToLabel.get(address));
    }

    /**
     * Devuelve la dirección asociada a una etiqueta (si existe).
     */
    public Optional<Integer> getAddress(String label) {
        return Optional.ofNullable(labelToAddress.get(label));
    }

    /**
     * Devuelve todas las entradas como pares dirección → etiqueta(s).
     */
    public Map<Integer, String> getAllSymbols() {
        return Collections.unmodifiableMap(addressToLabel);
    }

    /**
     * Búsqueda textual en etiquetas o en valores hexadecimales.
     * Se devuelve un mapa reducido con coincidencias.
     */
    public Map<Integer, String> search(String query) {
        Map<Integer, String> result = new LinkedHashMap<>();
        String q = query.toLowerCase();

        for (Map.Entry<Integer, String> entry : addressToLabel.entrySet()) {
            int addr = entry.getKey();
            String labels = entry.getValue();

            if (labels.toLowerCase().contains(q) || String.format("%04X", addr).toLowerCase().contains(q)) {
                result.put(addr, labels);
            }
        }
        return result;
    }

    public Map<Object, Object> getAll() {
        // TODO
        return null;
    }
}
