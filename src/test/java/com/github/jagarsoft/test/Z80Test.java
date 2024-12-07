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
            () -> assertFalse(cpu.getCF(), "RLCA Failed: Carry flag must be OFF")
        );

        cpu.setA((byte) 0xF0);

        cpu.RLCA();

        assertAll("RLCA Group",
                () -> assertEquals((byte)0xE1, cpu.getA(), "RLCA Failed: A<>0xE1 = " + Integer.toHexString((byte)(cpu.getA()&0xFF))),
                () -> assertNotEquals((byte)0xF0, cpu.getA(), "RLCA Failed: A still 0xF0 = " + Integer.toHexString((byte)(cpu.getA()&0xFF))),
                () -> assertTrue(cpu.getCF(), "RLCA Failed: Carry flag must be ON")
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
                () -> assertTrue(cpu.getCF(), "RRCA Failed: Carry flag must be ON")
        );

        cpu.setA((byte) 0xF0);

        cpu.RRCA();

        assertAll("RRCA Group",
                () -> assertEquals((byte)0x78, cpu.getA(), "RRCA Failed: A<>0x78 = " + Integer.toHexString((byte)(cpu.getA()&0xFF))),
                () -> assertNotEquals((byte)0xF0, cpu.getA(), "RRCA Failed: A still 0xF0 = " + Integer.toHexString((byte)(cpu.getA()&0xFF))),
                () -> assertFalse(cpu.getCF(), "RRCA Failed: Carry flag must be OFF")
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
                () -> assertFalse(cpu.getCF(), "RLA Failed: Carry flag must be OFF")
        );

        cpu.setCF();
        cpu.setA((byte) 0x0F);

        cpu.RLA();

        assertAll("RLA Group: C & 0x0F",
                () -> assertEquals((byte) 0x1F, cpu.getA(), "RLA Failed: A<>0x1F = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0x0F, cpu.getA(), "RLA Failed: A still 0x0F = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "RLA Failed: Carry flag must be OFF")
        );

        cpu.resCF();
        cpu.setA((byte) 0xF0);

        cpu.RLA();

        assertAll("RLA Group: NC & 0xF0",
                () -> assertEquals((byte) 0xE0, cpu.getA(), "RLA Failed: A<>0xE0 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0xF0, cpu.getA(), "RLA Failed: A still 0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "RLA Failed: Carry flag must be ON")
        );

        cpu.setCF();
        cpu.setA((byte) 0xF0);

        cpu.RLA();

        assertAll("RLA Group: C & 0xF0",
                () -> assertEquals((byte) 0xE1, cpu.getA(), "RLA Failed: A<>0xE0 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0xF0, cpu.getA(), "RLA Failed: A still 0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "RLA Failed: Carry flag must be ON")
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
                () -> assertTrue(cpu.getCF(), "RRA Failed: Carry flag must be ON")
        );

        cpu.setCF();
        cpu.setA((byte) 0x0F);

        cpu.RRA();

        assertAll("RRA Group: C & 0x0F",
                () -> assertEquals((byte) 0x87, cpu.getA(), "RRA Failed: A<>0x87 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0x0F, cpu.getA(), "RRA Failed: A still 0x0F = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "RRA Failed: Carry flag must be ON")
        );

        cpu.resCF();
        cpu.setA((byte) 0xF0);

        cpu.RRA();

        assertAll("RRA Group: NC & 0xF0",
                () -> assertEquals((byte) 0x78, cpu.getA(), "RRA Failed: A<>0x78 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0xF0, cpu.getA(), "RRA Failed: A still 0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "RRA Failed: Carry flag must be OFF")
        );

        cpu.setCF();
        cpu.setA((byte) 0xF0);

        cpu.RRA();

        assertAll("RRA Group: C & 0xF0",
                () -> assertEquals((byte) 0xF8, cpu.getA(), "RRA Failed: A<>0xE0 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0xF0, cpu.getA(), "RRA Failed: A still 0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "RRA Failed: Carry flag must be OFF")
        );
    }

    @Test
    void testDAA() {
        // TDDO
    }

    @Test
    void testCPL() {
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
    void testSCF() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setCF();

        assertTrue(cpu.getCF(), "SCF Failed: Carry flag must be ON");
    }

    @Test
    void testCCF() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setCF();

        cpu.CCF();

        assertFalse(cpu.getCF(), "CCF Failed: Carry flag must be OFF");

        cpu.resCF();

        cpu.CCF();

        assertTrue(cpu.getCF(), "CCF Failed: Carry flag must be ON");
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
    void testADD_A() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte)0x10);
        cpu.setB((byte)0x20);

        cpu.fetch((byte)0x80);

        assertEquals((byte) 0x30, cpu.getA(), "ADD A, B Failed: A<>0x30 = " + Integer.toHexString(cpu.getA()));
    }

    @Test
    void testADC_A() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte)0x10);
        cpu.setB((byte)0x20);
        cpu.resCF();

        cpu.fetch((byte)0x88);

        assertEquals((byte) 0x30, cpu.getA(), "ADC A, B; C=0 Failed: A<>0x30 = " + Integer.toHexString(cpu.getA()));

        cpu.setA((byte)0x10);
        cpu.setB((byte)0x20);
        cpu.setCF();

        cpu.fetch((byte)0x88);

        assertEquals((byte) 0x31, cpu.getA(), "ADC A, B; C=1 Failed: A<>0x31 = " + Integer.toHexString(cpu.getA()));
    }

    @Test
    void testSUB_A() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte)0x20);
        cpu.setB((byte)0x10);

        cpu.fetch((byte)0x90);

        assertEquals((byte) 0x10, cpu.getA(), "SUB A, B Failed: A<>0x10 = " + Integer.toHexString(cpu.getA()));
    }

    @Test
    void testSBC_A() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte)0x20);
        cpu.setB((byte)0x10);
        cpu.resCF();

        cpu.fetch((byte)0x98);

        assertEquals((byte) 0x10, cpu.getA(), "SBC A, B; C=0 Failed: A<>0x10 = " + Integer.toHexString(cpu.getA()));

        cpu.setA((byte)0x20);
        cpu.setB((byte)0x10);
        cpu.setCF();

        cpu.fetch((byte)0x98);

        assertEquals((byte) 0x0F, cpu.getA(), "SBC A, B; C=1 Failed: A<>0x0F = " + Integer.toHexString(cpu.getA()));
    }

    @Test
    void testAND() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte)0xF0);
        cpu.setB((byte)0x0F);

        cpu.fetch((byte)0xA0);

        assertEquals((byte) 0x00, cpu.getA(), "AND B Failed: A<>0x00 = " + Integer.toHexString(cpu.getA()));
    }

    @Test
    void testXOR() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte)0xF0);
        cpu.setB((byte)0xFF);

        cpu.fetch((byte)0xA8);

        assertEquals((byte) 0x0F, cpu.getA(), "XOR B Failed: A<>0x0F = " + Integer.toHexString(cpu.getA()));
    }

    @Test
    void testOR() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte)0xF0);
        cpu.setB((byte)0x0F);

        cpu.fetch((byte)0xB0);

        assertEquals((byte) 0xFF, cpu.getA(), "OR B Failed: A<>0xFF = " + Integer.toHexString(cpu.getA()));
    }

    @Test
    void testCP() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte)0xF0);
        cpu.setB((byte)0xF0);

        cpu.fetch((byte)0xB8);

        assertTrue(cpu.getZF(), "CP B Failed: Z (true) = " + cpu.getZF());

        cpu.setA((byte)0xF0);
        cpu.setB((byte)0x0F);

        cpu.fetch((byte)0xB8);

        assertFalse(cpu.getZF(), "CP B Failed: NZ (false) = " + cpu.getZF());
    }
    
    @Test
    void testRET_cc_y_MustRET() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0);
        compTest.poke(0x0001, (byte) 0xFF);
        
        cpu.setPC(0x0000);
        cpu.setSP((short) 0x0000);
        
        cpu.resZF();
        
        cpu.fetch((byte) 0xC0); // RET NZ

        assertAll("RET cc[y] Group",
            () -> assertEquals((short)0xFF00, (short)cpu.getPC(), "RET NZ Failed: PC was modified (PC=" + cpu.getPC() + ")"),
            () -> assertNotEquals(0x0000, cpu.getPC(), "RET NZ Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

            () -> assertEquals(0x0002, cpu.getSP(), "RET NZ Failed: SP was modified (SP=" + cpu.getSP() + ")"),
            () -> assertNotEquals(0x0000, cpu.getSP(), "RET NZ Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );
        
        cpu.setPC(0x0000);
        cpu.setSP((short) 0x0000);
        
        cpu.setZF();
        
        cpu.fetch((byte) 0xC8); // RET Z

        assertAll("RET cc[y] Group",
            () -> assertEquals((short)0xFF00, (short)cpu.getPC(), "RET Z Failed: PC was modified (PC=" + cpu.getPC() + ")"),
            () -> assertNotEquals(0x0000, cpu.getPC(), "RET Z Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

            () -> assertEquals(0x0002, cpu.getSP(), "RET Z Failed: SP was modified (SP=" + cpu.getSP() + ")"),
            () -> assertNotEquals(0x0000, cpu.getSP(), "RET Z Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );
        
        cpu.setPC(0x0000);
        cpu.setSP((short) 0x0000);
        
        cpu.resCF();
        
        cpu.fetch((byte) 0xD0); // RET NC

        assertAll("RET cc[y] Group",
            () -> assertEquals((short)0xFF00, (short)cpu.getPC(), "RET NC Failed: PC was modified (PC=" + cpu.getPC() + ")"),
            () -> assertNotEquals(0x0000, cpu.getPC(), "RET NC Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

            () -> assertEquals(0x0002, cpu.getSP(), "RET NC Failed: SP was modified (SP=" + cpu.getSP() + ")"),
            () -> assertNotEquals(0x0000, cpu.getSP(), "RET NC Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );
        
        cpu.setPC(0x0000);
        cpu.setSP((short) 0x0000);
        
        cpu.setCF();
        
        cpu.fetch((byte) 0xD8); // RET C

        assertAll("RET cc[y] Group",
            () -> assertEquals((short)0xFF00, (short)cpu.getPC(), "RET C Failed: PC was modified (PC=" + cpu.getPC() + ")"),
            () -> assertNotEquals(0x0000, cpu.getPC(), "RET C Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

            () -> assertEquals(0x0002, cpu.getSP(), "RET C Failed: SP was modified (SP=" + cpu.getSP() + ")"),
            () -> assertNotEquals(0x000, cpu.getSP(), "RET C Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );
        
        cpu.setPC(0x0000);
        cpu.setSP((short) 0x0000);
        
        cpu.resPF();
        
        cpu.fetch((byte) 0xE0); // RET PO

        assertAll("RET cc[y] Group",
            () -> assertEquals((short)0xFF00, (short)cpu.getPC(), "RET PO Failed: PC was modified (PC=" + cpu.getPC() + ")"),
            () -> assertNotEquals(0x0000, cpu.getPC(), "RET PO Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

            () -> assertEquals(0x0002, cpu.getSP(), "RET PO Failed: SP was modified (SP=" + cpu.getSP() + ")"),
            () -> assertNotEquals(0x0000, cpu.getSP(), "RET PO Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );
        
        cpu.setPC(0x0000);
        cpu.setSP((short) 0x0000);
        
        cpu.setPF();
        
        cpu.fetch((byte) 0xE8); // RET PE

        assertAll("RET cc[y] Group",
            () -> assertEquals((short)0xFF00, (short)cpu.getPC(), "RET PE Failed: PC was modified (PC=" + cpu.getPC() + ")"),
            () -> assertNotEquals(0x0000, cpu.getPC(), "RET PE Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

            () -> assertEquals((short)0x0002, cpu.getSP(), "RET PE Failed: SP was modified (SP=" + cpu.getSP() + ")"),
            () -> assertNotEquals(0x0000, cpu.getSP(), "RET PE Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );
        
        cpu.setPC(0x0000);
        cpu.setSP((short) 0x0000);
        
        cpu.resSF();
        
        cpu.fetch((byte) 0xF0); // RET P

        assertAll("RET cc[y] Group",
            () -> assertEquals((short)0xFF00, (short)cpu.getPC(), "RET P Failed: PC was modified (PC=" + cpu.getPC() + ")"),
            () -> assertNotEquals(0x0000, cpu.getPC(), "RET P Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

            () -> assertEquals(0x0002, cpu.getSP(), "RET P Failed: SP was modified (SP=" + cpu.getSP() + ")"),
            () -> assertNotEquals(0x0000, cpu.getSP(), "RET P Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );
        
        cpu.setPC(0x0000);
        cpu.setSP((short) 0x0000);
        
        cpu.setSF();
        
        cpu.fetch((byte) 0xF8); // RET M

        assertAll("RET cc[y] Group",
            () -> assertEquals((short)0xFF00, (short)cpu.getPC(), "RET M Failed: PC was modified (PC=" + cpu.getPC() + ")"),
            () -> assertNotEquals(0x0000, cpu.getPC(), "RET M Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

            () -> assertEquals(0x0002, cpu.getSP(), "RET M Failed: SP was modified (SP=" + cpu.getSP() + ")"),
            () -> assertNotEquals(0x0000, cpu.getSP(), "RET M Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );
    }
    
    @Test
    void testRET_cc_y_MustNotRET() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0);
        compTest.poke(0x0001, (byte) 0xFF);
        
        cpu.setPC(0x0000);
        cpu.setSP((short) 0x0000);
        
        cpu.setZF();
        
        cpu.fetch((byte) 0xC0); // RET NZ

        assertAll("RET cc[y] Group",
            () -> assertEquals(0x0000, cpu.getPC(), "RET NZ Failed: PC was modified (PC=" + cpu.getPC() + ")"),
            () -> assertNotEquals(0xFF00, cpu.getPC(), "RET NZ Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

            () -> assertEquals(0x0000, cpu.getSP(), "RET NZ Failed: SP was modified (SP=" + cpu.getSP() + ")"),
            () -> assertNotEquals(0x0002, cpu.getSP(), "RET NZ Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );
        
        cpu.resZF();
        
        cpu.fetch((byte) 0xC8); // RET Z

        assertAll("RET cc[y] Group",
            () -> assertEquals(0x0000, cpu.getPC(), "RET Z Failed: PC was modified (PC=" + cpu.getPC() + ")"),
            () -> assertNotEquals(0xFF00, cpu.getPC(), "RET Z Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

            () -> assertEquals(0x0000, cpu.getSP(), "RET Z Failed: SP was modified (SP=" + cpu.getSP() + ")"),
            () -> assertNotEquals(0x0002, cpu.getSP(), "RET Z Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );
        
        cpu.setCF();
        
        cpu.fetch((byte) 0xD0); // RET NC

        assertAll("RET cc[y] Group",
            () -> assertEquals(0x0000, cpu.getPC(), "RET NC Failed: PC was modified (PC=" + cpu.getPC() + ")"),
            () -> assertNotEquals(0xFF00, cpu.getPC(), "RET NC Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

            () -> assertEquals(0x0000, cpu.getSP(), "RET NC Failed: SP was modified (SP=" + cpu.getSP() + ")"),
            () -> assertNotEquals(0x0002, cpu.getSP(), "RET NC Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );
        
        cpu.resCF();
        
        cpu.fetch((byte) 0xD8); // RET C

        assertAll("RET cc[y] Group",
            () -> assertEquals(0x0000, cpu.getPC(), "RET C Failed: PC was modified (PC=" + cpu.getPC() + ")"),
            () -> assertNotEquals(0xFF00, cpu.getPC(), "RET C Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

            () -> assertEquals(0x0000, cpu.getSP(), "RET C Failed: SP was modified (SP=" + cpu.getSP() + ")"),
            () -> assertNotEquals(0x0002, cpu.getSP(), "RET C Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );
        
        cpu.setPF();
        
        cpu.fetch((byte) 0xE0); // RET PO

        assertAll("RET cc[y] Group",
            () -> assertEquals(0x0000, cpu.getPC(), "RET PO Failed: PC was modified (PC=" + cpu.getPC() + ")"),
            () -> assertNotEquals(0xFF00, cpu.getPC(), "RET PO Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

            () -> assertEquals(0x0000, cpu.getSP(), "RET PO Failed: SP was modified (SP=" + cpu.getSP() + ")"),
            () -> assertNotEquals(0x0002, cpu.getSP(), "RET PO Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );
        
        cpu.resPF();
        
        cpu.fetch((byte) 0xE8); // RET PE

        assertAll("RET cc[y] Group",
            () -> assertEquals(0x0000, cpu.getPC(), "RET PE Failed: PC was modified (PC=" + cpu.getPC() + ")"),
            () -> assertNotEquals(0xFF00, cpu.getPC(), "RET PE Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

            () -> assertEquals(0x0000, cpu.getSP(), "RET PE Failed: SP was modified (SP=" + cpu.getSP() + ")"),
            () -> assertNotEquals(0x0002, cpu.getSP(), "RET PE Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );
        
        cpu.setSF();
        
        cpu.fetch((byte) 0xF0); // RET P

        assertAll("RET cc[y] Group",
            () -> assertEquals(0x0000, cpu.getPC(), "RET P Failed: PC was modified (PC=" + cpu.getPC() + ")"),
            () -> assertNotEquals(0xFF00, cpu.getPC(), "RET P Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

            () -> assertEquals(0x0000, cpu.getSP(), "RET P Failed: SP was modified (SP=" + cpu.getSP() + ")"),
            () -> assertNotEquals(0x0002, cpu.getSP(), "RET P Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );
        
        cpu.resSF();
        
        cpu.fetch((byte) 0xF8); // RET M

        assertAll("RET cc[y] Group",
            () -> assertEquals(0x0000, cpu.getPC(), "RET M Failed: PC was modified (PC=" + cpu.getPC() + ")"),
            () -> assertNotEquals(0xFF00, cpu.getPC(), "RET M Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

            () -> assertEquals(0x0000, cpu.getSP(), "RET M Failed: SP was modified (SP=" + cpu.getSP() + ")"),
            () -> assertNotEquals(0x0002, cpu.getSP(), "RET M Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );
    }
    
    @Test
    void testPOP_rp2_p() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0);
        compTest.poke(0x0001, (byte) 0xFF);
        
        cpu.setSP((short) 0x0000);
        
        cpu.setBC((short) 0x0000);
        
        cpu.fetch((byte)0xC1); // POP BC
        
        assertAll("POP rp2[y] Group",
            () -> assertEquals((short)0xFF00, cpu.getBC(), "POP BC Failed: BC was not modified (BC=" + cpu.getBC() + ")"),
            () -> assertNotEquals(0x0000, cpu.getBC(), "POP BC Failed: BC still 0x0000 (BC=" + cpu.getBC() + ")")
        );
        
        cpu.setSP((short) 0x0000);
        
        cpu.setDE((short) 0x0000);
        
        cpu.fetch((byte)0xD1); // POP DE
        
        assertAll("POP rp2[y] Group",
            () -> assertEquals((short)0xFF00, cpu.getDE(), "POP DE Failed: DE was not modified (DE=" + cpu.getDE() + ")"),
            () -> assertNotEquals(0x0000, cpu.getDE(), "POP DE Failed: DE still 0x0000 (DE=" + cpu.getDE() + ")")
        );

        cpu.setSP((short) 0x0000);

        cpu.setHL((short) 0x0000);

        cpu.fetch((byte)0xE1); // POP HL

        assertAll("POP rp2[y] Group",
                () -> assertEquals((short)0xFF00, cpu.getHL(), "POP HL Failed: HL was not modified (HL=" + cpu.getHL() + ")"),
                () -> assertNotEquals(0x0000, cpu.getHL(), "POP HL Failed: HL still 0x0000 (HL=" + cpu.getHL() + ")")
        );

        cpu.setSP((short) 0x0000);

        cpu.setA((byte) 0x00);
        cpu.setF((byte) 0x00);

        cpu.fetch((byte)0xF1); // POP AF

        assertAll("POP rp2[y] Group",
                () -> assertEquals((byte)0xFF, cpu.getA(), "POP AF Failed: AF was not modified (A=" + Integer.toHexString(cpu.getA()) + ")")
                //() -> assertEquals((byte)0x00, cpu.getF(), "POP AF Failed: AF was not modified (F=" + Integer.toHexString(cpu.getF()) + ")")
        );
    }
    
    @Test
    void testRET() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0);
        compTest.poke(0x0001, (byte) 0xFF);

        cpu.setPC(0x0000);
        cpu.setSP((short) 0x0000);

        cpu.fetch((byte)0xC9); // RET

        assertAll("RET Group",
                () -> assertEquals((short)0xFF00, (short)cpu.getPC(), "RET Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals(0x0000, cpu.getPC(), "RET Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

                () -> assertEquals(0x0002, cpu.getSP(), "RET NZ Failed: SP was modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals(0x0000, cpu.getSP(), "RET NZ Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );
    }
    
    @Test
    void testEXX() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setBC((short) 0x0102);
        cpu.setDE((short) 0x0304);
        cpu.setHL((short) 0x0506);

        cpu.setBC_((short) 0x0708);
        cpu.setDE_((short) 0x090A);
        cpu.setHL_((short) 0x0B0C);

        cpu.EXX();

        assertAll("EXX Group",
                () -> assertEquals((short)0x0708, cpu.getBC(), "EXX Failed (BC="+ Integer.toHexString(cpu.getBC())+")"),
                () -> assertEquals((short)0x090A, cpu.getDE(), "EXX Failed (DE="+ Integer.toHexString(cpu.getDE())+")"),
                () -> assertEquals((short)0x0B0C, cpu.getHL(), "EXX Failed (HL="+ Integer.toHexString(cpu.getHL())+")"),

                () -> assertNotEquals((short)0x0102, cpu.getBC(), "EXX Failed (BC="+ Integer.toHexString(cpu.getBC())+")"),
                () -> assertNotEquals((short)0x0304, cpu.getDE(), "EXX Failed (DE="+ Integer.toHexString(cpu.getDE())+")"),
                () -> assertNotEquals((short)0x0506, cpu.getHL(), "EXX Failed (HL="+ Integer.toHexString(cpu.getHL())+")"),

                () -> assertNotEquals((short)0x0708, cpu.getBC_(), "EXX Failed (BC'="+ Integer.toHexString(cpu.getBC_())+")"),
                () -> assertNotEquals((short)0x090A, cpu.getDE_(), "EXX Failed (DE'="+ Integer.toHexString(cpu.getDE_())+")"),
                () -> assertNotEquals((short)0x0B0C, cpu.getHL_(), "EXX Failed (HL'="+ Integer.toHexString(cpu.getHL_())+")"),

                () -> assertEquals((short)0x0102, cpu.getBC_(), "EXX Failed (BC'="+ Integer.toHexString(cpu.getBC_())+")"),
                () -> assertEquals((short)0x0304, cpu.getDE_(), "EXX Failed (DE'="+ Integer.toHexString(cpu.getDE_())+")"),
                () -> assertEquals((short)0x0506, cpu.getHL_(), "EXX Failed (HL'="+ Integer.toHexString(cpu.getHL_())+")")
        );
    }
    
    @Test
    void testJP_HL() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setPC((short) 0x0102);
        cpu.setHL((short) 0x0304);

        cpu.JP_HL();

        assertAll("JP (HL) Group",
                () -> assertEquals((short)0x0304, cpu.getPC(), "JP (HL) Failed: not was modified (PC="+ Integer.toHexString(cpu.getPC())+")"),
                () -> assertNotEquals((short)0x0102, cpu.getPC(), "JP (HL) Failed: still is 0x0102 (PC="+ Integer.toHexString(cpu.getPC())+")")
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
    void testJP_cc_y_nn_MustNotJump() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0);
        compTest.poke(0x0001, (byte) 0xFF);

        cpu.setPC(0x0000);

        cpu.setZF();

        cpu.fetch((byte) 0xC2); // JP NZ, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "JP NZ, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0xFF00, cpu.getPC(), "JP NZ, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.resZF();

        cpu.fetch((byte)0xCA); // JP Z, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "JP Z, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0xFF00, cpu.getPC(), "JP Z, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.setCF();

        cpu.fetch((byte)0xD2); // JP NC, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "JP NC, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0xFF00, cpu.getPC(), "JP NC, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.resCF();

        cpu.fetch((byte)0xDA); // JP C, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "JP C, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0xFF00, cpu.getPC(), "JP C, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.setPF();

        cpu.fetch((byte)0xE2); // JP PO, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "JP PO, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0xFF00, cpu.getPC(), "JP PO, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.resPF();

        cpu.fetch((byte)0xEA); // JP PE, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "JP PE, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0xFF00, cpu.getPC(), "JP PE, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.setSF();

        cpu.fetch((byte)0xF2); // JP P, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "JP P, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0xFF00, cpu.getPC(), "JP P, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.resSF();

        cpu.fetch((byte)0xFA); // JP M, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "JP M, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0xFF00, cpu.getPC(), "JP M, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );
    }

    @Test
    void testJP_cc_y_nn_MustJump() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0);
        compTest.poke(0x0001, (byte) 0xFF);

        cpu.setPC(0x0000);

        cpu.resZF();

        cpu.fetch((byte) 0xC2); // JP NZ, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals((short)0xFF00, cpu.getPC(), "JP NZ, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "JP NZ, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.setZF();

        cpu.fetch((byte)0xCA); // JP Z, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals((short)0xFF00, cpu.getPC(), "JP Z, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "JP Z, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.resCF();

        cpu.fetch((byte)0xD2); // JP NC, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals((short)0xFF00, cpu.getPC(), "JP NC, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "JP NC, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.setCF();

        cpu.fetch((byte)0xDA); // JP C, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals((short)0xFF00, cpu.getPC(), "JP C, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "JP C, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.resPF();

        cpu.fetch((byte)0xE2); // JP PO, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals((short)0xFF00, cpu.getPC(), "JP PO, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "JP PO, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.setPF();

        cpu.fetch((byte)0xEA); // JP PE, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals((short)0xFF00, cpu.getPC(), "JP PE, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "JP PE, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.resSF();

        cpu.fetch((byte)0xF2); // JP P, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals((short)0xFF00, cpu.getPC(), "JP P, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "JP P, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.setSF();

        cpu.fetch((byte)0xFA); // JP M, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals((short)0xFF00, cpu.getPC(), "JP M, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "JP M, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );
    }

    @Test
    void testJP_nn() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0);
        compTest.poke(0x0001, (byte) 0xFF);

        cpu.setPC(0x0000);

        cpu.fetch((byte) 0xC3); // JP 0xFF00

        assertAll("JP nn Group",
                () -> assertEquals((short)0xFF00, cpu.getPC(), "JP 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "JP 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );
    }

    @Test
    void testOUT_n_A() {} // TODO

    @Test
    void testIN_A_n() {} // TODO

    @Test
    void testEX_SP_HL() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0);
        compTest.poke(0x0001, (byte) 0xFF);

        cpu.setSP((short) 0x0000);
        cpu.setHL((short) 0x1234);

        cpu.EX_SP_HL();

        assertAll("EX (SP), HL Group",
                () -> assertEquals((short)0xFF00, cpu.getHL(), "EX (SP), HL Failed HL="+ Integer.toHexString(cpu.getHL())),
                () -> assertEquals((byte) 0x34, compTest.peek(0x0000), "EX (SP), HL Failed (SP)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertEquals((byte) 0x12, compTest.peek(0x0001), "EX (SP), HL Failed (SP+1)="+ Integer.toHexString(compTest.peek(0x0001))),

                () -> assertNotEquals((short)0x1234, cpu.getHL(), "EX (SP), HL Failed HL="+ Integer.toHexString(cpu.getHL())),
                () -> assertNotEquals((byte) 0x00, compTest.peek(0x0000), "EX (SP), HL Failed (SP)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertNotEquals((byte) 0xFF, compTest.peek(0x0001), "EX (SP), HL Failed (SP+1)="+ Integer.toHexString(compTest.peek(0x0001)))
        );
    }

    @Test
    void testEX_DE_HL() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setDE((short) 0x1234);
        cpu.setHL((short) 0x5678);

        cpu.EX_DE_HL();

        assertAll("EXX Group",
                () -> assertEquals((short)0x5678, cpu.getDE(), "EX DE, HL Failed (DE="+ Integer.toHexString(cpu.getDE())+")"),
                () -> assertEquals((short)0x1234, cpu.getHL(), "EX DE, HL Failed (HL="+ Integer.toHexString(cpu.getHL())+")"),

                () -> assertNotEquals((short)0x1234, cpu.getDE(), "EX DE, HL Failed (DE="+ Integer.toHexString(cpu.getDE())+")"),
                () -> assertNotEquals((short)0x5678, cpu.getHL(), "EX DE, HL Failed (HL="+ Integer.toHexString(cpu.getHL())+")")
        );
    }

    @Test
    void testDI() {} // TODO

    @Test
    void testEI() {} // TODO

    @Test
    void testCALL_cc_y_nn_MustNotJump() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0);
        compTest.poke(0x0001, (byte) 0xFF);

        cpu.setPC(0x0000);

        cpu.setZF();

        cpu.fetch((byte) 0xC4); // CALL NZ, 0xFF00

        assertAll("CALL cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "CALL NZ, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0xFF00, cpu.getPC(), "CALL NZ, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.resZF();

        cpu.fetch((byte) 0xCC); // CALL Z, 0xFF00

        assertAll("CALL cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "CALL Z, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0xFF00, cpu.getPC(), "CALL Z, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.setCF();

        cpu.fetch((byte) 0xD4); // CALL NC, 0xFF00

        assertAll("CALL cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "CALL NC, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0xFF00, cpu.getPC(), "CALL NC, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.resCF();

        cpu.fetch((byte) 0xDC); // CALL C, 0xFF00

        assertAll("CALL cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "CALL C, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0xFF00, cpu.getPC(), "CALL C, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.setPF();

        cpu.fetch((byte) 0xE4); // CALL PO, 0xFF00

        assertAll("CALL cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "CALL PO, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0xFF00, cpu.getPC(), "CALL PO, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.resPF();

        cpu.fetch((byte) 0xEC); // CALL PE, 0xFF00

        assertAll("CALL cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "CALL PE, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0xFF00, cpu.getPC(), "CALL PE, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.setSF();

        cpu.fetch((byte) 0xF4); // CALL P, 0xFF00

        assertAll("CALL cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "CALL P, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0xFF00, cpu.getPC(), "CALL P, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.resSF();

        cpu.fetch((byte) 0xFC); // CALL M, 0xFF00

        assertAll("CALL cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "CALL M, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0xFF00, cpu.getPC(), "CALL M, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );
    }

    @Test
    void testCALL_cc_y_nn_MustJump() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0x34);
        compTest.poke(0x0001, (byte) 0x12);
        compTest.poke(0x0002, (byte) 0);
        compTest.poke(0x0003, (byte) 0xFF);

        cpu.setPC(0x0002);
        cpu.setSP((short) 0x0002);

        cpu.resZF();

        cpu.fetch((byte) 0xC4); // CALL NZ, 0xFF00

        assertAll("CALL cc[y], nn Group",
                () -> assertEquals((short)0xFF00, cpu.getPC(), "CALL NZ, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "CALL NZ, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),

                () -> assertEquals((short)0x0000, cpu.getSP(), "CALL NZ, 0xFF00 Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getSP(), "CALL NZ, 0xFF00 Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),

                () -> assertEquals((byte) 0x04, compTest.peek(0x0000), "CALL NZ, 0xFF00 Failed (SP-2)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertEquals((byte) 0x00, compTest.peek(0x0001), "CALL NZ, 0xFF00 Failed (SP-1)="+ Integer.toHexString(compTest.peek(0x0001))),

                () -> assertNotEquals((byte) 0x34, compTest.peek(0x0000), "CALL NZ, 0xFF00 Failed (SP-2)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertNotEquals((byte) 0x12, compTest.peek(0x0001), "CALL NZ, 0xFF00 Failed (SP-1)="+ Integer.toHexString(compTest.peek(0x0001)))
        );

        cpu.setPC(0x0002);
        cpu.setSP((short) 0x0002);

        cpu.setZF();

        cpu.fetch((byte) 0xCC); // CALL Z, 0xFF00

        assertAll("CALL cc[y], nn Group",
                () -> assertEquals((short)0xFF00, cpu.getPC(), "CALL Z, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "CALL Z, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),

                () -> assertEquals((short)0x0000, cpu.getSP(), "CALL Z, 0xFF00 Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getSP(), "CALL Z, 0xFF00 Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),

                () -> assertEquals((byte) 0x04, compTest.peek(0x0000), "CALL Z, 0xFF00 Failed (SP-2)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertEquals((byte) 0x00, compTest.peek(0x0001), "CALL Z, 0xFF00 Failed (SP-1)="+ Integer.toHexString(compTest.peek(0x0001))),

                () -> assertNotEquals((byte) 0x34, compTest.peek(0x0000), "CALL Z, 0xFF00 Failed (SP-2)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertNotEquals((byte) 0x12, compTest.peek(0x0001), "CALL Z, 0xFF00 Failed (SP-1)="+ Integer.toHexString(compTest.peek(0x0001)))
        );

        cpu.setPC(0x0002);
        cpu.setSP((short) 0x0002);

        cpu.resCF();

        cpu.fetch((byte) 0xD4); // CALL NC, 0xFF00

        assertAll("CALL cc[y], nn Group",
                () -> assertEquals((short)0xFF00, cpu.getPC(), "CALL NC, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "CALL NC, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),

                () -> assertEquals((short)0x0000, cpu.getSP(), "CALL NC, 0xFF00 Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getSP(), "CALL NC, 0xFF00 Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),

                () -> assertEquals((byte) 0x04, compTest.peek(0x0000), "CALL NC, 0xFF00 Failed (SP-2)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertEquals((byte) 0x00, compTest.peek(0x0001), "CALL NC, 0xFF00 Failed (SP-1)="+ Integer.toHexString(compTest.peek(0x0001))),

                () -> assertNotEquals((byte) 0x34, compTest.peek(0x0000), "CALL NC, 0xFF00 Failed (SP-2)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertNotEquals((byte) 0x12, compTest.peek(0x0001), "CALL NC, 0xFF00 Failed (SP-1)="+ Integer.toHexString(compTest.peek(0x0001)))
        );

        cpu.setPC(0x0002);
        cpu.setSP((short) 0x0002);

        cpu.setCF();

        cpu.fetch((byte) 0xDC); // CALL C, 0xFF00

        assertAll("CALL cc[y], nn Group",
                () -> assertEquals((short)0xFF00, cpu.getPC(), "CALL C, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "CALL C, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),

                () -> assertEquals((short)0x0000, cpu.getSP(), "CALL C, 0xFF00 Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getSP(), "CALL C, 0xFF00 Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),

                () -> assertEquals((byte) 0x04, compTest.peek(0x0000), "CALL C, 0xFF00 Failed (SP-2)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertEquals((byte) 0x00, compTest.peek(0x0001), "CALL C, 0xFF00 Failed (SP-1)="+ Integer.toHexString(compTest.peek(0x0001))),

                () -> assertNotEquals((byte) 0x34, compTest.peek(0x0000), "CALL C, 0xFF00 Failed (SP-2)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertNotEquals((byte) 0x12, compTest.peek(0x0001), "CALL C, 0xFF00 Failed (SP-1)="+ Integer.toHexString(compTest.peek(0x0001)))
        );

        cpu.setPC(0x0002);
        cpu.setSP((short) 0x0002);

        cpu.resPF();

        cpu.fetch((byte) 0xE4); // CALL PO, 0xFF00

        assertAll("CALL cc[y], nn Group",
                () -> assertEquals((short)0xFF00, cpu.getPC(), "CALL PO, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "CALL PO, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),

                () -> assertEquals((short)0x0000, cpu.getSP(), "CALL PO, 0xFF00 Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getSP(), "CALL PO, 0xFF00 Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),

                () -> assertEquals((byte) 0x04, compTest.peek(0x0000), "CALL PO, 0xFF00 Failed (SP-2)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertEquals((byte) 0x00, compTest.peek(0x0001), "CALL PO, 0xFF00 Failed (SP-1)="+ Integer.toHexString(compTest.peek(0x0001))),

                () -> assertNotEquals((byte) 0x34, compTest.peek(0x0000), "CALL PO, 0xFF00 Failed (SP-2)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertNotEquals((byte) 0x12, compTest.peek(0x0001), "CALL PO, 0xFF00 Failed (SP-1)="+ Integer.toHexString(compTest.peek(0x0001)))
        );

        cpu.setPC(0x0002);
        cpu.setSP((short) 0x0002);

        cpu.setPF();

        cpu.fetch((byte) 0xEC); // CALL PE, 0xFF00

        assertAll("CALL cc[y], nn Group",
                () -> assertEquals((short)0xFF00, cpu.getPC(), "CALL PE, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "CALL PE, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),

                () -> assertEquals((short)0x0000, cpu.getSP(), "CALL PE, 0xFF00 Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getSP(), "CALL PE, 0xFF00 Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),

                () -> assertEquals((byte) 0x04, compTest.peek(0x0000), "CALL PE, 0xFF00 Failed (SP-2)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertEquals((byte) 0x00, compTest.peek(0x0001), "CALL PE, 0xFF00 Failed (SP-1)="+ Integer.toHexString(compTest.peek(0x0001))),

                () -> assertNotEquals((byte) 0x34, compTest.peek(0x0000), "CALL PE, 0xFF00 Failed (SP-2)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertNotEquals((byte) 0x12, compTest.peek(0x0001), "CALL PE, 0xFF00 Failed (SP-1)="+ Integer.toHexString(compTest.peek(0x0001)))
        );

        cpu.setPC(0x0002);
        cpu.setSP((short) 0x0002);

        cpu.resSF();

        cpu.fetch((byte) 0xF4); // CALL P, 0xFF00

        assertAll("CALL cc[y], nn Group",
                () -> assertEquals((short)0xFF00, cpu.getPC(), "CALL P, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "CALL P, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),

                () -> assertEquals((short)0x0000, cpu.getSP(), "CALL P, 0xFF00 Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getSP(), "CALL P, 0xFF00 Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),

                () -> assertEquals((byte) 0x04, compTest.peek(0x0000), "CALL P, 0xFF00 Failed (SP-2)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertEquals((byte) 0x00, compTest.peek(0x0001), "CALL P, 0xFF00 Failed (SP-1)="+ Integer.toHexString(compTest.peek(0x0001))),

                () -> assertNotEquals((byte) 0x34, compTest.peek(0x0000), "CALL P, 0xFF00 Failed (SP-2)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertNotEquals((byte) 0x12, compTest.peek(0x0001), "CALL P, 0xFF00 Failed (SP-1)="+ Integer.toHexString(compTest.peek(0x0001)))
        );

        cpu.setPC(0x0002);
        cpu.setSP((short) 0x0002);

        cpu.setSF();

        cpu.fetch((byte) 0xFC); // CALL M, 0xFF00

        assertAll("CALL cc[y], nn Group",
                () -> assertEquals((short)0xFF00, cpu.getPC(), "CALL M, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "CALL M, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),

                () -> assertEquals((short)0x0000, cpu.getSP(), "CALL M, 0xFF00 Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getSP(), "CALL M, 0xFF00 Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),

                () -> assertEquals((byte) 0x04, compTest.peek(0x0000), "CALL M, 0xFF00 Failed (SP-2)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertEquals((byte) 0x00, compTest.peek(0x0001), "CALL M, 0xFF00 Failed (SP-1)="+ Integer.toHexString(compTest.peek(0x0001))),

                () -> assertNotEquals((byte) 0x34, compTest.peek(0x0000), "CALL M, 0xFF00 Failed (SP-2)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertNotEquals((byte) 0x12, compTest.peek(0x0001), "CALL M, 0xFF00 Failed (SP-1)="+ Integer.toHexString(compTest.peek(0x0001)))
        );
    }

    @Test
    void testPUSH_rp2_p() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0x34);
        compTest.poke(0x0001, (byte) 0x12);

        cpu.setSP((short)0x0002);
        cpu.setBC((short)0x5678);

        cpu.fetch((byte)0xC5); // PUSH BC

        assertAll("PUSH rp2[p] Group",
                () -> assertEquals((short)0x0000, cpu.getSP(), "PUSH BC Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getSP(), "PUSH BC Failed: PC was NOT modified (SP=" + cpu.getSP() + ")")
        );

        // rest of registers TODO?
    }

    @Test
    void testCALL_nn() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0x34);
        compTest.poke(0x0001, (byte) 0x12);
        compTest.poke(0x0002, (byte) 0);
        compTest.poke(0x0003, (byte) 0xFF);

        cpu.setPC(0x0002);
        cpu.setSP((short) 0x0002);

        cpu.fetch((byte) 0xCD); // CALL 0xFF00

        assertAll("CALL nn Group",
                () -> assertEquals((short)0xFF00, cpu.getPC(), "CALL 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "CALL 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),

                () -> assertEquals((short)0x0000, cpu.getSP(), "CALL 0xFF00 Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getSP(), "CALL 0xFF00 Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),

                () -> assertEquals((byte) 0x04, compTest.peek(0x0001), "CALL 0xFF00 Failed (SP+1)="+ Integer.toHexString(compTest.peek(0x0001))),
                () -> assertEquals((byte) 0x00, compTest.peek(0x0000), "CALL 0xFF00 Failed (SP)="+ Integer.toHexString(compTest.peek(0x0000))),

                () -> assertNotEquals((byte) 0x12, compTest.peek(0x0001), "CALL 0xFF00 Failed (SP+1)="+ Integer.toHexString(compTest.peek(0x0001))),
                () -> assertNotEquals((byte) 0x34, compTest.peek(0x0000), "CALL 0xFF00 Failed (SP)="+ Integer.toHexString(compTest.peek(0x0000)))
        );
    }

    @Test
    void testADD_A_n() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0x20);

        cpu.setPC(0x0000);
        cpu.setA((byte)0x10);

        cpu.fetch((byte)0xC6); // ADD A, xx20

        assertEquals((byte) 0x30, cpu.getA(), "ADD A, 0x20 Failed: A<>0x30 = " + Integer.toHexString(cpu.getA()));
    }

    @Test
    void testADC_A_n() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0x20);

        cpu.setPC(0x0000);
        cpu.setA((byte)0x10);
        cpu.resCF();

        cpu.fetch((byte)0xCE); // ADC A, 0x20

        assertEquals((byte) 0x30, cpu.getA(), "ADC A, 0x20; C=0 Failed: A<>0x30 = " + Integer.toHexString(cpu.getA()));

        cpu.setPC(0x0000);
        cpu.setA((byte)0x10);
        cpu.setCF();

        cpu.fetch((byte)0xCE);

        assertEquals((byte) 0x31, cpu.getA(), "ADC A, 0x20; C=1 Failed: A<>0x31 = " + Integer.toHexString(cpu.getA()));
    }

    @Test
    void testSUB_A_n() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0x10);

        cpu.setPC(0x0000);
        cpu.setA((byte)0x20);

        cpu.fetch((byte)0xD6);

        assertEquals((byte) 0x10, cpu.getA(), "SUB A, 0x10 Failed: A<>0x10 = " + Integer.toHexString(cpu.getA()));
    }

    @Test
    void testSBC_A_n() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0x10);

        cpu.setPC(0x0000);
        cpu.setA((byte)0x20);
        cpu.resCF();

        cpu.fetch((byte)0xDE);

        assertEquals((byte) 0x10, cpu.getA(), "SBC A, 0x10; C=0 Failed: A<>0x10 = " + Integer.toHexString(cpu.getA()));

        cpu.setPC(0x0000);
        cpu.setA((byte)0x20);
        cpu.setCF();

        cpu.fetch((byte)0xDE);

        assertEquals((byte) 0x0F, cpu.getA(), "SBC A, 0x10; C=1 Failed: A<>0x0F = " + Integer.toHexString(cpu.getA()));
    }

    @Test
    void testAND_n() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0x0F);

        cpu.setPC(0x0000);
        cpu.setA((byte)0xF0);

        cpu.fetch((byte)0xE6);

        assertEquals((byte) 0x00, cpu.getA(), "AND 0x0F Failed: A<>0x00 = " + Integer.toHexString(cpu.getA()));
    }

    @Test
    void testXOR_n() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xFF);

        cpu.setPC(0x0000);
        cpu.setA((byte)0xF0);

        cpu.fetch((byte)0xEE);

        assertEquals((byte) 0x0F, cpu.getA(), "XOR 0xFF Failed: A<>0x0F = " + Integer.toHexString(cpu.getA()));
    }

    @Test
    void testOR_n() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0x0F);

        cpu.setPC(0x0000);
        cpu.setA((byte)0xF0);

        cpu.fetch((byte)0xF6);

        assertEquals((byte) 0xFF, cpu.getA(), "OR 0x0F Failed: A<>0xFF = " + Integer.toHexString(cpu.getA()));
    }

    @Test
    void testCP_n() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xF0);

        cpu.setPC(0x0000);
        cpu.setA((byte)0xF0);

        cpu.fetch((byte)0xFE);

        assertTrue(cpu.getZF(), "CP 0xF0 Failed: Z (true) = " + cpu.getZF());

        cpu.setPC(0x0000);
        cpu.setA((byte)0x0F);

        cpu.fetch((byte)0xFE);

        assertFalse(cpu.getZF(), "CP 0xF0 Failed: NZ (false) = " + cpu.getZF());
    }

    @Test
    void testRST_y_8() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0x34);
        compTest.poke(0x0001, (byte) 0x12);

        cpu.setPC(0x0002);
        cpu.setSP((short) 0x0002);

        cpu.fetch((byte) 0xC7); // RST 0x00

        assertAll("RST y*8 Group",
                () -> assertEquals((short)0x0000, cpu.getPC(), "RST 0x00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "RST 0x00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),

                () -> assertEquals((short)0x0000, cpu.getSP(), "RST 0x00 Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getSP(), "RST 0x00 Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),

                () -> assertEquals((byte) 0x02, compTest.peek(0x0000), "RST 0x00 Failed (SP-2)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertEquals((byte) 0x00, compTest.peek(0x0001), "RST 0x00 Failed (SP-1)="+ Integer.toHexString(compTest.peek(0x0001))),

                () -> assertNotEquals((byte) 0x34, compTest.peek(0x0000), "RST 0x00 Failed (SP-2)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertNotEquals((byte) 0x12, compTest.peek(0x0001), "RST 0x00 Failed (SP-1)="+ Integer.toHexString(compTest.peek(0x0001)))
        );

        compTest.poke(0x0000, (byte) 0x34);
        compTest.poke(0x0001, (byte) 0x12);

        cpu.setPC(0x0002);
        cpu.setSP((short) 0x0002);

        cpu.fetch((byte) 0xCF); // RST 0x08

        assertAll("RST y*8 Group",
                () -> assertEquals((short)0x0008, cpu.getPC(), "RST 0x00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "RST 0x00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),

                () -> assertEquals((short)0x0000, cpu.getSP(), "RST 0x00 Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getSP(), "RST 0x00 Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),

                () -> assertEquals((byte) 0x02, compTest.peek(0x0000), "RST 0x00 Failed (SP)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertEquals((byte) 0x00, compTest.peek(0x0001), "RST 0x00 Failed (SP+1)="+ Integer.toHexString(compTest.peek(0x0001))),

                () -> assertNotEquals((byte) 0x34, compTest.peek(0x0000), "RST 0x00 Failed (SP)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertNotEquals((byte) 0x12, compTest.peek(0x0001), "RST 0x00 Failed (SP+1)="+ Integer.toHexString(compTest.peek(0x0001)))
        );
    }
}
