package com.github.jagarsoft;

public class Instruction {
    public boolean hasBreakPoint;
    public int index;
    int PC;
    byte[] opCodes = new byte[4];
    int opCodeCounter = 0;
    String label = null;
    String mnemonic;
    String comment;

    public String toString() {
        int i;
        StringBuilder line = new StringBuilder(String.format("%04X ", PC));
        for (i = 0; i < opCodeCounter; i++)
            line.append(String.format("%02X", opCodes[i]));
        for (; i < 8; i++)
            line.append("  "); // 2 spaces
        //line.append("\t");
        line.append(mnemonic);
        return line.toString();
    }

    public int getIndex() {
        return index;
    }

    public int getAddress() { return PC; }

    public String getHexBytes() {
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < opCodeCounter; i++)
            hex.append(String.format("%02X", opCodes[i]));
        return hex.toString();
    }

    public String getLabel() { return label != null ? label : ""; }
    public String getMnemonic() { return mnemonic; }
    public String getComment() { return comment; }
}
