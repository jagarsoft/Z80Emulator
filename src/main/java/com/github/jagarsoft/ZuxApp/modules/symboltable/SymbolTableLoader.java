package com.github.jagarsoft.ZuxApp.modules.symboltable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class SymbolTableLoader {

    public static SymbolTable2 load(File file) throws IOException {
        SymbolTable2 table = new SymbolTable2();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // ejemplo:   _main   = $042C ; addr, public, ...
                String[] parts = line.split("=");
                if (parts.length < 2) continue;

                String label = parts[0].trim();

                String rhs = parts[1];
                int dollarIdx = rhs.indexOf('$');
                if (dollarIdx == -1) continue;

                int semiIdx = rhs.indexOf(';', dollarIdx);
                String hexValue;
                if (semiIdx > -1) {
                    hexValue = rhs.substring(dollarIdx + 1, semiIdx).trim();
                } else {
                    hexValue = rhs.substring(dollarIdx + 1).trim();
                }

                try {
                    int value = Integer.parseInt(hexValue, 16);
                    table.addSymbol(label, value);
                } catch (NumberFormatException e) {
                    // línea mal formada, ignorar
                }
            }
        }

        return table;
    }
}
