package com.github.jagarsoft;

public interface Z80OpCode {
    final String[] cc = new String[] {
            "NZ", "Z", "NC", "C", "PO", "PE", "P", "M"
    };

    void NOP(byte z, byte y);
    void EX_AF_AF_(byte z, byte y);
    void DJNZ(byte y, byte d);
    void JR(byte y, byte d);
    void JR_cc(byte y, byte d);
    
    
    void LD_BC_A(byte z, byte y);
    void LD_DE_A(byte z, byte y);
    void LD_nn_HL(byte z, byte y);
    void LD_nn_A(byte z, byte y);
    void LD_A_BC(byte z, byte y);
    void LD_A_DE(byte z, byte y);
    void LD_HL_nn(byte z, byte y);
    void LD_A_nn(byte z, byte y);
}
