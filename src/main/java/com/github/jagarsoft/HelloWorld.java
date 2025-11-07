package com.github.jagarsoft;


public class HelloWorld {
    public static void main(String[ ] arg) {
        Computer myComp = new Computer();
        myComp.addCPU(new Z80());
        // myComp.addIODevice(new short[][]{0}, new Console());
        myComp.addMemory(0x0000, new ROMMemory(new byte[]{
                0X21, 0x10, 0X00,       // 0000 LD HL, 0x0010
                0x7E,                   // 0003 LD A, (HL)
                (byte) 0xFE, 0x00,      // 0004 CP 0
                (byte) 0xCA, 0x0F, 0x00,// 0006 JP Z, 0x000F
                (byte) 0xD3, 0x00,      // 0009 OUT (0x00), A
                0x23,                   // 000B INC HL
                (byte) 0xC3, 0x03, 0x00,// 000C JP 0x0003
                0X76,                   // 000F HALT
                'H','E','L','L','O',' ','W','O','R','L','D','\n', 0x00, // 0010
                0,0,0
        }));

        myComp.reset();
        myComp.run();
    }
}
