package com.github.jagarsoft.test;

import com.github.jagarsoft.Computer;
import com.github.jagarsoft.RAMMemory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Z80Test {

    @Test
    void testEX_AF_AF_() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte) 0x01);
        cpu.setF((byte) 0x02);

        cpu.setA_((byte) 0x03);
        cpu.setF_((byte) 0x04);

        cpu.EX_AF_AF_();

        assertAll("EX_AF_AF' Group",
                () -> assertEquals(0x03, cpu.getA(), "EX_AF_AF_ Failed (A)"),
                () -> assertEquals(0x01, cpu.getA_(), "EX_AF_AF_ Failed (A')"),

                () -> assertEquals(0x04, cpu.getF(), "EX_AF_AF_ Failed (F)"),
                () -> assertEquals(0x02, cpu.getF_(), "EX_AF_AF_ Failed (F')")
        );
    }

    @Test
    void testDJNZ_B_NotZero_MustJumpBackward() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0001, (byte) -1);


        cpu.setB((byte) 0xFF);
        cpu.setPC(0x0001);

        cpu.DJNZ();

        assertAll("DJNZ Group",
                () -> assertEquals((byte) 0xFE, cpu.getB(), "DJNZ Failed: B<>0 (B=" + cpu.getB() + ")"),
                () -> assertEquals(0x0001, cpu.getPC(), "DJNZ Failed: PC was not modified (PC=" + cpu.getPC() + ")")
        );
    }

    @Test
    void testDJNZ_B_Zero_MustNotJump() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0001, (byte) -10);

        cpu.setB((byte) 1);
        cpu.setPC(0x0001);

        cpu.DJNZ();

        assertAll("DJNZ Group",
                () -> assertEquals(0, cpu.getB(), "DJNZ Failed: B=0 (B=" + cpu.getB() + ")"),
                () -> assertEquals(0x0002, cpu.getPC(), "DJNZ Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );
    }

    @Test
    void testJR_MustJumpBackward() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0001, (byte) -1);

        cpu.setPC(0x0001);

        cpu.JR();

        assertEquals(0x0001, cpu.getPC(), "JR Failed: PC was not modified (PC=" + cpu.getPC() + ")");
    }

    @Test
    void testJR_cc_MustJumpBackward() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0001, (byte) -1);

        cpu.setPC(0x0001);
        cpu.resZF();

        cpu.fetch((byte) 0x20); // JR NZ

        assertEquals(0x0001, cpu.getPC(), "JR NZ Failed: PC was not modified (PC=" + cpu.getPC() + ")");

        cpu.setPC(0x0001);
        cpu.setZF();

        cpu.fetch((byte) 0x28); // JR Z

        assertEquals(0x0001, cpu.getPC(), "JR Z Failed: PC was not modified (PC=" + cpu.getPC() + ")");

        cpu.setPC(0x0001);
        cpu.resCF();

        cpu.fetch((byte) 0x30); // JR NC

        assertEquals(0x0001, cpu.getPC(), "JR NC Failed: PC was not modified (PC=" + cpu.getPC() + ")");

        cpu.setPC(0x0001);
        cpu.setCF();

        cpu.fetch((byte) 0x38); // JR C

        assertEquals(0x0001, cpu.getPC(), "JR C Failed: PC was not modified (PC=" + cpu.getPC() + ")");
    }

    @Test
    void testJR_cc_MustNotJump() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0001, (byte) -1);

        cpu.setPC(0x0001);
        cpu.setZF();

        cpu.fetch((byte) 0x20); // JR NZ

        assertEquals(0x0002, cpu.getPC(), "JR NZ Failed: PC was modified (PC=" + cpu.getPC() + ")");

        cpu.setPC(0x0001);
        cpu.resZF();

        cpu.fetch((byte) 0x28); // JR Z

        assertEquals(0x0002, cpu.getPC(), "JR Z Failed: PC was modified (PC=" + cpu.getPC() + ")");

        cpu.setPC(0x0001);
        cpu.setCF();

        cpu.fetch((byte) 0x30); // JR NC

        assertEquals(0x0002, cpu.getPC(), "JR NC Failed: PC was modified (PC=" + cpu.getPC() + ")");

        cpu.setPC(0x0001);
        cpu.resCF();

        cpu.fetch((byte) 0x38); // JR C

        assertEquals(0x0002, cpu.getPC(), "JR C Failed: PC was modified (PC=" + cpu.getPC() + ")");
    }

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
    void testADD_HL_rp_p() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setHL((short)0x1234);
        cpu.setBC((short)0x0001);
        cpu.setDE((short)0x0002);
        cpu.setSP((short)0x00FF);

        cpu.fetch((byte)0x09); // ADD HL, BC

        // assertAll("ADD HL, rp_p Group",
        assertEquals((short) 0x1235, cpu.getHL(), "ADD HL, BC Failed: HL<>0x1235 HL=" + Integer.toHexString(cpu.getHL()));
        assertNotEquals((short) 0x1234, cpu.getHL(), "ADD HL, BC Failed: HL Still 0x1234 HL=" + Integer.toHexString(cpu.getHL()));

        cpu.fetch((byte)0x19); // ADD HL, DE

        assertEquals((short) 0x1237, cpu.getHL(), "ADD HL, DE Failed: HL<>0x1237 HL=" + Integer.toHexString(cpu.getHL()));
        assertNotEquals((short) 0x1235, cpu.getHL(), "ADD HL, DE Failed: HL Still 0x1235 HL=" + Integer.toHexString(cpu.getHL()));

        cpu.fetch((byte)0x29); // ADD HL, HL

        assertEquals((short) 0x246E, cpu.getHL(), "ADD HL, HL Failed: HL<>0x246E HL=" + Integer.toHexString(cpu.getHL()));
        assertNotEquals((short) 0x1237, cpu.getHL(), "ADD HL, HL Failed: HL Still 0x1237 HL=" + Integer.toHexString(cpu.getHL()));

        cpu.fetch((byte)0x39); // ADD HL, SP

        assertEquals((short) 0x256D, cpu.getHL(), "ADD HL, SP Failed: HL<>0x256D HL=" + Integer.toHexString(cpu.getHL()));
        assertNotEquals((short) 0x246E, cpu.getHL(), "ADD HL, SP Failed: HL Still 0x246E HL=" + Integer.toHexString(cpu.getHL()));
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
                () -> assertEquals((byte) 0xFF, compTest.peek(0), "LD (BC), A Failed: (BC)<>0xFF ((BC)=" + compTest.peek(0) + ")"),
                () -> assertNotEquals((byte) 0x80, compTest.peek(0), "LD (BC), A Failed: (BC) still 0x80 ((BC)=" + compTest.peek(0) + ")")
        );
    }

    @Test
    void testLD_DE_A() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(1));
        cpu.setComputer(compTest);
        compTest.poke(0, (byte) 0x80);

        cpu.setDE((short) 0);
        cpu.setA((byte) 0xFF);

        cpu.LD_DE_A();

        assertAll("LD (DE), A Group",
                () -> assertEquals((byte) 0xFF, compTest.peek(0), "LD (DE), A Failed: (DE)<>0xFF ((DE)=" + compTest.peek(0) + ")"),
                () -> assertNotEquals((byte) 0x80, compTest.peek(0), "LD (DE), A Failed: (DE) still 0x80 ((DE)=" + compTest.peek(0) + ")")
        );
    }

    @Test
    void testLD_nn_HL() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0x02);
        compTest.poke(0x0001, (byte) 0x00);
        compTest.poke(0x0002, (byte) 0x56);
        compTest.poke(0x0003, (byte) 0x78);

        cpu.setPC(0x0000);
        cpu.setHL((short) 0x1234);

        cpu.LD_nn_HL(); // LD (0x0002), HL

        assertAll("LD_nn_HL Group",
                () -> assertEquals((byte) 0x34, compTest.peek(2), "LD (0x0002), HL Failed: (0x0002)<>0x34=" + Integer.toHexString(compTest.peek(2))),
                () -> assertNotEquals((byte) 0x56, compTest.peek(2), "LD (0x002), HL Failed: (0x0002) still 0x56=" + Integer.toHexString(compTest.peek(2))),
                () -> assertEquals((byte) 0x12, compTest.peek(3), "LD (0x0002), HL Failed: (0x0003)<>0x12" + Integer.toHexString(compTest.peek(3))),
                () -> assertNotEquals((byte) 0x78, compTest.peek(3), "LD (0x0002), HL Failed: (0x0003) still 0x78=" + Integer.toHexString(compTest.peek(3))),
                () -> assertEquals((byte) 0x0002, cpu.getPC(), "LD (0x0002), HL Failed: PC no was modified PC<>0x0002 PC=" + Integer.toHexString(cpu.getPC()))
        );
    }

    @Test
    void testLD_nn_A(){
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

        cpu.LD_nn_A();

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

    @Test
    void testLD_HL_nn(){
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

        cpu.LD_HL_nn();

        assertAll("LD HL, (0x0000) Group",
            () -> assertEquals((short)0x1234, cpu.getHL(), "LD HL, (0x0000) Failed: HL<>0x1234 = " + Integer.toHexString(cpu.getHL())),
            () -> assertNotEquals((short)0x5678, cpu.getHL(), "LD HL, (0x0000) Failed: HL still 0x1234 = " + Integer.toHexString(cpu.getHL()))
        );
    }

    @Test
    void testLD_A_nn(){
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

        cpu.LD_A_nn();
        
        assertAll("LD A, (0x0000) Group",
            () -> assertEquals((byte)0x12, cpu.getA(), "LD A, (0x0000) Failed: A<>0x12 = " + Integer.toHexString(cpu.getA())),
            () -> assertNotEquals((byte)0x34, cpu.getA(), "LD A, (0x0000) Failed: A still 0x34 = " + Integer.toHexString(cpu.getA()))
        );
    }

    @Test
    void testINC_rp_p() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setBC((short)0x1234);
        cpu.setDE((short)0x1234);
        cpu.setHL((short)0x1234);
        cpu.setSP((short)0x1234);

        cpu.fetch((byte)0x03); // INC BC
        cpu.fetch((byte)0x13); // INC DE
        cpu.fetch((byte)0x23); // INC HL
        cpu.fetch((byte)0x33); // INC SP

        assertAll("INC rp[p] Group",
                () -> assertEquals((short)0x1235, cpu.getBC(), "INC BC Failed: BC<>0x1235 = " + Integer.toHexString(cpu.getBC())),
                () -> assertNotEquals((short)0x1234, cpu.getBC(), "INC BC Failed: BC still 0x1234 = " + Integer.toHexString(cpu.getBC())),

                () -> assertEquals((short)0x1235, cpu.getDE(), "INC DE Failed: DE<>0x1235 = " + Integer.toHexString(cpu.getDE())),
                () -> assertNotEquals((short)0x1234, cpu.getDE(), "INC DE Failed: DE still 0x1234 = " + Integer.toHexString(cpu.getDE())),

                () -> assertEquals((short)0x1235, cpu.getHL(), "INC HL Failed: HL<>0x1235 = " + Integer.toHexString(cpu.getHL())),
                () -> assertNotEquals((short)0x1234, cpu.getHL(), "INC HL Failed: HL still 0x1234 = " + Integer.toHexString(cpu.getHL())),

                () -> assertEquals((short)0x1235, cpu.getSP(), "INC SP Failed: SP<>0x1235 = " + Integer.toHexString(cpu.getSP())),
                () -> assertNotEquals((short)0x1234, cpu.getSP(), "INC SP Failed: SP still 0x1234 = " + Integer.toHexString(cpu.getSP()))
        );

        cpu.setHL((short)0x00FF);

        cpu.fetch((byte)0x23); // INC HL

        assertAll("INC rp[p] Group",
                () -> assertEquals((short)0x0100, cpu.getHL(), "INC HL Failed: HL<>0x0100 = " + Integer.toHexString(cpu.getHL())),
                () -> assertNotEquals((short)0x00FF, cpu.getHL(), "INC HL Failed: HL still 0x00FF = " + Integer.toHexString(cpu.getHL()))
        );
    }

    @Test
    void testDEC_rp_p() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setBC((short)0x1234);
        cpu.setDE((short)0x1234);
        cpu.setHL((short)0x1234);
        cpu.setSP((short)0x1234);

        cpu.fetch((byte)0x0B); // DEC BC
        cpu.fetch((byte)0x1B); // DEC DE
        cpu.fetch((byte)0x2B); // DEC HL
        cpu.fetch((byte)0x3B); // DEC SP

        assertAll("DEC rp[p] Group",
                () -> assertEquals((short)0x1233, cpu.getBC(), "DEC BC Failed: BC<>0x1233 = " + Integer.toHexString(cpu.getBC())),
                () -> assertNotEquals((short)0x1234, cpu.getBC(), "DEC BC Failed: BC still 0x1234 = " + Integer.toHexString(cpu.getBC())),

                () -> assertEquals((short)0x1233, cpu.getDE(), "DEC DE Failed: DE<>0x1233 = " + Integer.toHexString(cpu.getDE())),
                () -> assertNotEquals((short)0x1234, cpu.getDE(), "DEC DE Failed: DE still 0x1234 = " + Integer.toHexString(cpu.getDE())),

                () -> assertEquals((short)0x1233, cpu.getHL(), "DEC HL Failed: HL<>0x1233 = " + Integer.toHexString(cpu.getHL())),
                () -> assertNotEquals((short)0x1234, cpu.getHL(), "DEC HL Failed: HL still 0x1234 = " + Integer.toHexString(cpu.getHL())),

                () -> assertEquals((short)0x1233, cpu.getSP(), "DEC SP Failed: SP<>0x1233 = " + Integer.toHexString(cpu.getSP())),
                () -> assertNotEquals((short)0x1234, cpu.getSP(), "DEC SP Failed: SP still 0x1234 = " + Integer.toHexString(cpu.getSP()))
        );

        cpu.setHL((short)0x0200);

        cpu.fetch((byte)0x2B); // DEC HL

        assertAll("INC rp[p] Group",
                () -> assertEquals((short)0x01FF, cpu.getHL(), "DEC HL Failed: HL<>0x01FF = " + Integer.toHexString(cpu.getHL())),
                () -> assertNotEquals((short)0x0200, cpu.getHL(), "DEC HL Failed: HL still 0x0200 = " + Integer.toHexString(cpu.getHL()))
        );
    }

    @Test
    void testINC_r_y() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000,(byte)0x17);

        cpu.setB((byte)0x10);
        cpu.setC((byte)0x11);
        cpu.setD((byte)0x12);
        cpu.setE((byte)0x13);
        cpu.setH((byte)0x14);
        cpu.setL((byte)0x15);
        // (HL) = 0x16
        cpu.setA((byte)0x16);

        cpu.fetch((byte)0x04); // INC B
        cpu.fetch((byte)0x0C); // INC C
        cpu.fetch((byte)0x14); // INC D
        cpu.fetch((byte)0x1C); // INC E
        cpu.fetch((byte)0x24); // INC H
        // INC (HL)
        cpu.fetch((byte)0x2C); // INC L
        cpu.fetch((byte)0x3C); // INC A

        assertAll("INC r[y] Group",
                () -> assertEquals((short)0x11, cpu.getB(), "INC B Failed: B<>0x11 = " + Integer.toHexString(cpu.getB())),
                () -> assertNotEquals((short)0x10, cpu.getB(), "INC B Failed: B still 0x10 = " + Integer.toHexString(cpu.getB())),

                () -> assertEquals((short)0x12, cpu.getC(), "INC C Failed: C<>0x12 = " + Integer.toHexString(cpu.getC())),
                () -> assertNotEquals((short)0x11, cpu.getC(), "INC C Failed: C still 0x11 = " + Integer.toHexString(cpu.getC())),

                () -> assertEquals((short)0x13, cpu.getD(), "INC D Failed: D<>0x13 = " + Integer.toHexString(cpu.getD())),
                () -> assertNotEquals((short)0x12, cpu.getD(), "INC D Failed: D still 0x12 = " + Integer.toHexString(cpu.getD())),

                () -> assertEquals((short)0x14, cpu.getE(), "INC E Failed: E<>0x14 = " + Integer.toHexString(cpu.getE())),
                () -> assertNotEquals((short)0x13, cpu.getE(), "INC E Failed: E still 0x13 = " + Integer.toHexString(cpu.getE())),

                () -> assertEquals((short)0x15, cpu.getH(), "INC H Failed: H<>0x15 = " + Integer.toHexString(cpu.getH())),
                () -> assertNotEquals((short)0x14, cpu.getH(), "INC H Failed: H still 0x14 = " + Integer.toHexString(cpu.getH())),

                () -> assertEquals((short)0x16, cpu.getL(), "INC L Failed: L<>0x16 = " + Integer.toHexString(cpu.getL())),
                () -> assertNotEquals((short)0x15, cpu.getL(), "INC L Failed: L still 0x15 = " + Integer.toHexString(cpu.getL()))
        );

        cpu.setHL((short)0x0000);

        cpu.fetch((byte)0x34); // INC (HL)

        assertAll("INC r[y] Group",
                () -> assertEquals((short)0x18, compTest.peek(cpu.getHL()), "INC (HL) Failed: (HL)<>0x18 = " + Integer.toHexString(compTest.peek(cpu.getHL()))),
                () -> assertNotEquals((short)0x17, compTest.peek(cpu.getHL()), "INC (HL) Failed: (HL) still 0x17 = " + Integer.toHexString(compTest.peek(cpu.getHL())))
        );
    }

    @Test
    void testDEC_r_y() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000,(byte)0x17);

        cpu.setB((byte)0x10);
        cpu.setC((byte)0x11);
        cpu.setD((byte)0x12);
        cpu.setE((byte)0x13);
        cpu.setH((byte)0x14);
        cpu.setL((byte)0x15);
        // (HL) = 0x16
        cpu.setA((byte)0x16);

        cpu.fetch((byte)0x05); // DEC B
        cpu.fetch((byte)0x0D); // DEC C
        cpu.fetch((byte)0x15); // DEC D
        cpu.fetch((byte)0x1D); // DEC E
        cpu.fetch((byte)0x25); // DEC H
        cpu.fetch((byte)0x2D); // DEC L
        // DEC (HL)
        cpu.fetch((byte)0x3D); // DEC A

        assertAll("DEC r[y] Group",
                () -> assertEquals((short)0x0F, cpu.getB(), "DEC B Failed: B<>0x0F = " + Integer.toHexString(cpu.getB())),
                () -> assertNotEquals((short)0x10, cpu.getB(), "DEC B Failed: B still 0x10 = " + Integer.toHexString(cpu.getB())),

                () -> assertEquals((short)0x10, cpu.getC(), "DEC C Failed: C<>0x11 = " + Integer.toHexString(cpu.getC())),
                () -> assertNotEquals((short)0x11, cpu.getC(), "DEC C Failed: C still 0x11 = " + Integer.toHexString(cpu.getC())),

                () -> assertEquals((short)0x11, cpu.getD(), "DEC D Failed: D<>0x12 = " + Integer.toHexString(cpu.getD())),
                () -> assertNotEquals((short)0x12, cpu.getD(), "DEC D Failed: D still 0x12 = " + Integer.toHexString(cpu.getD())),

                () -> assertEquals((short)0x12, cpu.getE(), "DEC E Failed: E<>0x12 = " + Integer.toHexString(cpu.getE())),
                () -> assertNotEquals((short)0x13, cpu.getE(), "DEC E Failed: E still 0x13 = " + Integer.toHexString(cpu.getE())),

                () -> assertEquals((short)0x13, cpu.getH(), "DEC H Failed: H<>0x13 = " + Integer.toHexString(cpu.getH())),
                () -> assertNotEquals((short)0x14, cpu.getH(), "DEC H Failed: H still 0x14 = " + Integer.toHexString(cpu.getH())),

                () -> assertEquals((short)0x14, cpu.getL(), "DEC L Failed: L<>0x14 = " + Integer.toHexString(cpu.getL())),
                () -> assertNotEquals((short)0x15, cpu.getL(), "DEC L Failed: L still 0x15 = " + Integer.toHexString(cpu.getL()))
        );

        cpu.setHL((short)0x0000);

        cpu.fetch((byte)0x35); // DEC (HL)

        assertAll("DEC r[y] Group",
                () -> assertEquals((short)0x16, compTest.peek(cpu.getHL()), "DEC (HL) Failed: (HL)<>0x16 = " + Integer.toHexString((short)compTest.peek(cpu.getHL()))),
                () -> assertNotEquals((short)0x17, compTest.peek(cpu.getHL()), "DEC (HL) Failed: (HL) still 0x17 = " + Integer.toHexString(cpu.getSP()))
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
    void testRLCA() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte) 0x0F);

        cpu.RLCA();

        assertAll("RLCA Group",
            () -> assertEquals((byte)0x1E, cpu.getA(), "RLCA Failed: A<>0x1E = " + Integer.toHexString(cpu.getA())),
            () -> assertNotEquals((byte)0x0F, cpu.getA(), "RLCA Failed: A still 0x0F = " + Integer.toHexString(cpu.getA())),
            () -> assertEquals(false, cpu.getCF(), "RLCA Failed: Carry flag must be OFF")
        );

        cpu.setA((byte) 0xF0);

        cpu.RLCA();

        assertAll("RLCA Group",
                () -> assertEquals((byte)0xE1, cpu.getA(), "RLCA Failed: A<>0xE1 = " + Integer.toHexString((byte)(cpu.getA()&0xFF))),
                () -> assertNotEquals((byte)0xF0, cpu.getA(), "RLCA Failed: A still 0xF0 = " + Integer.toHexString((byte)(cpu.getA()&0xFF))),
                () -> assertEquals(true, cpu.getCF(), "RLCA Failed: Carry flag must be ON")
        );
    }

    @Test
    void testRRCA() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte) 0x0F);

        cpu.RRCA();

        assertAll("RRCA Group",
                () -> assertEquals((byte)0x87, cpu.getA(), "RRCA Failed: A<>0x87 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte)0x0F, cpu.getA(), "RRCA Failed: A still 0x0F = " + Integer.toHexString(cpu.getA())),
                () -> assertEquals(true, cpu.getCF(), "RRCA Failed: Carry flag must be ON")
        );

        cpu.setA((byte) 0xF0);

        cpu.RRCA();

        assertAll("RRCA Group",
                () -> assertEquals((byte)0x78, cpu.getA(), "RRCA Failed: A<>0x78 = " + Integer.toHexString((byte)(cpu.getA()&0xFF))),
                () -> assertNotEquals((byte)0xF0, cpu.getA(), "RRCA Failed: A still 0xF0 = " + Integer.toHexString((byte)(cpu.getA()&0xFF))),
                () -> assertEquals(false, cpu.getCF(), "RRCA Failed: Carry flag must be OFF")
        );
    }
    
    @Test
    void testRLA() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.resCF();
        cpu.setA((byte) 0x0F);

        cpu.RLA();

        assertAll("RLA Group: NC & 0x0F",
                () -> assertEquals((byte) 0x1E, cpu.getA(), "RLA Failed: A<>0x1E = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0x0F, cpu.getA(), "RLA Failed: A still 0x0F = " + Integer.toHexString(cpu.getA())),
                () -> assertEquals(false, cpu.getCF(), "RLA Failed: Carry flag must be OFF" )
        );

        cpu.setCF();
        cpu.setA((byte) 0x0F);

        cpu.RLA();

        assertAll("RLA Group: C & 0x0F",
                () -> assertEquals((byte) 0x1F, cpu.getA(), "RLA Failed: A<>0x1F = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0x0F, cpu.getA(), "RLA Failed: A still 0x0F = " + Integer.toHexString(cpu.getA())),
                () -> assertEquals(false, cpu.getCF(), "RLA Failed: Carry flag must be OFF" )
        );

        cpu.resCF();
        cpu.setA((byte) 0xF0);

        cpu.RLA();

        assertAll("RLA Group: NC & 0xF0",
                () -> assertEquals((byte) 0xE0, cpu.getA(), "RLA Failed: A<>0xE0 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0xF0, cpu.getA(), "RLA Failed: A still 0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertEquals(true, cpu.getCF(), "RLA Failed: Carry flag must be ON" )
        );

        cpu.setCF();
        cpu.setA((byte) 0xF0);

        cpu.RLA();

        assertAll("RLA Group: C & 0xF0",
                () -> assertEquals((byte) 0xE1, cpu.getA(), "RLA Failed: A<>0xE0 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0xF0, cpu.getA(), "RLA Failed: A still 0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertEquals(true, cpu.getCF(), "RLA Failed: Carry flag must be ON" )
        );
    }

    @Test
    void testRRA() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.resCF();
        cpu.setA((byte) 0x0F);

        cpu.RRA();

        assertAll("RRA Group: NC & 0x0F",
                () -> assertEquals((byte) 0x07, cpu.getA(), "RRA Failed: A<>0x07 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0x0F, cpu.getA(), "RRA Failed: A still 0x0F = " + Integer.toHexString(cpu.getA())),
                () -> assertEquals(true, cpu.getCF(), "RRA Failed: Carry flag must be ON" )
        );

        cpu.setCF();
        cpu.setA((byte) 0x0F);

        cpu.RRA();

        assertAll("RRA Group: C & 0x0F",
                () -> assertEquals((byte) 0x87, cpu.getA(), "RRA Failed: A<>0x87 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0x0F, cpu.getA(), "RRA Failed: A still 0x0F = " + Integer.toHexString(cpu.getA())),
                () -> assertEquals(true, cpu.getCF(), "RRA Failed: Carry flag must be ON" )
        );

        cpu.resCF();
        cpu.setA((byte) 0xF0);

        cpu.RRA();

        assertAll("RRA Group: NC & 0xF0",
                () -> assertEquals((byte) 0x78, cpu.getA(), "RRA Failed: A<>0x78 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0xF0, cpu.getA(), "RRA Failed: A still 0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertEquals(false, cpu.getCF(), "RRA Failed: Carry flag must be OFF" )
        );

        cpu.setCF();
        cpu.setA((byte) 0xF0);

        cpu.RRA();

        assertAll("RRA Group: C & 0xF0",
                () -> assertEquals((byte) 0xF8, cpu.getA(), "RRA Failed: A<>0xE0 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0xF0, cpu.getA(), "RRA Failed: A still 0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertEquals(false, cpu.getCF(), "RRA Failed: Carry flag must be OFF" )
        );
    }

    @Test
    void testDAA() {
        // TDDO
    }

    @Test
    void CPL() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte) 0x0F);

        cpu.CPL();

        assertAll("CPL Group",
                () -> assertEquals((byte) 0xF0, cpu.getA(), "CPL Failed: A<>0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0x0F, cpu.getA(), "CPL Failed: A still 0x0F = " + Integer.toHexString(cpu.getA()))
          //      () -> assertEquals(false, cpu.getCF(), "RRA Failed: Carry flag must be OFF" )
        );
    }

    @Test
    void SCF() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setCF();

        assertEquals(true, cpu.getCF(), "SCF Failed: Carry flag must be ON");
    }

    @Test
    void CCF() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setCF();

        cpu.CCF();

        assertEquals(false, cpu.getCF(), "CCF Failed: Carry flag must be OFF");

        cpu.resCF();

        cpu.CCF();

        assertEquals(true, cpu.getCF(), "CCF Failed: Carry flag must be ON");
    }

    @Test
    void LD_r_y_r_z() {
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
                () -> assertEquals((byte) 0x20, cpu.getC(), "LD C, A Failed: C<>0x20 = " + Integer.toHexString(cpu.getC())),
                () -> assertEquals((byte) 0x20, cpu.getD(), "LD D, A Failed: D<>0x20 = " + Integer.toHexString(cpu.getD())),
                () -> assertEquals((byte) 0x20, cpu.getE(), "LD E, A Failed: E<>0x20 = " + Integer.toHexString(cpu.getE())),
                () -> assertEquals((byte) 0x20, cpu.getH(), "LD H, A Failed: H<>0x20 = " + Integer.toHexString(cpu.getH())),
                () -> assertEquals((byte) 0x20, cpu.getL(), "LD L, A Failed: L<>0x20 = " + Integer.toHexString(cpu.getL()))
        );
    }
}
