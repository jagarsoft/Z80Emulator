package com.github.jagarsoft.test;

import com.github.jagarsoft.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BitOperationTest {
    @Test
    public void testBIT_y_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xCB);

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x80);
        compTest.poke(0x0001, (byte) 0x7F); // BIT 7, A

        cpu.fetch();

        assertAll(
                () -> assertFalse(cpu.getZF(), "BIT 7, A Failed: bit must be set"),
                () -> assertTrue(cpu.getSF(), "BIT 7, A Failed: bit must be set")
        );

        cpu.setPC(0x0000);
        cpu.setB((byte) 0x00);
        compTest.poke(0x0001, (byte) 0x78); // BIT 7, B

        cpu.fetch();

        assertAll(
                () -> assertTrue(cpu.getZF(), "BIT 7, B Failed: bit must be clear"),
                () -> assertFalse(cpu.getSF(), "BIT 7, B Failed: bit must be clear")
        );
    }

    @Test
    void testRES_y_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xCB);
        compTest.poke(0x0001, (byte) 0x88); // RES 1, B

        cpu.setPC(0x0000);
        cpu.setB((byte) 0x02);

        cpu.fetch();

        assertAll(
                () -> assertEquals(0x00, cpu.getB(), "REST 1, B Failed: must be clear")
        );
    }

    @Test
    void testSET_y_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xCB);
        compTest.poke(0x0001, (byte) 0xC7); // SET 0, A

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x0);

        cpu.fetch();

        assertAll(
                () -> assertEquals(0x01, cpu.getA(), "SET 0, A Failed: must be set")
        );
    }

    @Test
    void testSET_y_IY_d() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(8));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xFD);
        compTest.poke(0x0001, (byte) 0xCB);
        compTest.poke(0x0002, (byte) 0x04);
        compTest.poke(0x0003, (byte) 0xCE);
        compTest.poke(0x0004, (byte) 0x00);

        cpu.setPC(0x0000);
        cpu.setIY((short) 0x0000);

        cpu.fetch(); // SET 1, (IY + 4);

        assertAll(
                () -> assertEquals(2, compTest.peek(4), "SET 1, (IY + 4) Failed: was not set")
        );
    }

    @Test
    void testRRD() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);

        compTest.poke(0x0000, (byte) 0xED);
        compTest.poke(0x0001, (byte) 0x67); // RRD
        compTest.poke(0x0002, (byte) 0xF0);

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x05);
        cpu.setHL((short) 0x0002);

        cpu.fetch();

        assertAll(
                () -> assertEquals(0x00, cpu.getA(), "RRD Failed: A<>0x00="+Integer.toHexString(cpu.getA())),
                () -> assertNotEquals(0x05, cpu.getA(), "RRD Failed: A still 0x05="+Integer.toHexString(cpu.getA())),
                () -> assertEquals(0x5F, compTest.peek(cpu.getHL()), "RRD Failed: (HL)<>0x5F="+Integer.toHexString(compTest.peek(cpu.getHL()))),
                () -> assertNotEquals(0xF0, compTest.peek(cpu.getHL()), "RRD Failed: (HL) still 0xF0="+Integer.toHexString(compTest.peek(cpu.getHL())))
        );
    }

    @Test
    void testRLD() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);

        compTest.poke(0x0000, (byte) 0xED);
        compTest.poke(0x0001, (byte) 0x6F);  // RLD
        compTest.poke(0x0002, (byte) 0xF0);

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x05);
        cpu.setHL((short) 0x0002);

        cpu.fetch();

        assertAll(
                () -> assertEquals(0x0F, cpu.getA(), "RLD Failed: A<>0x0F="+Integer.toHexString(cpu.getA())),
                () -> assertNotEquals(0x05, cpu.getA(), "RLD Failed: A still 0x05="+Integer.toHexString(cpu.getA())),
                () -> assertEquals(0x05, compTest.peek(cpu.getHL()), "RLD Failed: (HL)<>0x05="+Integer.toHexString(compTest.peek(cpu.getHL()))),
                () -> assertNotEquals(0xF0, compTest.peek(cpu.getHL()), "RLD Failed: (HL) still 0xF0="+Integer.toHexString(compTest.peek(cpu.getHL())))
        );
    }
}
