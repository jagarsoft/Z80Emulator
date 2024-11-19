package com.github.jagarsoft;

public interface Z80OpCode {
    final String[] cc = new String[] {
            "NZ", "Z", "NC", "C", "PO", "PE", "P", "M"
    };

    int NOP(byte z, byte y);

    int EX_AF_AF_(byte z, byte y);

    int DJNZ(byte y, byte d);

    int JR(byte y, byte d);

    int JR_cc(byte y, byte d);
}
