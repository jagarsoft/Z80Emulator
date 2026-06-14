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

        long initTState = cpu.getTState();
        cpu.fetch();

        assertAll(
                () -> assertFalse(cpu.getZF(), "BIT 7, A Failed: bit must be OFF"),
                () -> assertTrue(cpu.getSF(), "BIT 7, A Failed: bit must be ON"),
                () -> assertFalse(cpu.getPF(), "BIT 7, A Failed: bit must be OFF"),

                () -> assertEquals(8, cpu.getTState()-initTState, "BIT 7, A TState Failed")
        );

        cpu.setPC(0x0000);
        cpu.setB((byte) 0x00);
        compTest.poke(0x0001, (byte) 0x78); // BIT 7, B

        cpu.fetch();

        assertAll(
                () -> assertTrue(cpu.getZF(), "BIT 7, B Failed: bit must be ON"),
                () -> assertTrue(cpu.getSF(), "BIT 7, B Failed: bit must be ON"),
                () -> assertTrue(cpu.getPF(), "BIT 7, A Failed: bit must be ON")
        );

        cpu.setPC(0x0000);
        cpu.setHL((byte) 0x0003);
        compTest.poke(0x0001, (byte) 0x7E); // BIT 7, (HL)
        compTest.poke(0x0003, (byte) 0x80); // BIT 7, (HL)

        long initTState2 = cpu.getTState();
        cpu.fetch();

        assertAll(
                () -> assertFalse(cpu.getZF(), "BIT 7, (HL) Failed: bit must be OFF"),
                () -> assertTrue(cpu.getSF(), "BIT 7, (HL) Failed: bit must be ON"),
                () -> assertFalse(cpu.getPF(), "BIT 7, (HL) Failed: bit must be OFF"),

                () -> assertEquals(12, cpu.getTState()-initTState2, "BIT 7, (HL) TState Failed")
        );

        fail("XF flag Set if n = 3 and tested bit is set");
        fail("YF flag Set if n = 5 and tested bit is set");
    }

    @Test
    public void testBIT_y_IX_d() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(8));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xDD);
        compTest.poke(0x0001, (byte) 0xCB);
        compTest.poke(0x0002, (byte) 0x04);
        compTest.poke(0x0003, (byte) 0x6E);
        compTest.poke(0x0004, (byte) 0x20);

        cpu.setPC(0x0000);
        cpu.setIX((short) 0x0000);

        long initTState = cpu.getTState();
        cpu.fetch(); // BIT 5, (IX+4)

        assertAll(
                () -> assertFalse(cpu.getZF(), "BIT 5, (IX+4) Failed: bit must be OFF"),
                () -> assertFalse(cpu.getSF(), "BIT 5, (IX+4) Failed: bit must be OFF"),
                () -> assertFalse(cpu.getPF(), "BIT 5, (IX+4) Failed: bit must be OFF"),

                () -> assertEquals(20, cpu.getTState()-initTState, "BIT 5, (IX+4) TState Failed")
        );

        cpu.setPC(0x0000);
        compTest.poke(0x0004, (byte) 0x00);

        cpu.fetch();  // BIT 5, (IX+4)

        assertAll(
                () -> assertTrue(cpu.getZF(), "BIT 5, (IX+4) Failed: bit must be ON"),
                () -> assertFalse(cpu.getSF(), "BIT 5, (IX+4) Failed: bit must be OFF"),
                () -> assertTrue(cpu.getPF(), "BIT 5, (IX+4) Failed: bit must be ON")
        );
    }

    @Test
    public void testBIT_y_IY_d() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(8));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xFD);
        compTest.poke(0x0001, (byte) 0xCB);
        compTest.poke(0x0002, (byte) 0x04);
        compTest.poke(0x0003, (byte) 0x7E);
        compTest.poke(0x0004, (byte) 0x80);

        cpu.setPC(0x0000);
        cpu.setIY((short) 0x0000);

        long initTState = cpu.getTState();

        cpu.fetch(); // BIT 7, (IY+4)

        assertAll(
                () -> assertFalse(cpu.getZF(), "BIT 7, (IY+4) Failed: bit must be OFF"),
                () -> assertTrue(cpu.getSF(), "BIT 7, (IY+4) Failed: bit must be ON"),
                () -> assertFalse(cpu.getPF(), "BIT 7, (IX+4) Failed: bit must be OFF"),

                () -> assertEquals(20, cpu.getTState()-initTState, "BIT 7, (IX+4) TState Failed")
        );

        cpu.setPC(0x0000);
        compTest.poke(0x0004, (byte) 0x00);

        cpu.fetch();  // BIT 7, (IX+4)

        assertAll(
                () -> assertTrue(cpu.getZF(), "BIT 7, (IY+4) Failed: bit must be clear"),
                () -> assertTrue(cpu.getSF(), "BIT 7, (IY+4) Failed: bit must be clear"),
                () -> assertTrue(cpu.getPF(), "BIT 7, (IX+4) Failed: bit must be ON")
        );
    }

    @Test
    void testRES_y_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xCB);
        compTest.poke(0x0001, (byte) 0x88); // RES 1, B

        cpu.setPC(0x0000);
        cpu.setB((byte) 0x02);
        cpu.setF((byte) 0x00);

        long initTState = cpu.getTState();
        cpu.fetch();

        assertAll(
                () -> assertEquals(0x00, cpu.getB(), "RES 1, B Failed: must be clear"),
                () -> assertEquals(0, cpu.getF(), "RES 1, B Failed: F was affected"),

                () -> assertEquals(8, cpu.getTState()-initTState, "RES 1, B TState Failed")
        );

        cpu.setPC(0x0000);
        cpu.setHL((short) 0x0003);
        cpu.setF((byte) 0x00);

        compTest.poke(0x0001, (byte) 0x8E); // RES 1, (HL)
        compTest.poke(0x0003, (byte) 0x02);

        long initTState2 = cpu.getTState();
        cpu.fetch();

        assertAll(
                () -> assertEquals(0x00, compTest.peek(3), "RES 1, (HL) Failed: must be clear"),
                () -> assertEquals(0, cpu.getF(), "RES 1, (HL) Failed: F was affected"),

                () -> assertEquals(15, cpu.getTState()-initTState2, "RES 1, (HL) TState Failed")
        );
    }

    @Test
    public void tesRES_y_IX_d() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(8));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xDD);
        compTest.poke(0x0001, (byte) 0xCB);
        compTest.poke(0x0002, (byte) 0x04);
        compTest.poke(0x0003, (byte) 0x9E);
        compTest.poke(0x0004, (byte) 0x08);

        cpu.setPC(0x0000);
        cpu.setIY((short) 0x0000);
        cpu.setF((byte) 0x00);

        long initTState = cpu.getTState();
        cpu.fetch(); // RES 3, (IX + 4);

        assertAll(
                () -> assertEquals(0, compTest.peek(4), "RES 3, (IX + 4) Failed: was not set"),
                () -> assertEquals(0, cpu.getF(), "RES 3, (IX + 4) Failed: F was affected"),

                () -> assertEquals(23, cpu.getTState()-initTState, "RES 3, (IX + 4) TState Failed")
        );
    }

    @Test
    public void testRES_y_IY_d() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(8));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xFD);
        compTest.poke(0x0001, (byte) 0xCB);
        compTest.poke(0x0002, (byte) 0x04);
        compTest.poke(0x0003, (byte) 0xA6);
        compTest.poke(0x0004, (byte) 0x10);

        cpu.setPC(0x0000);
        cpu.setIY((short) 0x0000);
        cpu.setF((byte) 0x00);

        long initTState = cpu.getTState();
        cpu.fetch(); // RES 4, (IX + 4);

        assertAll(
                () -> assertEquals(0, compTest.peek(4), "RES 4, (IY + 4) Failed: was not set"),
                () -> assertEquals(0, cpu.getF(), "RES 4, (IY + 4) Failed: F was affected"),

                () -> assertEquals(23, cpu.getTState()-initTState, "RES 4, (IY + 4) TState Failed")
        );
    }

    @Test
    void testSET_y_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xCB);
        compTest.poke(0x0001, (byte) 0xC7); // SET 0, A

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x0);
        cpu.setF((byte) 0x00);

        long initTState = cpu.getTState();
        cpu.fetch();

        assertAll(
                () -> assertEquals(0x01, cpu.getA(), "SET 0, A Failed: must be set"),
                () -> assertEquals(0, cpu.getF(), "SET 0, A Failed: F was affected"),

                () -> assertEquals(8, cpu.getTState()-initTState, "SET 0, A TState Failed")
        );

        cpu.setPC(0x0000);
        cpu.setHL((short) 0x0003);
        cpu.setF((byte) 0x00);

        compTest.poke(0x0001, (byte) 0xC6); // SET 1, (HL)
        compTest.poke(0x0003, (byte) 0x00);

        long initTState2 = cpu.getTState();
        cpu.fetch();

        assertAll(
                () -> assertEquals(0x01, compTest.peek(3), "SET 0, (HL) Failed: must be set"),
                () -> assertEquals(0, cpu.getF(), "SET 0, (HL) Failed: F was affected"),

                () -> assertEquals(15, cpu.getTState()-initTState2, "SET 0, (HL) TState Failed")
        );
    }

    @Test
    public void tesSET_y_IX_d() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(8));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xDD);
        compTest.poke(0x0001, (byte) 0xCB);
        compTest.poke(0x0002, (byte) 0x04);
        compTest.poke(0x0003, (byte) 0xC6);
        compTest.poke(0x0004, (byte) 0x00);

        cpu.setPC(0x0000);
        cpu.setIY((short) 0x0000);
        cpu.setF((byte) 0x00);

        long initTState = cpu.getTState();
        cpu.fetch(); // SET 0, (IX + 4);

        assertAll(
                () -> assertEquals(1, compTest.peek(4), "SET 0, (IX + 4) Failed: was not set"),
                () -> assertEquals(0, cpu.getF(), "SET 0, (IX + 4) Failed: F was affected"),

                () -> assertEquals(23, cpu.getTState()-initTState, "SET 0, (IX + 4) TState Failed")
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
        cpu.setF((byte) 0x00);

        long initTState = cpu.getTState();
        cpu.fetch(); // SET 1, (IY + 4);

        assertAll(
                () -> assertEquals(2, compTest.peek(4), "SET 1, (IY + 4) Failed: was not set"),
                () -> assertEquals(0, cpu.getF(), "SET 1, (IX + 4) Failed: F was affected"),

                () -> assertEquals(23, cpu.getTState()-initTState, "SET 1, (IY + 4) TState Failed")
        );
    }
}
