package com.github.jagarsoft.test;

import com.github.jagarsoft.*;
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
        cpu.fetch((byte)0x31); // LD SP, 0xDEF0

        assertAll("LD rp[p], nn, A Group",
                () -> assertEquals((short)0x1234, cpu.getBC(), "LD BC, 0x1234 Failed: BC<>0x1234 BC=" + cpu.getBC() + ")"),
                () -> assertNotEquals((short)0x0000, cpu.getBC(), "LD BC, 0x1234 Failed: BC still 0x0000 BC=" + cpu.getBC() + ")"),

                () -> assertEquals((short)0x5678, cpu.getDE(), "LD DE, 0x5678 Failed: DE<>0x5678 DE=" + cpu.getDE() + ")"),
                () -> assertNotEquals((short)0x0000, cpu.getDE(), "LD DE, 0x5678 Failed: DE still 0x0000 DE=" + cpu.getDE() + ")"),

                () -> assertEquals((short)0x9ABC, cpu.getHL(), "LD HL, 0x9ABC Failed: HL<>0x9ABC HL=" + cpu.getHL() + ")"),
                () -> assertNotEquals((short)0x0000, cpu.getHL(), "LD HL, 0x9ABC Failed: HL still 0x0000 HL=" + cpu.getHL() + ")"),

                () -> assertEquals((short)0xDEF0, cpu.getSP(), "LD SP, 0xDEF0 Failed: SP<>0xDEF0 SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals((short)0x0000, cpu.getSP(), "LD SP, 0xDEF0 Failed: SP still 0x0000 SP=" + cpu.getSP() + ")")
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

        cpu.LD_BC_A();

        assertAll("LD (BC), A Group",
                () -> assertEquals((byte) 0x0FF, compTest.peek(0), "LD (BC), A Failed: (BC)<>0xFF ((BC)=" + compTest.peek(0) + ")"),
                () -> assertNotEquals((short) 0x080, compTest.peek(0), "LD (BC), A Failed: (BC) still 0x80 ((BC)=" + compTest.peek(0) + ")")
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

        cpu.LD_DE_A();

        assertAll("LD (DE), A Group",
                () -> assertEquals((byte) 0x0FF, compTest.peek(0), "LD (DE), A Failed: (DE)<>0xFF ((DE)=" + compTest.peek(0) + ")"),
                () -> assertNotEquals((short) 0x080, compTest.peek(0), "LD (DE), A Failed: (DE) still 0x80 ((DE)=" + compTest.peek(0) + ")")
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
        cpu.setBC((short) 0x1234);

        compTest.poke(0x0001, (byte) 0x43); // LD (0x0002), BC
        cpu.fetch();

        assertAll("LD (0x0004), BC Group",
                () -> assertEquals((byte) 0x34, compTest.peek(4), "LD (0x0004), BC Failed: (0x0004)<>0x34=" + Integer.toHexString(compTest.peek(4))),
                () -> assertNotEquals((byte) 0x78, compTest.peek(4), "LD (0x004), BC Failed: (0x0004) still 0x78=" + Integer.toHexString(compTest.peek(4))),
                () -> assertEquals((byte) 0x12, compTest.peek(5), "LD (0x0005), BC Failed: (0x0005)<>0x12" + Integer.toHexString(compTest.peek(5))),
                () -> assertNotEquals((byte) 0x56, compTest.peek(5), "LD (0x0005), BC Failed: (0x0005) still 0x56=" + Integer.toHexString(compTest.peek(5))),
                () -> assertEquals((byte) 0x0004, cpu.getPC(), "LD (0x0004), BC Failed: PC no was modified PC<>0x0004 PC=" + Integer.toHexString(cpu.getPC()))
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

        cpu.LD_mm_A();

        assertAll("LD (nn), A Group",
                () -> assertEquals((byte) 0xBB, compTest.peek(0x0002), "LD (0x0002), A Failed: (0x0002)<>0xBB = " + Integer.toHexString(compTest.peek(0x0002))),
                () -> assertNotEquals((byte) 0xAA, compTest.peek(0x0002), "LD (0x0002), A Failed: (0x0002) still 0xAA = " + Integer.toHexString(compTest.peek(0x0002)))
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

        cpu.LD_A_BC();

        assertAll("LD A, (BC) Group",
                () -> assertEquals((byte)0xBB, cpu.getA(), "LD A, (BC) Failed: A<>0xBB = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte)0xAA, cpu.getA(), "LD A, (BC) Failed: A still 0xAA = " + Integer.toHexString(cpu.getA()))
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

        cpu.LD_A_DE();

        assertAll("LD A, (BC) Group",
                () -> assertEquals((byte)0xBB, cpu.getA(), "LD A, (DE) Failed: A<>0xBB = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte)0xAA, cpu.getA(), "LD A, (DE) Failed: A still 0xAA = " + Integer.toHexString(cpu.getA()))
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

        cpu.fetch(); // LD HL, (0x0004) without ED prefix

        assertAll("LD HL, (0x0004) Group without ED prefix",
                () -> assertEquals((byte) 0x78, cpu.getL(), "LD HL, (0x0004) Failed: L<>0x78=" + Integer.toHexString(cpu.getL())),
                () -> assertNotEquals((byte) 0x34, cpu.getL(), "LD HL, (0x0004) Failed: L still 0x34=" + Integer.toHexString(cpu.getL())),
                () -> assertEquals((byte) 0x56, cpu.getH(), "LD HL, (0x0004) Failed: H<>0x56" + Integer.toHexString(cpu.getH())),
                () -> assertNotEquals((byte) 0x12, cpu.getH(), "LD HL, (0x0004) Failed: H still 0x12=" + Integer.toHexString(cpu.getH())),
                () -> assertEquals((byte) 0x0004, cpu.getPC(), "LD HL, (0x0004) Failed: PC no was modified PC<>0x0004 PC=" + Integer.toHexString(cpu.getPC()))
        );

        compTest.poke(0x0004, (byte) 0x78);
        compTest.poke(0x0005, (byte) 0x56);

        cpu.setPC(0x0000);
        cpu.setBC((short) 0x1234);

        compTest.poke(0x0001, (byte) 0x4B); // LD BC, (0x0004)
        cpu.fetch();

        assertAll("LD BC, (0x0004) Group",
                () -> assertEquals((byte) 0x78, cpu.getC(), "LD BC, (0x0004) Failed: C<>0x78=" + Integer.toHexString(cpu.getC())),
                () -> assertNotEquals((byte) 0x34, cpu.getC(), "LD BC, (0x0004) Failed: C still 0x34=" + Integer.toHexString(cpu.getC())),
                () -> assertEquals((byte) 0x56, cpu.getB(), "LD BC, (0x0004) Failed: B<>0x56" + Integer.toHexString(cpu.getB())),
                () -> assertNotEquals((byte) 0x12, cpu.getB(), "LD BC, (0x0004) Failed: B still 0x12=" + Integer.toHexString(cpu.getB())),
                () -> assertEquals((byte) 0x0004, cpu.getPC(), "LD BC, (0x0004) Failed: PC no was modified PC<>0x0004 PC=" + Integer.toHexString(cpu.getPC()))
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

        cpu.LD_A_mm();

        assertAll("LD A, (0x0000) Group",
                () -> assertEquals((byte)0x12, cpu.getA(), "LD A, (0x0000) Failed: A<>0x12 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte)0x34, cpu.getA(), "LD A, (0x0000) Failed: A still 0x34 = " + Integer.toHexString(cpu.getA()))
        );
    }

    @Test
    void testLD_r_y_n() {
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

        cpu.fetch((byte)0x36); // LD (HL), n

        assertAll("LD r[y], n Group",
                () -> assertEquals((short)0x26, compTest.peek(cpu.getHL()), "LD (HL), 0x26 Failed: (HL)<>0x26 = " + Integer.toHexString(compTest.peek(cpu.getHL()))),
                () -> assertNotEquals((short)0x27, compTest.peek(cpu.getHL()), "LD (HL), 0x26 Failed: (HL) still 0x27 = " + Integer.toHexString(compTest.peek(cpu.getHL())))
        );
    }

    @Test
    void testLD_r_y_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setB((byte)0x10);
        cpu.setC((byte)0x11);
        cpu.setD((byte)0x12);
        cpu.setE((byte)0x13);
        cpu.setH((byte)0x14);
        cpu.setL((byte)0x15);

        // assertAll("LD A, r[z] Group",
        cpu.fetch((byte)0x78); // LD A, B
        assertEquals((byte) 0x10, cpu.getA(), "LD A, B Failed: A<>0x10 = " + Integer.toHexString(cpu.getA()));

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
    }

    @Test
    void testLD_SP_HL() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setSP((short) 0x0102);
        cpu.setHL((short) 0x0304);

        cpu.LD_SP_HL();

        assertAll("LD SP, HL Group",
                () -> assertEquals((short)0x0304, cpu.getSP(), "LD SP, HL Failed: not was modified (SP="+ Integer.toHexString(cpu.getSP())+")"),
                () -> assertNotEquals((short)0x0102, cpu.getSP(), "LD SP, HL Failed: still is 0x0102 (SP="+ Integer.toHexString(cpu.getSP())+")")
        );
    }

    @Test
    void testLD_A_I() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(16));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setI((byte) 0x55);
        cpu.setA((byte) 0x00);
        cpu.setF((byte) 0x00);
        compTest.poke(0x0000, (byte) 0xED); // ED prefix
        compTest.poke(0x0001, (byte) 0x57); // LD A, I
        cpu.fetch(); // ED prefix + LD A, I

        assertAll("LD A, I Group",
                () -> assertEquals((byte) 0x55, cpu.getA(), "LD A, I Failed: A<>0x55"),
                () -> assertFalse(cpu.getSF(), "LD A, I Failed: S Flag incorrectly set"),
                () -> assertFalse(cpu.getZF(), "LD A, I Failed: Z Flag incorrectly set"),
                () -> assertFalse(cpu.getNF(), "LD A, I Failed: N Flag incorrectly set"),
                () -> assertFalse(cpu.getHF(), "LD A, I Failed: H Flag incorrectly set")
        );
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
        compTest.poke(0x0000, (byte) 0xED); // ED prefix
        compTest.poke(0x0001, (byte) 0x47); // LD I, A
        cpu.fetch(); // ED prefix + LD I, A

        assertAll("LD I, A Group",
                () -> assertEquals((byte) 0xAA, cpu.getI(), "LD I, A Failed: I<>0xAA"),
                () -> assertEquals((byte) 0xAA, cpu.getA(), "LD I, A Failed: A modified")
        );
    }

    @Test
    void testLD_A_R() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(16));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setR((byte) 0x33);
        cpu.setA((byte) 0x00);
        cpu.setF((byte) 0x00);
        compTest.poke(0x0000, (byte) 0xED); // ED prefix
        compTest.poke(0x0001, (byte) 0x5F); // LD A, R
        cpu.fetch(); // ED prefix + LD A, R

        assertAll("LD A, R Group",
                () -> assertEquals((byte) 0x33, cpu.getA(), "LD A, R Failed: A<>0x33"),
                () -> assertFalse(cpu.getSF(), "LD A, R Failed: S Flag incorrectly set"),
                () -> assertFalse(cpu.getZF(), "LD A, R Failed: Z Flag incorrectly set"),
                () -> assertFalse(cpu.getNF(), "LD A, R Failed: N Flag incorrectly set"),
                () -> assertFalse(cpu.getHF(), "LD A, R Failed: H Flag incorrectly set")
        );
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
        cpu.fetch(); // ED prefix + LD R, A

        assertAll("LD R, A Group",
                () -> assertEquals((byte) 0x77, cpu.getR(), "LD R, A Failed: R<>0x77"),
                () -> assertEquals((byte) 0x77, cpu.getA(), "LD R, A Failed: A modified")
        );
    }
}