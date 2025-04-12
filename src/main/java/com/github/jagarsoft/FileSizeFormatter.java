package com.github.jagarsoft;

import java.util.Locale;

// Java version of
// https://stackoverflow.com/a/63512259/2928048
public class FileSizeFormatter {
    private static final String[][] FILE_SIZE_UNITS = {
            {"B"}, {"KB"}, {"MB"}, {"GB"}, {"TB"}, {"PB"}, {"EB"}, {"ZB"}
    };

    public static String stringifyFileSize(long size, int precision, int base) {
        if (base != 1000 && base != 1024) {
            throw new IllegalArgumentException("Base must be 1000 or 1024");
        }

        int unit = logFloor(size, base);
        double divisor = Math.pow(base, unit);
        String result = stringifyFraction(size, divisor, precision);

        StringBuilder sb = new StringBuilder(result);
        sb.append(' ');

        char first = FILE_SIZE_UNITS[unit][0].charAt(0);
        if (base == 1000) {
            first = Character.toLowerCase(first);
        }
        sb.append(first);

        if (unit != 0) {
            if (base == 1024) {
                sb.append('i');
            }
            sb.append(FILE_SIZE_UNITS[unit][1]);
        }

        return sb.toString();
    }

    private static int logFloor(long size, int base) {
        int unit = 0;
        while (size >= base) {
            size /= base;
            unit++;
        }
        return unit;
    }

    private static String stringifyFraction(long size, double divisor, int precision) {
        double value = size / divisor;
        //return String.format(Locale.US, "%." + precision + "f", value);
        return String.format(Locale.US, "%f", value);
    }
}
