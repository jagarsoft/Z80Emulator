package com.github.jagarsoft.test;

import com.github.jagarsoft.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ArithmeticTest {
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

        cpu.setHL((short)0xFFFF);
        cpu.setDE((short)0x0001);

        cpu.fetch((byte)0x19); // ADD HL, DE

        assertAll(
                () -> assertEquals((short)0x0000, cpu.getHL(), "ADD HL, DE Failed: HL<>0x0000"+Integer.toHexString(cpu.getHL())),
                () -> assertTrue(cpu.getCF(), "ADD HL, DE Carry was not affected (C="+cpu.getCF()+")")
        );
    }

    @Test
    void testADC_HL_rp_p() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xED);
        compTest.poke(0x0001, (byte) 0x4A);

        cpu.setPC(0x0000);

        cpu.setHL((short)0x1234);
        cpu.setBC((short)0x0001);
        cpu.setDE((short)0x0002);
        cpu.setSP((short)0x00FF);

        cpu.SCF();
        cpu.fetch(); // ADC HL, BC

        // assertAll("ADC HL, rp_p Group",
        assertEquals((short) 0x1236, cpu.getHL(), "ADC HL, BC Failed: HL<>0x1236 HL=" + Integer.toHexString(cpu.getHL()));
        assertNotEquals((short) 0x1234, cpu.getHL(), "ADC HL, BC Failed: HL Still 0x1234 HL=" + Integer.toHexString(cpu.getHL()));

        compTest.poke(0x0001, (byte) 0x5A);
        cpu.setPC(0x0000);

        cpu.SCF();
        cpu.fetch(); // ADC HL, DE

        assertEquals((short) 0x1239, cpu.getHL(), "ADC HL, DE Failed: HL<>0x1239 HL=" + Integer.toHexString(cpu.getHL()));
        assertNotEquals((short) 0x1236, cpu.getHL(), "ADC HL, DE Failed: HL Still 0x1236 HL=" + Integer.toHexString(cpu.getHL()));


        compTest.poke(0x0001, (byte) 0x6A);
        cpu.setPC(0x0000);

        cpu.SCF();
        cpu.fetch(); // ADC HL, HL

        assertEquals((short) 0x2473, cpu.getHL(), "ADC HL, HL Failed: HL<>0x2473 HL=" + Integer.toHexString(cpu.getHL()));
        assertNotEquals((short) 0x1239, cpu.getHL(), "ADC HL, HL Failed: HL Still 0x1239 HL=" + Integer.toHexString(cpu.getHL()));


        compTest.poke(0x0001, (byte) 0x7A);
        cpu.setPC(0x0000);

        cpu.SCF();
        cpu.fetch();  // ADC HL, SP

        assertEquals((short) 0x2573, cpu.getHL(), "ADC HL, SP Failed: HL<>0x2573 HL=" + Integer.toHexString(cpu.getHL()));
        assertNotEquals((short) 0x2473, cpu.getHL(), "ADC HL, SP Failed: HL Still 0x2473 HL=" + Integer.toHexString(cpu.getHL()));


        compTest.poke(0x0001, (byte) 0x5A);
        cpu.setPC(0x0000);

        cpu.SCF();
        cpu.setHL((short)0xFFFE);
        cpu.setDE((short)0x0001);

        cpu.fetch(); // ADC HL, DE

        assertAll(
                () -> assertEquals((short)0x0000, cpu.getHL(), "ADC HL, DE Failed: HL<>0x0000"+Integer.toHexString(cpu.getHL())),
                () -> assertTrue(cpu.getCF(), "ADC HL, DE Carry was not affected (C="+cpu.getCF()+")")
        );
    }

    @Test
    void testADD_IX_rp_p() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xDD);
        compTest.poke(0x0001, (byte) 0x09);

        cpu.setPC(0x0000);

        cpu.setIX((short)0x1234);
        cpu.setBC((short)0x0001);
        cpu.setDE((short)0x0002);
        cpu.setSP((short)0x00FF);

        cpu.fetch(); // ADD IX, BC

        // assertAll("ADD IX, rp_p Group",
        assertAll(
                () -> assertEquals((short) 0x1235, cpu.getIX(), "ADD IX, BC Failed: IX<>0x1235 IX=" + Integer.toHexString(cpu.getIX())),
                () -> assertNotEquals((short) 0x1234, cpu.getIX(), "ADD IX, BC Failed: IX Still 0x1234 IX=" + Integer.toHexString(cpu.getIX()))
        );

        compTest.poke(0x0001, (byte) 0x19);
        cpu.setPC(0x0000);

        cpu.fetch(); // ADD IX, DE

        assertEquals((short) 0x1237, cpu.getIX(), "ADD IX, DE Failed: IX<>0x1237 IX=" + Integer.toHexString(cpu.getIX()));
        assertNotEquals((short) 0x1235, cpu.getIX(), "ADD IX, DE Failed: IX Still 0x1235 IX=" + Integer.toHexString(cpu.getIX()));


        compTest.poke(0x0001, (byte) 0x29);
        cpu.setPC(0x0000);

        cpu.fetch(); // ADD IX, IX

        assertEquals((short) 0x246E, cpu.getIX(), "ADD IX, IX Failed: IX<>0x246E IX=" + Integer.toHexString(cpu.getIX()));
        assertNotEquals((short) 0x1237, cpu.getIX(), "ADD IX, IX Failed: IX Still 0x1237 IX=" + Integer.toHexString(cpu.getIX()));


        compTest.poke(0x0001, (byte) 0x39);
        cpu.setPC(0x0000);

        cpu.fetch();  // ADD IX, SP

        assertEquals((short) 0x256D, cpu.getIX(), "ADD IX, SP Failed: IX<>0x256D IX=" + Integer.toHexString(cpu.getIX()));
        assertNotEquals((short) 0x246E, cpu.getIX(), "ADD IX, SP Failed: IX Still 0x246E IX=" + Integer.toHexString(cpu.getIX()));


        compTest.poke(0x0001, (byte) 0x19);
        cpu.setPC(0x0000);

        cpu.setIX((short)0xFFFF);
        cpu.setDE((short)0x0001);

        cpu.fetch(); // ADD IX, DE

        assertAll(
                () -> assertEquals((short)0x0000, cpu.getIX(), "ADD HL, DE Failed: IX<>0x0000"+Integer.toHexString(cpu.getIX())),
                () -> assertTrue(cpu.getCF(), "ADD IX, DE Carry was not affected (C="+cpu.getCF()+")")
        );
    }

    @Test
    void testADD_IY_rp_p() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xFD);
        compTest.poke(0x0001, (byte) 0x09);

        cpu.setPC(0x0000);

        cpu.setIY((short)0x1234);
        cpu.setBC((short)0x0001);
        cpu.setDE((short)0x0002);
        cpu.setSP((short)0x00FF);

        cpu.fetch(); // ADD IY, BC

        // assertAll("ADD IY, rp_p Group",
        assertAll(
                () -> assertEquals((short) 0x1235, cpu.getIY(), "ADD IY, BC Failed: IY<>0x1235 IY=" + Integer.toHexString(cpu.getIY())),
                () -> assertNotEquals((short) 0x1234, cpu.getIY(), "ADD IY, BC Failed: IY Still 0x1234 IY=" + Integer.toHexString(cpu.getIY()))
        );

        compTest.poke(0x0001, (byte) 0x19);
        cpu.setPC(0x0000);

        cpu.fetch(); // ADD IY, DE

        assertEquals((short) 0x1237, cpu.getIY(), "ADD IY, DE Failed: IY<>0x1237 IY=" + Integer.toHexString(cpu.getIY()));
        assertNotEquals((short) 0x1235, cpu.getIY(), "ADD IY, DE Failed: IY Still 0x1235 IY=" + Integer.toHexString(cpu.getIY()));


        compTest.poke(0x0001, (byte) 0x29);
        cpu.setPC(0x0000);

        cpu.fetch(); // ADD IY, IY

        assertEquals((short) 0x246E, cpu.getIY(), "ADD IY, IY Failed: IY<>0x246E IY=" + Integer.toHexString(cpu.getIY()));
        assertNotEquals((short) 0x1237, cpu.getIY(), "ADD IY, IY Failed: IY Still 0x1237 IY=" + Integer.toHexString(cpu.getIY()));


        compTest.poke(0x0001, (byte) 0x39);
        cpu.setPC(0x0000);

        cpu.fetch();  // ADD IY, SP

        assertEquals((short) 0x256D, cpu.getIY(), "ADD IY, SP Failed: IY<>0x256D IY=" + Integer.toHexString(cpu.getIY()));
        assertNotEquals((short) 0x246E, cpu.getIY(), "ADD IY, SP Failed: IY Still 0x246E IY=" + Integer.toHexString(cpu.getIY()));


        compTest.poke(0x0001, (byte) 0x19);
        cpu.setPC(0x0000);

        cpu.setIY((short)0xFFFF);
        cpu.setDE((short)0x0001);

        cpu.fetch(); // ADD IY, DE

        assertAll(
                () -> assertEquals((short)0x0000, cpu.getIY(), "ADD HL, DE Failed: IY<>0x0000"+Integer.toHexString(cpu.getIY())),
                () -> assertTrue(cpu.getCF(), "ADD IY, DE Carry was not affected (C="+cpu.getCF()+")")
        );
    }

    @Test
    void testADD_A_r_z() { // TODO pending and Parity flag tests
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte)0x10);
        cpu.setB((byte)0x20);

        cpu.fetch((byte)0x80); // ADD A, B

        assertAll(
                () -> assertEquals((byte) 0x30, cpu.getA(), "ADD A, B Failed: A<>0x30 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getNF(), "N flag must be OFF (N=" + cpu.getNF() + ")")
        );

        cpu.setA((byte)0x7F);
        cpu.setB((byte)0x01);

        cpu.fetch((byte)0x80); // ADD A, B

        assertAll(
                () -> assertEquals((byte) 0x80, cpu.getA(), "ADD A, B Failed: A<>0x80 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertTrue(cpu.getVF(), "Overflow flag must be ON (V=" + cpu.getVF() + ")")
        );

        cpu.setA((byte)0x04);
        cpu.setB((byte)0xFE); // -2

        cpu.fetch((byte)0x80); // ADD A, B

        assertAll(
                () -> assertEquals((byte) 0x02, cpu.getA(), "ADD A, B Failed: A<>0x02 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")")
        );

        cpu.setA((byte)0x02);
        cpu.setB((byte)0xFC); // -4

        cpu.fetch((byte)0x80); // ADD A, B

        assertAll(
                () -> assertEquals((byte) 0xFE, cpu.getA(), "ADD A, B Failed: A<>-2 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")")
        );

        cpu.setA((byte)0xFE); // -2
        cpu.setB((byte)0xFC); // -4

        cpu.fetch((byte)0x80); // ADD A, B

        assertAll(
                () -> assertEquals((byte) 0xFA, cpu.getA(), "ADD A, B Failed: A<>-6= " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")")
        );

        cpu.setA((byte)0x81); // -127
        cpu.setB((byte)0xC2); // -62

        cpu.fetch((byte)0x80); // ADD A, B

        assertAll(
                () -> assertEquals((byte) 0x43, cpu.getA(), "ADD A, B Failed: A<>0x43 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")"),
                () -> assertTrue(cpu.getVF(), "Overflow flag must be ON (V=" + cpu.getVF() + ")")
        );

        cpu.setA((byte)0);
        cpu.setB((byte)0);

        cpu.fetch((byte)0x80); // ADD A, B

        assertAll(
                () -> assertEquals((byte) 0, cpu.getA(), "ADD A, B Failed: A<>0 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")")
        );

        cpu.setA((byte)0x0F);
        cpu.setB((byte)1);

        cpu.fetch((byte)0x80); // ADD A, B

        assertAll(
                () -> assertEquals((byte) 0x10, cpu.getA(), "ADD A, B Failed: A<>0x10 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON (H=" + cpu.getHF() + ")")
        );
    }

    @Test
    void testADC_A_r_z() { // TODO Cambiar el test a los otros registros no siempre B
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte)0x10);
        cpu.setB((byte)0x20);
        cpu.resCF();

        cpu.fetch((byte)0x88); // ADC A, B

        assertAll(
                () -> assertEquals((byte) 0x30, cpu.getA(), "ADC A, B Failed: A<>0x30 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getNF(), "N flag must be OFF (N=" + cpu.getNF() + ")")
        );

        cpu.setA((byte)0x10);
        cpu.setB((byte)0x20);
        cpu.setCF();

        cpu.fetch((byte)0x88); // ADC A, B

        assertAll(
                () -> assertEquals((byte) 0x31, cpu.getA(), "ADC A, B Failed: A<>0x31 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getNF(), "N flag must be OFF (N=" + cpu.getNF() + ")")
        );

        cpu.setA((byte)0x7F);
        cpu.setB((byte)0x01);
        cpu.setCF();

        cpu.fetch((byte)0x88); // ADC A, B

        assertAll(
                () -> assertEquals((byte) 0x81, cpu.getA(), "ADC A, B Failed: A<>0x81 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertTrue(cpu.getVF(), "Overflow flag must be ON (V=" + cpu.getVF() + ")")
        );

        cpu.setA((byte)0x04);
        cpu.setB((byte)0xFE); // -2
        cpu.setCF();

        cpu.fetch((byte)0x88); // ADC A, B

        assertAll(
                () -> assertEquals((byte) 0x03, cpu.getA(), "ADC A, B Failed: A<>0x03 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")")
        );

        cpu.setA((byte)0x02);
        cpu.setB((byte)0xFC); // -4
        cpu.setCF();

        cpu.fetch((byte)0x88); // ADC A, B

        assertAll(
                () -> assertEquals((byte) 0xFF, cpu.getA(), "ADC A, B Failed: A<>-1 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")")
        );

        cpu.setA((byte)0xFE); // -2
        cpu.setB((byte)0xFC); // -4
        cpu.setCF();

        cpu.fetch((byte)0x88); // ADC A, B

        assertAll(
                () -> assertEquals((byte) 0xFB, cpu.getA(), "ADC A, B Failed: A<>-5= " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")")
        );

        cpu.setA((byte)0x81); // -127
        cpu.setB((byte)0xC2); // -62
        cpu.setCF();

        cpu.fetch((byte)0x88); // ADC A, B

        assertAll(
                () -> assertEquals((byte) 0x44, cpu.getA(), "ADC A, B Failed: A<>0x44 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")"),
                () -> assertTrue(cpu.getVF(), "Overflow flag must be ON (V=" + cpu.getVF() + ")")
        );

        cpu.setA((byte)0xFF);
        cpu.setB((byte)0);
        cpu.setCF();

        cpu.fetch((byte)0x88); // ADC A, B

        assertAll(
                () -> assertEquals((byte) 0, cpu.getA(), "ADC A, B Failed: A<>0 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")")
        );

        cpu.setA((byte)0x0F);
        cpu.setB((byte)0);
        cpu.setCF();

        cpu.fetch((byte)0x88); // ADC A, B

        assertAll(
                () -> assertEquals((byte) 0x10, cpu.getA(), "ADC A, B Failed: A<>0x10 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON (H=" + cpu.getHF() + ")")
        );
    }

    @Test
    void testADD_A_n() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0x01);

        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);

        cpu.fetch((byte)0xC6); // ADD A, 0x01

        assertAll(
                () -> assertEquals((byte) 0x00, cpu.getA(), "ADD A, 0x01 Failed: A<>0x00 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte)0x0F);

        cpu.fetch((byte)0xC6); // ADD A, 0x01

        assertAll(
                () -> assertEquals((byte) 0x10, cpu.getA(), "ADD A, 0x10 Failed: A<>0x10 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON (H=" + cpu.getHF() + ")")
        );
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

        assertAll(
                () -> assertEquals((byte) 0x30, cpu.getA(), "ADC A, 0x20; C=0 Failed: A<>0x30 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (H=" + cpu.getCF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte)0x10);
        cpu.setCF();

        cpu.fetch((byte)0xCE);

        assertAll(
                () -> assertEquals((byte) 0x31, cpu.getA(), "ADC A, 0x20; C=1 Failed: A<>0x31 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")")
        );

        compTest.poke(0x0000, (byte) 0x00);
        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);
        cpu.setCF();

        cpu.fetch((byte)0xCE); // ADC A, 0x00

        assertAll(
                () -> assertEquals((byte) 0x00, cpu.getA(), "ADC A, 0x01 Failed: A<>0x00 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")")
        );
    }

    @Test
    void testADD_A_IX_d() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xDD); // ADD A, (IX+2)
        compTest.poke(0x0001, (byte) 0x86);
        compTest.poke(0x0002, (byte) 0x02);

        cpu.setPC(0x0000);
        cpu.setIX((short) 0);
        cpu.setA((byte)0x0FF);

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x01, cpu.getA(), "ADD A, (IX+2) Failed: A<>0x01 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")")
        );
    }

    @Test
    void testADC_A_IX_d() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xDD); // ADC A, (IX+3)
        compTest.poke(0x0001, (byte) 0x8E);
        compTest.poke(0x0002, (byte) 0x03);
        compTest.poke(0x0003, (byte) 0x20);

        cpu.setPC(0x0000);
        cpu.setA((byte)0x10);
        cpu.setIX((short) 0);
        cpu.resCF();

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x30, cpu.getA(), "ADC A, (IX+3); C=0 Failed: A<>0x30 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (H=" + cpu.getCF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte)0x10);
        cpu.setCF();

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x31, cpu.getA(), "ADC A, (IX+3); C=1 Failed: A<>0x31 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")")
        );

        compTest.poke(0x0003, (byte) 0x00);
        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);
        cpu.setCF();

        cpu.fetch(); // ADC A, (IX+3)

        assertAll(
                () -> assertEquals((byte) 0x00, cpu.getA(), "ADC A, (IX+3) Failed: A<>0x00 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")")
        );
    }

    @Test
    void testADD_A_IY_d() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xFD); // ADD A, (IY+2)
        compTest.poke(0x0001, (byte) 0x86);
        compTest.poke(0x0002, (byte) 0x02);

        cpu.setPC(0x0000);
        cpu.setA((byte)0x0FF);
        cpu.setIY((short) 0);

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x01, cpu.getA(), "ADD A, (IY+2) Failed: A<>0x01 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")")
        );
    }

    @Test
    void testSUB_n() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);

        cpu.setPC(0x0000);
        cpu.setA((byte)0x10);
        compTest.poke(0x0000, (byte) 0x05);

        cpu.fetch((byte)0xD6);

        assertAll(
                () -> assertEquals((byte) 0x0B, cpu.getA(), "SUB 0x05 Failed: A<>0x0B = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x10);
        compTest.poke(0x0000, (byte) 0x10);

        cpu.fetch((byte) 0xD6);

        assertAll(
                () -> assertEquals((byte) 0x00, cpu.getA(), "SUB 0x10 Failed: A<>0x00 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x7F);
        compTest.poke(0x0000, (byte) 0xFF); // -1

        cpu.fetch((byte) 0xD6);

        assertAll(
                () -> assertEquals((byte) 0x80, cpu.getA(), "SUB 0xFF Failed: A<>0x80 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getVF(), "Overflow flag must be ON (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x10);
        compTest.poke(0x0000, (byte) 0x20);

        cpu.fetch((byte) 0xD6);

        assertAll(
                () -> assertEquals((byte) 0xF0, cpu.getA(), "SUB 0x20 Failed: A<>0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")"),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x1E);
        compTest.poke(0x0000, (byte) 0x0F);

        cpu.fetch((byte) 0xD6);

        assertAll(
                () -> assertEquals((byte) 0x0F, cpu.getA(), "SUB 0x0F Failed: A<>0x0F = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x80); // -128
        compTest.poke(0x0000, (byte) 0x80); // -128

        cpu.fetch((byte) 0xD6);

        assertAll(
                () -> assertEquals((byte) 0x00, cpu.getA(), "SUB 0x80 Failed: A<>0x00 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );
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

        assertAll(
                () -> assertEquals((byte) 0x10, cpu.getA(), "SBC 0x10; C=0 Failed: A<>0x10 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte)0x20);
        cpu.setCF();

        cpu.fetch((byte)0xDE);

        assertAll(
                () -> assertEquals((byte) 0x0F, cpu.getA(), "SBC 0x10; C=1 Failed: A<>0x0F = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );
    }

    @Test
    void testSUB_r_z() { // TODO REFERENCIA TODOS LOS REGISTROS Y FLAGS MENOS (HL)
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte) 0x10);
        cpu.setB((byte) 0x05);

        cpu.fetch((byte) 0x90); // SUB B

        assertAll(
                () -> assertEquals((byte) 0x0B, cpu.getA(), "SUB B Failed: A<>0x0B = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setA((byte) 0x10);
        cpu.setC((byte) 0x10);

        cpu.fetch((byte) 0x91);

        assertAll(
                () -> assertEquals((byte) 0x00, cpu.getA(), "SUB C Failed: A<>0x00 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setA((byte) 0x7F);
        cpu.setD((byte) 0xFF); // -1

        cpu.fetch((byte) 0x92);

        assertAll(
                () -> assertEquals((byte) 0x80, cpu.getA(), "SUB D Failed: A<>0x80 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getVF(), "Overflow flag must be ON (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setA((byte) 0x10);
        cpu.setE((byte) 0x20);

        cpu.fetch((byte) 0x93);

        assertAll(
                () -> assertEquals((byte) 0xF0, cpu.getA(), "SUB E Failed: A<>0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")"),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setA((byte) 0x1E);
        cpu.setH((byte) 0x0F);

        cpu.fetch((byte) 0x94);

        assertAll(
                () -> assertEquals((byte) 0x0F, cpu.getA(), "SUB H Failed: A<>0x0F = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setA((byte) 0x80); // -128
        cpu.setL((byte) 0x80); // -128

        cpu.fetch((byte) 0x95);

        assertAll(
                () -> assertEquals((byte) 0x00, cpu.getA(), "SUB B Failed: A<>0x00 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );
    }

    @Test
    void testSBC_A_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte)0x20);
        cpu.setB((byte)0x10);
        cpu.resCF();

        cpu.fetch((byte)0x98);

        assertAll(
                () -> assertEquals((byte) 0x10, cpu.getA(), "SBC A, B; C=0 Failed: A<>0x10 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setA((byte)0x20);
        cpu.setB((byte)0x10);
        cpu.setCF();

        cpu.fetch((byte)0x98);

        assertAll(
                () -> assertEquals((byte) 0x0F, cpu.getA(), "SBC A, B; C=1 Failed: A<>0x0F = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );
    }

    @Test
    void testSBC_HL_rp_p() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xED);
        compTest.poke(0x0001, (byte) 0x52);

        cpu.setPC(0x0000);
        cpu.setDE((short) 0xFFFF);
        cpu.setHL((short) 0x3FFF);
        cpu.resCF(); // Carry = 0

        cpu.fetch(); // SBC_HL_rp_p

        assertAll("SBC_HL_rp_p Group",
                () -> assertEquals((short)0x4000, cpu.getHL(), "SBC_HL_rp_p Failed: HL was not modified (HL=" + cpu.getHL() + ")"),
                () -> assertTrue(cpu.getCF(), "Carry flag was not affected (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag was not affected (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getSF(), "Sign flag was not affected (S=" + cpu.getSF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setDE((short) 0xFFFF);
        cpu.setHL((short) 0x0000);
        cpu.setCF(); // Carry = 1

        cpu.fetch(); // SBC_HL_rp_p

        assertAll("SBC_HL_rp_p Group",
                () -> assertEquals((short)0x0000, cpu.getHL(), "SBC_HL_rp_p Failed: HL was not modified (HL=" + cpu.getHL() + ")"),
                () -> assertTrue(cpu.getCF(), "Carry flag was not affected (C=" + cpu.getCF() + ")"),
                () -> assertTrue(cpu.getZF(), "Zero flag was not affected (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getSF(), "Sign flag was not affected (S=" + cpu.getSF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setDE((short) 0x0FFFF);
        cpu.setHL((short) 0x08000);
        cpu.resCF(); // Carry = 0

        cpu.fetch(); // SBC_HL_rp_p

        assertAll("SBC_HL_rp_p Group",
                () -> assertEquals((short)0x08001, cpu.getHL(), "SBC_HL_rp_p Failed: HL was not modified (HL=" + cpu.getHL() + ")"),
                () -> assertTrue(cpu.getCF(), "Carry flag was not affected (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag was not affected (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getSF(), "Sign flag was not affected (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag was affected (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );
    }

    @Test
    void testSUB_IX_d() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xDD); // SUB (IX+3)
        compTest.poke(0x0001, (byte) 0x96);
        compTest.poke(0x0002, (byte) 0x03);

        cpu.setIX((short) 0);

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x10);
        compTest.poke(0x0003, (byte) 0x05);

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x0B, cpu.getA(), "SUB (IX+3) Failed: A<>0x0B = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x10);
        compTest.poke(0x0003, (byte) 0x10);

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x00, cpu.getA(), "SUB (IX+3) Failed: A<>0x00 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x7F);
        compTest.poke(0x0003, (byte) 0xFF);

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x80, cpu.getA(), "SUB (IX+3) Failed: A<>0x80 = " + Integer.toHexString(cpu.getA())),
                //() -> assertTrue(cpu.getVF(), "Overflow flag must be ON (V=" + cpu.getVF() + ")"), TODO
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x10);
        compTest.poke(0x0003, (byte) 0x20);

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0xF0, cpu.getA(), "SUB (IX+3) Failed: A<>0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")"),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x1E);
        compTest.poke(0x0003, (byte) 0x0F);

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x0F, cpu.getA(), "SUB (IX+3) Failed: A<>0x0F = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x80); // -128
        compTest.poke(0x0003, (byte) 0x80); // -128

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x00, cpu.getA(), "SUB (IX+3) Failed: A<>0x00 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );
    }

    @Test
    void testSBC_A_IX_d() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xDD); // SBC A, (IX+3)
        compTest.poke(0x0001, (byte) 0x9E);
        compTest.poke(0x0002, (byte) 0x03);

        cpu.setIX((short) 0);

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x10);
        compTest.poke(0x0003, (byte) 0x05);
        cpu.setCF();

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x0A, cpu.getA(), "SBC A, (IX+3) (C=1) Failed: A<>0x0A = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x10);
        compTest.poke(0x0003, (byte) 0x10);
        cpu.setCF();

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x0FF, cpu.getA(), "SUB A, (IX+3) (C=1) Failed: A<>0xFF = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")"),
                () -> assertTrue(cpu.getVF(), "Overflow flag must be ON (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x7F);
        compTest.poke(0x0003, (byte) 0xFF);
        cpu.resCF();

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x080, cpu.getA(), "SBC A, (IX+3) (C=0) Failed: A<>0x80 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x10);
        compTest.poke(0x0003, (byte) 0x20);
        cpu.setCF();

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x0EF, cpu.getA(), "SBC A, (IX+3) (C=1) Failed: A<>0xEF = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")"),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x1E);
        compTest.poke(0x0003, (byte) 0x0F);
        cpu.setCF();

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x0E, cpu.getA(), "SBC A, (IX+3) (C=1) Failed: A<>0x0E = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getHF(), "Half-carry flag must be OFF (H=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );


        cpu.setPC(0x0000);
        cpu.setA((byte) 0x80); // -128
        compTest.poke(0x0003, (byte) 0x80); // -128
        cpu.setCF();

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x0FF, cpu.getA(), "SBC A, (IX+3) (C=1) Failed: A<>0xFF = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

    }

    @Test
    void testSBC_A_IY_d() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xFD); // SBC A, (IY+3)
        compTest.poke(0x0001, (byte) 0x9E);
        compTest.poke(0x0002, (byte) 0x03);

        cpu.setIY((short) 0);

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x10);
        compTest.poke(0x0003, (byte) 0x05);
        cpu.setCF();

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x0A, cpu.getA(), "SBC A, (IY+3) (C=1) Failed: A<>0x0A = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x10);
        compTest.poke(0x0003, (byte) 0x10);
        cpu.setCF();

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x0FF, cpu.getA(), "SUB A, (IY+3) (C=1) Failed: A<>0xFF = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")"),
                () -> assertTrue(cpu.getVF(), "Overflow flag must be ON (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x7F);
        compTest.poke(0x0003, (byte) 0xFF);
        cpu.resCF();

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x080, cpu.getA(), "SBC A, (IY+3) (C=0) Failed: A<>0x80 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x10);
        compTest.poke(0x0003, (byte) 0x20);
        cpu.setCF();

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x0EF, cpu.getA(), "SBC A, (IY+3) (C=1) Failed: A<>0xEF = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")"),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x1E);
        compTest.poke(0x0003, (byte) 0x0F);
        cpu.setCF();

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x0E, cpu.getA(), "SBC A, (IY+3) (C=1) Failed: A<>0x0E = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getHF(), "Half-carry flag must be OFF (H=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );


        cpu.setPC(0x0000);
        cpu.setA((byte) 0x80); // -128
        compTest.poke(0x0003, (byte) 0x80); // -128
        cpu.setCF();

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x0FF, cpu.getA(), "SBC A, (IY+3) (C=1) Failed: A<>0xFF = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
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
        compTest.poke(0x0000,(byte)0xFF);

        // 1
        cpu.setB((byte)0x00);
        cpu.setF((byte)0xFF);
        cpu.fetch((byte)0x04); // INC B
        assertEquals((short)0x01, cpu.getB(), "INC B Failed: B<>0x01 = " + Integer.toHexString(cpu.getB()));
        assertNotEquals((short)0x00, cpu.getB(), "INC B Failed: B still 0x00 = " + Integer.toHexString(cpu.getB()));
        assertTrue(cpu.getCF(), "INC B Failed: C Flag was affected F=" + Integer.toHexString(cpu.getF()));
        cpu.resCF();
        cpu.resxF();
        cpu.resyF();
        assertEquals(0x00, cpu.getF(), "INC B Failed: : Others bits of Flag were affected F=" + Integer.toHexString(cpu.getF()));

        // 2
        cpu.setC((byte)0x7F);
        cpu.setF((byte)0xFF);
        cpu.fetch((byte)0x0C); // INC C
        assertEquals((byte)0x080, cpu.getC(), "INC C Failed: C<>0x80 = " + Integer.toHexString(cpu.getC()));
        assertNotEquals((byte)0x7F, cpu.getC(), "INC C Failed: C still 0x7F = " + Integer.toHexString(cpu.getC()));
        assertTrue(cpu.getSF(), "INC C Failed: S Flag was not affected");
        assertTrue(cpu.getVF(), "INC C Failed: V Flag was not affected");
        assertTrue(cpu.getHF(), "INC C Failed: H Flag was not affected");
        assertFalse(cpu.getNF(), "INC C Failed: N Flag was not affected");
        assertFalse(cpu.getZF(), "INC C Failed: Z Flag was not affected");
        assertTrue(cpu.getCF(), "INC C Failed: C Flag was affected F=" + Integer.toHexString(cpu.getF()));
        cpu.resSF();
        cpu.resVF();
        cpu.resHF();
        cpu.resCF();
        cpu.resxF();
        cpu.resyF();
        assertEquals(0x00, cpu.getF(), "INC C Failed: Others bits of Flag were affected F=" + Integer.toHexString(cpu.getF()));

        // 3
        cpu.setD((byte)0x80);
        cpu.setF((byte) 0xFF);
        cpu.fetch((byte)0x14); // INC D
        assertEquals((byte)0x081, cpu.getD(), "INC D Failed: D<>0x81 = " + Integer.toHexString(cpu.getD()));
        assertNotEquals((byte)0x080, cpu.getD(), "INC D Failed: D still 0x80 = " + Integer.toHexString(cpu.getD()));
        assertTrue(cpu.getSF(), "INC C Failed: S Flag was not affected");
        assertTrue(cpu.getCF(), "INC C Failed: C Flag was affected F=" + Integer.toHexString(cpu.getF()));
        cpu.resSF();
        cpu.resCF();
        cpu.resxF();
        cpu.resyF();
        assertEquals(0x00, cpu.getF(), "INC D Failed: Others bits of Flag were affected F=" + Integer.toHexString(cpu.getF()));

        // 4
        cpu.setE((byte)0xFF);
        cpu.setF((byte) 0xFF);
        cpu.fetch((byte)0x1C); // INC E
        assertEquals((byte)0x00, cpu.getE(), "INC E Failed: E<>0x00 = " + Integer.toHexString(cpu.getE()));
        assertNotEquals((byte)0xFF, cpu.getE(), "INC E Failed: E still 0xFF = " + Integer.toHexString(cpu.getE()));
        assertTrue(cpu.getZF(), "INC E Failed: Z Flag was not affected");
        assertTrue(cpu.getHF(), "INC E Failed: H Flag was not affected");
        assertTrue(cpu.getCF(), "INC C Failed: C Flag was affected F=" + Integer.toHexString(cpu.getF()));
        cpu.resZF();
        cpu.resHF();
        cpu.resCF();
        cpu.resxF();
        cpu.resyF();
        assertEquals(0x00, cpu.getF(), "INC E Failed: Others bits of Flag were affected F=" + Integer.toHexString(cpu.getF()));

        // 5
        cpu.setH((byte)0x0F);
        cpu.setF((byte) 0xFF);
        cpu.fetch((byte)0x24); // INC H
        assertEquals((byte)0x10, cpu.getH(), "INC H Failed: H<>0x10 = " + Integer.toHexString(cpu.getH()));
        assertNotEquals((byte)0x0F, cpu.getH(), "INC H Failed: H still 0x0F = " + Integer.toHexString(cpu.getH()));
        assertTrue(cpu.getHF(), "INC H Failed: H Flag was not affected");
        assertTrue(cpu.getCF(), "INC C Failed: C Flag was affected F=" + Integer.toHexString(cpu.getF()));
        cpu.resHF();
        cpu.resCF();
        cpu.resxF();
        cpu.resyF();
        assertEquals(0x00, cpu.getF(), "INC H Failed: Others bits of Flag were affected F=" + Integer.toHexString(cpu.getF()));

        // 6
        cpu.setL((byte)0x15);
        cpu.setF((byte) 0xFF);
        cpu.fetch((byte)0x2C); // INC L
        assertEquals((byte)0x16, cpu.getL(), "INC L Failed: L<>0x16 = " + Integer.toHexString(cpu.getL()));
        assertNotEquals((byte)0x15, cpu.getL(), "INC L Failed: L still 0x15 = " + Integer.toHexString(cpu.getL()));
        assertTrue(cpu.getCF(), "INC C Failed: C Flag was affected F=" + Integer.toHexString(cpu.getF()));
        cpu.resCF();
        cpu.resxF();
        cpu.resyF();
        assertEquals(0x00, cpu.getF(), "INC L Failed: : Others bits of Flag were affected F=" + Integer.toHexString(cpu.getF()));

        cpu.setA((byte)0x16);
        cpu.setF((byte) 0xFF);
        cpu.fetch((byte)0x3C); // INC A
        assertEquals((byte)0x17, cpu.getA(), "INC A Failed: A<>0x17 = " + Integer.toHexString(cpu.getA()));
        assertNotEquals((byte)0x16, cpu.getA(), "INC A Failed: A still 0x16 = " + Integer.toHexString(cpu.getA()));
        assertTrue(cpu.getCF(), "INC C Failed: C Flag was affected F=" + Integer.toHexString(cpu.getF()));
        cpu.resCF();
        cpu.resxF();
        cpu.resyF();
        assertEquals(0x00, cpu.getF(), "INC A Failed: : Others bits of Flag were affected F=" + Integer.toHexString(cpu.getF()));

        cpu.setHL((short)0x0000);
        cpu.setF((byte) 0xFF);
        cpu.fetch((byte)0x34); // INC (HL)
        assertEquals((byte)0x00, compTest.peek(cpu.getHL()), "INC (HL) Failed: (HL)<>0x18 = " + Integer.toHexString(compTest.peek(cpu.getHL())));
        assertNotEquals((byte)0xFF, compTest.peek(cpu.getHL()), "INC (HL) Failed: (HL) still 0x17 = " + Integer.toHexString(compTest.peek(cpu.getHL())));
        assertTrue(cpu.getZF(), "INC (HL) Failed: Z Flag was not affected");
        assertTrue(cpu.getHF(), "INC (HL) Failed: H Flag was not affected");
        assertTrue(cpu.getCF(), "INC C Failed: C Flag was affected F=" + Integer.toHexString(cpu.getF()));
        cpu.resZF();
        cpu.resHF();
        cpu.resCF();
        cpu.resxF();
        cpu.resyF();
        assertEquals(0x00, cpu.getF(), "INC (HL) Failed: Others bits of Flag were affected F=" + Integer.toHexString(cpu.getF()));
    }

    @Test
    void testDEC_r_y() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000,(byte)0x01);

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
        cpu.resCF();

        cpu.fetch((byte)0x35); // DEC (HL)

        assertAll("DEC r[y] Group",
                () -> assertEquals((short)0x00, compTest.peek(cpu.getHL()), "DEC (HL) Failed: (HL)<>0x00 = " + Integer.toHexString((short)compTest.peek(cpu.getHL()))),
                () -> assertNotEquals((short)0x01, compTest.peek(cpu.getHL()), "DEC (HL) Failed: (HL) still 0x01 = " + Integer.toHexString((short)compTest.peek(cpu.getHL()))),
                () -> assertFalse(cpu.getCF(), "DEC (HL) Failed: Carry Flag must be OFF"),
                () -> assertTrue(cpu.getZF(), "DEC (HL) Failed: Zero Flag must be ON")
        );
    }

    @Test
    void testINC_IX() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(256));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setIX((short) 0x1000);
        cpu.setF((byte) 0xFF); // All flags set to test they're not affected
        compTest.poke(0x0000, (byte) 0xDD); // DD prefix
        compTest.poke(0x0001, (byte) 0x23); // INC IX
        cpu.fetch(); // DD prefix + INC IX

        assertAll("INC IX Group",
                () -> assertEquals((short) 0x1001, cpu.getIX(), "INC IX Failed: IX<>0x1001"),
                () -> assertEquals((byte) 0xFF, cpu.getF(), "INC IX Failed: Flags were affected")
        );
    }

    @Test
    void testINC_IY() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setIY((short) 0x1000);
        cpu.setF((byte) 0xFF); // All flags set to test they're not affected
        compTest.poke(0x0000, (byte) 0xFD); // FD prefix
        compTest.poke(0x0001, (byte) 0x23); // INC IY
        cpu.fetch(); // DD prefix + INC IY

        assertAll("INC IY Group",
                () -> assertEquals((short) 0x1001, cpu.getIY(), "INC IY Failed: IY<>0x1001"),
                () -> assertEquals((byte) 0xFF, cpu.getF(), "INC IY Failed: Flags were affected")
        );
    }

    @Test
    void testDEC_IX() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(256));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setIX((short) 0x1000);
        cpu.setF((byte) 0xFF); // All flags set to test they're not affected
        compTest.poke(0x0000, (byte) 0xDD); // DD prefix
        compTest.poke(0x0001, (byte) 0x2B); // DEC IX
        cpu.fetch(); // DD prefix + DEC IX

        assertAll("DEC IX Group",
                () -> assertEquals((short) 0x0FFF, cpu.getIX(), "DEC IX Failed: IX<>0x0FFF"),
                () -> assertEquals((byte) 0xFF, cpu.getF(), "DEC IX Failed: Flags were affected")
        );
    }

    @Test
    void testDEC_IY() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setIY((short) 0x1000);
        cpu.setF((byte) 0xFF); // All flags set to test they're not affected
        compTest.poke(0x0000, (byte) 0xFD); // FD prefix
        compTest.poke(0x0001, (byte) 0x2B); // DEC IY
        cpu.fetch(); // DD prefix + DEC IY

        assertAll("DEC IY Group",
                () -> assertEquals((short) 0x0FFF, cpu.getIY(), "DEC IY Failed: IY<>0x0FFF"),
                () -> assertEquals((byte) 0xFF, cpu.getF(), "DEC IY Failed: Flags were affected")
        );
    }

    @Test
    void testNEG() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(16));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setA((byte) 0x01);
        cpu.setF((byte) 0x00);
        compTest.poke(0x0000, (byte) 0xED); // ED prefix
        compTest.poke(0x0001, (byte) 0x44); // NEG
        cpu.fetch(); // ED prefix + NEG

        assertAll("NEG Group",
                () -> assertEquals((byte) 0xFF, cpu.getA(), "NEG Failed: A<>0xFF"),
                () -> assertTrue(cpu.getSF(), "NEG Failed: S Flag not set"),
                () -> assertTrue(cpu.getCF(), "NEG Failed: C Flag not set"),
                () -> assertTrue(cpu.getNF(), "NEG Failed: N Flag not set")
        );

        cpu.reset();

        cpu.setA((byte) 0x00);
        cpu.setF((byte) 0x00);
        compTest.poke(0x0000, (byte) 0xED); // ED prefix
        compTest.poke(0x0001, (byte) 0x44); // NEG
        cpu.fetch(); // ED prefix + NEG

        assertAll("NEG Group Zero",
                () -> assertEquals((byte) 0x00, cpu.getA(), "NEG Failed: A<>0x00"),
                () -> assertTrue(cpu.getZF(), "NEG Failed: Z Flag not set"),
                () -> assertFalse(cpu.getCF(), "NEG Failed: C Flag incorrectly set")
        );
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
    void testDAA() {
        Z80ForTesting cpu = new Z80ForTesting();
        
// --- Caso 1: A=0x09 (sin ajustes, resultado 0x09) ---
        cpu.setA((byte) 0x09);
        cpu.resNF();
        cpu.resHF();
        cpu.resCF();
        cpu.fetch((byte)0x27);
        
        assertAll(
                () -> assertEquals((byte) 0x09, cpu.getA(), "DAA Failed: A<>0x09"),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF"),
                () -> assertFalse(cpu.getHF(), "Half-carry flag must be OFF"),
                () -> assertFalse(cpu.getNF(), "N flag must be OFF"),
                () -> assertTrue(cpu.getPF(), "Parity flag must be ON (0x09 has even bits=2)"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF")
        );

// --- Caso 2: A=0x0A (ajuste +6, resultado 0x10) ---
        cpu.setA((byte) 0x0A);
        cpu.resNF();
        cpu.resHF();
        cpu.resCF();
        cpu.fetch((byte)0x27);
        
        assertAll(
                () -> assertEquals((byte) 0x10, cpu.getA(), "DAA Failed: A<>0x10"),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF"),
                () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON"),
                () -> assertFalse(cpu.getNF(), "N flag must be OFF"),
                () -> assertFalse(cpu.getPF(), "Parity flag must be OFF (0x10 has odd bits=1)"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF")
        );

// --- Caso 3: A=0x9A (ADD con Carry final, resultado 0x00, CF=1) ---
        cpu.setA((byte) 0x9A);
        cpu.resNF();
        cpu.resHF();
        cpu.resCF();
        cpu.fetch((byte)0x27);
        
        assertAll(
                () -> assertEquals((byte) 0x00, cpu.getA(), "DAA Failed: A<>0x00"),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF"),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON"),
                () -> assertTrue(cpu.getPF(), "Parity flag must be ON (0x00 has even parity)"),
                () -> assertFalse(cpu.getNF(), "N flag must be OFF"),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON")
        );

// --- Caso 4: SUB sin half ni carry (resultado inalterado) ---
        cpu.setA((byte) 0x99);
        cpu.setNF();
        cpu.resHF();
        cpu.resCF();
        cpu.fetch((byte)0x27);
        
        assertAll(
                () -> assertEquals((byte) 0x99, cpu.getA(), "DAA Failed: A<>0x99"),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (bit7=1)"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF"),
                () -> assertFalse(cpu.getHF(), "Half-carry flag must be OFF"),
                () -> assertTrue(cpu.getNF(), "N flag must be ON"),
                () -> assertTrue(cpu.getPF(), "Parity flag must be ON (0x99 has odd bits=4?)"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF")
        );

// --- Caso 5: SUB con half carry (resultado 0x94) ---
        cpu.setA((byte) 0x9A);
        cpu.setNF();
        cpu.setHF();
        cpu.resCF();
        cpu.fetch((byte)0x27);
        
        assertAll(
                () -> assertEquals((byte) 0x34, cpu.getA(), "DAA Failed: A<>0x94"),
                //() -> assertTrue(cpu.getSF(), "Sign flag must be ON"),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF"),
                () -> assertFalse(cpu.getHF(), "Half-carry flag must be OFF"),
                () -> assertTrue(cpu.getNF(), "N flag must be ON"),
                () -> assertFalse(cpu.getPF(), "Parity flag must be OFF (0x94 has odd bits)"),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON")
        );

// --- Caso 6: SUB con carry + half carry (resultado 0x00, CF=1) ---
        cpu.setA((byte) 0x9A);
        cpu.setNF();
        cpu.setHF();
        cpu.setCF();
        cpu.fetch((byte)0x27);
        
        assertAll(
                () -> assertEquals((byte) 0x34, cpu.getA(), "DAA Failed: A<>0x00"),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF"),
                //() -> assertTrue(cpu.getZF(), "Zero flag must be ON"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF"),
                //() -> assertTrue(cpu.getHF(), "Half-carry flag must be ON"),
                () -> assertFalse(cpu.getHF(), "Half-carry flag must be OFF"),
                () -> assertTrue(cpu.getNF(), "N flag must be ON"),
                //() -> assertTrue(cpu.getPF(), "Parity flag must be ON (0x00 has even bits)"),
                () -> assertFalse(cpu.getPF(), "Parity flag must be ON (0x00 has even bits)"),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON")
        );
    }

}
