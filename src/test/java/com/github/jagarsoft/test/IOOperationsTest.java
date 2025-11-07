package com.github.jagarsoft.test;

import com.github.jagarsoft.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class IOOperationsTest {
    @Test
    void testOUT_n_A_IN_n_A() {
        Z80ForTesting cpu = new Z80ForTesting();
        ZXSpectrumIOForTesting iotest = new ZXSpectrumIOForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        compTest.addIODevice(new short[]{(short) 0x080FF, (short)0x00FF}, new ZXSpectrumIO(iotest, iotest));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xFF);

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x80);

        cpu.OUT_n_A();

        cpu.setA((byte) 0x00); // sentinel

        cpu.setPC(0x0000);
        cpu.IN_A_n();

        assertAll("OUT/IN n,A Group",
                () -> assertEquals((byte)0x080, cpu.getA(), "OUT (n), A Failed: A<>0x80 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0x00, cpu.getA(), "OUT (n), A Failed: A<>0x80 = " + Integer.toHexString(cpu.getA()))
        );
    }

    @Test
    void OUT_C_r_y_IN_C_r_y() {
        Z80ForTesting cpu = new Z80ForTesting();
        ZXSpectrumIOForTesting iotest = new ZXSpectrumIOForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        compTest.addIODevice((short)0x0FF, new ZXSpectrumIO(iotest, iotest));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xED);
        compTest.poke(0x0001, (byte) 0x79);
        compTest.poke(0x0002, (byte) 0xED);
        compTest.poke(0x0003, (byte) 0x78);

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x80);
        cpu.setB((byte) 0x00);
        cpu.setC((byte) 0xFF);

        cpu.fetch(); // OUT (C), A

        cpu.setA((byte) 0x00); // sentinel

        cpu.fetch(); // IN A, (C)

        assertAll("OUT/IN n,A Group",
                () -> assertEquals((byte) 0x080, cpu.getA(), "OUT (n), A Failed: A<>0x80 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals(0x00, cpu.getA(), "OUT (n), A Failed: A<>0x80 = " + Integer.toHexString(cpu.getA()))
        );
    }
}
