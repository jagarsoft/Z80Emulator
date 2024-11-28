package com.github.jagarsoft;

public class Z80Emulator {
    public static void main(String[ ] arg) {
        Computer myComp = new Computer();
        myComp.addCPU(new Z80());
        myComp.addMemory(0x0000, new ROMMemory(new byte[]{1, 2, 3, 4}));
        myComp.addMemory(0x0004, new RAMMemory(16));

        for(;;) {
            myComp.reset();
            myComp.run();
        }
    }
}


