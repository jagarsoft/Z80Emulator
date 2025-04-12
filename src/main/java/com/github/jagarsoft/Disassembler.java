package com.github.jagarsoft;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Disassembler {
    public static void main(String[ ] args) {
        Z80Disassembler disassembler = new Z80Disassembler();
        FileInputStream dataStream;
        int size;

        File file = new File(args[0]);
        try {
            dataStream = new FileInputStream(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        size = (int)file.length();

        Computer comp = new Computer();
        comp.addCPU(disassembler);
        comp.addMemory(0x0000, new RAMMemory(comp.upper_power_of_two(size)));

        disassembler.setComputer(comp);

        try {
            comp.load(dataStream, size);
            dataStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        comp.reset();
        do {
            disassembler.fetch();
        } while( disassembler.getPC() < size);
    }
}