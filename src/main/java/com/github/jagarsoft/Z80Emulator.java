package com.github.jagarsoft;

public class Z80Emulator {
    public static void main(String[ ] arg) {
        createComputer();
    }

    private static void createComputer() {
        Z80cpu Z80cpu = new Z80cpu();
        ROMMemory rom = new ROMMemory(new byte[]{1, 2, 3});
        RAMMemory ram = new RAMMemory(16);

        Z80cpu.reset();
        //while(true){
        for (int i=0; i<3; i++) {
            int pc = Z80cpu.getPC();
            byte code = rom.peek(pc);
            Z80cpu.exec(code);
        }
    }
}


