package com.github.jagarsoft;

public interface Z80OpCode {
    public int NOP(byte y, byte z);

    public int EX_AF_AF_(byte y, byte z);

    public int DJNZ(byte d, byte z);

    public int JR(byte d, byte z);

    public int JR_cc(byte y, byte d);
}
