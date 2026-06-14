package com.github.jagarsoft.test;

import com.github.jagarsoft.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RotateShiftTest {
    @Test
    void testRRCA() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte) 0x0F);

        cpu.fetch((byte) 0x0F); // RRCA

        assertAll("RRCA Group",
                () -> assertEquals((byte)0x87, cpu.getA(), "RRCA Failed: A<>0x87 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte)0x0F, cpu.getA(), "RRCA Failed: A still 0x0F = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "RRCA Failed: Carry flag must be ON")
        );

        cpu.setA((byte) 0xF0);

        cpu.fetch((byte) 0x0F); // RRCA

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

        cpu.fetch((byte) 0x17); // RLA

        assertAll("RLA Group: NC & 0x0F",
                () -> assertEquals((byte) 0x1E, cpu.getA(), "RLA Failed: A<>0x1E = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0x0F, cpu.getA(), "RLA Failed: A still 0x0F = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "RLA Failed: Carry flag must be OFF")
        );

        cpu.setCF();
        cpu.setA((byte) 0x0F);

        cpu.fetch((byte) 0x17); // RLA

        assertAll("RLA Group: C & 0x0F",
                () -> assertEquals((byte) 0x1F, cpu.getA(), "RLA Failed: A<>0x1F = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0x0F, cpu.getA(), "RLA Failed: A still 0x0F = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "RLA Failed: Carry flag must be OFF")
        );

        cpu.resCF();
        cpu.setA((byte) 0xF0);

        cpu.fetch((byte) 0x17); // RLA

        assertAll("RLA Group: NC & 0xF0",
                () -> assertEquals((byte) 0xE0, cpu.getA(), "RLA Failed: A<>0xE0 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0xF0, cpu.getA(), "RLA Failed: A still 0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "RLA Failed: Carry flag must be ON"),
                () -> assertFalse(cpu.getPF(), "RLA Failed: Parity flag must be OFF")
        );

        cpu.setCF();
        cpu.setA((byte) 0xF0);

        cpu.fetch((byte) 0x17); // RLA

        assertAll("RLA Group: C & 0xF0",
                () -> assertEquals((byte) 0xE1, cpu.getA(), "RLA Failed: A<>0xE1 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0xF0, cpu.getA(), "RLA Failed: A still 0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "RLA Failed: Carry flag must be ON")
        );
    }

    @Test
    void testRRA() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.resCF();
        cpu.setA((byte) 0x0F);

        cpu.fetch((byte) 0x1F); // RRA

        assertAll("RRA Group: NC & 0x0F",
                () -> assertEquals((byte) 0x07, cpu.getA(), "RRA Failed: A<>0x07 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0x0F, cpu.getA(), "RRA Failed: A still 0x0F = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "RRA Failed: Carry flag must be ON"),
                () -> assertFalse(cpu.getPF(), "RRA Failed: Parity flag must be OFF")
        );

        cpu.setCF();
        cpu.setA((byte) 0x0F);

        cpu.fetch((byte) 0x1F); // RRA

        assertAll("RRA Group: C & 0x0F",
                () -> assertEquals((byte) 0x87, cpu.getA(), "RRA Failed: A<>0x87 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0x0F, cpu.getA(), "RRA Failed: A still 0x0F = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "RRA Failed: Carry flag must be ON"),
                () -> assertTrue(cpu.getPF(), "RRA Failed: Parity flag must be OFF")
        );

        cpu.resCF();
        cpu.setA((byte) 0xF0);

        cpu.fetch((byte) 0x1F); // RRA

        assertAll("RRA Group: NC & 0xF0",
                () -> assertEquals((byte) 0x78, cpu.getA(), "RRA Failed: A<>0x78 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0xF0, cpu.getA(), "RRA Failed: A still 0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "RRA Failed: Carry flag must be OFF"),
                () -> assertTrue(cpu.getPF(), "RRA Failed: Parity flag must be ON")
        );

        cpu.setCF();
        cpu.setA((byte) 0xF0);

        cpu.fetch((byte) 0x1F); // RRA

        assertAll("RRA Group: C & 0xF0",
                () -> assertEquals((byte) 0xF8, cpu.getA(), "RRA Failed: A<>0xF8 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0xF0, cpu.getA(), "RRA Failed: A still 0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "RRA Failed: Carry flag must be OFF"),
                () -> assertFalse(cpu.getPF(), "RRA Failed: Parity flag must be OFF")
        );
    }

    @Test
    void testRL_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xCB); // RL r[z]

        cpu.setPC(0x0000);
        cpu.resCF();
        cpu.setB((byte) 0x0F);

        compTest.poke(0x0001, (byte) 0x10); // RL B
        long initTState = cpu.getTState();
        cpu.fetch();

        assertAll("RL B Group: NC & 0x0F",
                () -> assertEquals((byte) 0x1E, cpu.getB(), "RL B Failed: A<>0x1E = " + Integer.toHexString(cpu.getB())),
                () -> assertNotEquals((byte) 0x0F, cpu.getB(), "RL B Failed: A still 0x0F = " + Integer.toHexString(cpu.getB())),
                () -> assertFalse(cpu.getCF(), "RL B Failed: Carry flag must be OFF"),
                () -> assertTrue(cpu.getPF(), "RL B Failed: Parity flag must be ON"),

                () -> assertEquals(8, cpu.getTState()-initTState, "RL B TState Failed")
        );

        cpu.setPC(0x0000);
        cpu.setCF();
        cpu.setC((byte) 0x0F);

        compTest.poke(0x0001, (byte) 0x11); // RL C
        cpu.fetch();

        assertAll("RL C Group: C & 0x0F",
                () -> assertEquals((byte) 0x1F, cpu.getC(), "RL C Failed: C<>0x1F = " + Integer.toHexString(cpu.getC())),
                () -> assertNotEquals((byte) 0x0F, cpu.getC(), "RL C Failed: C still 0x0F = " + Integer.toHexString(cpu.getC())),
                () -> assertFalse(cpu.getCF(), "RL C Failed: Carry flag must be OFF"),
                () -> assertFalse(cpu.getPF(), "RL C Failed: Parity flag must be OFF")
        );

        cpu.setPC(0x0000);
        cpu.resCF();
        cpu.setA((byte) 0xF0);

        compTest.poke(0x0001, (byte) 0x17); // RL A
        cpu.fetch();

        assertAll("RL A Group: NC & 0xF0",
                () -> assertEquals((byte) 0xE0, cpu.getA(), "RL A Failed: A<>0xE0 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0xF0, cpu.getA(), "RL A Failed: A still 0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "RL A Failed: Carry flag must be ON"),
                () -> assertFalse(cpu.getPF(), "RL A Failed: Parity flag must be OFF")
        );

        cpu.setPC(0x0000);
        cpu.setCF();
        cpu.setD((byte) 0xF0);

        compTest.poke(0x0001, (byte) 0x12); // RL D
        cpu.fetch();

        assertAll("RL Group: C & 0xF0",
                () -> assertEquals((byte) 0xE1, cpu.getD(), "RL D Failed: D<>0xE1 = " + Integer.toHexString(cpu.getD())),
                () -> assertNotEquals((byte) 0xF0, cpu.getD(), "RL D Failed: D still 0xF0 = " + Integer.toHexString(cpu.getD())),
                () -> assertTrue(cpu.getCF(), "RL D Failed: Carry flag must be ON"),
                () -> assertTrue(cpu.getPF(), "RL D Failed: Parity flag must be ON")
        );

        cpu.setPC(0x0000);
        cpu.resCF();
        cpu.setE((byte) 0x80);

        compTest.poke(0x0001, (byte) 0x13); // RL E
        cpu.fetch();

        assertAll("RL Group: Z & 0x80",
                () -> assertEquals((byte) 0x00, cpu.getE(), "RL E Failed: E<>0xE1 = " + Integer.toHexString(cpu.getE())),
                () -> assertNotEquals((byte) 0x80, cpu.getE(), "RL E Failed: E still 0x80 = " + Integer.toHexString(cpu.getE())),
                () -> assertTrue(cpu.getCF(), "RL E Failed: Carry flag must be ON"),
                () -> assertTrue(cpu.getPF(), "RL E Failed: Parity flag must be ON"),
                () -> assertTrue(cpu.getZF(), "RL E Failed: Zero flag must be ON")
        );
    }

    @Test
    void testRR_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xCB); // RR r[z]

        cpu.setPC(0x0000);
        cpu.resCF();
        cpu.setB((byte) 0x0F);

        compTest.poke(0x0001, (byte) 0x18); // RR B
        long initTState = cpu.getTState();
        cpu.fetch();

        assertAll("RR B Group: NC & 0x0F",
                () -> assertEquals((byte) 0x07, cpu.getB(), "RR B Failed: B<>0x07 = " + Integer.toHexString(cpu.getB())),
                () -> assertNotEquals((byte) 0x0F, cpu.getB(), "RR Failed: B still 0x0F = " + Integer.toHexString(cpu.getB())),
                () -> assertTrue(cpu.getCF(), "RR Failed: Carry flag must be ON"),
                () -> assertFalse(cpu.getPF(), "RR Failed: Parity flag must be OFF"),

                () -> assertEquals(8, cpu.getTState()-initTState, "RR B TState Failed")
        );

        cpu.setPC(0x0000);
        cpu.setCF();
        cpu.setC((byte) 0x0F);

        compTest.poke(0x0001, (byte) 0x19); // RR C
        cpu.fetch();

        assertAll("RR C Group: C & 0x0F",
                () -> assertEquals((byte) 0x87, cpu.getC(), "RR C Failed: C<>0x87 = " + Integer.toHexString(cpu.getC())),
                () -> assertNotEquals((byte) 0x0F, cpu.getC(), "RR C Failed: C still 0x0F = " + Integer.toHexString(cpu.getC())),
                () -> assertTrue(cpu.getCF(), "RR C Failed: Carry flag must be ON"),
                () -> assertTrue(cpu.getPF(), "RR C Failed: Parity flag must be OFF")
        );

        cpu.setPC(0x0000);
        cpu.resCF();
        cpu.setA((byte) 0xF0);

        compTest.poke(0x0001, (byte) 0x1F); // RR A
        cpu.fetch();

        assertAll("RR A Group: NC & 0xF0",
                () -> assertEquals((byte) 0x78, cpu.getA(), "RR A Failed: A<>0x78 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0xF0, cpu.getA(), "RR A Failed: A still 0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "RR A Failed: Carry flag must be OFF"),
                () -> assertTrue(cpu.getPF(), "RR A Failed: Parity flag must be ON")
        );

        cpu.setPC(0x0000);
        cpu.setCF();
        cpu.setD((byte) 0xF0);

        compTest.poke(0x0001, (byte) 0x1A); // RR D
        cpu.fetch();

        assertAll("RR Group: C & 0xF0",
                () -> assertEquals((byte) 0xF8, cpu.getD(), "RR D Failed: D<>0xF8 = " + Integer.toHexString(cpu.getD())),
                () -> assertNotEquals((byte) 0xF0, cpu.getD(), "RR D Failed: D still 0xF0 = " + Integer.toHexString(cpu.getD())),
                () -> assertFalse(cpu.getCF(), "RR D Failed: Carry flag must be OFF"),
                () -> assertFalse(cpu.getPF(), "RR D Failed: Parity flag must be OFF")
        );

        cpu.setPC(0x0000);
        cpu.resCF();
        cpu.setE((byte) 0x01);

        compTest.poke(0x0001, (byte) 0x1B); // RR E
        cpu.fetch();

        assertAll("RL Group: Z & 0x80",
                () -> assertEquals((byte) 0x00, cpu.getE(), "RLA Failed: E<>0x00 = " + Integer.toHexString(cpu.getE())),
                () -> assertNotEquals((byte) 0x01, cpu.getE(), "RLA Failed: E still 0x01 = " + Integer.toHexString(cpu.getE())),
                () -> assertTrue(cpu.getCF(), "RL E Failed: Carry flag must be ON"),
                () -> assertTrue(cpu.getPF(), "RL E Failed: Parity flag must be ON"),
                () -> assertTrue(cpu.getZF(), "RL E Failed: Zero flag must be ON")
        );
    }


    @Test
    void testRLCA() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte) 0x0F);

        cpu.fetch((byte) 0x07); // RLCA

        assertAll("RLCA Group",
                () -> assertEquals((byte)0x1E, cpu.getA(), "RLCA Failed: A<>0x1E = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte)0x0F, cpu.getA(), "RLCA Failed: A still 0x0F = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "RLCA Failed: Carry flag must be OFF")
        );

        cpu.setA((byte) 0xF0);

        cpu.fetch((byte) 0x07); // RLCA

        assertAll("RLCA Group",
                () -> assertEquals((byte)0xE1, cpu.getA(), "RLCA Failed: A<>0xE1 = " + Integer.toHexString((byte)(cpu.getA()&0xFF))),
                () -> assertNotEquals((byte)0xF0, cpu.getA(), "RLCA Failed: A still 0xF0 = " + Integer.toHexString((byte)(cpu.getA()&0xFF))),
                () -> assertTrue(cpu.getCF(), "RLCA Failed: Carry flag must be ON")
        );
    }

    @Test
    void testRLC_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xCB); // RLC r[z]

        cpu.setPC(0x0000);
        cpu.setB((byte) 0x0F);
        compTest.poke(0x0001, (byte) 0x00); // RLC B

        long initTState = cpu.getTState();
        cpu.fetch();

        assertAll("RLC r[z] Group",
                () -> assertEquals((byte)0x1E, cpu.getB(), "RLC B Failed: B<>0x1E = " + Integer.toHexString(cpu.getB())),
                () -> assertNotEquals((byte)0x0F, cpu.getB(), "RLC B Failed: B still 0x0F = " + Integer.toHexString(cpu.getB())),
                () -> assertFalse(cpu.getCF(), "RLC B Failed: Carry flag must be OFF"),
                () -> assertFalse(cpu.getZF(), "RLC B Failed: Zero flag must be OFF"),
                () -> assertTrue(cpu.getPF(), "RLC B Failed: Parity flag must be ON"),

                () -> assertEquals(8, cpu.getTState()-initTState, "RLC B TState Failed")
        );

        cpu.setPC(0x0000);
        cpu.setC((byte) 0x0F);
        compTest.poke(0x0001, (byte) 0x01); // RLC C

        cpu.fetch();

        assertAll("RLC r[z] Group",
                () -> assertEquals((byte)0x1E, cpu.getC(), "RLC C Failed: B<>0x1E = " + Integer.toHexString(cpu.getC())),
                () -> assertNotEquals((byte)0x0F, cpu.getC(), "RLC C Failed: B still 0x0F = " + Integer.toHexString(cpu.getC())),
                () -> assertFalse(cpu.getCF(), "RLC C Failed: Carry flag must be OFF"),
                () -> assertFalse(cpu.getZF(), "RLC C Failed: Zero flag must be OFF"),
                () -> assertTrue(cpu.getPF(), "RLC C Failed: Parity flag must be ON")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0xF0);
        compTest.poke(0x0001, (byte) 0x07); // RLC A

        cpu.fetch();

        assertAll("RLC r[z] Group",
                () -> assertEquals((byte)0xE1, cpu.getA(), "RLC A Failed: A<>0xE1 = " + Integer.toHexString((byte)(cpu.getA()&0xFF))),
                () -> assertNotEquals((byte)0xF0, cpu.getA(), "RLC A Failed: A still 0xF0 = " + Integer.toHexString((byte)(cpu.getA()&0xFF))),
                () -> assertTrue(cpu.getCF(), "RLC A Failed: Carry flag must be ON"),
                () -> assertFalse(cpu.getZF(), "RLC A Failed: Zero flag must be OFF"),
                () -> assertTrue(cpu.getPF(), "RLC A Failed: Parity flag must be ON")
        );

        cpu.setPC(0x0000);
        cpu.setD((byte) 0x00);
        compTest.poke(0x0001, (byte) 0x02); // RLC D

        cpu.fetch();

        assertAll("RLC r[z] Group",
                () -> assertEquals((byte)0x00, cpu.getD(), "RLC D Failed: D<>0x00 = " + Integer.toHexString((byte)(cpu.getD()&0xFF))),

                () -> assertFalse(cpu.getCF(), "RLC D Failed: Carry flag must be OFF"),
                () -> assertTrue(cpu.getZF(), "RLC D Failed: Zero flag must be ON"),
                () -> assertTrue(cpu.getPF(), "RLC D Failed: Parity flag must be ON")
        );
    }

    @Test
    void testRRC_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xCB); // RRC r[z]

        cpu.setPC(0x0000);
        cpu.setB((byte) 0x0F);
        compTest.poke(0x0001, (byte) 0x08); // RRC B

        long initTState = cpu.getTState();
        cpu.fetch();

        assertAll("RRC r[z] Group",
                () -> assertEquals((byte)0x87, cpu.getB(), "RRC B Failed: B<>0x87 = " + Integer.toHexString(cpu.getB())),
                () -> assertNotEquals((byte)0x0F, cpu.getB(), "RRC B Failed: B still 0x0F = " + Integer.toHexString(cpu.getB())),
                () -> assertTrue(cpu.getCF(), "RRC B Failed: Carry flag must be ON"),
                () -> assertFalse(cpu.getZF(), "RRC B Failed: Zero flag must be OFF"),
                () -> assertTrue(cpu.getPF(), "RRC B Failed: Parity flag must be ON"),

                () -> assertEquals(8, cpu.getTState()-initTState, "RRC B TState Failed")
        );

        cpu.setPC(0x0000);
        cpu.setC((byte) 0x0E);
        compTest.poke(0x0001, (byte) 0x09); // RRC C

        cpu.fetch();

        assertAll("RRC r[z] Group",
                () -> assertEquals((byte)0x07, cpu.getC(), "RRC C Failed: C<>0x07 = " + Integer.toHexString(cpu.getC())),
                () -> assertNotEquals((byte)0x0E, cpu.getC(), "RRC C Failed: C still 0x0E = " + Integer.toHexString(cpu.getC())),
                () -> assertFalse(cpu.getCF(), "RRC C Failed: Carry flag must be OFF"),
                () -> assertFalse(cpu.getZF(), "RRC C Failed: Zero flag must be OFF"),
                () -> assertFalse(cpu.getPF(), "RRC C Failed: Parity flag must be OFF")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0xF0);
        compTest.poke(0x0001, (byte) 0x0F); // RRC A

        cpu.fetch();

        assertAll("RRC r[z] Group",
                () -> assertEquals((byte)0x78, cpu.getA(), "RRC A Failed: A<>0x78 = " + Integer.toHexString((byte)(cpu.getA()&0xFF))),
                () -> assertNotEquals((byte)0xF0, cpu.getA(), "RRC A Failed: A still 0xF0 = " + Integer.toHexString((byte)(cpu.getA()&0xFF))),
                () -> assertFalse(cpu.getCF(), "RRC A Failed: Carry flag must be ON"),
                () -> assertFalse(cpu.getZF(), "RRC A Failed: Zero flag must be OFF"),
                () -> assertTrue(cpu.getPF(), "RRC A Failed: Parity flag must be ON")
        );

        cpu.setPC(0x0000);
        cpu.setD((byte) 0x00);
        compTest.poke(0x0001, (byte) 0x0A); // RRC D

        cpu.fetch();

        assertAll("RRC r[z] Group",
                () -> assertEquals((byte)0x00, cpu.getD(), "RRC D Failed: D<>0x00 = " + Integer.toHexString((byte)(cpu.getD()&0xFF))),

                () -> assertFalse(cpu.getCF(), "RRC D Failed: Carry flag must be OFF"),
                () -> assertTrue(cpu.getZF(), "RRC D Failed: Zero flag must be ON"),
                () -> assertTrue(cpu.getPF(), "RRC D Failed: Parity flag must be ON")
        );
    }

    @Test
    void testSRA_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xCB); // SRA r[z]

        cpu.setPC(0x0000);
        cpu.setB((byte) 0x0F);
        compTest.poke(0x0001, (byte) 0x28); // SRA B
        
        long initTState = cpu.getTState();
        cpu.fetch();

        assertAll("SRA r[z]",
                () -> assertEquals((byte) 0x07, cpu.getB(), "SRA B Failed: B<>0x07 = " + Integer.toHexString(cpu.getB())),
                () -> assertNotEquals((byte) 0x0F, cpu.getB(), "SRA B Failed: B still 0x0F = " + Integer.toHexString(cpu.getB())),
                () -> assertTrue(cpu.getCF(), "SRA B Failed: Carry flag must be ON"),
                () -> assertFalse(cpu.getZF(), "SRA B Failed: Zero flag must be OFF"),
                () -> assertFalse(cpu.getPF(), "SRA B Failed: Parity flag must be OFF"),

                () -> assertEquals(8, cpu.getTState()-initTState, "SRA B TState Failed")
        );

        cpu.setPC(0x0000);
        cpu.resCF();
        cpu.setA((byte) 0xF1);
        compTest.poke(0x0001, (byte) 0x2F); // SRA A

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0xF8, cpu.getA(), "SRA A Failed: A<>0xF8 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0xF1, cpu.getA(), "SRA A Failed: A still 0x0F = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "SRA A Failed: Carry flag must be OON"),
                () -> assertFalse(cpu.getZF(), "SRA A Failed: Zero flag must be OFF"),
                () -> assertFalse(cpu.getPF(), "SRA A Failed: Parity flag must be OFF")
        );

        cpu.setPC(0x0000);
        cpu.setCF();
        cpu.setC((byte) 0x00);
        compTest.poke(0x0001, (byte) 0x29); // SRA C

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x00, cpu.getC(), "SRC C Failed: C<>0x00 = " + Integer.toHexString(cpu.getC())),

                () -> assertFalse(cpu.getCF(), "SRC C Failed: Carry flag must be OFF"),
                () -> assertTrue(cpu.getZF(), "SRC C Failed: Zero flag must be ON"),
                () -> assertTrue(cpu.getPF(), "SRC C Failed: Parity flag must be ON")
        );
    }

    @Test
    void testSLA_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xCB); // SLA r[z]

        cpu.setPC(0x0000);
        cpu.setB((byte) 0x0F);
        compTest.poke(0x0001, (byte) 0x20); // SLA B

        long initTState = cpu.getTState();
        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x1E, cpu.getB(), "SRL B Failed: B<>0x1E = " + Integer.toHexString(cpu.getB())),
                () -> assertNotEquals((byte) 0x0F, cpu.getB(), "SRL B Failed: B still 0x0F = " + Integer.toHexString(cpu.getB())),
                () -> assertFalse(cpu.getCF(), "SRL B Failed: Carry flag must be OFF"),
                () -> assertFalse(cpu.getZF(), "SRL B Failed: Zero flag must be OFF"),
                () -> assertTrue(cpu.getPF(), "SRL B Failed: Parity flag must be ON"),

                () -> assertEquals(8, cpu.getTState()-initTState, "SRL B TState Failed")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0xF0);
        compTest.poke(0x0001, (byte) 0x27); // SLA A

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0xE0, cpu.getA(), "SRL A Failed: A<>0xE0 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0xF0, cpu.getA(), "SRL A Failed: A still 0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "SRL B Failed: Carry flag must be ON"),
                () -> assertFalse(cpu.getZF(), "SRL B Failed: Zero flag must be OFF"),
                () -> assertFalse(cpu.getPF(), "SRL B Failed: Parity flag must be OFF")
        );

        cpu.setPC(0x0000);
        cpu.setC((byte) 0x80);
        compTest.poke(0x0001, (byte) 0x21); // SLA C

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x00, cpu.getC(), "SRL C Failed: C<>0x00 = " + Integer.toHexString(cpu.getC())),
                () -> assertNotEquals((byte) 0x80, cpu.getC(), "SRL A Failed: C still 0x80 = " + Integer.toHexString(cpu.getC())),
                () -> assertTrue(cpu.getCF(), "SRL B Failed: Carry flag must be ON"),
                () -> assertTrue(cpu.getZF(), "SRL B Failed: Zero flag must be ON"),
                () -> assertTrue(cpu.getPF(), "SRL B Failed: Parity flag must be ON")
        );
    }

    @Test
    void testSRL_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xCB); // SRL r[z]

        cpu.setPC(0x0000);
        cpu.setB((byte) 0xF0);
        compTest.poke(0x0001, (byte) 0x38); // SRL B

        long initTState = cpu.getTState();
        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x78, cpu.getB(), "SRL B Failed: B<>0x78 = " + Integer.toHexString(cpu.getB())),
                () -> assertNotEquals((byte) 0xF0, cpu.getB(), "SRL B Failed: B still 0xF0 = " + Integer.toHexString(cpu.getB())),
                () -> assertFalse(cpu.getCF(), "SRL B Failed: Carry flag must be OFF"),
                () -> assertFalse(cpu.getZF(), "SRL B Failed: Zero flag must be OFF"),
                () -> assertTrue(cpu.getPF(), "SRL B Failed: Parity flag must be ON"),

                () -> assertEquals(8, cpu.getTState()-initTState, "SRL B TState Failed")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x01);
        compTest.poke(0x0001, (byte) 0x3F); // SRL A

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x00, cpu.getA(), "SRL A Failed: A<>0x00 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0x01, cpu.getA(), "SRL A Failed: A still 0x01 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "SRL B Failed: Carry flag must be ON"),
                () -> assertTrue(cpu.getZF(), "SRL B Failed: Zero flag must be ON"),
                () -> assertTrue(cpu.getPF(), "SRL B Failed: Parity flag must be ON")
        );
    }

    @Test
    void testSLL_r_z() { // undocumented
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xCB); // SLL r[z]

        cpu.setPC(0x0000);
        cpu.setB((byte) 0x0F);
        compTest.poke(0x0001, (byte) 0x30); // SLL B

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x1F, cpu.getB(), "SRL B Failed: B<>0x1F = " + Integer.toHexString(cpu.getB())),
                () -> assertNotEquals((byte) 0x0F, cpu.getB(), "SRL B Failed: B still 0x0F = " + Integer.toHexString(cpu.getB())),
                () -> assertFalse(cpu.getCF(), "SRL B Failed: Carry flag must be OFF"),
                () -> assertFalse(cpu.getZF(), "SRL B Failed: Zero flag must be OFF"),
                () -> assertFalse(cpu.getPF(), "SRL B Failed: Parity flag must be OFF")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0xF0);
        compTest.poke(0x0001, (byte) 0x37); // SLL A

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0xE1, cpu.getA(), "SRL A Failed: A<>0xE1 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0xF0, cpu.getA(), "SRL A Failed: A still 0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "SRL B Failed: Carry flag must be ON"),
                () -> assertFalse(cpu.getZF(), "SRL B Failed: Zero flag must be OFF"),
                () -> assertTrue(cpu.getPF(), "SRL B Failed: Parity flag must be ON")
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
