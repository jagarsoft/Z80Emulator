package com.github.jagarsoft;

public class Instruction {
    public boolean hasBreakPoint;
    public int index;
    public boolean isData;
    public boolean isBankingOn;
    public int bank;
    int PC;
    byte[] opCodes = new byte[4];
    int opCodeCounter = 0; // 0..7
    String label = null;
    String mnemonic;
    String comment;

    public String toString() {
        int i;
        StringBuilder line;

        if( !isBankingOn() )
            line = new StringBuilder(String.format("%04X ", PC));
        else
            line = new StringBuilder(String.format("%04X:%04X ", bank, PC));

        for (i = 0; i < opCodeCounter; i++)
            line.append(String.format("%02X", opCodes[i]));
        for (; i < 4*2; i++)
            line.append("  "); // 2 spaces
        //line.append("\t");
        line.append(mnemonic);
        if( comment != null ) {
            line.append(" "); // 1 space
            line.append(comment);
        }
        return line.toString();
    }

    public boolean isBankingOn() { return isBankingOn; }

    public int getIndex() {
        return index;
    }

    public int getBank() { return bank; }

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
