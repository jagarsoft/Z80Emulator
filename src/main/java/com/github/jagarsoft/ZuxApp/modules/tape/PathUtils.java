package com.github.jagarsoft.ZuxApp.modules.tape;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtils {
    public static String getDirectoryPath(String pathString) {
        if (pathString == null || pathString.isEmpty()) {
            return "";
        }

        // Usar Path maneja automáticamente los separadores de Windows y Linux
        Path path = Paths.get(pathString).normalize();
        Path parent = path.getParent();

        if (parent == null) {
            // Significa que era una ruta relativa sin carpetas (ej: "archivo.txt")
            // El "padre" lógico es el directorio actual de ejecución
            return ".";
        }

        return parent.toString();
    }

    public static String getBaseNameWithoutExtension(String pathString) {
        Path path = Paths.get(pathString);
        String fileName = path.getFileName().toString();

        // Caso esquina: archivos ocultos de UNIX que empiezan con punto y no tienen otra extensión (ej: .gitignore)
        if (fileName.startsWith(".") && fileName.lastIndexOf('.') == 0) {
            return fileName;
        }

        int lastDot = fileName.lastIndexOf('.');
        return (lastDot == -1) ? fileName : fileName.substring(0, lastDot);
    }

    public static String getExtension(String pathString) {
        if (pathString == null) return "";

        Path path = Paths.get(pathString);
        // 1. Aislamos el nombre del archivo (ej: "foto.jpg")
        Path fileNamePath = path.getFileName();
        if (fileNamePath == null) return "";

        String fileName = fileNamePath.toString();
        int lastDot = fileName.lastIndexOf('.');

        // 2. Evaluamos los casos esquina:
        // - No hay punto (-1)
        // - El único punto está al inicio (archivo oculto como .gitignore o .className)
        if (lastDot <= 0) {
            return "";
        }

        // 3. Si pasa los filtros, devolvemos lo que está tras el punto
        return fileName.substring(lastDot + 1);
    }
}
