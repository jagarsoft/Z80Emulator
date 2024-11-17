package com.github.jagarsoft;

public class Z80Emulator {
    public static void main(String[ ] arg) {
        Computer myComp = new Computer();
        myComp.addCPU(new Z80());
        myComp.addMemory(new ROMMemory(new byte[]{1, 2, 3}));
        myComp.addMemory(new RAMMemory(16));

        for(;;) {
            myComp.reset();
            myComp.run();
        }
    }
}


