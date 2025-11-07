package com.github.jagarsoft.test;

import com.github.jagarsoft.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StackTest {
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
                () -> assertEquals((short) 0x0FF00, cpu.getBC(), "POP BC Failed: BC was not modified (BC=" + cpu.getBC() + ")"),
                () -> assertNotEquals(0x0000, cpu.getBC(), "POP BC Failed: BC still 0x0000 (BC=" + cpu.getBC() + ")")
        );

        cpu.setSP((short) 0x0000);

        cpu.setDE((short) 0x0000);

        cpu.fetch((byte)0xD1); // POP DE

        assertAll("POP rp2[y] Group",
                () -> assertEquals((short) 0x0FF00, cpu.getDE(), "POP DE Failed: DE was not modified (DE=" + cpu.getDE() + ")"),
                () -> assertNotEquals(0x0000, cpu.getDE(), "POP DE Failed: DE still 0x0000 (DE=" + cpu.getDE() + ")")
        );

        cpu.setSP((short) 0x0000);

        cpu.setHL((short) 0x0000);

        cpu.fetch((byte)0xE1); // POP HL

        assertAll("POP rp2[y] Group",
                () -> assertEquals((short)0x0FF00, cpu.getHL(), "POP HL Failed: HL was not modified (HL=" + cpu.getHL() + ")"),
                () -> assertNotEquals(0x0000, cpu.getHL(), "POP HL Failed: HL still 0x0000 (HL=" + cpu.getHL() + ")")
        );

        cpu.setSP((short) 0x0000);

        cpu.setA((byte) 0x00);
        cpu.setF((byte) 0x00);

        cpu.fetch((byte)0xF1); // POP AF

        assertAll("POP rp2[y] Group",
                () -> assertEquals((byte)0xFF, cpu.getA(), "POP AF Failed: AF was not modified (A=" + Integer.toHexString(cpu.getA()) + ")"),
                () -> assertEquals((byte)0x00, cpu.getF(), "POP AF Failed: AF was not modified (F=" + Integer.toHexString(cpu.getF()) + ")")
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

        cpu.setSP((short)0x0002);
        cpu.setDE((short)0x5678);

        cpu.fetch((byte)0xD5); // PUSH DE

        assertAll("PUSH rp2[p] Group",
                () -> assertEquals((short)0x0000, cpu.getSP(), "PUSH DE Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getSP(), "PUSH DE Failed: PC was NOT modified (SP=" + cpu.getSP() + ")")
        );

        cpu.setSP((short)0x0002);
        cpu.setHL((short)0x5678);

        cpu.fetch((byte)0xE5); // PUSH HL

        assertAll("PUSH rp2[p] Group",
                () -> assertEquals((short)0x0000, cpu.getSP(), "PUSH HL Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getSP(), "PUSH HL Failed: PC was NOT modified (SP=" + cpu.getSP() + ")")
        );

        cpu.setSP((short)0x0002);
        cpu.setA((byte)0x56);
        cpu.setF((byte)0x78);

        cpu.fetch((byte)0xF5); // PUSH AF

        assertAll("PUSH rp2[p] Group",
                () -> assertEquals((short)0x0000, cpu.getSP(), "PUSH AF Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getSP(), "PUSH AF Failed: PC was NOT modified (SP=" + cpu.getSP() + ")")
        );
    }

    @Test
    void testPUSH_IX() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(256));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setSP((short) 0x0100);
        cpu.setIX((short) 0x1234);
        compTest.poke(0x0000, (byte) 0xDD); // DD prefix
        compTest.poke(0x0001, (byte) 0xE5); // PUSH IX
        cpu.fetch(); // DD prefix + PUSH IX

        assertAll("PUSH IX Group",
                () -> assertEquals((short) 0x00FE, cpu.getSP(), "PUSH IX Failed: SP<>0x00FE"),
                () -> assertEquals((byte) 0x34, compTest.peek(0x00FE), "PUSH IX Failed: Low byte not pushed"),
                () -> assertEquals((byte) 0x12, compTest.peek(0x00FF), "PUSH IX Failed: High byte not pushed")
        );
    }

    @Test
    void testPOP_IX() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(256));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setSP((short) 0x00FE);
        cpu.setIX((short) 0x0000);
        compTest.poke(0x00FE, (byte) 0x78); // Low byte
        compTest.poke(0x00FF, (byte) 0x56); // High byte
        compTest.poke(0x0000, (byte) 0xDD); // DD prefix
        compTest.poke(0x0001, (byte) 0xE1); // POP IX
        cpu.fetch(); // DD prefix + POP IX

        assertAll("POP IX Group",
                () -> assertEquals((short) 0x5678, cpu.getIX(), "POP IX Failed: IX<>0x5678"),
                () -> assertEquals((short) 0x0100, cpu.getSP(), "POP IX Failed: SP<>0x0100")
        );
    }

    @Test
    void testPUSH_IY() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(256));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setSP((short) 0x0100);
        cpu.setIY((short) 0x9ABC);
        compTest.poke(0x0000, (byte) 0xFD); // FD prefix
        compTest.poke(0x0001, (byte) 0xE5); // PUSH IY
        cpu.fetch(); // FD prefix + PUSH IY

        assertAll("PUSH IY Group",
                () -> assertEquals((short) 0x00FE, cpu.getSP(), "PUSH IY Failed: SP<>0x00FE"),
                () -> assertEquals((byte) 0xBC, compTest.peek(0x00FE), "PUSH IY Failed: Low byte not pushed"),
                () -> assertEquals((byte) 0x9A, compTest.peek(0x00FF), "PUSH IY Failed: High byte not pushed")
        );
    }

    @Test
    void testPOP_IY() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(256));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setSP((short) 0x00FE);
        cpu.setIY((short) 0x0000);
        compTest.poke(0x00FE, (byte) 0xEF); // Low byte
        compTest.poke(0x00FF, (byte) 0xCD); // High byte
        compTest.poke(0x0000, (byte) 0xFD); // FD prefix
        compTest.poke(0x0001, (byte) 0xE1); // POP IY
        cpu.fetch(); // FD prefix + POP IY

        assertAll("POP IY Group",
                () -> assertEquals((short) 0xCDEF, cpu.getIY(), "POP IY Failed: IY<>0xCDEF"),
                () -> assertEquals((short) 0x0100, cpu.getSP(), "POP IY Failed: SP<>0x0100")
        );
    }
}
