package com.github.jagarsoft.test;

import com.github.jagarsoft.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LogicTest {
    @Test
    void testAND_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        // --- Caso 1: resultado 0 ---
        cpu.setA((byte)0x55);
        cpu.setB((byte)0xAA);

        cpu.fetch((byte)0xA0); // AND B

        assertAll(
                () -> assertEquals((byte)0x00, cpu.getA(), "AND B failed: A<>0x00 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertTrue(cpu.getPF(), "Parity flag must be ON (P/V=" + cpu.getPF() + ")"),
                () -> assertFalse(cpu.getNF(), "N flag must be OFF (N=" + cpu.getNF() + ")"),
                () -> assertFalse(cpu.getCF(), "C flag must be OFF (C=" + cpu.getCF() + ")")
        );

        // --- Caso 2: bit de signo activo ---
        cpu.setA((byte)0xF0);
        cpu.setC((byte)0x80);

        cpu.fetch((byte)0xA1); // AND C

        assertAll(
                () -> assertEquals((byte)0x80, cpu.getA(), "AND C failed: A<>0x80 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")")
        );
    }

    @Test
    void testXOR_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        // --- Caso 1: resultado 0 ---
        cpu.setA((byte)0xFF);
        cpu.setB((byte)0xFF);

        cpu.fetch((byte)0xA8); // XOR B

        assertAll(
                () -> assertEquals((byte)0x00, cpu.getA(), "XOR B failed: A<>0x00 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getPF(), "Parity flag must be ON (P/V=" + cpu.getPF() + ")"),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getHF(), "H flag must be OFF (H=" + cpu.getHF() + ")")
        );

        // --- Caso 2: resultado con bit de signo ---
        cpu.setA((byte)0xAA);
        cpu.setC((byte)0x55);

        cpu.fetch((byte)0xA9); // XOR C

        assertAll(
                () -> assertEquals((byte)0xFF, cpu.getA(), "XOR C failed: A<>0xFF = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")")
        );

        // --- Caso 3: patrón con paridad par ---
        cpu.setA((byte)0x3C);
        cpu.setD((byte)0x0F);

        cpu.fetch((byte)0xAA); // XOR D

        assertAll(
                () -> assertEquals((byte)0x33, cpu.getA(), "XOR A,B failed: A<>0x33 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getPF(), "Parity flag must be ON (P/V=" + cpu.getPF() + ")")
        );
    }

    @Test
    void testOR_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        // --- Caso 1: resultado 0 ---
        cpu.setA((byte)0x00);
        cpu.setB((byte)0x00);

        cpu.fetch((byte)0xB0); // OR B

        assertAll(
                () -> assertEquals((byte)0x00, cpu.getA(), "OR B failed: A<>0x00 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getHF(), "H flag must be OFF (H=" + cpu.getHF() + ")"),
                () -> assertTrue(cpu.getPF(), "Parity flag must be ON (P/V=" + cpu.getPF() + ")"),
                () -> assertFalse(cpu.getNF(), "N flag must be OFF (N=" + cpu.getNF() + ")"),
                () -> assertFalse(cpu.getCF(), "C flag must be OFF (C=" + cpu.getCF() + ")")
        );

        // --- Caso 2: todos los bits a 1 ---
        cpu.setA((byte)0x0F);
        cpu.setC((byte)0xF0);

        cpu.fetch((byte)0xB1); // OR C

        assertAll(
                () -> assertEquals((byte)0xFF, cpu.getA(), "OR C failed: A<>0xFF = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getHF(), "H flag must be OFF (H=" + cpu.getHF() + ")")
        );
    }

    @Test
    void testCP_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte) 0x10);
        cpu.setB((byte) 0x05);

        cpu.fetch((byte) 0xB8); // CP B

        assertAll(
                () -> assertEquals((byte) 0x10, cpu.getA(), "CP B Failed: A<>0x0B = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setA((byte) 0x10);
        cpu.setC((byte) 0x10);

        cpu.fetch((byte) 0xB9); // CP C

        assertAll(
                () -> assertEquals((byte) 0x10, cpu.getA(), "CP C Failed: A<>0x00 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setA((byte) 0x7F);
        cpu.setD((byte) 0xFF); // -1

        cpu.fetch((byte) 0xBA); // CP D

        assertAll(
                () -> assertEquals((byte) 0x7F, cpu.getA(), "CP D Failed: A<>0x80 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getVF(), "Overflow flag must be ON (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setA((byte) 0x10);
        cpu.setE((byte) 0x20);

        cpu.fetch((byte) 0xBB); // CP E

        assertAll(
                () -> assertEquals((byte) 0x10, cpu.getA(), "CP E Failed: A<>0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")"),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setA((byte) 0x1E);
        cpu.setH((byte) 0x0F);

        cpu.fetch((byte) 0xBC); // CP H

        assertAll(
                () -> assertEquals((byte) 0x1E, cpu.getA(), "CP H Failed: A<>0x0F = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setA((byte) 0x80); // -128
        cpu.setL((byte) 0x80); // -128

        cpu.fetch((byte) 0xBD); // CP L

        assertAll(
                () -> assertEquals((byte) 0x80, cpu.getA(), "SUB L Failed: A<>0x00 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );
    }

    @Test
    void testCP_IX_d() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xDD); // CP (IX+3)
        compTest.poke(0x0001, (byte) 0xBE);
        compTest.poke(0x0002, (byte) 0x03);
        compTest.poke(0x0003, (byte) 0x20);

        cpu.setPC(0x0000);
        cpu.setA((byte)0x10);
        cpu.resCF();

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x10, cpu.getA(), "CP (IX+3); C=0 Failed: A<>0x10 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (H=" + cpu.getCF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte)0x20);

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x20, cpu.getA(), "CP (IX+3) Failed: A<>0x20 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte)0x30);

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x30, cpu.getA(), "CP (IX+3) Failed: A<>0x30 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")")
        );
    }

    @Test
    void testCP_IY_d() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xFD); // CP (IY+3)
        compTest.poke(0x0001, (byte) 0xBE);
        compTest.poke(0x0002, (byte) 0x03);
        compTest.poke(0x0003, (byte) 0x20);

        cpu.setPC(0x0000);
        cpu.setA((byte)0x10);
        cpu.resCF();

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x10, cpu.getA(), "CP (IY+3); C=0 Failed: A<>0x10 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (H=" + cpu.getCF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte)0x20);

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x20, cpu.getA(), "CP (IY+3) Failed: A<>0x20 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte)0x30);

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x30, cpu.getA(), "CP (IY+3) Failed: A<>0x30 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")")
        );
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
    void testAND_IX_d() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xDD); // AND (IX+3)
        compTest.poke(0x0001, (byte) 0xA6);
        compTest.poke(0x0002, (byte) 0x03);


        // --- Caso 1: resultado 0 ---
        cpu.setPC(0x0000);
        cpu.setA((byte)0x55);
        compTest.poke(0x0003, (byte) 0xAA);

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte)0x00, cpu.getA(), "AND (IX+3) failed: A<>0x00 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertTrue(cpu.getPF(), "Parity flag must be ON (P/V=" + cpu.getPF() + ")"),
                () -> assertFalse(cpu.getNF(), "N flag must be OFF (N=" + cpu.getNF() + ")"),
                () -> assertFalse(cpu.getCF(), "C flag must be OFF (C=" + cpu.getCF() + ")")
        );

        // --- Caso 2: bit de signo activo ---
        cpu.setPC(0x0000);
        cpu.setA((byte)0xF0);
        compTest.poke(0x0003, (byte) 0x80);

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte)0x80, cpu.getA(), "AND (IX+3) failed: A<>0x80 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")")
        );
    }

    @Test
    void testAND_IY_d() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xFD); // AND (IY+3)
        compTest.poke(0x0001, (byte) 0xA6);
        compTest.poke(0x0002, (byte) 0x03);


        // --- Caso 1: resultado 0 ---
        cpu.setPC(0x0000);
        cpu.setA((byte)0x55);
        compTest.poke(0x0003, (byte) 0xAA);

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte)0x00, cpu.getA(), "AND (IY+3) failed: A<>0x00 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertTrue(cpu.getPF(), "Parity flag must be ON (P/V=" + cpu.getPF() + ")"),
                () -> assertFalse(cpu.getNF(), "N flag must be OFF (N=" + cpu.getNF() + ")"),
                () -> assertFalse(cpu.getCF(), "C flag must be OFF (C=" + cpu.getCF() + ")")
        );

        // --- Caso 2: bit de signo activo ---
        cpu.setPC(0x0000);
        cpu.setA((byte)0xF0);
        compTest.poke(0x0003, (byte) 0x80);

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte)0x80, cpu.getA(), "AND (IY+3) failed: A<>0x80 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")")
        );
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

        // --- Caso 1: igualdad ---
        cpu.setPC(0x0000);
        cpu.setA((byte)0x10);
        compTest.poke(0x0000, (byte) 0x10);

        cpu.fetch((byte)0xFE);

        assertAll(
                () -> assertEquals((byte)0x10, cpu.getA(), "CP must not change A"),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getNF(), "N flag must be ON (N=" + cpu.getNF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")")
        );

        // --- Caso 2: A < B (borrow) ---
        cpu.setPC(0x0000);
        cpu.setA((byte)0x00);
        compTest.poke(0x0000, (byte) 0x01);

        cpu.fetch((byte)0xFE);

        assertAll(
                () -> assertEquals((byte)0x00, cpu.getA(), "CP must not change A"),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")"),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertTrue(cpu.getHF(), "Half-carry must be ON (H=" + cpu.getHF() + ")")
        );

        // --- Caso 3: overflow (positivo - negativo = negativo) ---
        cpu.setPC(0x0000);
        cpu.setA((byte)0x7F);
        compTest.poke(0x0000, (byte) 0xFF);

        cpu.fetch((byte)0xFE);

        assertAll(
                () -> assertTrue(cpu.getVF(), "Overflow flag must be ON (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")")
        );

        // --- Caso 4: resultado cero ---
        cpu.setPC(0x0000);
        cpu.setA((byte)0x12);
        compTest.poke(0x0000, (byte) 0x12);

        cpu.fetch((byte)0xFE);

        assertAll(
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")")
        );
    }
}

