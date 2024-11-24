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
    
    
    int LD_BC_A(byte z, byte y);
    int LD_DE_A(byte z, byte y);
    int LD_nn_HL(byte z, byte y);
    int LD_nn_A(byte z, byte y);
    int LD_A_BC(byte z, byte y);
    int LD_A_DE(byte z, byte y);
    int LD_HL_nn(byte z, byte y);
    int LD_A_nn(byte z, byte y);
}
