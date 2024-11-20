package com.github.jagarsoft;

public class Z80Disassembler {
    public static void main(String[ ] arg) {
        Dispatcher d;

        d = new Dispatcher(new Disassembler());

        byte[] block = new byte[] {0, 8, 16, 0, 24, 1, 32, 2, 40, 3, 48, 4, 56, 5};

        for(int i = 0; i<block.length; ) {
            int operands = d.execute(block[i++]); // peek opCode
            while(operands-- > 0) {
                d.execute(block[i++]); // peek data
            }
        }
    }
}