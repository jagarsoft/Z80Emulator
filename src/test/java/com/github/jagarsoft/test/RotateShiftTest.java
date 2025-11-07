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
        cpu.fetch();

        assertAll("RL B Group: NC & 0x0F",
                () -> assertEquals((byte) 0x1E, cpu.getB(), "RL B Failed: A<>0x1E = " + Integer.toHexString(cpu.getB())),
                () -> assertNotEquals((byte) 0x0F, cpu.getB(), "RL B Failed: A still 0x0F = " + Integer.toHexString(cpu.getB())),
                () -> assertFalse(cpu.getCF(), "RL B Failed: Carry flag must be OFF"),
                () -> assertTrue(cpu.getPF(), "RL B Failed: Parity flag must be ON")
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
                () -> assertEquals((byte) 0xE0, cpu.getA(), "RLA Failed: A<>0xE0 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0xF0, cpu.getA(), "RLA Failed: A still 0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "RLA Failed: Carry flag must be ON"),
                () -> assertFalse(cpu.getPF(), "RLA Failed: Parity flag must be OFF")
        );

        cpu.setPC(0x0000);
        cpu.setCF();
        cpu.setA((byte) 0xF0);

        compTest.poke(0x0001, (byte) 0x17); // RL C
        cpu.fetch();

        assertAll("RLA Group: C & 0xF0",
                () -> assertEquals((byte) 0xE1, cpu.getA(), "RLA Failed: A<>0xE1 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0xF0, cpu.getA(), "RLA Failed: A still 0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "RLA Failed: Carry flag must be ON"),
                () -> assertTrue(cpu.getPF(), "RLA Failed: Parity flag must be ON")
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
        cpu.fetch();

        assertAll("RR B Group: NC & 0x0F",
                () -> assertEquals((byte) 0x07, cpu.getB(), "RR B Failed: B<>0x07 = " + Integer.toHexString(cpu.getB())),
                () -> assertNotEquals((byte) 0x0F, cpu.getB(), "RR Failed: B still 0x0F = " + Integer.toHexString(cpu.getB())),
                () -> assertTrue(cpu.getCF(), "RR Failed: Carry flag must be ON"),
                () -> assertFalse(cpu.getPF(), "RR Failed: Parity flag must be OFF")
        );

        cpu.setPC(0x0000);
        cpu.setCF();
        cpu.setC((byte) 0x0F);

        compTest.poke(0x0001, (byte) 0x19); // RR C
        cpu.fetch();

        assertAll("RR C Group: C & 0x0F",
                () -> assertEquals((byte) 0x87, cpu.getC(), "RR C Failed: C<>0x87 = " + Integer.toHexString(cpu.getC())),
                () -> assertNotEquals((byte) 0x0F, cpu.getC(), "RR C Failed: C still 0x0F = " + Integer.toHexString(cpu.getC())),
                () -> assertTrue(cpu.getCF(), "RR Failed: Carry flag must be ON"),
                () -> assertTrue(cpu.getPF(), "RR Failed: Parity flag must be OFF")
        );

        cpu.setPC(0x0000);
        cpu.resCF();
        cpu.setA((byte) 0xF0);

        compTest.poke(0x0001, (byte) 0x1F); // RR A
        cpu.fetch();

        assertAll("RR A Group: NC & 0xF0",
                () -> assertEquals((byte) 0x78, cpu.getA(), "RR A Failed: A<>0x78 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0xF0, cpu.getA(), "RR Failed: A still 0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "RR Failed: Carry flag must be OFF"),
                () -> assertTrue(cpu.getPF(), "RR Failed: Parity flag must be ON")
        );

        cpu.setPC(0x0000);
        cpu.setCF();
        cpu.setA((byte) 0xF0);

        compTest.poke(0x0001, (byte) 0x1F); // RR A
        cpu.fetch();

        assertAll("RR Group: C & 0xF0",
                () -> assertEquals((byte) 0xF8, cpu.getA(), "RR Failed: A<>0xF8 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0xF0, cpu.getA(), "RR Failed: A still 0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "RR Failed: Carry flag must be OFF"),
                () -> assertFalse(cpu.getPF(), "RR Failed: Parity flag must be OFF")
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

        cpu.fetch();

        assertAll("RLC r[z] Group",
                () -> assertEquals((byte)0x1E, cpu.getB(), "RLC B Failed: B<>0x1E = " + Integer.toHexString(cpu.getB())),
                () -> assertNotEquals((byte)0x0F, cpu.getB(), "RLC B Failed: B still 0x0F = " + Integer.toHexString(cpu.getB())),
                () -> assertFalse(cpu.getCF(), "RLC B Failed: Carry flag must be OFF"),
                () -> assertFalse(cpu.getZF(), "RLC B Failed: Zero flag must be OFF"),
                () -> assertTrue(cpu.getPF(), "RLC B Failed: Parity flag must be ON")
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

        cpu.fetch();

        assertAll("RRC r[z] Group",
                () -> assertEquals((byte)0x87, cpu.getB(), "RRC B Failed: B<>0x87 = " + Integer.toHexString(cpu.getB())),
                () -> assertNotEquals((byte)0x0F, cpu.getB(), "RRC B Failed: B still 0x0F = " + Integer.toHexString(cpu.getB())),
                () -> assertTrue(cpu.getCF(), "RRC B Failed: Carry flag must be ON"),
                () -> assertFalse(cpu.getZF(), "RRC B Failed: Zero flag must be OFF"),
                () -> assertTrue(cpu.getPF(), "RRC B Failed: Parity flag must be ON")
        );

        cpu.setPC(0x0000);
        cpu.setC((byte) 0x0F);
        compTest.poke(0x0001, (byte) 0x09); // RRC C

        cpu.fetch();

        assertAll("RRC r[z] Group",
                () -> assertEquals((byte)0x87, cpu.getC(), "RRC C Failed: C<>0x87 = " + Integer.toHexString(cpu.getC())),
                () -> assertNotEquals((byte)0x0F, cpu.getC(), "RRC C Failed: C still 0x0F = " + Integer.toHexString(cpu.getC())),
                () -> assertTrue(cpu.getCF(), "RRC C Failed: Carry flag must be ON"),
                () -> assertFalse(cpu.getZF(), "RRC C Failed: Zero flag must be OFF"),
                () -> assertTrue(cpu.getPF(), "RRC C Failed: Parity flag must be ON")
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

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x07, cpu.getB(), "SRL B Failed: B<>0x07 = " + Integer.toHexString(cpu.getB())),
                () -> assertNotEquals((byte) 0x0F, cpu.getB(), "SRL B Failed: B still 0x0F = " + Integer.toHexString(cpu.getB())),
                () -> assertTrue(cpu.getCF(), "SRL B Failed: Carry flag must be OON"),
                () -> assertFalse(cpu.getZF(), "SRL B Failed: Zero flag must be OFF"),
                () -> assertFalse(cpu.getPF(), "SRL B Failed: Parity flag must be OFF")
        );

        cpu.setPC(0x0000);
        cpu.resCF();
        cpu.setA((byte) 0xF1);
        compTest.poke(0x0001, (byte) 0x2F); // SRA A

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0xF8, cpu.getA(), "SRL A Failed: A<>0xF8 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0xF1, cpu.getA(), "SRL A Failed: A still 0x0F = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "SRL A Failed: Carry flag must be OON"),
                () -> assertFalse(cpu.getZF(), "SRL A Failed: Zero flag must be OFF"),
                () -> assertFalse(cpu.getPF(), "SRL A Failed: Parity flag must be OFF")
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

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x1E, cpu.getB(), "SRL B Failed: B<>0x1E = " + Integer.toHexString(cpu.getB())),
                () -> assertNotEquals((byte) 0x0F, cpu.getB(), "SRL B Failed: B still 0x0F = " + Integer.toHexString(cpu.getB())),
                () -> assertFalse(cpu.getCF(), "SRL B Failed: Carry flag must be OFF"),
                () -> assertFalse(cpu.getZF(), "SRL B Failed: Zero flag must be OFF"),
                () -> assertTrue(cpu.getPF(), "SRL B Failed: Parity flag must be ON")
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
        cpu.setB((byte) 0x0F);
        compTest.poke(0x0001, (byte) 0x38); // SRL B

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x07, cpu.getB(), "SRL B Failed: B<>0x07 = " + Integer.toHexString(cpu.getB())),
                () -> assertNotEquals((byte) 0x0F, cpu.getB(), "SRL B Failed: B still 0x0F = " + Integer.toHexString(cpu.getB())),
                () -> assertTrue(cpu.getCF(), "SRL B Failed: Carry flag must be ON"),
                () -> assertFalse(cpu.getZF(), "SRL B Failed: Zero flag must be OFF"),
                () -> assertFalse(cpu.getPF(), "SRL B Failed: Parity flag must be OFF")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0xF0);
        compTest.poke(0x0001, (byte) 0x3F); // SRL A

        cpu.fetch();

        assertAll(
                () -> assertEquals((byte) 0x78, cpu.getA(), "SRL A Failed: A<>0x78 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0xF0, cpu.getA(), "SRL A Failed: A still 0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getCF(), "SRL B Failed: Carry flag must be OFF"),
                () -> assertFalse(cpu.getZF(), "SRL B Failed: Zero flag must be OFF"),
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
}
