package com.github.jagarsoft.test;

import com.github.jagarsoft.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoadStoreTest {
    @Test
    void testLD_rp_p_nn() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(8));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0x34);
        compTest.poke(0x0001, (byte) 0x12);
        compTest.poke(0x0002, (byte) 0x78);
        compTest.poke(0x0003, (byte) 0x56);
        compTest.poke(0x0004, (byte) 0xBC);
        compTest.poke(0x0005, (byte) 0x9A);
        compTest.poke(0x0006, (byte) 0xF0);
        compTest.poke(0x0007, (byte) 0xDE);

        cpu.setPC((short)0x0000);
        cpu.setBC((short)0x0000);
        cpu.setDE((short)0x0000);
        cpu.setHL((short)0x0000);
        cpu.setSP((short)0x0000);

        cpu.fetch((byte)0x01); // LD BC, 0x1234
        cpu.fetch((byte)0x11); // LD DE, 0x5678
        cpu.fetch((byte)0x21); // LD HL, 0x9ABC
        long initTState = cpu.getTState();
        cpu.fetch((byte)0x31); // LD SP, 0xDEF0

        assertAll("LD rp[p], nn, A Group",
                () -> assertEquals((short)0x1234, cpu.getBC(), "LD BC, 0x1234 Failed: BC<>0x1234 BC=" + cpu.getBC() + ")"),
                () -> assertNotEquals((short)0x0000, cpu.getBC(), "LD BC, 0x1234 Failed: BC still 0x0000 BC=" + cpu.getBC() + ")"),

                () -> assertEquals((short)0x5678, cpu.getDE(), "LD DE, 0x5678 Failed: DE<>0x5678 DE=" + cpu.getDE() + ")"),
                () -> assertNotEquals((short)0x0000, cpu.getDE(), "LD DE, 0x5678 Failed: DE still 0x0000 DE=" + cpu.getDE() + ")"),

                () -> assertEquals((short)0x9ABC, cpu.getHL(), "LD HL, 0x9ABC Failed: HL<>0x9ABC HL=" + cpu.getHL() + ")"),
                () -> assertNotEquals((short)0x0000, cpu.getHL(), "LD HL, 0x9ABC Failed: HL still 0x0000 HL=" + cpu.getHL() + ")"),

                () -> assertEquals(0x0DEF0, cpu.getSP(), "LD SP, 0xDEF0 Failed: SP<>0xDEF0 SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals((short)0x0000, cpu.getSP(), "LD SP, 0xDEF0 Failed: SP still 0x0000 SP=" + cpu.getSP() + ")"),

                () -> assertEquals(10, cpu.getTState()-initTState, "LD SP, 0xDEF0 TState Failed")
        );
    }

    @Test
    void testLD_BC_A() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(1));
        cpu.setComputer(compTest);
        compTest.poke(0, (byte) 0x80);

        cpu.setBC((short) 0);
        cpu.setA((byte) 0xFF);

        long initTState = cpu.getTState();
        cpu.LD_BC_A();

        assertAll("LD (BC), A Group",
                () -> assertEquals((byte) 0x0FF, compTest.peek(0), "LD (BC), A Failed: (BC)<>0xFF ((BC)=" + compTest.peek(0) + ")"),
                () -> assertNotEquals((short) 0x080, compTest.peek(0), "LD (BC), A Failed: (BC) still 0x80 ((BC)=" + compTest.peek(0) + ")"),
                () -> assertEquals(7, cpu.getTState()-initTState, "LD (BC), A TState Failed")
        );
    }

    @Test
    void testLD_DE_A() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(1));
        cpu.setComputer(compTest);
        compTest.poke(0, (byte) 0x080);

        cpu.setDE((short) 0);
        cpu.setA((byte) 0xFF);

        long initTState = cpu.getTState();
        cpu.LD_DE_A();

        assertAll("LD (DE), A Group",
                () -> assertEquals((byte) 0x0FF, compTest.peek(0), "LD (DE), A Failed: (DE)<>0xFF ((DE)=" + compTest.peek(0) + ")"),
                () -> assertNotEquals((short) 0x080, compTest.peek(0), "LD (DE), A Failed: (DE) still 0x80 ((DE)=" + compTest.peek(0) + ")"),
                () -> assertEquals(7, cpu.getTState()-initTState, "LD (DE), A TState Failed")
        );
    }

    @Test
    void testLD_mm_rp_p() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(8));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xED);
        compTest.poke(0x0001, (byte) 0x22); // LD (0x0004), HL without ED prefix
        compTest.poke(0x0002, (byte) 0x04);
        compTest.poke(0x0003, (byte) 0x00);
        compTest.poke(0x0004, (byte) 0x78); // sentinel 0x5678
        compTest.poke(0x0005, (byte) 0x56);

        cpu.setPC(0x0001);
        cpu.setHL((short) 0x1234);

        long initTState = cpu.getTState();
        cpu.fetch();

        assertAll("LD (0x0004), HL Group",
                () -> assertEquals((byte) 0x34, compTest.peek(4), "LD (0x0004), HL Failed: (0x0004)<>0x34=" + Integer.toHexString(compTest.peek(4))),
                () -> assertNotEquals((byte) 0x78, compTest.peek(4), "LD (0x004), HL Failed: (0x0004) still 0x78=" + Integer.toHexString(compTest.peek(4))),
                () -> assertEquals((byte) 0x12, compTest.peek(5), "LD (0x0005), HL Failed: (0x0005)<>0x12" + Integer.toHexString(compTest.peek(5))),
                () -> assertNotEquals((byte) 0x56, compTest.peek(5), "LD (0x0005), HL Failed: (0x0005) still 0x56=" + Integer.toHexString(compTest.peek(5))),
                () -> assertEquals((byte) 0x0004, cpu.getPC(), "LD (0x0004), HL Failed: PC no was modified PC<>0x0004 PC=" + Integer.toHexString(cpu.getPC())),
                () -> assertEquals(16, cpu.getTState()-initTState, "LD (0x0004), HL TState Failed")
        );

        compTest.poke(0x0004, (byte) 0x78);
        compTest.poke(0x0005, (byte) 0x56);

        cpu.setPC(0x0000);
        cpu.setBC((short) 0x1234);

        compTest.poke(0x0001, (byte) 0x43); // LD (0x0002), BC
        long initTState2 = cpu.getTState();
        cpu.fetch();

        assertAll("LD (0x0004), BC Group",
                () -> assertEquals((byte) 0x34, compTest.peek(4), "LD (0x0004), BC Failed: (0x0004)<>0x34=" + Integer.toHexString(compTest.peek(4))),
                () -> assertNotEquals((byte) 0x78, compTest.peek(4), "LD (0x004), BC Failed: (0x0004) still 0x78=" + Integer.toHexString(compTest.peek(4))),
                () -> assertEquals((byte) 0x12, compTest.peek(5), "LD (0x0005), BC Failed: (0x0005)<>0x12" + Integer.toHexString(compTest.peek(5))),
                () -> assertNotEquals((byte) 0x56, compTest.peek(5), "LD (0x0005), BC Failed: (0x0005) still 0x56=" + Integer.toHexString(compTest.peek(5))),
                () -> assertEquals((byte) 0x0004, cpu.getPC(), "LD (0x0004), BC Failed: PC no was modified PC<>0x0004 PC=" + Integer.toHexString(cpu.getPC())),
                () -> assertEquals(20, cpu.getTState()-initTState2, "LD (0x0004), BC TState Failed")
        );

        compTest.poke(0x0004, (byte) 0x78);
        compTest.poke(0x0005, (byte) 0x56);

        cpu.setPC(0x0000);
        cpu.setDE((short) 0x1234);

        compTest.poke(0x0001, (byte) 0x53); // LD (0x0002), DE
        cpu.fetch();

        assertAll("LD (0x0004), DE Group",
                () -> assertEquals((byte) 0x34, compTest.peek(4), "LD (0x0004), DE Failed: (0x0004)<>0x34=" + Integer.toHexString(compTest.peek(4))),
                () -> assertNotEquals((byte) 0x78, compTest.peek(4), "LD (0x004), DE Failed: (0x0004) still 0x78=" + Integer.toHexString(compTest.peek(4))),
                () -> assertEquals((byte) 0x12, compTest.peek(5), "LD (0x0005), DE Failed: (0x0005)<>0x12" + Integer.toHexString(compTest.peek(5))),
                () -> assertNotEquals((byte) 0x56, compTest.peek(5), "LD (0x0005), DE Failed: (0x0005) still 0x56=" + Integer.toHexString(compTest.peek(5))),
                () -> assertEquals((byte) 0x0004, cpu.getPC(), "LD (0x0004), DE Failed: PC no was modified PC<>0x0004 PC=" + Integer.toHexString(cpu.getPC()))
        );

        compTest.poke(0x0004, (byte) 0x78);
        compTest.poke(0x0005, (byte) 0x56);

        cpu.setPC(0x0000);
        cpu.setHL((short) 0x1234);

        compTest.poke(0x0001, (byte) 0x63); // LD (0x0002), HL with ED prefix
        cpu.fetch();

        assertAll("LD (0x0004), HL Group",
                () -> assertEquals((byte) 0x34, compTest.peek(4), "LD (0x0004), HL Failed: (0x0004)<>0x34=" + Integer.toHexString(compTest.peek(4))),
                () -> assertNotEquals((byte) 0x78, compTest.peek(4), "LD (0x004), HL Failed: (0x0004) still 0x78=" + Integer.toHexString(compTest.peek(4))),
                () -> assertEquals((byte) 0x12, compTest.peek(5), "LD (0x0005), HL Failed: (0x0005)<>0x12" + Integer.toHexString(compTest.peek(5))),
                () -> assertNotEquals((byte) 0x56, compTest.peek(5), "LD (0x0005), HL Failed: (0x0005) still 0x56=" + Integer.toHexString(compTest.peek(5))),
                () -> assertEquals((byte) 0x0004, cpu.getPC(), "LD (0x0004), HL Failed: PC no was modified PC<>0x0004 PC=" + Integer.toHexString(cpu.getPC()))
        );

        compTest.poke(0x0004, (byte) 0x78);
        compTest.poke(0x0005, (byte) 0x56);

        cpu.setPC(0x0000);
        cpu.setSP((short) 0x1234);

        compTest.poke(0x0001, (byte) 0x73); // LD (0x0002), SP
        cpu.fetch();

        assertAll("LD (0x0004), SP Group",
                () -> assertEquals((byte) 0x34, compTest.peek(4), "LD (0x0004), SP Failed: (0x0004)<>0x34=" + Integer.toHexString(compTest.peek(4))),
                () -> assertNotEquals((byte) 0x78, compTest.peek(4), "LD (0x004), SP Failed: (0x0004) still 0x78=" + Integer.toHexString(compTest.peek(4))),
                () -> assertEquals((byte) 0x12, compTest.peek(5), "LD (0x0005), SP Failed: (0x0005)<>0x12" + Integer.toHexString(compTest.peek(5))),
                () -> assertNotEquals((byte) 0x56, compTest.peek(5), "LD (0x0005), SP Failed: (0x0005) still 0x56=" + Integer.toHexString(compTest.peek(5))),
                () -> assertEquals((byte) 0x0004, cpu.getPC(), "LD (0x0004), SP Failed: PC no was modified PC<>0x0004 PC=" + Integer.toHexString(cpu.getPC()))
        );
    }

    @Test
    void testLD_mm_A(){
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000,(byte)0x02);
        compTest.poke(0x0001,(byte)0x00);
        compTest.poke(0x0002,(byte)0xAA);

        cpu.setPC(0x0000);
        cpu.setA((byte)0xBB);

        long initTState = cpu.getTState();
        cpu.LD_mm_A();

        assertAll("LD (nn), A Group",
                () -> assertEquals((byte) 0xBB, compTest.peek(0x0002), "LD (0x0002), A Failed: (0x0002)<>0xBB = " + Integer.toHexString(compTest.peek(0x0002))),
                () -> assertNotEquals((byte) 0xAA, compTest.peek(0x0002), "LD (0x0002), A Failed: (0x0002) still 0xAA = " + Integer.toHexString(compTest.peek(0x0002))),
                () -> assertEquals(13, cpu.getTState()-initTState, "LD (0x0002), A TState Failed")
        );
    }

    @Test
    void testLD_A_BC(){
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(1));
        cpu.setComputer(compTest);
        compTest.poke(0x0000,(byte)0xBB);

        cpu.setBC((short) 0x0000);
        cpu.setA((byte)0xAA);

        long initTState = cpu.getTState();
        cpu.LD_A_BC();

        assertAll("LD A, (BC) Group",
                () -> assertEquals((byte)0xBB, cpu.getA(), "LD A, (BC) Failed: A<>0xBB = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte)0xAA, cpu.getA(), "LD A, (BC) Failed: A still 0xAA = " + Integer.toHexString(cpu.getA())),
                () -> assertEquals(7, cpu.getTState()-initTState, "LD A, (BC) TState Failed")
        );
    }

    @Test
    void testLD_A_DE(){
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(1));
        cpu.setComputer(compTest);
        compTest.poke(0x0000,(byte)0xBB);

        cpu.setDE((short) 0x0000);
        cpu.setA((byte)0xAA);

        long initTState = cpu.getTState();
        cpu.LD_A_DE();

        assertAll("LD A, (BC) Group",
                () -> assertEquals((byte)0xBB, cpu.getA(), "LD A, (DE) Failed: A<>0xBB = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte)0xAA, cpu.getA(), "LD A, (DE) Failed: A still 0xAA = " + Integer.toHexString(cpu.getA())),
                () -> assertEquals(7, cpu.getTState()-initTState, "LD A, (DE) TState Failed")
        );
    }

    /*@Test
    void testLD_HL_mm(){
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000,(byte)0x02);
        compTest.poke(0x0001,(byte)0x00);
        compTest.poke(0x0002,(byte)0x34);
        compTest.poke(0x0003,(byte)0x12);

        cpu.setPC(0x0000);
        cpu.setHL((short) 0x5678);

        cpu.LD_HL_mm();

        assertAll("LD HL, (0x0000) Group",
                () -> assertEquals((short)0x1234, cpu.getHL(), "LD HL, (0x0000) Failed: HL<>0x1234 = " + Integer.toHexString(cpu.getHL())),
                () -> assertNotEquals((short)0x5678, cpu.getHL(), "LD HL, (0x0000) Failed: HL still 0x1234 = " + Integer.toHexString(cpu.getHL()))
        );
    }*/

    @Test
    void testLD_rp_p_mm(){
        long initTState;
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(8));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xED);
        compTest.poke(0x0001, (byte) 0x2A); // LD HL, (0x0004) without ED prefix
        compTest.poke(0x0002, (byte) 0x04);
        compTest.poke(0x0003, (byte) 0x00);
        compTest.poke(0x0004, (byte) 0x78); // sentinel 0x5678
        compTest.poke(0x0005, (byte) 0x56);

        cpu.setPC(0x0001);
        cpu.setHL((short) 0x1234);

        initTState = cpu.getTState();
        cpu.fetch(); // LD HL, (0x0004) without ED prefix

        assertAll("LD HL, (0x0004) Group without ED prefix",
                () -> assertEquals((byte) 0x78, cpu.getL(), "LD HL, (0x0004) Failed: L<>0x78=" + Integer.toHexString(cpu.getL())),
                () -> assertNotEquals((byte) 0x34, cpu.getL(), "LD HL, (0x0004) Failed: L still 0x34=" + Integer.toHexString(cpu.getL())),
                () -> assertEquals((byte) 0x56, cpu.getH(), "LD HL, (0x0004) Failed: H<>0x56" + Integer.toHexString(cpu.getH())),
                () -> assertNotEquals((byte) 0x12, cpu.getH(), "LD HL, (0x0004) Failed: H still 0x12=" + Integer.toHexString(cpu.getH())),
                () -> assertEquals((byte) 0x0004, cpu.getPC(), "LD HL, (0x0004) Failed: PC no was modified PC<>0x0004 PC=" + Integer.toHexString(cpu.getPC())),
                () -> assertEquals(16, cpu.getTState()-initTState, "LD HL, (0x0004) TState Failed")
        );

        compTest.poke(0x0004, (byte) 0x78);
        compTest.poke(0x0005, (byte) 0x56);

        cpu.setPC(0x0000);
        cpu.setBC((short) 0x1234);

        compTest.poke(0x0001, (byte) 0x4B); // LD BC, (0x0004)
        long initTState2 = cpu.getTState();
        cpu.fetch();

        assertAll("LD BC, (0x0004) Group",
                () -> assertEquals((byte) 0x78, cpu.getC(), "LD BC, (0x0004) Failed: C<>0x78=" + Integer.toHexString(cpu.getC())),
                () -> assertNotEquals((byte) 0x34, cpu.getC(), "LD BC, (0x0004) Failed: C still 0x34=" + Integer.toHexString(cpu.getC())),
                () -> assertEquals((byte) 0x56, cpu.getB(), "LD BC, (0x0004) Failed: B<>0x56" + Integer.toHexString(cpu.getB())),
                () -> assertNotEquals((byte) 0x12, cpu.getB(), "LD BC, (0x0004) Failed: B still 0x12=" + Integer.toHexString(cpu.getB())),
                () -> assertEquals((byte) 0x0004, cpu.getPC(), "LD BC, (0x0004) Failed: PC no was modified PC<>0x0004 PC=" + Integer.toHexString(cpu.getPC())),
                () -> assertEquals(20, cpu.getTState()-initTState2, "LD BC, (0x0004) TState Failed")
        );

        compTest.poke(0x0004, (byte) 0x78);
        compTest.poke(0x0005, (byte) 0x56);

        cpu.setPC(0x0000);
        cpu.setDE((short) 0x1234);

        compTest.poke(0x0001, (byte) 0x5B); // LD DE, (0x0004)
        cpu.fetch();

        assertAll("LD DE, (0x0004) Group",
                () -> assertEquals((byte) 0x78, cpu.getE(), "LD DE, (0x0004) Failed: E<>0x78=" + Integer.toHexString(cpu.getE())),
                () -> assertNotEquals((byte) 0x34, cpu.getE(), "LD DE, (0x0004) Failed: E still 0x78=" + Integer.toHexString(cpu.getE())),
                () -> assertEquals((byte) 0x56, cpu.getD(), "LD DE, (0x0004) Failed: D<>0x56" + Integer.toHexString(cpu.getD())),
                () -> assertNotEquals((byte) 0x12, cpu.getD(), "LD DE, (0x0004) Failed: D still 0x12=" + Integer.toHexString(cpu.getD())),
                () -> assertEquals((byte) 0x0004, cpu.getPC(), "LD DE, (0x0004) Failed: PC no was modified PC<>0x0004 PC=" + Integer.toHexString(cpu.getPC()))
        );

        compTest.poke(0x0004, (byte) 0x78);
        compTest.poke(0x0005, (byte) 0x56);

        cpu.setPC(0x0000);
        cpu.setHL((short) 0x1234);

        compTest.poke(0x0001, (byte) 0x6B); // LD HL, (0x0004) with ED prefix
        cpu.fetch();

        assertAll("LD HL, (0x0004) Group with ED prefix",
                () -> assertEquals((byte) 0x78, cpu.getL(), "LD HL, (0x0004) Failed: L<>0x78=" + Integer.toHexString(cpu.getL())),
                () -> assertNotEquals((byte) 0x34, cpu.getL(), "LD HL, (0x0004) Failed: L still 0x34=" + Integer.toHexString(cpu.getL())),
                () -> assertEquals((byte) 0x56, cpu.getH(), "LD HL, (0x0004) Failed: H<>0x56" + Integer.toHexString(cpu.getH())),
                () -> assertNotEquals((byte) 0x12, cpu.getH(), "LD HL, (0x0004) Failed: H still 0x12=" + Integer.toHexString(cpu.getH())),
                () -> assertEquals((byte) 0x0004, cpu.getPC(), "LD HL, (0x0004) Failed: PC no was modified PC<>0x0004 PC=" + Integer.toHexString(cpu.getPC()))
        );

        compTest.poke(0x0004, (byte) 0x78);
        compTest.poke(0x0005, (byte) 0x56);

        cpu.setPC(0x0000);
        cpu.setSP((short) 0x1234);

        compTest.poke(0x0001, (byte) 0x7B); // LD SP, (0x0004)
        cpu.fetch();

        assertAll("LD SP, (0x0004) Group",
                () -> assertEquals(0x5678, cpu.getSP(), "LD SP, (0x0004) Failed: SP<>0x5678=" + Integer.toHexString(cpu.getSP())),
                () -> assertNotEquals((byte) 0x1234, cpu.getSP(), "LD SP, (0x0004) Failed: SP still 0x1234=" + Integer.toHexString(cpu.getSP())),
                () -> assertEquals((byte) 0x0004, cpu.getPC(), "LD SP, (0x0004) Failed: PC no was modified PC<>0x0004 PC=" + Integer.toHexString(cpu.getPC()))
        );
    }

    @Test
    void testLD_A_mm(){
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000,(byte)0x02);
        compTest.poke(0x0001,(byte)0x00);
        compTest.poke(0x0002,(byte)0x12);

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x34);

        long initTState = cpu.getTState();
        cpu.LD_A_mm();

        assertAll("LD A, (0x0000) Group",
                () -> assertEquals((byte)0x12, cpu.getA(), "LD A, (0x0000) Failed: A<>0x12 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte)0x34, cpu.getA(), "LD A, (0x0000) Failed: A still 0x34 = " + Integer.toHexString(cpu.getA())),
                () -> assertEquals(13, cpu.getTState()-initTState, "LD A, (0x0000) TState Failed")
        );
    }

    @Test
    void testLD_r_y_n() {
        long initTState;
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(8));
        cpu.setComputer(compTest);
        compTest.poke(0x0000,(byte)0x20);
        compTest.poke(0x0001,(byte)0x21);
        compTest.poke(0x0002,(byte)0x22);
        compTest.poke(0x0003,(byte)0x23);
        compTest.poke(0x0004,(byte)0x24);
        compTest.poke(0x0005,(byte)0x25);
        compTest.poke(0x0006,(byte)0x27);
        compTest.poke(0x0007,(byte)0x26);

        cpu.setPC(0x0000);

        cpu.setB((byte)0x10);
        cpu.setC((byte)0x11);
        cpu.setD((byte)0x12);
        cpu.setE((byte)0x13);
        cpu.setH((byte)0x14);
        cpu.setL((byte)0x15);
        // (HL)
        cpu.setA((byte)0x17);

        cpu.fetch((byte)0x06); // LD B, n
        cpu.fetch((byte)0x0E); // LD C, n
        cpu.fetch((byte)0x16); // LD D, n
        cpu.fetch((byte)0x1E); // LD E, n
        cpu.fetch((byte)0x26); // LD H, n
        cpu.fetch((byte)0x2E); // LD L, n

        cpu.fetch((byte)0x3E); // LD A, n

        assertAll("LD r[y], n Group",
                () -> assertEquals((short)0x20, cpu.getB(), "LD B, 0x20 Failed: B<>0x20 = " + Integer.toHexString(cpu.getB())),
                () -> assertNotEquals((short)0x10, cpu.getB(), "LD B, 0x20 Failed: B still 0x10 = " + Integer.toHexString(cpu.getB())),

                () -> assertEquals((short)0x21, cpu.getC(), "LD C, 0x21 Failed: C<>0x21 = " + Integer.toHexString(cpu.getC())),
                () -> assertNotEquals((short)0x11, cpu.getC(), "LD C, 0x21 Failed: C still 0x20 = " + Integer.toHexString(cpu.getC())),

                () -> assertEquals((short)0x22, cpu.getD(), "LD D, 0x22 Failed: D<>0x22 = " + Integer.toHexString(cpu.getD())),
                () -> assertNotEquals((short)0x12, cpu.getD(), "LD D, 0x22 Failed: D still 0x12 = " + Integer.toHexString(cpu.getD())),

                () -> assertEquals((short)0x23, cpu.getE(), "LD E, 0x23 Failed: E<>0x23 = " + Integer.toHexString(cpu.getE())),
                () -> assertNotEquals((short)0x13, cpu.getE(), "LD E, 0x23 Failed: E still 0x13 = " + Integer.toHexString(cpu.getE())),

                () -> assertEquals((short)0x24, cpu.getH(), "LD H, 0x24 Failed: H<>0x24 = " + Integer.toHexString(cpu.getH())),
                () -> assertNotEquals((short)0x14, cpu.getH(), "LD H, 0x24 Failed: H still 0x14 = " + Integer.toHexString(cpu.getH())),

                () -> assertEquals((short)0x25, cpu.getL(), "LD L, 0x25 Failed: L<>0x25 = " + Integer.toHexString(cpu.getL())),
                () -> assertNotEquals((short)0x15, cpu.getL(), "LD L, 0x25 Failed: L still 0x15 = " + Integer.toHexString(cpu.getL())),

                () -> assertEquals((short)0x27, cpu.getA(), "LD A, 0x27 Failed: A<>0x27 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((short)0x17, cpu.getA(), "LD A, 0x27 Failed: A still 0x17 = " + Integer.toHexString(cpu.getA()))
        );

        cpu.setHL((short)0x0006);

        initTState = cpu.getTState();
        cpu.fetch((byte)0x36); // LD (HL), n

        assertAll("LD r[y], n Group",
                () -> assertEquals((short)0x26, compTest.peek(cpu.getHL()), "LD (HL), 0x26 Failed: (HL)<>0x26 = " + Integer.toHexString(compTest.peek(cpu.getHL()))),
                () -> assertNotEquals((short)0x27, compTest.peek(cpu.getHL()), "LD (HL), 0x26 Failed: (HL) still 0x27 = " + Integer.toHexString(compTest.peek(cpu.getHL()))),
                () -> assertEquals(10, cpu.getTState()-initTState, "LD (HL), 0x26 TState Failed")
        );
    }

    @Test
    void testLD_r_y_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();
        long initTState;

        cpu.setB((byte)0x10);
        cpu.setC((byte)0x11);
        cpu.setD((byte)0x12);
        cpu.setE((byte)0x13);
        cpu.setH((byte)0x14);
        cpu.setL((byte)0x15);

        // assertAll("LD A, r[z] Group",
        initTState = cpu.getTState();
        cpu.fetch((byte)0x78); // LD A, B
        assertEquals((byte) 0x10, cpu.getA(), "LD A, B Failed: A<>0x10 = " + Integer.toHexString(cpu.getA()));
        assertEquals(4, cpu.getTState()-initTState, "LD A, B TState Failed");

        cpu.fetch((byte)0x79); // LD A, C
        assertEquals((byte) 0x11, cpu.getA(), "LD A, C Failed: A<>0x11 = " + Integer.toHexString(cpu.getA()));

        cpu.fetch((byte)0x7A); // LD A, D
        assertEquals((byte) 0x12, cpu.getA(), "LD A, D Failed: A<>0x12 = " + Integer.toHexString(cpu.getA()));

        cpu.fetch((byte)0x7B); // LD A, E
        assertEquals((byte) 0x13, cpu.getA(), "LD A, E Failed: A<>0x13 = " + Integer.toHexString(cpu.getA()));

        cpu.fetch((byte)0x7C); // LD A, H
        assertEquals((byte) 0x14, cpu.getA(), "LD A, H Failed: A<>0x14 = " + Integer.toHexString(cpu.getA()));

        cpu.fetch((byte)0x7D); // LD A, L
        assertEquals((byte) 0x15, cpu.getA(), "LD A, L Failed: A<>0x15 = " + Integer.toHexString(cpu.getA()));

        cpu.setA((byte)0x20);

        cpu.fetch((byte)0x47); // LD B, A
        cpu.fetch((byte)0x4F); // LD C, A
        cpu.fetch((byte)0x57); // LD D, A
        cpu.fetch((byte)0x5F); // LD E, A
        cpu.fetch((byte)0x67); // LD H, A
        cpu.fetch((byte)0x6F); // LD L, A

        assertAll("LD r[y], A Group",
                () -> assertEquals((byte) 0x20, cpu.getB(), "LD B, A Failed: B<>0x20 = " + Integer.toHexString(cpu.getB())),
                // () -> assertNotEquals() missing TODO?...
                () -> assertEquals((byte) 0x20, cpu.getC(), "LD C, A Failed: C<>0x20 = " + Integer.toHexString(cpu.getC())),
                () -> assertEquals((byte) 0x20, cpu.getD(), "LD D, A Failed: D<>0x20 = " + Integer.toHexString(cpu.getD())),
                () -> assertEquals((byte) 0x20, cpu.getE(), "LD E, A Failed: E<>0x20 = " + Integer.toHexString(cpu.getE())),
                () -> assertEquals((byte) 0x20, cpu.getH(), "LD H, A Failed: H<>0x20 = " + Integer.toHexString(cpu.getH())),
                () -> assertEquals((byte) 0x20, cpu.getL(), "LD L, A Failed: L<>0x20 = " + Integer.toHexString(cpu.getL()))
        );

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);

        cpu.setHL((short) 0x0000);
        cpu.setA((byte) 0x0FF);
        compTest.poke(0x0000, (byte) 0x00);

        long initTState2 = cpu.getTState();
        cpu.fetch((byte)0x7E); // LD A, (HL)

        assertAll(
                () -> assertEquals(0, cpu.getA(), "LD A, (HL) Failed: A still 0xFF = " + Integer.toHexString(cpu.getA())),
                () -> assertEquals(7, cpu.getTState()-initTState2, "LD A, (HL) TState Failed")
        );
    }

    @Test
    void testLD_X_r_y_r_z() {
        long initTState;
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000,(byte)0xDD);

        cpu.setB((byte)0x10);
        cpu.setC((byte)0x11);
        cpu.setD((byte)0x12);
        cpu.setE((byte)0x13);

        // assertAll("LD A, r[z] Group with DD prefix",
        cpu.setPC(0x0000);
        compTest.poke(0x0001,(byte)0x60);
        initTState = cpu.getTState();
        cpu.fetch(); // LD IXH, B
        assertEquals((byte) 0x10, cpu.getIXH(), "LD IXH, B Failed: IXH<>0x10 = " + Integer.toHexString(cpu.getIXH()));
        assertEquals(8, cpu.getTState()-initTState, "LD IXH, B TState Failed");

        cpu.setPC(0x0000);
        compTest.poke(0x0001,(byte)0x69);
        cpu.fetch(); // LD IXL, C
        assertEquals((byte) 0x11, cpu.getIXL(), "LD IXL, C Failed: IXL<>0x11 = " + Integer.toHexString(cpu.getIXL()));

        cpu.setIXH((byte)0x14);
        cpu.setIXL((byte)0x15);

        cpu.setPC(0x0000);
        compTest.poke(0x0001,(byte)0x78);
        initTState = cpu.getTState();
        cpu.fetch(); // LD A, B
        assertEquals((byte) 0x10, cpu.getA(), "LD A, B Failed: A<>0x10 = " + Integer.toHexString(cpu.getA()));
        assertEquals(8, cpu.getTState()-initTState, "LD A, B TState Failed");

        cpu.setPC(0x0000);
        compTest.poke(0x0001,(byte)0x79);
        cpu.fetch(); // LD A, C
        assertEquals((byte) 0x11, cpu.getA(), "LD A, C Failed: A<>0x11 = " + Integer.toHexString(cpu.getA()));

        cpu.setPC(0x0000);
        compTest.poke(0x0001,(byte)0x7A);
        cpu.fetch(); // LD A, D
        assertEquals((byte) 0x12, cpu.getA(), "LD A, D Failed: A<>0x12 = " + Integer.toHexString(cpu.getA()));

        cpu.setPC(0x0000);
        compTest.poke(0x0001,(byte)0x7B);
        cpu.fetch(); // LD A, E
        assertEquals((byte) 0x13, cpu.getA(), "LD A, E Failed: A<>0x13 = " + Integer.toHexString(cpu.getA()));

        cpu.setPC(0x0000);
        compTest.poke(0x0001,(byte)0x7C);
        cpu.fetch(); // LD A, IXH
        assertEquals((byte) 0x14, cpu.getA(), "LD A, IXH Failed: A<>0x14 = " + Integer.toHexString(cpu.getA()));

        cpu.setPC(0x0000);
        compTest.poke(0x0001,(byte)0x7D);
        cpu.fetch(); // LD A, IXL
        assertEquals((byte) 0x15, cpu.getA(), "LD A, IXL Failed: A<>0x15 = " + Integer.toHexString(cpu.getA()));

        cpu.setPC(0x0000);
        compTest.poke(0x0001,(byte)0x3E);
        compTest.poke(0x0002,(byte)0x16);
        cpu.fetch(); // LD A, 0x16 : LD prefixed by 0xDD
        assertEquals((byte) 0x16, cpu.getA(), "LD A, 0x16 (prefixed by 0xDD) Failed: A<>0x16 = " + Integer.toHexString(cpu.getA()));
    }

    @Test
    void testLD_Y_r_y_r_z() {
        long initTState;
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000,(byte)0xFD);

        cpu.setB((byte)0x10);
        cpu.setC((byte)0x11);
        cpu.setD((byte)0x12);
        cpu.setE((byte)0x13);

        // assertAll("LD A, r[z] Group with FD prefix",
        cpu.setPC(0x0000);
        compTest.poke(0x0001,(byte)0x60);
        initTState = cpu.getTState();
        cpu.fetch(); // LD IYH, B
        assertEquals((byte) 0x10, cpu.getIYH(), "LD IYH, B Failed: IYH<>0x10 = " + Integer.toHexString(cpu.getIYH()));
        assertEquals(8, cpu.getTState()-initTState, "LD IYH, B TState Failed");

        cpu.setPC(0x0000);
        compTest.poke(0x0001,(byte)0x69);
        cpu.fetch(); // LD IYL, C
        assertEquals((byte) 0x11, cpu.getIYL(), "LD IYL, C Failed: IYL<>0x11 = " + Integer.toHexString(cpu.getIYL()));

        cpu.setIYH((byte)0x14);
        cpu.setIYL((byte)0x15);

        cpu.setPC(0x0000);
        compTest.poke(0x0001,(byte)0x78);
        initTState = cpu.getTState();
        cpu.fetch(); // LD A, B
        assertEquals((byte) 0x10, cpu.getA(), "LD A, B Failed: A<>0x10 = " + Integer.toHexString(cpu.getA()));
        assertEquals(8, cpu.getTState()-initTState, "LD A, B TState Failed");

        cpu.setPC(0x0000);
        compTest.poke(0x0001,(byte)0x79);
        cpu.fetch(); // LD A, C
        assertEquals((byte) 0x11, cpu.getA(), "LD A, C Failed: A<>0x11 = " + Integer.toHexString(cpu.getA()));

        cpu.setPC(0x0000);
        compTest.poke(0x0001,(byte)0x7A);
        cpu.fetch(); // LD A, D
        assertEquals((byte) 0x12, cpu.getA(), "LD A, D Failed: A<>0x12 = " + Integer.toHexString(cpu.getA()));

        cpu.setPC(0x0000);
        compTest.poke(0x0001,(byte)0x7B);
        cpu.fetch(); // LD A, E
        assertEquals((byte) 0x13, cpu.getA(), "LD A, E Failed: A<>0x13 = " + Integer.toHexString(cpu.getA()));

        cpu.setPC(0x0000);
        compTest.poke(0x0001,(byte)0x7C);
        cpu.fetch(); // LD A, IYH
        assertEquals((byte) 0x14, cpu.getA(), "LD A, IYH Failed: A<>0x14 = " + Integer.toHexString(cpu.getA()));

        cpu.setPC(0x0000);
        compTest.poke(0x0001,(byte)0x7D);
        cpu.fetch(); // LD A, IYL
        assertEquals((byte) 0x15, cpu.getA(), "LD A, IYL Failed: A<>0x15 = " + Integer.toHexString(cpu.getA()));

        cpu.setPC(0x0000);
        compTest.poke(0x0001,(byte)0x3E);
        compTest.poke(0x0002,(byte)0x16);
        cpu.fetch(); // LD A, 0x16 : LD prefixed by 0xFD
        assertEquals((byte) 0x16, cpu.getA(), "LD A, 0x16 (prefixed by 0xFD) Failed: A<>0x16 = " + Integer.toHexString(cpu.getA()));
    }

    @Test
    @Disabled("TODO")
    void LD_IXH_n() {

    }

    @Test
    @Disabled("TODO")
    void LD_IXL_n() {

    }

    @Test
    @Disabled("TODO")
    void LD_IYH_n() {

    }

    @Test
    @Disabled("TODO")
    void LD_IYL_n() {

    }

    @Test
    @Disabled("TODO")
    void LD_IXH_IXH() {

    }

    @Test
    @Disabled("TODO")
    void LD_IXL_IXL() {

    }

    @Test
    @Disabled("TODO")
    void LD_IYH_IYH() {

    }

    @Test
    @Disabled("TODO")
    void LD_IYL_IYL() {

    }

    @Test
    void testLD_SP_HL() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setSP((short) 0x0102);
        cpu.setHL((short) 0x0304);

        long initTState = cpu.getTState();
        cpu.LD_SP_HL();

        assertAll("LD SP, HL Group",
                () -> assertEquals((short)0x0304, cpu.getSP(), "LD SP, HL Failed: not was modified (SP="+ Integer.toHexString(cpu.getSP())+")"),
                () -> assertNotEquals((short)0x0102, cpu.getSP(), "LD SP, HL Failed: still is 0x0102 (SP="+ Integer.toHexString(cpu.getSP())+")"),
                () -> assertEquals(6, cpu.getTState()-initTState, "LD SP, HL TState Failed")
        );
    }

    @Test
    void testLD_SP_IX() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setSP((byte) 0xFFFF);
        cpu.setIX((byte) 0x0000);
        compTest.poke(0x0000, (byte) 0xDD); // DD prefix
        compTest.poke(0x0001, (byte) 0xF9); // LD SP, IX

        long initTState = cpu.getTState();
        cpu.fetch(); // FD prefix + LD SP, IX

        assertAll("LD SP, IX Group",
                () -> assertEquals((byte) 0x0000, cpu.getSP(), "LD SP, IX Failed: SP<>0x0000"),
                () -> assertNotEquals((byte) 0xFFFF, cpu.getSP(), "LD SP, IX Failed: SP still 0xFFF"),

                () -> assertEquals(10, cpu.getTState()-initTState, "LD SP, IX TState Failed")
        );
    }

    @Test
    void testLD_SP_IY() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setSP((byte) 0xFFFF);
        cpu.setIY((byte) 0x0000);
        compTest.poke(0x0000, (byte) 0xFD); // FD prefix
        compTest.poke(0x0001, (byte) 0xF9); // LD SP, IY

        long initTState = cpu.getTState();
        cpu.fetch(); // FD prefix + LD SP, IY

        assertAll("LD SP, IY Group",
                () -> assertEquals((byte) 0x0000, cpu.getSP(), "LD SP, IY Failed: SP<>0x0000"),
                () -> assertNotEquals((byte) 0xFFFF, cpu.getSP(), "LD SP, IY Failed: SP still 0xFFF"),

                () -> assertEquals(10, cpu.getTState()-initTState, "LD SP, IY TState Failed")
        );
    }

    @Test
    void testLD_A_I() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setI((byte) 0xFF);
        cpu.setA((byte) 0x00);
        cpu.setF((byte) 0x00);
        compTest.poke(0x0000, (byte) 0xED); // ED prefix
        compTest.poke(0x0001, (byte) 0x57); // LD A, I
        long initTState = cpu.getTState();
        cpu.fetch(); // ED prefix + LD A, I

        assertAll("LD A, I Group",
                () -> assertEquals((byte) 0xFF, cpu.getA(), "LD A, I Failed: A<>0xFF"),
                () -> assertTrue(cpu.getSF(), "LD A, I Failed: S Flag incorrectly set"),
                () -> assertFalse(cpu.getNF(), "LD A, I Failed: N Flag incorrectly set"),
                () -> assertFalse(cpu.getHF(), "LD A, I Failed: H Flag incorrectly set"),

                () -> assertEquals(9, cpu.getTState()-initTState, "LD A, I TState Failed")
        );


        cpu.setPC(0x0000);
        cpu.setI((byte) 0x00);
        cpu.setA((byte) 0xFF);
        cpu.setF((byte) 0x00);

        cpu.fetch(); // ED prefix + LD A, I

        assertAll("LD A, I Group",
                () -> assertEquals((byte) 0x00, cpu.getA(), "LD A, I Failed: A<>0x00"),
                () -> assertTrue(cpu.getZF(), "LD A, I Failed: Z Flag incorrectly set")
        );

        cpu.setPC(0x0000);
        cpu.DI();

        cpu.fetch(); // ED prefix + LD A, I

        assertFalse(cpu.getPF(), "LD A, I Failed: P/V Flag incorrectly set (IFF2 must be false)");

        cpu.setPC(0x0000);
        cpu.EI();

        cpu.fetch(); // ED prefix + LD A, I

        assertTrue(cpu.getPF(), "LD A, I Failed: P/V Flag incorrectly set (IFF2 must be true)");
    }

    @Test
    void testLD_I_A() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(16));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setA((byte) 0xAA);
        cpu.setI((byte) 0x00);
        cpu.setF((byte) 0x00);
        compTest.poke(0x0000, (byte) 0xED); // ED prefix
        compTest.poke(0x0001, (byte) 0x47); // LD I, A
        long initTState = cpu.getTState();
        cpu.fetch(); // ED prefix + LD I, A

        assertAll("LD I, A Group",
                () -> assertEquals((byte) 0xAA, cpu.getI(), "LD I, A Failed: I<>0xAA"),
                () -> assertEquals((byte) 0xAA, cpu.getA(), "LD I, A Failed: A modified"),
                () -> assertEquals((byte) 0x00, cpu.getF(), "LD I, A Failed: F was affected "+Integer.toHexString(cpu.getF())+")"),

                () -> assertEquals(9, cpu.getTState()-initTState, "LD I, A TState Failed")
        );
    }

    @Test
    void testLD_A_R() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setR((byte) 0xFF);
        cpu.setA((byte) 0x00);
        cpu.setF((byte) 0x00);
        compTest.poke(0x0000, (byte) 0xED); // ED prefix
        compTest.poke(0x0001, (byte) 0x5F); // LD A, R
        long initTState = cpu.getTState();
        cpu.fetch(); // ED prefix + LD A, R

        assertAll("LD A, R Group",
                () -> assertEquals((byte) 0xFF, cpu.getA(), "LD A, R Failed: A<>0xFF"),
                () -> assertTrue(cpu.getSF(), "LD A, R Failed: S Flag incorrectly set"),
                () -> assertFalse(cpu.getNF(), "LD A, R Failed: N Flag incorrectly set"),
                () -> assertFalse(cpu.getHF(), "LD A, R Failed: H Flag incorrectly set"),

                () -> assertEquals(9, cpu.getTState()-initTState, "LD A, R TState Failed")
        );


        cpu.setPC(0x0000);
        cpu.setR((byte) 0x00);
        cpu.setA((byte) 0xFF);
        cpu.setF((byte) 0x00);

        cpu.fetch(); // ED prefix + LD A, R

        assertAll("LD A, R Group",
                () -> assertEquals((byte) 0x00, cpu.getA(), "LD A, R Failed: A<>0x00"),
                () -> assertTrue(cpu.getZF(), "LD A, R Failed: Z Flag incorrectly set")
        );

        cpu.setPC(0x0000);
        cpu.DI();

        cpu.fetch(); // ED prefix + LD A, R

        assertFalse(cpu.getPF(), "LD A, R Failed: P/V Flag incorrectly set (IFF2 must be false)");

        cpu.setPC(0x0000);
        cpu.EI();

        cpu.fetch(); // ED prefix + LD A, R

        assertTrue(cpu.getPF(), "LD A, R Failed: P/V Flag incorrectly set (IFF2 must be true)");
    }

    @Test
    void testLD_R_A() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(16));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setA((byte) 0x77);
        cpu.setR((byte) 0x00);
        compTest.poke(0x0000, (byte) 0xED); // ED prefix
        compTest.poke(0x0001, (byte) 0x4F); // LD R, A
        long initTState = cpu.getTState();
        cpu.fetch(); // ED prefix + LD R, A

        assertAll("LD R, A Group",
                () -> assertEquals((byte) 0x77, cpu.getR(), "LD R, A Failed: R<>0x77"),
                () -> assertEquals((byte) 0x77, cpu.getA(), "LD R, A Failed: A modified"),

                () -> assertEquals(9, cpu.getTState()-initTState, "LD R, A TState Failed")
        );
    }

    @Test
    void testLD_A_IX_d() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(16));
        cpu.setComputer(compTest);

        compTest.poke(0x0000, (byte) 0xDD); // DD prefix
        compTest.poke(0x0001, (byte) 0x7E); // LD A, (IX+d)
        compTest.poke(0x0002, (byte) 0x03); // displacement = 3
        compTest.poke(0x0008, (byte) 0xAA); // data at IX+3

        cpu.setPC(0x0000);
        cpu.setIX((short) 0x0005); // IX = 0x0005, so IX+3 = 0x0008
        cpu.setA((byte) 0x00);

        long initTState = cpu.getTState();
        cpu.fetch(); // DD 7E 03

        assertAll("LD A, (IX+d) Group",
                () -> assertEquals((byte) 0xAA, cpu.getA(), "LD A, (IX+3) Failed: A<>0xAA = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0x00, cpu.getA(), "LD A, (IX+3) Failed: A still 0x00 = " + Integer.toHexString(cpu.getA())),
                () -> assertEquals(19, cpu.getTState()-initTState, "LD A, (IX+3) TState Failed")
        );
    }

    @Test
    void testLD_A_IY_d() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(16));
        cpu.setComputer(compTest);

        compTest.poke(0x0000, (byte) 0xFD); // FD prefix
        compTest.poke(0x0001, (byte) 0x7E); // LD A, (IY+d)
        compTest.poke(0x0002, (byte) 0x02); // displacement = 2
        compTest.poke(0x000A, (byte) 0xBB); // data at IY+2

        cpu.setPC(0x0000);
        cpu.setIY((short) 0x0008); // IY = 0x0008, so IY+2 = 0x000A
        cpu.setA((byte) 0x00);

        long initTState = cpu.getTState();
        cpu.fetch(); // FD 7E 02

        assertAll("LD A, (IY+d) Group",
                () -> assertEquals((byte) 0xBB, cpu.getA(), "LD A, (IY+2) Failed: A<>0xBB = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0x00, cpu.getA(), "LD A, (IY+2) Failed: A still 0x00 = " + Integer.toHexString(cpu.getA())),
                () -> assertEquals(19, cpu.getTState()-initTState, "LD A, (IY+2) TState Failed")
        );
    }

    @Test
    @Disabled("TODO")
    void testLD_IX_nn() {

    }

    @Test
    @Disabled("TODO")
    void testLD_mm_IX() {

    }

    @Test
    @Disabled("TODO")
    void testLD_IX_mm() {

    }

    @Test
    @Disabled("TODO")
    void testLD_IY_nn() {

    }

    @Test
    @Disabled("TODO")
    void testLD_mm_IY() {

    }

    @Test
    @Disabled("TODO")
    void testLD_IY_mm() {

    }

    @Test
    void testLD_IX_d_A() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(16));
        cpu.setComputer(compTest);

        compTest.poke(0x0000, (byte) 0xDD); // DD prefix
        compTest.poke(0x0001, (byte) 0x77); // LD (IX+d), A
        compTest.poke(0x0002, (byte) 0x05); // displacement = 5
        compTest.poke(0x000A, (byte) 0x00); // initial data at IX+5

        cpu.setPC(0x0000);
        cpu.setIX((short) 0x0005); // IX = 0x0005, so IX+5 = 0x000A
        cpu.setA((byte) 0xCC);

        long initTState = cpu.getTState();
        cpu.fetch(); // DD 77 05

        assertAll("LD (IX+d), A Group",
                () -> assertEquals((byte) 0xCC, compTest.peek(0x000A), "LD (IX+5), A Failed: (IX+5)<>0xCC = " + Integer.toHexString(compTest.peek(0x000A))),
                () -> assertNotEquals((byte) 0x00, compTest.peek(0x000A), "LD (IX+5), A Failed: (IX+5) still 0x00 = " + Integer.toHexString(compTest.peek(0x000A))),
                () -> assertEquals(19, cpu.getTState()-initTState, "LD (IX+5), A TState Failed")
        );
    }

    @Test
    void testLD_IY_d_A() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(16));
        cpu.setComputer(compTest);

        compTest.poke(0x0000, (byte) 0xFD); // FD prefix
        compTest.poke(0x0001, (byte) 0x77); // LD (IY+d), A
        compTest.poke(0x0002, (byte) 0x04); // displacement = 4
        compTest.poke(0x000C, (byte) 0x00); // initial data at IY+4

        cpu.setPC(0x0000);
        cpu.setIY((short) 0x0008); // IY = 0x0008, so IY+4 = 0x000C
        cpu.setA((byte) 0xDD);

        long initTState = cpu.getTState();
        cpu.fetch(); // FD 77 04

        assertAll("LD (IY+d), A Group",
                () -> assertEquals((byte) 0xDD, compTest.peek(0x000C), "LD (IY+4), A Failed: (IY+4)<>0xDD = " + Integer.toHexString(compTest.peek(0x000C))),
                () -> assertNotEquals((byte) 0x00, compTest.peek(0x000C), "LD (IY+4), A Failed: (IY+4) still 0x00 = " + Integer.toHexString(compTest.peek(0x000C))),
                () -> assertEquals(19, cpu.getTState()-initTState, "LD (IY+4), A TState Failed")
        );
    }

    @Test
    void testLD_r_IX_d() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(16));
        cpu.setComputer(compTest);

        compTest.poke(0x0008, (byte) 0x11);
        compTest.poke(0x0009, (byte) 0x22);
        compTest.poke(0x000A, (byte) 0x33);
        compTest.poke(0x000B, (byte) 0x44);
        compTest.poke(0x000C, (byte) 0x55);
        compTest.poke(0x000D, (byte) 0x66);

        cpu.setIX((short) 0x0008);

        // LD B, (IX+1)
        compTest.poke(0x0000, (byte) 0xDD);
        compTest.poke(0x0001, (byte) 0x46);
        compTest.poke(0x0002, (byte) 0x01);
        cpu.setPC(0x0000);
        cpu.setB((byte) 0x00);
        long initTState = cpu.getTState();
        cpu.fetch();
        
        assertAll("LD B, (IX+d) Group",
                () -> assertEquals((byte) 0x22, cpu.getB(), "LD B, (IX+1) Failed: B<>0x22 = " + Integer.toHexString(cpu.getB())),
                () -> assertNotEquals((byte) 0x00, cpu.getB(), "LD B, (IX+1) Failed: B still 0x00 = " + Integer.toHexString(cpu.getB())),
                () -> assertEquals(19, cpu.getTState()-initTState, "LD B, (IX+1) TState Failed")
        );

        // LD C, (IX+2)
        compTest.poke(0x0000, (byte) 0xDD);
        compTest.poke(0x0001, (byte) 0x4E);
        compTest.poke(0x0002, (byte) 0x02);
        cpu.setPC(0x0000);
        cpu.setC((byte) 0x00);
        cpu.fetch();
        assertEquals((byte) 0x33, cpu.getC(), "LD C, (IX+2) Failed: C<>0x33 = " + Integer.toHexString(cpu.getC()));

        // LD D, (IX+3)
        compTest.poke(0x0000, (byte) 0xDD);
        compTest.poke(0x0001, (byte) 0x56);
        compTest.poke(0x0002, (byte) 0x03);
        cpu.setPC(0x0000);
        cpu.setD((byte) 0x00);
        cpu.fetch();
        assertEquals((byte) 0x44, cpu.getD(), "LD D, (IX+3) Failed: D<>0x44 = " + Integer.toHexString(cpu.getD()));

        // LD E, (IX+4)
        compTest.poke(0x0000, (byte) 0xDD);
        compTest.poke(0x0001, (byte) 0x5E);
        compTest.poke(0x0002, (byte) 0x04);
        cpu.setPC(0x0000);
        cpu.setE((byte) 0x00);
        cpu.fetch();
        assertEquals((byte) 0x55, cpu.getE(), "LD E, (IX+4) Failed: E<>0x55 = " + Integer.toHexString(cpu.getE()));

        // LD H, (IX+5)
        compTest.poke(0x0000, (byte) 0xDD);
        compTest.poke(0x0001, (byte) 0x66);
        compTest.poke(0x0002, (byte) 0x05);
        cpu.setPC(0x0000);
        cpu.setH((byte) 0x00);
        cpu.fetch();
        assertEquals((byte) 0x66, cpu.getH(), "LD H, (IX+5) Failed: H<>0x66 = " + Integer.toHexString(cpu.getH()));

        // LD L, (IX+0)
        compTest.poke(0x0000, (byte) 0xDD);
        compTest.poke(0x0001, (byte) 0x6E);
        compTest.poke(0x0002, (byte) 0x00);
        cpu.setPC(0x0000);
        cpu.setL((byte) 0x00);
        cpu.fetch();
        assertEquals((byte) 0x11, cpu.getL(), "LD L, (IX+0) Failed: L<>0x11 = " + Integer.toHexString(cpu.getL()));
    }

    @Test
    void testLD_r_IY_d() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(16));
        cpu.setComputer(compTest);

        compTest.poke(0x0008, (byte) 0xAA);
        compTest.poke(0x0009, (byte) 0xBB);
        compTest.poke(0x000A, (byte) 0xCC);
        compTest.poke(0x000B, (byte) 0xDD);
        compTest.poke(0x000C, (byte) 0xEE);
        compTest.poke(0x000D, (byte) 0xFF);

        cpu.setIY((short) 0x0008);

        // LD B, (IY+1)
        compTest.poke(0x0000, (byte) 0xFD);
        compTest.poke(0x0001, (byte) 0x46);
        compTest.poke(0x0002, (byte) 0x01);
        cpu.setPC(0x0000);
        cpu.setB((byte) 0x00);
        long initTState = cpu.getTState();
        cpu.fetch();
        
        assertAll("LD B, (IY+d) Group",
                () -> assertEquals((byte) 0xBB, cpu.getB(), "LD B, (IY+1) Failed: B<>0xBB = " + Integer.toHexString(cpu.getB())),
                () -> assertNotEquals((byte) 0x00, cpu.getB(), "LD B, (IY+1) Failed: B still 0x00 = " + Integer.toHexString(cpu.getB())),
                () -> assertEquals(19, cpu.getTState()-initTState, "LD B, (IY+1) TState Failed")
        );

        // LD C, (IY+2)
        compTest.poke(0x0000, (byte) 0xFD);
        compTest.poke(0x0001, (byte) 0x4E);
        compTest.poke(0x0002, (byte) 0x02);
        cpu.setPC(0x0000);
        cpu.setC((byte) 0x00);
        cpu.fetch();
        assertEquals((byte) 0xCC, cpu.getC(), "LD C, (IY+2) Failed: C<>0xCC = " + Integer.toHexString(cpu.getC()));

        // LD D, (IY+3)
        compTest.poke(0x0000, (byte) 0xFD);
        compTest.poke(0x0001, (byte) 0x56);
        compTest.poke(0x0002, (byte) 0x03);
        cpu.setPC(0x0000);
        cpu.setD((byte) 0x00);
        cpu.fetch();
        assertEquals((byte) 0xDD, cpu.getD(), "LD D, (IY+3) Failed: D<>0xDD = " + Integer.toHexString(cpu.getD()));

        // LD E, (IY+4)
        compTest.poke(0x0000, (byte) 0xFD);
        compTest.poke(0x0001, (byte) 0x5E);
        compTest.poke(0x0002, (byte) 0x04);
        cpu.setPC(0x0000);
        cpu.setE((byte) 0x00);
        cpu.fetch();
        assertEquals((byte) 0xEE, cpu.getE(), "LD E, (IY+4) Failed: E<>0xEE = " + Integer.toHexString(cpu.getE()));

        // LD H, (IY+5)
        compTest.poke(0x0000, (byte) 0xFD);
        compTest.poke(0x0001, (byte) 0x66);
        compTest.poke(0x0002, (byte) 0x05);
        cpu.setPC(0x0000);
        cpu.setH((byte) 0x00);
        cpu.fetch();
        assertEquals((byte) 0xFF, cpu.getH(), "LD H, (IY+5) Failed: H<>0xFF = " + Integer.toHexString(cpu.getH()));

        // LD L, (IY+0)
        compTest.poke(0x0000, (byte) 0xFD);
        compTest.poke(0x0001, (byte) 0x6E);
        compTest.poke(0x0002, (byte) 0x00);
        cpu.setPC(0x0000);
        cpu.setL((byte) 0x00);
        cpu.fetch();
        assertEquals((byte) 0xAA, cpu.getL(), "LD L, (IY+0) Failed: L<>0xAA = " + Integer.toHexString(cpu.getL()));
    }

    @Test
    void testLD_IX_d_r() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(16));
        cpu.setComputer(compTest);

        // Initialize memory with zeros
        for (int i = 0x0008; i <= 0x000E; i++) {
            compTest.poke(i, (byte) 0x00);
        }

        cpu.setIX((short) 0x0008);
        cpu.setB((byte) 0x11);
        cpu.setC((byte) 0x22);
        cpu.setD((byte) 0x33);
        cpu.setE((byte) 0x44);
        cpu.setH((byte) 0x55);
        cpu.setL((byte) 0x66);

        // LD (IX+0), B
        compTest.poke(0x0000, (byte) 0xDD);
        compTest.poke(0x0001, (byte) 0x70);
        compTest.poke(0x0002, (byte) 0x00);
        cpu.setPC(0x0000);
        long initTState = cpu.getTState();
        cpu.fetch();
        
        assertAll("LD (IX+d), r Group",
                () -> assertEquals((byte) 0x11, compTest.peek(0x0008), "LD (IX+0), B Failed: (IX+0)<>0x11 = " + Integer.toHexString(compTest.peek(0x0008))),
                () -> assertNotEquals((byte) 0x00, compTest.peek(0x0008), "LD (IX+0), B Failed: (IX+0) still 0x00 = " + Integer.toHexString(compTest.peek(0x0008))),
                () -> assertEquals(19, cpu.getTState()-initTState, "LD (IX+0), B TState Failed")
        );

        // LD (IX+1), C
        compTest.poke(0x0000, (byte) 0xDD);
        compTest.poke(0x0001, (byte) 0x71);
        compTest.poke(0x0002, (byte) 0x01);
        cpu.setPC(0x0000);
        cpu.fetch();
        assertEquals((byte) 0x22, compTest.peek(0x0009), "LD (IX+1), C Failed: (IX+1)<>0x22 = " + Integer.toHexString(compTest.peek(0x0009)));

        // LD (IX+2), D
        compTest.poke(0x0000, (byte) 0xDD);
        compTest.poke(0x0001, (byte) 0x72);
        compTest.poke(0x0002, (byte) 0x02);
        cpu.setPC(0x0000);
        cpu.fetch();
        assertEquals((byte) 0x33, compTest.peek(0x000A), "LD (IX+2), D Failed: (IX+2)<>0x33 = " + Integer.toHexString(compTest.peek(0x000A)));

        // LD (IX+3), E
        compTest.poke(0x0000, (byte) 0xDD);
        compTest.poke(0x0001, (byte) 0x73);
        compTest.poke(0x0002, (byte) 0x03);
        cpu.setPC(0x0000);
        cpu.fetch();
        assertEquals((byte) 0x44, compTest.peek(0x000B), "LD (IX+3), E Failed: (IX+3)<>0x44 = " + Integer.toHexString(compTest.peek(0x000B)));

        // LD (IX+4), H
        compTest.poke(0x0000, (byte) 0xDD);
        compTest.poke(0x0001, (byte) 0x74);
        compTest.poke(0x0002, (byte) 0x04);
        cpu.setPC(0x0000);
        cpu.fetch();
        assertEquals((byte) 0x55, compTest.peek(0x000C), "LD (IX+4), H Failed: (IX+4)<>0x55 = " + Integer.toHexString(compTest.peek(0x000C)));

        // LD (IX+5), L
        compTest.poke(0x0000, (byte) 0xDD);
        compTest.poke(0x0001, (byte) 0x75);
        compTest.poke(0x0002, (byte) 0x05);
        cpu.setPC(0x0000);
        cpu.fetch();
        assertEquals((byte) 0x66, compTest.peek(0x000D), "LD (IX+5), L Failed: (IX+5)<>0x66 = " + Integer.toHexString(compTest.peek(0x000D)));
    }

    @Test
    void testLD_IY_d_r() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(16));
        cpu.setComputer(compTest);

        // Initialize memory with zeros
        for (int i = 0x0008; i <= 0x000E; i++) {
            compTest.poke(i, (byte) 0x00);
        }

        cpu.setIY((short) 0x0008);
        cpu.setB((byte) 0xAA);
        cpu.setC((byte) 0xBB);
        cpu.setD((byte) 0xCC);
        cpu.setE((byte) 0xDD);
        cpu.setH((byte) 0xEE);
        cpu.setL((byte) 0xFF);

        // LD (IY+0), B
        compTest.poke(0x0000, (byte) 0xFD);
        compTest.poke(0x0001, (byte) 0x70);
        compTest.poke(0x0002, (byte) 0x00);
        cpu.setPC(0x0000);
        long initTState = cpu.getTState();
        cpu.fetch();
        
        assertAll("LD (IY+d), r Group",
                () -> assertEquals((byte) 0xAA, compTest.peek(0x0008), "LD (IY+0), B Failed: (IY+0)<>0xAA = " + Integer.toHexString(compTest.peek(0x0008))),
                () -> assertNotEquals((byte) 0x00, compTest.peek(0x0008), "LD (IY+0), B Failed: (IY+0) still 0x00 = " + Integer.toHexString(compTest.peek(0x0008))),
                () -> assertEquals(19, cpu.getTState()-initTState, "LD (IY+0), B TState Failed")
        );

        // LD (IY+1), C
        compTest.poke(0x0000, (byte) 0xFD);
        compTest.poke(0x0001, (byte) 0x71);
        compTest.poke(0x0002, (byte) 0x01);
        cpu.setPC(0x0000);
        cpu.fetch();
        assertEquals((byte) 0xBB, compTest.peek(0x0009), "LD (IY+1), C Failed: (IY+1)<>0xBB = " + Integer.toHexString(compTest.peek(0x0009)));

        // LD (IY+2), D
        compTest.poke(0x0000, (byte) 0xFD);
        compTest.poke(0x0001, (byte) 0x72);
        compTest.poke(0x0002, (byte) 0x02);
        cpu.setPC(0x0000);
        cpu.fetch();
        assertEquals((byte) 0xCC, compTest.peek(0x000A), "LD (IY+2), D Failed: (IY+2)<>0xCC = " + Integer.toHexString(compTest.peek(0x000A)));

        // LD (IY+3), E
        compTest.poke(0x0000, (byte) 0xFD);
        compTest.poke(0x0001, (byte) 0x73);
        compTest.poke(0x0002, (byte) 0x03);
        cpu.setPC(0x0000);
        cpu.fetch();
        assertEquals((byte) 0xDD, compTest.peek(0x000B), "LD (IY+3), E Failed: (IY+3)<>0xDD = " + Integer.toHexString(compTest.peek(0x000B)));

        // LD (IY+4), H
        compTest.poke(0x0000, (byte) 0xFD);
        compTest.poke(0x0001, (byte) 0x74);
        compTest.poke(0x0002, (byte) 0x04);
        cpu.setPC(0x0000);
        cpu.fetch();
        assertEquals((byte) 0xEE, compTest.peek(0x000C), "LD (IY+4), H Failed: (IY+4)<>0xEE = " + Integer.toHexString(compTest.peek(0x000C)));

        // LD (IY+5), L
        compTest.poke(0x0000, (byte) 0xFD);
        compTest.poke(0x0001, (byte) 0x75);
        compTest.poke(0x0002, (byte) 0x05);
        cpu.setPC(0x0000);
        cpu.fetch();
        assertEquals((byte) 0xFF, compTest.peek(0x000D), "LD (IY+5), L Failed: (IY+5)<>0xFF = " + Integer.toHexString(compTest.peek(0x000D)));
    }

    @Test
    void testLD_IX_d_n() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(16));
        cpu.setComputer(compTest);

        // Initialize memory with zeros
        for (int i = 0x0008; i <= 0x000F; i++) {
            compTest.poke(i, (byte) 0x00);
        }

        cpu.setIX((short) 0x0008);

        // LD (IX+0), 0x12
        compTest.poke(0x0000, (byte) 0xDD); // DD prefix
        compTest.poke(0x0001, (byte) 0x36); // LD (IX+d), n
        compTest.poke(0x0002, (byte) 0x00); // displacement = 0
        compTest.poke(0x0003, (byte) 0x12); // immediate value = 0x12
        cpu.setPC(0x0000);
        long initTState = cpu.getTState();
        cpu.fetch();
        
        assertAll("LD (IX+d), n Group",
                () -> assertEquals((byte) 0x12, compTest.peek(0x0008), "LD (IX+0), 0x12 Failed: (IX+0)<>0x12 = " + Integer.toHexString(compTest.peek(0x0008))),
                () -> assertNotEquals((byte) 0x00, compTest.peek(0x0008), "LD (IX+0), 0x12 Failed: (IX+0) still 0x00 = " + Integer.toHexString(compTest.peek(0x0008))),
                () -> assertEquals(19, cpu.getTState()-initTState, "LD (IX+0), 0x12 TState Failed")
        );

        // LD (IX+2), 0x34
        compTest.poke(0x0000, (byte) 0xDD);
        compTest.poke(0x0001, (byte) 0x36);
        compTest.poke(0x0002, (byte) 0x02); // displacement = 2
        compTest.poke(0x0003, (byte) 0x34); // immediate value = 0x34
        cpu.setPC(0x0000);
        cpu.fetch();
        assertEquals((byte) 0x34, compTest.peek(0x000A), "LD (IX+2), 0x34 Failed: (IX+2)<>0x34 = " + Integer.toHexString(compTest.peek(0x000A)));

        // LD (IX+5), 0x78
        compTest.poke(0x0000, (byte) 0xDD);
        compTest.poke(0x0001, (byte) 0x36);
        compTest.poke(0x0002, (byte) 0x05); // displacement = 5
        compTest.poke(0x0003, (byte) 0x78); // immediate value = 0x78
        cpu.setPC(0x0000);
        cpu.fetch();
        assertEquals((byte) 0x78, compTest.peek(0x000D), "LD (IX+5), 0x78 Failed: (IX+5)<>0x78 = " + Integer.toHexString(compTest.peek(0x000D)));

        // Test with negative displacement - LD (IX-1), 0x9A
        compTest.poke(0x0000, (byte) 0xDD);
        compTest.poke(0x0001, (byte) 0x36);
        compTest.poke(0x0002, (byte) 0xFF); // displacement = -1 (0xFF as signed byte)
        compTest.poke(0x0003, (byte) 0x9A); // immediate value = 0x9A
        cpu.setPC(0x0000);
        cpu.fetch();
        assertEquals((byte) 0x9A, compTest.peek(0x0007), "LD (IX-1), 0x9A Failed: (IX-1)<>0x9A = " + Integer.toHexString(compTest.peek(0x0007)));
    }

    @Test
    void testLD_IY_d_n() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(16));
        cpu.setComputer(compTest);

        // Initialize memory with zeros
        for (int i = 0x0008; i <= 0x000F; i++) {
            compTest.poke(i, (byte) 0x00);
        }

        cpu.setIY((short) 0x0008);

        // LD (IY+0), 0xAB
        compTest.poke(0x0000, (byte) 0xFD); // FD prefix
        compTest.poke(0x0001, (byte) 0x36); // LD (IY+d), n
        compTest.poke(0x0002, (byte) 0x00); // displacement = 0
        compTest.poke(0x0003, (byte) 0xAB); // immediate value = 0xAB
        cpu.setPC(0x0000);
        long initTState = cpu.getTState();
        cpu.fetch();
        
        assertAll("LD (IY+d), n Group",
                () -> assertEquals((byte) 0xAB, compTest.peek(0x0008), "LD (IY+0), 0xAB Failed: (IY+0)<>0xAB = " + Integer.toHexString(compTest.peek(0x0008))),
                () -> assertNotEquals((byte) 0x00, compTest.peek(0x0008), "LD (IY+0), 0xAB Failed: (IY+0) still 0x00 = " + Integer.toHexString(compTest.peek(0x0008))),
                () -> assertEquals(19, cpu.getTState()-initTState, "LD (IY+0), 0xAB TState Failed")
        );

        // LD (IY+3), 0xCD
        compTest.poke(0x0000, (byte) 0xFD);
        compTest.poke(0x0001, (byte) 0x36);
        compTest.poke(0x0002, (byte) 0x03); // displacement = 3
        compTest.poke(0x0003, (byte) 0xCD); // immediate value = 0xCD
        cpu.setPC(0x0000);
        cpu.fetch();
        assertEquals((byte) 0xCD, compTest.peek(0x000B), "LD (IY+3), 0xCD Failed: (IY+3)<>0xCD = " + Integer.toHexString(compTest.peek(0x000B)));

        // LD (IY+6), 0xEF
        compTest.poke(0x0000, (byte) 0xFD);
        compTest.poke(0x0001, (byte) 0x36);
        compTest.poke(0x0002, (byte) 0x06); // displacement = 6
        compTest.poke(0x0003, (byte) 0xEF); // immediate value = 0xEF
        cpu.setPC(0x0000);
        cpu.fetch();
        assertEquals((byte) 0xEF, compTest.peek(0x000E), "LD (IY+6), 0xEF Failed: (IY+6)<>0xEF = " + Integer.toHexString(compTest.peek(0x000E)));

        // Test with negative displacement - LD (IY-1), 0x55
        compTest.poke(0x0000, (byte) 0xFD);
        compTest.poke(0x0001, (byte) 0x36);
        compTest.poke(0x0002, (byte) 0xFF); // displacement = -1 (0xFF as signed byte)
        compTest.poke(0x0003, (byte) 0x55); // immediate value = 0x55
        cpu.setPC(0x0000);
        cpu.fetch();
        assertEquals((byte) 0x55, compTest.peek(0x0007), "LD (IY-1), 0x55 Failed: (IY-1)<>0x55 = " + Integer.toHexString(compTest.peek(0x0007)));
    }

    @Test
    void testLD_bcdexya() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(8));
        cpu.setComputer(compTest);

        compTest.poke(0x0000, (byte) 0xDD);
        compTest.poke(0x0001, (byte) 0x47);// LD B, A
        compTest.poke(0x0002, (byte) 0xDD);
        compTest.poke(0x0003, (byte) 0x7D);// LD A, IXL
        compTest.poke(0x0004, (byte) 0xDD);
        compTest.poke(0x0005, (byte) 0x4F);// LD C, A

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x55);
        cpu.setIXL((byte) 0xAA);

        long initTState = cpu.getTState();
        cpu.fetch();
        cpu.fetch();
        cpu.fetch();

        assertAll("LD <bcdexya>,<bcdexya> Group",
                () -> assertEquals(0x55, cpu.getB()&0x0055, "LD <bcdexya>,<bcdexya> Failed: B<>0x55 B=" + Integer.toHexString(cpu.getB())),
                () -> assertEquals(0xAA, cpu.getC()&0x00AA, "LD <bcdexya>,<bcdexya> Failed: C<>0xAA C=" + Integer.toHexString(cpu.getC())),

                () -> assertEquals(3*8, cpu.getTState()-initTState, "LD <bcdexya>,<bcdexya> TState Failed")
        );
    }

    @Test
    void testLD_XY_prefix_chain() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        int delta = 0; // TODO añadir el tiempo que consume el primer prefijo

        compTest.poke(0x0000, (byte) 0xDD); // will be ignored
        compTest.poke(0x0001, (byte) 0xFD);
        compTest.poke(0x0002, (byte) 0x7D);// LD A, IYL

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x55);
        cpu.setIYL((byte) 0xAA);

        long initTState = cpu.getTState();
        cpu.fetch();

        assertAll("LD_XY_prefix_chain Group",
                () -> assertNotEquals(0x0055, cpu.getA()&0x00FF, "LD_XY_prefix_chain Failed: A<>0x55 B=" + Integer.toHexString(cpu.getA())),
                () -> assertEquals(0x00AA, cpu.getIYL()&0x00FF, "LD_XY_prefix_chain Failed: IYL<>0xAA C=" + Integer.toHexString(cpu.getIYL())),

                () -> assertEquals(8+delta, cpu.getTState()-initTState, "LD_XY_prefix_chain TState Failed")
        );

        compTest.poke(0x0000, (byte) 0xFD); // will be ignored
        compTest.poke(0x0001, (byte) 0xDD);
        compTest.poke(0x0002, (byte) 0x7D);// LD A, IXL

        cpu.reset();
        cpu.setPC(0x0000);
        cpu.setA((byte) 0x55);
        cpu.setIXL((byte) 0xAA);

        cpu.fetch();

        assertAll("LD_XY_prefix_chain Group",
                () -> assertNotEquals(0x0055, cpu.getA()&0x00FF, "LD_XY_prefix_chain Failed: A<>0x55 B=" + Integer.toHexString(cpu.getA())),
                () -> assertEquals(0x00AA, cpu.getIXL()&0x00FF, "LD_XY_prefix_chain Failed: IXL<>0xAA C=" + Integer.toHexString(cpu.getIXL())),

                () -> assertEquals(8+delta, cpu.getTState()-initTState, "LD_XY_prefix_chain TState Failed")
        );
    }
}