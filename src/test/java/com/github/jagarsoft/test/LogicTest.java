package com.github.jagarsoft.test;

import com.github.jagarsoft.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LogicTest {
    @Test
    void testAND_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte)0xFF);
        cpu.setB((byte)0x00);

        long initTState = cpu.getTState();
        cpu.fetch((byte)0xA0); // AND B

        assertEquals(4, cpu.getTState()-initTState, "AND n TState Failed");

        AND_A_case1(cpu, "AND B Failed: A<>0x00 = ");

        cpu.setA((byte)0xFF);
        cpu.setC((byte)0b0010_1011);

        cpu.fetch((byte)0xA1); // AND C

        AND_A_case2(cpu, "AND C Failed: A<>0x2B = ");

        cpu.setA((byte)0xFF);
        cpu.setD((byte)0x80);

        cpu.fetch((byte)0xA2); // AND D

        AND_A_case3(cpu, "AND D Failed: A<>0x80 = ");
    }

    @Test
    void testXOR_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte)0x00);
        cpu.setB((byte)0x00);

        long initTState = cpu.getTState();
        cpu.fetch((byte)0xA8); // XOR B

        assertEquals(4, cpu.getTState()-initTState, "XOR B TState Failed");

        OR_A_case1(cpu, "XOR B Failed: A<>0x00 = ");

        cpu.setA((byte)0x80);
        cpu.setC((byte)0x00);

        cpu.fetch((byte)0xA9); // XOR C

        OR_A_case2(cpu, "XOR C Failed: A<>0x80 = ");

        cpu.setA((byte)0x09);
        cpu.setD((byte)0x22);

        cpu.fetch((byte)0xAA); // XOR D

        OR_A_case3(cpu, "XOR 0x22 Failed: A<>0x2B = ");
    }

    @Test
    void testXOR_IX_d() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setPC((byte) 0x0000);
        cpu.setIX((byte) 0x0000);
        cpu.setA((byte) 0xF0);
        compTest.poke(0x0000, (byte) 0xDD); // DD prefix
        compTest.poke(0x0001, (byte) 0xAE); // XOR (IX+3)
        compTest.poke(0x0002, (byte) 0x03);
        compTest.poke(0x0003, (byte) 0xFF);


        long initTState = cpu.getTState();
        cpu.fetch(); // DD prefix + XOR (IX+3)

        assertAll("XOR (IX+d) Group",
                () -> assertEquals((byte) 0x0F, cpu.getA(), "XOR (IX+3) Failed: A<>0x0F"),
                () -> assertNotEquals((byte) 0xF0, cpu.getA(), "XOR (IX+3) Failed: A still 0xF0"),

                () -> assertEquals(19, cpu.getTState()-initTState, "XOR (IX+3) TState Failed")
        );
    }

    @Test
    void testXOR_IY_d() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setPC((byte) 0x0000);
        cpu.setIY((byte) 0x0000);
        cpu.setA((byte) 0xF0);
        compTest.poke(0x0000, (byte) 0xFD); // FD prefIY
        compTest.poke(0x0001, (byte) 0xAE); // XOR (IY+3)
        compTest.poke(0x0002, (byte) 0x03);
        compTest.poke(0x0003, (byte) 0xFF);


        long initTState = cpu.getTState();
        cpu.fetch(); // FD prefIY + XOR (IY+3)

        assertAll("XOR (IY+d) Group",
                () -> assertEquals((byte) 0x0F, cpu.getA(), "XOR (IY+3) Failed: A<>0x0F"),
                () -> assertNotEquals((byte) 0xF0, cpu.getA(), "XOR (IY+3) Failed: A still 0xF0"),

                () -> assertEquals(19, cpu.getTState()-initTState, "XOR (IY+3) TState Failed")
        );
    }

    @Test
    void testOR_IX_d() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setPC((byte) 0x0000);
        cpu.setIX((byte) 0x0000);
        cpu.setA((byte) 0x00);
        compTest.poke(0x0000, (byte) 0xDD); // DD prefix
        compTest.poke(0x0001, (byte) 0xB6); // OR (IX+3)
        compTest.poke(0x0002, (byte) 0x03);
        compTest.poke(0x0003, (byte) 0x0F);


        long initTState = cpu.getTState();
        cpu.fetch(); // DD prefix + OR (IX+3)

        assertAll("OR (IX+d) Group",
                () -> assertEquals((byte) 0x0F, cpu.getA(), "OR (IX+3) Failed: A<>0x0F"),
                () -> assertNotEquals((byte) 0x00, cpu.getA(), "OR (IX+3) Failed: A still 0x0F"),

                () -> assertEquals(19, cpu.getTState()-initTState, "OR (IX+3) TState Failed")
        );
    }

    @Test
    void testOR_IY_d() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setPC((byte) 0x0000);
        cpu.setIY((byte) 0x0000);
        cpu.setA((byte) 0x00);
        compTest.poke(0x0000, (byte) 0xFD); // FD prefIY
        compTest.poke(0x0001, (byte) 0xB6); // OR (IY+3)
        compTest.poke(0x0002, (byte) 0x03);
        compTest.poke(0x0003, (byte) 0x0F);


        long initTState = cpu.getTState();
        cpu.fetch(); // FD prefIY + OR (IY+3)

        assertAll("OR (IY+d) Group",
                () -> assertEquals((byte) 0x0F, cpu.getA(), "OR (IY+3) Failed: A<>0x0F"),
                () -> assertNotEquals((byte) 0xF0, cpu.getA(), "OR (IY+3) Failed: A still 0xF0"),

                () -> assertEquals(19, cpu.getTState()-initTState, "OR (IY+3) TState Failed")
        );
    }

    @Test
    void testOR_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte)0x00);
        cpu.setB((byte)0x00);

        long initTState = cpu.getTState();
        cpu.fetch((byte)0xB0); // OR B

        assertEquals(4, cpu.getTState()-initTState, "OR B TState Failed");

        OR_A_case1(cpu, "OR B Failed: A<>0x00 = ");

        cpu.setA((byte)0x80);
        cpu.setC((byte)0x00);

        cpu.fetch((byte)0xB1); // OR C

        OR_A_case2(cpu, "OR C Failed: A<>0x80 = ");

        cpu.setA((byte)0x09);
        cpu.setD((byte)0x22);

        cpu.fetch((byte)0xB2); // OR D

        OR_A_case3(cpu, "OR D Failed: A<>0x2B = ");
    }

    @Test
    void testCP_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte) 0x00);
        cpu.setB((byte) 0x01);

        long initTState = cpu.getTState();
        cpu.fetch((byte) 0xB8); // CP B

        assertEquals(4, cpu.getTState()-initTState, "CP B TState Failed");

        CP_A_case1(cpu, "CP B  Failed");

        cpu.setA((byte) 0xFF);
        cpu.setC((byte) 0xFF);

        cpu.fetch((byte) 0xB9); // CP C

        CP_A_case2(cpu, "CP C Failed");

        cpu.setA((byte) 0x10);
        cpu.setD((byte) 0x01);

        cpu.fetch((byte) 0xBA); // CP D

        CP_A_case3(cpu, "CP D Failed");

        cpu.setA((byte) 0x80);
        cpu.setE((byte) 0x01);

        cpu.fetch((byte) 0xBB); // CP E

        CP_A_case4(cpu, "CP E Failed");
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
        compTest.poke(0x0003, (byte) 0x01);

        cpu.setPC(0x0000);
        cpu.setA((byte)0x00);

        long initTState = cpu.getTState();
        cpu.fetch();

        assertEquals(19, cpu.getTState()-initTState, "CP (IX+3) TState Failed");

        /*assertAll(
                () -> assertEquals((byte) 0x10, cpu.getA(), "CP (IX+3); C=0 Failed: A<>0x10 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (H=" + cpu.getCF() + ")"),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (H=" + cpu.getCF() + ")"),

                () ->
        );*/
        CP_A_case1(cpu, "CP (IX+3) Failed");

        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);
        compTest.poke(0x0003, (byte) 0xFF);

        cpu.fetch();

        CP_A_case2(cpu, "CP (IX+3) Failed");

        cpu.setPC(0x0000);
        cpu.setA((byte)0x10);
        compTest.poke(0x0003, (byte) 0x01);

        cpu.fetch();

        CP_A_case3(cpu, "CP (IX+3) Failed");

        cpu.setPC(0x0000);
        cpu.setA((byte)0x80);

        cpu.fetch();

        CP_A_case4(cpu, "CP (IX+3) Failed");
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
        compTest.poke(0x0003, (byte) 0x01);

        cpu.setPC(0x0000);
        cpu.setA((byte)0x00);

        long initTState = cpu.getTState();
        cpu.fetch();

        assertEquals(19, cpu.getTState()-initTState, "CP (IY+3) TState Failed");

        /*assertAll(
                () -> assertEquals((byte) 0x10, cpu.getA(), "CP (IY+3); C=0 Failed: A<>0x10 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (H=" + cpu.getCF() + ")"),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (H=" + cpu.getCF() + ")"),

                () ->
        );*/
        CP_A_case1(cpu, "CP (IY+3) Failed");

        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);
        compTest.poke(0x0003, (byte) 0xFF);

        cpu.fetch();

        CP_A_case2(cpu, "CP (IY+3) Failed");

        cpu.setPC(0x0000);
        cpu.setA((byte)0x10);
        compTest.poke(0x0003, (byte) 0x01);

        cpu.fetch();

        CP_A_case3(cpu, "CP (IY+3) Failed");

        cpu.setPC(0x0000);
        cpu.setA((byte)0x80);

        cpu.fetch();

        CP_A_case4(cpu, "CP (IY+3) Failed");
    }

/*
AND
|   A  |   n  | Result | S | Z | F5 | H | F3 | P/v | N | C | What test?            |
| ---- | ---- | ------ | - | - | -- | - | -- | --- | - | - | --------------------- |
| 0xFF | 0x00 |  0x00  | 0 | 1 |  0 | 1 |  0 |  1  | 0 | 0 | zero + even parity    |
| 0xFF | 0x2B |  0x2B  | 0 | 0 |  1 | 1 |  1 |  1  | 0 | 0 | even parity + YF + XF |
| 0xFF | 0x80 |  0x80  | 1 | 0 |  0 | 1 |  0 |  0  | 0 | 0 | sign + odd parity     |
*/
    @Test
    void testAND_n() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xE6);

        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);
        compTest.poke(0x0001, (byte) 0x00);

        long initTState = cpu.getTState();
        cpu.fetch();

        assertEquals(7, cpu.getTState()-initTState, "AND n TState Failed");

        AND_A_case1(cpu, "AND 0x00 Failed: A<>0x00 = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);
        compTest.poke(0x0001, (byte) 0b0010_1011);

        cpu.fetch();

        AND_A_case2(cpu, "AND 0x2B Failed: A<>0x2B = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);
        compTest.poke(0x0001, (byte) 0x80);

        cpu.fetch();

        AND_A_case3(cpu, "AND 0x80 Failed: A<>0x80 = ");
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

        cpu.setPC(0x0000);
        cpu.setIX((short) 0x0000);
        cpu.setA((byte)0xFF);
        compTest.poke(0x0003, (byte) 0x00);

        long initTState = cpu.getTState();
        cpu.fetch();

        assertEquals(19, cpu.getTState()-initTState, "AND n TState Failed");

        AND_A_case1(cpu, "AND (IX+3) Failed: A<>0x00 = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);
        compTest.poke(0x0003, (byte) 0b0010_1011);

        cpu.fetch();

        AND_A_case2(cpu, "AND (IX+3) Failed: A<>0x2B = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);
        compTest.poke(0x0003, (byte) 0x80);

        cpu.fetch();

        AND_A_case3(cpu, "AND (IX+3) Failed: A<>0x80 = ");
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

        cpu.setPC(0x0000);
        cpu.setIY((short) 0x0000);
        cpu.setA((byte)0xFF);
        compTest.poke(0x0003, (byte) 0x00);

        long initTState = cpu.getTState();
        cpu.fetch();

        assertEquals(19, cpu.getTState()-initTState, "AND n TState Failed");

        AND_A_case1(cpu, "AND (IY+3) Failed: A<>0x00 = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);
        compTest.poke(0x0003, (byte) 0b0010_1011);

        cpu.fetch();

        AND_A_case2(cpu, "AND (IY+3) Failed: A<>0x2B = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);
        compTest.poke(0x0003, (byte) 0x80);

        cpu.fetch();

        AND_A_case3(cpu, "AND (IY+3) Failed: A<>0x80 = ");
    }

    @Test
    void testXOR_n() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xEE); // XOR 0x0F

        cpu.setPC(0x0000);
        cpu.setA((byte)0x00);
        compTest.poke(0x0001, (byte) 0x00);

        long initTState = cpu.getTState();
        cpu.fetch();

        assertEquals(7, cpu.getTState()-initTState, "XOR n TState Failed");

        OR_A_case1(cpu, "XOR 0x00 Failed: A<>0x00 = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0x80);
        compTest.poke(0x0001, (byte) 0x00);

        cpu.fetch();

        OR_A_case2(cpu, "XOR 0x00 Failed: A<>0x80 = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0x09);
        compTest.poke(0x0001, (byte) 0x22);

        cpu.fetch();

        OR_A_case3(cpu, "XOR 0x22 Failed: A<>0x2B = ");
    }


/*
OR
XOR
|   A  |   n  | Result | S | Z | F5 | H | F3 | P/v | N | C | What test?            |
| ---- | ---- | ------ | - | - | -- | - | -- | --- | - | - | --------------------- |
| 0x00 | 0x00 |  0x00  | 0 | 1 |  0 | 0 |  0 |  1  | 0 | 0 | zero + parity         |
| 0x80 | 0x00 |  0x80  | 1 | 0 |  0 | 0 |  0 |  0  | 0 | 0 | sign                  |
| 0x09 | 0x22 |  0x2B  | 0 | 0 |  1 | 0 |  1 |  1  | 0 | 0 | even parity + YF + XF |
*/
    @Test
    void testOR_n() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xF6); // OR 0x0F

        cpu.setPC(0x0000);
        cpu.setA((byte)0x00);
        compTest.poke(0x0001, (byte) 0x00);

        long initTState = cpu.getTState();
        cpu.fetch();

        assertEquals(7, cpu.getTState()-initTState, "OR n TState Failed");

        OR_A_case1(cpu, "OR 0x00 Failed: A<>0x00 = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0x80);
        compTest.poke(0x0001, (byte) 0x00);

        cpu.fetch();

        OR_A_case2(cpu, "OR 0x00 Failed: A<>0x80 = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0x09);
        compTest.poke(0x0001, (byte) 0x22);

        cpu.fetch();

        OR_A_case3(cpu, "OR 0x22 Failed: A<>0x2B = ");
    }

/*
SUB
SBC
CP
n/r_z/(IX+d)/(IY+d)
|   A  |   n  | S | Z | YF | H | XF | p/V | N | C | What test?       | Cases
| ---- | ---- | - | - | -- | - | -- | --- | - | - | ---------------- | -----
| 0x00 | 0x01 | 1 | 0 |  0 | 1 |  0 |  0  | 1 | 1 | borrow + sign    | Case 1
| 0xFF | 0xFF | 0 | 1 |  1 | 0 |  1 |  0  | 1 | 0 | zero clean       | Case 2
| 0x10 | 0x01 | 0 | 0 |  0 | 1 |  0 |  0  | 1 | 0 | half borrow      | Case 3
| 0x80 | 0x01 | 0 | 0 |  0 | 1 |  0 |  1  | 1 | 0 | overflow         | Case 4
*/
    @Test
    void testCP_n() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0x01);

        cpu.setPC(0x0000);
        cpu.setA((byte)0x00);

        long initTState = cpu.getTState();
        cpu.fetch((byte)0xFE); // CP 0x01

        assertEquals(7, cpu.getTState()-initTState, "CP n TState Failed");

        CP_A_case1(cpu, "CP 0x01  Failed");

        cpu.setPC(0x0000);
        cpu.setA((byte) 0xFF);
        compTest.poke(0x0000, (byte) 0xFF);

        cpu.fetch((byte)0xFE); // CP 0xFF

        CP_A_case2(cpu, "CP 0xFF Failed");

        cpu.setPC(0x0000);
        cpu.setA((byte)0x10);
        compTest.poke(0x0000, (byte) 0x01);

        cpu.fetch((byte)0xFE); // CP 0x01

        CP_A_case3(cpu, "CP 0x01 Failed");

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x80);

        cpu.fetch((byte)0xFE); // CP 0x01

        CP_A_case4(cpu, "CP 0x01 Failed");
    }

    @Test
    void testAND_XY() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);

        compTest.poke(0, (byte) 0xDD);
        compTest.poke(1, (byte) 0xA4); // AND IXH

        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);
        cpu.setIXH((byte) 0x00);

        long initTState = cpu.getTState();
        cpu.fetch();

        assertEquals(8, cpu.getTState()-initTState, "AND IXH TState Failed");

        AND_A_case1(cpu, "AND IXH Failed: A<>0x00 = ");

        compTest.poke(0, (byte) 0xFD);
        compTest.poke(1, (byte) 0xA4); // AND IYH

        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);
        cpu.setIYH((byte)0b0010_1011);

        cpu.fetch();

        AND_A_case2(cpu, "AND IYH Failed: A<>0x2B = ");

        compTest.poke(0, (byte) 0xDD);
        compTest.poke(1, (byte) 0xA5); // AND IXL

        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);
        cpu.setIXL((byte) 0x80);

        cpu.fetch();

        AND_A_case3(cpu, "AND IXL Failed: A<>0x80 = ");

        compTest.poke(0, (byte) 0xFD);
        compTest.poke(1, (byte) 0xA5); // AND IYL

        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);
        cpu.setIYL((byte)0x80);

        cpu.fetch();

        AND_A_case3(cpu, "AND IYL Failed: A<>0x80 = ");
    }

    @Test
    void testXOR_XY() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);

        // XOR IXH - Result = 0 (same values)
        compTest.poke(0, (byte) 0xDD);
        compTest.poke(1, (byte) 0xAC); // XOR IXH

        cpu.setPC(0x0000);
        cpu.setA((byte)0x55);
        cpu.setIXH((byte) 0x55);

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0, cpu.getA(), "XOR IXH Failed: A<>0"),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON"),
                () -> assertTrue(cpu.getPF(), "Parity flag must be ON"),
                () -> assertFalse(cpu.getHF(), "Half carry flag must be OFF"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF")
        );

        // XOR IYH - Result with odd parity
        compTest.poke(0, (byte) 0xFD);
        compTest.poke(1, (byte) 0xAC); // XOR IYH

        cpu.setPC(0x0000);
        cpu.setA((byte)0xAA);
        cpu.setIYH((byte)0x55);

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0xFF, cpu.getA(), "XOR IYH Failed: A<>0xFF"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF"),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON"),
                () -> assertTrue(cpu.getPF(), "Parity flag must be ON (even parity)")
        );

        // XOR IXL - Result = 0 (same values)
        compTest.poke(0, (byte) 0xDD);
        compTest.poke(1, (byte) 0xAD); // XOR IXL

        cpu.setPC(0x0000);
        cpu.setA((byte)0x55);
        cpu.setIXL((byte) 0x55);

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0, cpu.getA(), "XOR IXL Failed: A<>0"),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON"),
                () -> assertTrue(cpu.getPF(), "Parity flag must be ON"),
                () -> assertFalse(cpu.getHF(), "Half carry flag must be OFF"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF")
        );

        // XOR IYL - Result with odd parity
        compTest.poke(0, (byte) 0xFD);
        compTest.poke(1, (byte) 0xAD); // XOR IYL

        cpu.setPC(0x0000);
        cpu.setA((byte)0xAA);
        cpu.setIYL((byte)0x55);

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0xFF, cpu.getA(), "XOR IYL Failed: A<>0xFF"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF"),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON"),
                () -> assertTrue(cpu.getPF(), "Parity flag must be ON (even parity)")
        );
    }

    @Test
    void testOR_XY() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);

        // OR IXH - Result = 0 (both operands = 0)
        compTest.poke(0, (byte) 0xDD);
        compTest.poke(1, (byte) 0xB4); // OR IXH

        cpu.setPC(0x0000);
        cpu.setA((byte)0x00);
        cpu.setIXH((byte) 0x00);

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0, cpu.getA(), "OR IXH Failed: A<>0"),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON"),
                () -> assertTrue(cpu.getPF(), "Parity flag must be ON"),
                () -> assertFalse(cpu.getHF(), "Half carry flag must be OFF"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF")
        );

        // OR IYH - Result with even parity
        compTest.poke(0, (byte) 0xFD);
        compTest.poke(1, (byte) 0xB4); // OR IYH

        cpu.setPC(0x0000);
        cpu.setA((byte)0xAA);
        cpu.setIYH((byte)0x05);

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0xAF, cpu.getA(), "OR IYH Failed: A<>0xAF"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF"),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON"),
                () -> assertTrue(cpu.getPF(), "Parity flag must be ON (even parity)")
        );

        // OR IXL - Result = 0 (both operands = 0)
        compTest.poke(0, (byte) 0xDD);
        compTest.poke(1, (byte) 0xB5); // OR IXL

        cpu.setPC(0x0000);
        cpu.setA((byte)0x00);
        cpu.setIXL((byte) 0x00);

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0, cpu.getA(), "OR IXL Failed: A<>0"),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON"),
                () -> assertTrue(cpu.getPF(), "Parity flag must be ON"),
                () -> assertFalse(cpu.getHF(), "Half carry flag must be OFF"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF")
        );

        // OR IYL - Result with even parity
        compTest.poke(0, (byte) 0xFD);
        compTest.poke(1, (byte) 0xB5); // OR IYL

        cpu.setPC(0x0000);
        cpu.setA((byte)0xAA);
        cpu.setIYL((byte)0x05);

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0xAF, cpu.getA(), "OR IYL Failed: A<>0xAF"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF"),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON"),
                () -> assertTrue(cpu.getPF(), "Parity flag must be ON (even parity)")
        );
    }

    @Test
    void testCP_XY() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);

        compTest.poke(0, (byte) 0xDD);
        compTest.poke(1, (byte) 0xBC); // CP IXH

        cpu.setPC(0x0000);
        cpu.setA((byte)0x00);
        cpu.setIXH((byte) 0x01);

        long initTState = cpu.getTState();
        cpu.fetch();

        assertEquals(8, cpu.getTState()-initTState, "CP IXH TState Failed");

        CP_A_case1(cpu, "CP IXH  Failed");

        compTest.poke(0, (byte) 0xDD);
        compTest.poke(1, (byte) 0xBD); // CP IXL

        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);
        cpu.setIXL((byte)0xFF);

        cpu.fetch();

        CP_A_case2(cpu, "CP IXL Failed");

        compTest.poke(0, (byte) 0xFD);
        compTest.poke(1, (byte) 0xBC); // CP IYH

        cpu.setPC(0x0000);
        cpu.setA((byte)0x10);
        cpu.setIYH((byte)0x01);

        cpu.fetch();

        CP_A_case3(cpu, "CP IYH Failed");

        compTest.poke(0, (byte) 0xFD);
        compTest.poke(1, (byte) 0xBD); // CP IYL

        cpu.setPC(0x0000);
        cpu.setA((byte)0x80);
        cpu.setIYL((byte) 0x01);

        cpu.fetch();

        CP_A_case4(cpu, "CP IYL Failed");
    }

    private static void CP_A_case1(Z80ForTesting cpu, String msg) {
        assertAll(
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF flag must be OFF (Y=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Halfcarry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getXF(), "XF flag must be OFF (X=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")"),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")")
        );
    }

    private static void CP_A_case2(Z80ForTesting cpu, String msg) {
        assertAll(
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getYF(), "YF flag must be ON (Y=" + cpu.getYF() + ")"),
                () -> assertFalse(cpu.getHF(), "Halfcarry flag must be OFF (H=" + cpu.getHF() + ")"),
                () -> assertTrue(cpu.getXF(), "XF flag must be ON (X=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")")
        );
    }

    private static void CP_A_case3(Z80ForTesting cpu, String msg) {
        assertAll(
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF flag must be OFF (Y=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Halfcarry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getXF(), "XF flag must be OFF (X=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")")
        );
    }

    private static void CP_A_case4(Z80ForTesting cpu, String msg) {
        assertAll(
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF flag must be ON (Y=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Halfcarry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getXF(), "XF flag must be ON (X=" + cpu.getXF() + ")"),
                () -> assertTrue(cpu.getVF(), "Overflow flag must be ON (V=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")")
        );
    }

/*    private static void CP_A_case5(Z80ForTesting cpu, String msg) {
        assertAll(
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF flag must be OFF (Y=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Halfcarry flag must be OFF (H=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getXF(), "XF flag must be OFF (X=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")")
        );
    }
*/
    private static void AND_A_case1(Z80ForTesting cpu, String msg) {
        assertAll(
                () -> assertEquals((byte) 0x00, cpu.getA(),  msg + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF flag must be OFF (Y=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Halfcarry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getXF(), "XF flag must be OFF (X=" + cpu.getXF() + ")"),
                () -> assertTrue(cpu.getPF(), "Parity flag must be ON (V=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getNF(), "Negative flag must be OFF (N=" + cpu.getNF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")")
        );
    }

    private static void AND_A_case2(Z80ForTesting cpu, String msg) {
        assertAll(
                () -> assertEquals((byte) 0x2B, cpu.getA(), msg + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getYF(), "YF flag must be ON (Y=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Halfcarry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertTrue(cpu.getXF(), "XF flag must be ON (X=" + cpu.getXF() + ")"),
                () -> assertTrue(cpu.getPF(), "Parity flag must be ON (V=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getNF(), "Negative flag must be OFF (N=" + cpu.getNF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")")
        );
    }

    private static void AND_A_case3(Z80ForTesting cpu, String msg) {
        assertAll(
                () -> assertEquals((byte) 0x80, cpu.getA(), msg + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF flag must be OFF (Y=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Halfcarry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getXF(), "XF flag must be OFF (X=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getPF(), "Parity flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getNF(), "Negative flag must be OFF (N=" + cpu.getNF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")")
        );
    }

    private static void OR_A_case1(Z80ForTesting cpu, String msg) {
        assertAll(
                () -> assertEquals((byte) 0x00, cpu.getA(),  msg + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF flag must be OFF (Y=" + cpu.getYF() + ")"),
                () -> assertFalse(cpu.getHF(), "Halfcarry flag must be OFF (H=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getXF(), "XF flag must be OFF (X=" + cpu.getXF() + ")"),
                () -> assertTrue(cpu.getPF(), "Parity flag must be ON (V=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getNF(), "Negative flag must be OFF (N=" + cpu.getNF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")")
        );
    }

    private static void OR_A_case2(Z80ForTesting cpu, String msg) {
        assertAll(
                () -> assertEquals((byte) 0x80, cpu.getA(), msg + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF flag must be OFF (Y=" + cpu.getYF() + ")"),
                () -> assertFalse(cpu.getHF(), "Halfcarry flag must be OFF (H=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getXF(), "XF flag must be OFF (X=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getPF(), "Parity flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getNF(), "Negative flag must be OFF (N=" + cpu.getNF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")")
        );
    }

    private static void OR_A_case3(Z80ForTesting cpu, String msg) {
        assertAll(
                () -> assertEquals((byte) 0x2B, cpu.getA(), msg + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getYF(), "YF flag must be ON (Y=" + cpu.getYF() + ")"),
                () -> assertFalse(cpu.getHF(), "Halfcarry flag must be OFF (H=" + cpu.getHF() + ")"),
                () -> assertTrue(cpu.getXF(), "XF flag must be ON (X=" + cpu.getXF() + ")"),
                () -> assertTrue(cpu.getPF(), "Parity flag must be ON (V=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getNF(), "Negative flag must be OFF (N=" + cpu.getNF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")")
        );
    }
}

