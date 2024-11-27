package com.github.jagarsoft;

public class Z80Disassembler {
    public static void main(String[ ] arg) {
        Z80 cpu = new Z80(new Disassembler());

        byte[] block = new byte[] {0, 8, 16, 0, 24, 1, 32, 2, 40, 3, 48, 4, 56, 5};

        for(int i = 0; i<block.length; ) {
            cpu.fetch(block[i++]); // fetch opCode
        }
    }
}