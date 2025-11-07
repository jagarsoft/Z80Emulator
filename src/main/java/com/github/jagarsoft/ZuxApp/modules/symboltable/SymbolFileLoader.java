package com.github.jagarsoft.ZuxApp.modules.symboltable;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SymbolFileLoader {

    // Expresión regular para: NOMBRE = $HEX ; ...
    private static final Pattern LINE_PATTERN = Pattern.compile("^\\s*([A-Za-z0-9_]+)\\s*=\\s*\\$(\\p{XDigit}+)", Pattern.CASE_INSENSITIVE);

    public static void load(File file, SymbolTable table) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = LINE_PATTERN.matcher(line);
                if (matcher.find()) {
                    String name = matcher.group(1);
                    String hex = matcher.group(2);

                    try {
                        long address = Long.parseLong(hex, 16);

                        // Aquí decidimos si ignorar símbolos fuera de 16 bits
                        if (address >= 0 && address <= 0xFFFF) { // TODO
                            table.addSymbol(name, (int) address);
                        }
                    } catch (NumberFormatException ignored) {
                        // Si el número no es válido, lo saltamos
                    }
                }
            }
        }
    }
}
