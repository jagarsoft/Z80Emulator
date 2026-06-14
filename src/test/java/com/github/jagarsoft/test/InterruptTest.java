package com.github.jagarsoft.test;

import com.github.jagarsoft.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InterruptTest {
    @Test
    void testNOP() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(0x2));
        cpu.setComputer(compTest);

        // According to https://skoolkid.github.io/rom/asm/11B7.html#11CB
        // 6 NOPs takes 24 Tstates

        long initTState = cpu.getTState();
        for(int i=0; i<6; i++)
            cpu.NOP();

        assertEquals(6*4, cpu.getTState()-initTState, "NOP TState Failed");
    }

    @Test
    void testIM_im_y() {
        fail("Not yet implemented");
    }

    @Test
    void testDI() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(0x100));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0); // NOP

        // ISR RST 38h
        compTest.poke(0x0038, (byte) 0xAF); // XOR A

        cpu.reset();
        cpu.setSP((short) 0x100);
        cpu.DI();
        cpu.setA((byte) 1);

        cpu.interrupt(); // RST PC to 38h

        cpu.fetch(); // NOP or XOR A

        assertAll("DI Group",
                () -> assertEquals((short)0x01, cpu.getA(), "DI Failed: A was modified (A=" + cpu.getA() + ")")
        );
    }

    @Test
    void testEI() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(0x100));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0); // NOP

        // ISR RST 38h
        compTest.poke(0x0038, (byte) 0xAF); // XOR A

        cpu.reset();
        cpu.setSP((short) 0x100);
        cpu.fetch((byte) 0xFB); // EI
        cpu.setA((byte) 1);

        cpu.fetch((byte) 0); // dummy instruction behind EI is protected against interruptions
        cpu.interrupt(); // RST PC to 38h

        cpu.fetch(); // NOP or XOR A

        assertAll("EI Group",
                () -> assertEquals((short)0x00, cpu.getA(), "EI Failed: A was not modified (A=" + cpu.getA() + ")")
        );
    }

    @Test
    @Disabled("Skip testInterrupt")
    void testInterrupt() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(0x100));
        cpu.setComputer(compTest);
        compTest.poke(0x0038, (byte) 0xFB); // EI
        compTest.poke(0x0039, (byte) 0xC9); // RET
        compTest.poke(0x0000, (byte) 0x00);
        compTest.poke(0x0001, (byte) 0x00);
        compTest.poke(0x0003, (byte) 0x00);

        cpu.reset();
        cpu.setSP((short) 0x0100);

        cpu.DI();

        cpu.interrupt(); // PC -> RST 38

        cpu.fetch(); // EI

        cpu.interrupt(); // interrupt must not be allowed

        assertNotEquals(0x0038, cpu.getPC(), "ADC A, (IX+3); C=0 Failed: A<>0x30 = " + Integer.toHexString(cpu.getA()));

        cpu.fetch((byte) 0x00);

        cpu.interrupt();


        cpu.fetch(); // RST 38 -> EI
        cpu.fetch(); //     39 -> RET
    }

    @Test
    void testHALT() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(0x100));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xFB); // EI
        compTest.poke(0x0001, (byte) 0x76); // HALT
        compTest.poke(0x0002, (byte) 0xAF); // XOR A
        // RST 38h
        compTest.poke(0x0038, (byte) 0xFB); // EI
        compTest.poke(0x0039, (byte) 0xC9); // RET

        cpu.reset();
        cpu.setSP((short) 0x0100);
        cpu.setA((byte) 1); // sentinel

        cpu.fetch(); // EI
        cpu.fetch(); // HALT CPU until next INT
        cpu.fetch(); // XOR A must not executed

        cpu.interrupt(); // INT now

        assertAll("HALT Group",
                () -> assertEquals((short)0x01, cpu.getA(), "HALT Failed: A was modified (A=" + cpu.getA() + ")")
        );

        // execute ISR now
        cpu.fetch(); // EI
        cpu.fetch(); // RET

        cpu.fetch(); // XOR A must be executed now

        assertAll("HALT Group",
                () -> assertEquals((short)0x00, cpu.getA(), "HALT Failed: A was not modified (A=" + cpu.getA() + ")")
        );
    }

    @Test
    void testOneSecondTiming() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(0x100));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xFB); // EI
        compTest.poke(0x0001, (byte) 0x76); // HALT
        compTest.poke(0x0002, (byte) 0x10); // DJNZ
        compTest.poke(0x0003, (byte) 0xFD); // -3
        compTest.poke(0x0038, (byte) 0xFB); // EI
        compTest.poke(0x0039, (byte) 0xC9); // RET for RST 38h

        cpu.reset();
        cpu.setSP((short) 0x0100);
        cpu.setB((byte) 0x32); // 50 interruptions according to https://skoolkid.github.io/rom/asm/0970.html#0991
        cpu.run();

        System.out.println("Timing TState: " + cpu.getTiming()/1_000_000_000.0 + " second ("+cpu.getTiming()+" nanos)");
        assertAll("Timing Group", // 1s <= timing <= 1.2s
                () -> assertTrue(1_000_000_000 <= cpu.getTiming(), "TState timing failed: Too fast??"),
                () -> assertTrue(cpu.getTiming() <= 1_200_000_000, "TState timing failed: Too slow!!")
        );
    }
}
