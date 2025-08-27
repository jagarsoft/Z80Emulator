package com.github.jagarsoft;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Disassembler {
    static Z80Disassembler disassembler = new Z80Disassembler();

    public static void main(String[ ] args) {

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

        try {
            comp.load(dataStream, size);
            dataStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        list(comp, 0, size);
    }

    static void list(Computer comp, int org, int size) {

        disassembler.setComputer(comp);

        comp.reset();
        comp.setOrigin(org);
        do {
            disassembler.fetch();
        } while( (disassembler.getPC() - org) < size);
    }
}