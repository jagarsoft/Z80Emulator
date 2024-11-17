package com.github.jagarsoft;

public class Z80Disassembler {
    static Dispatcher d;

    public static void main(String[ ] arg) {
        byte opCode;
        byte data;
        int operands = 0;

        d = new Dispatcher(new Disassembler());

        byte[] block = new byte[] {0, 8, 16, 0, 24, 1, 32, 2, 40, 3, 48, 4, 56, 5};

        for(int i = 0; i<block.length; ) {
            opCode = block[i++]; // peek
            operands = d.execute(opCode);
            while(operands-- > 0) {
                data = block[i++];
                d.execute(data);
            }
        }
    }
}