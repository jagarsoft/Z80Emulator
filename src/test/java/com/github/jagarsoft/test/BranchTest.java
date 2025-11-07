package com.github.jagarsoft.test;

import com.github.jagarsoft.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BranchTest {
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
                () -> assertEquals(0x0FF00, cpu.getPC(), "CALL 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "CALL 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),

                () -> assertEquals((short)0x0000, cpu.getSP(), "CALL 0xFF00 Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getSP(), "CALL 0xFF00 Failed: SP was NOT modified (SP=" + cpu.getSP() + ")"),

                () -> assertEquals((byte) 0x00, compTest.peek(0x0001), "CALL 0xFF00 Failed (SP+1)="+ Integer.toHexString(compTest.peek(0x0001))),
                () -> assertEquals((byte) 0x04, compTest.peek(0x0000), "CALL 0xFF00 Failed (SP)="+ Integer.toHexString(compTest.peek(0x0000))),

                () -> assertNotEquals((byte) 0x12, compTest.peek(0x0001), "CALL 0xFF00 Failed (SP+1)="+ Integer.toHexString(compTest.peek(0x0001))),
                () -> assertNotEquals((byte) 0x34, compTest.peek(0x0000), "CALL 0xFF00 Failed (SP)="+ Integer.toHexString(compTest.peek(0x0000)))
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
                () -> assertEquals(0x0FF00, cpu.getPC(), "CALL NZ, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals(0x0002, cpu.getPC(), "CALL NZ, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),

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
                () -> assertEquals(0x0FF00, cpu.getPC(), "CALL Z, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
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
                () -> assertEquals(0x0FF00, cpu.getPC(), "CALL NC, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
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
                () -> assertEquals(0x0FF00, cpu.getPC(), "CALL C, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
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
                () -> assertEquals(0x0FF00, cpu.getPC(), "CALL PO, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
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
                () -> assertEquals(0x0FF00, cpu.getPC(), "CALL PE, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
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
                () -> assertEquals(0x0FF00, cpu.getPC(), "CALL P, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
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
                () -> assertEquals(0x0FF00, cpu.getPC(), "CALL M, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
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
                () -> assertNotEquals(0x0FF00, cpu.getPC(), "CALL NZ, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.resZF();

        cpu.fetch((byte) 0xCC); // CALL Z, 0xFF00

        assertAll("CALL cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "CALL Z, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals(0x0FF00, cpu.getPC(), "CALL Z, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.setCF();

        cpu.fetch((byte) 0xD4); // CALL NC, 0xFF00

        assertAll("CALL cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "CALL NC, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals(0x0FF00, cpu.getPC(), "CALL NC, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.resCF();

        cpu.fetch((byte) 0xDC); // CALL C, 0xFF00

        assertAll("CALL cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "CALL C, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals(0x0FF00, cpu.getPC(), "CALL C, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.setPF();

        cpu.fetch((byte) 0xE4); // CALL PO, 0xFF00

        assertAll("CALL cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "CALL PO, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals(0x0FF00, cpu.getPC(), "CALL PO, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.resPF();

        cpu.fetch((byte) 0xEC); // CALL PE, 0xFF00

        assertAll("CALL cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "CALL PE, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals(0x0FF00, cpu.getPC(), "CALL PE, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.setSF();

        cpu.fetch((byte) 0xF4); // CALL P, 0xFF00

        assertAll("CALL cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "CALL P, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals(0x0FF00, cpu.getPC(), "CALL P, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.resSF();

        cpu.fetch((byte) 0xFC); // CALL M, 0xFF00

        assertAll("CALL cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "CALL M, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals(0x0FF00, cpu.getPC(), "CALL M, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
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
                () -> assertEquals(0x0FF00, cpu.getPC(), "JP NZ, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "JP NZ, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.setZF();

        cpu.fetch((byte)0xCA); // JP Z, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals(0x0FF00, cpu.getPC(), "JP Z, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "JP Z, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.resCF();

        cpu.fetch((byte)0xD2); // JP NC, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals(0x0FF00, cpu.getPC(), "JP NC, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "JP NC, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.setCF();

        cpu.fetch((byte)0xDA); // JP C, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals(0x0FF00, cpu.getPC(), "JP C, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "JP C, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.resPF();

        cpu.fetch((byte)0xE2); // JP PO, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals(0x0FF00, cpu.getPC(), "JP PO, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "JP PO, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.setPF();

        cpu.fetch((byte)0xEA); // JP PE, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals(0x0FF00, cpu.getPC(), "JP PE, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "JP PE, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.resSF();

        cpu.fetch((byte)0xF2); // JP P, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals(0x0FF00, cpu.getPC(), "JP P, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "JP P, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.setSF();

        cpu.fetch((byte)0xFA); // JP M, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals(0x0FF00, cpu.getPC(), "JP M, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
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
                () -> assertEquals(0x0FF00, cpu.getPC(), "JP 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals((short)0x0002, cpu.getPC(), "JP 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
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
                () -> assertNotEquals(0x0FF00, cpu.getPC(), "JP NZ, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.resZF();

        cpu.fetch((byte)0xCA); // JP Z, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "JP Z, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals(0x0FF00, cpu.getPC(), "JP Z, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.setCF();

        cpu.fetch((byte)0xD2); // JP NC, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "JP NC, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals(0x0FF00, cpu.getPC(), "JP NC, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.resCF();

        cpu.fetch((byte)0xDA); // JP C, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "JP C, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals(0x0FF00, cpu.getPC(), "JP C, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.setPF();

        cpu.fetch((byte)0xE2); // JP PO, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "JP PO, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals(0x0FF00, cpu.getPC(), "JP PO, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.resPF();

        cpu.fetch((byte)0xEA); // JP PE, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "JP PE, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals(0x0FF00, cpu.getPC(), "JP PE, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.setSF();

        cpu.fetch((byte)0xF2); // JP P, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "JP P, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals(0x0FF00, cpu.getPC(), "JP P, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );

        cpu.setPC(0x0000);

        cpu.resSF();

        cpu.fetch((byte)0xFA); // JP M, 0xFF00

        assertAll("JP cc[y], nn Group",
                () -> assertEquals((short)0x0002, cpu.getPC(), "JP M, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals(0x0FF00, cpu.getPC(), "JP M, 0xFF00 Failed: PC was modified (PC=" + cpu.getPC() + ")")
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
    void testJP_IX() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(16384));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setIX((short) 0x2000);
        compTest.poke(0x0000, (byte) 0xDD); // DD prefix
        compTest.poke(0x0001, (byte) 0xE9); // JP (IX)
        cpu.fetch(); // DD prefix + JP (IX)

        assertAll("JP (IX) Group",
                () -> assertEquals((short) 0x2000, cpu.getPC(), "JP (IX) Failed: PC<>0x2000")
        );
    }

    @Test
    void testJP_IY() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(16384));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setIY((short) 0x3000);
        compTest.poke(0x0000, (byte) 0xFD); // FD prefix
        compTest.poke(0x0001, (byte) 0xE9); // JP (IY)
        cpu.fetch(); // FD prefix + JP (IY)

        assertAll("JP (IY) Group",
                () -> assertEquals((short) 0x3000, cpu.getPC(), "JP (IY) Failed: PC<>0x3000")
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
                () -> assertEquals(0x0FF00, cpu.getPC(), "RET Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals(0x0000, cpu.getPC(), "RET Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

                () -> assertEquals(0x0002, cpu.getSP(), "RET NZ Failed: SP was modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals(0x0000, cpu.getSP(), "RET NZ Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );
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
                () -> assertEquals(0x0FF00, cpu.getPC(), "RET NZ Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals(0x0000, cpu.getPC(), "RET NZ Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

                () -> assertEquals(0x0002, cpu.getSP(), "RET NZ Failed: SP was modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals(0x0000, cpu.getSP(), "RET NZ Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setSP((short) 0x0000);

        cpu.setZF();

        cpu.fetch((byte) 0xC8); // RET Z

        assertAll("RET cc[y] Group",
                () -> assertEquals(0x0FF00, cpu.getPC(), "RET Z Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals(0x0000, cpu.getPC(), "RET Z Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

                () -> assertEquals(0x0002, cpu.getSP(), "RET Z Failed: SP was modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals(0x0000, cpu.getSP(), "RET Z Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setSP((short) 0x0000);

        cpu.resCF();

        cpu.fetch((byte) 0xD0); // RET NC

        assertAll("RET cc[y] Group",
                () -> assertEquals(0x0FF00, cpu.getPC(), "RET NC Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals(0x0000, cpu.getPC(), "RET NC Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

                () -> assertEquals(0x0002, cpu.getSP(), "RET NC Failed: SP was modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals(0x0000, cpu.getSP(), "RET NC Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setSP((short) 0x0000);

        cpu.setCF();

        cpu.fetch((byte) 0xD8); // RET C

        assertAll("RET cc[y] Group",
                () -> assertEquals(0x0FF00, cpu.getPC(), "RET C Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals(0x0000, cpu.getPC(), "RET C Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

                () -> assertEquals(0x0002, cpu.getSP(), "RET C Failed: SP was modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals(0x000, cpu.getSP(), "RET C Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setSP((short) 0x0000);

        cpu.resPF();

        cpu.fetch((byte) 0xE0); // RET PO

        assertAll("RET cc[y] Group",
                () -> assertEquals(0x0FF00, cpu.getPC(), "RET PO Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals(0x0000, cpu.getPC(), "RET PO Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

                () -> assertEquals(0x0002, cpu.getSP(), "RET PO Failed: SP was modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals(0x0000, cpu.getSP(), "RET PO Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setSP((short) 0x0000);

        cpu.setPF();

        cpu.fetch((byte) 0xE8); // RET PE

        assertAll("RET cc[y] Group",
                () -> assertEquals(0x0FF00, cpu.getPC(), "RET PE Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals(0x0000, cpu.getPC(), "RET PE Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

                () -> assertEquals((short)0x0002, cpu.getSP(), "RET PE Failed: SP was modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals(0x0000, cpu.getSP(), "RET PE Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setSP((short) 0x0000);

        cpu.resSF();

        cpu.fetch((byte) 0xF0); // RET P

        assertAll("RET cc[y] Group",
                () -> assertEquals(0x0FF00, cpu.getPC(), "RET P Failed: PC was modified (PC=" + cpu.getPC() + ")"),
                () -> assertNotEquals(0x0000, cpu.getPC(), "RET P Failed: PC was not pop (PC=" + cpu.getPC() + ")"),

                () -> assertEquals(0x0002, cpu.getSP(), "RET P Failed: SP was modified (SP=" + cpu.getSP() + ")"),
                () -> assertNotEquals(0x0000, cpu.getSP(), "RET P Failed: SP was pop (SP=" + cpu.getSP() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setSP((short) 0x0000);

        cpu.setSF();

        cpu.fetch((byte) 0xF8); // RET M

        assertAll("RET cc[y] Group",
                () -> assertEquals(0x0FF00, cpu.getPC(), "RET M Failed: PC was modified (PC=" + cpu.getPC() + ")"),
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
    void testRETI() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(256));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setSP((short) 0x00FE);
        compTest.poke(0x00FF, (byte) 0x12); // Return address low
        compTest.poke(0x00FE, (byte) 0x34); // Return address high
        compTest.poke(0x0000, (byte) 0xED); // ED prefix
        compTest.poke(0x0001, (byte) 0x4D); // RETI
        cpu.fetch(); // ED prefix + RETI

        assertAll("RETI Group",
                () -> assertEquals((short) 0x1234, cpu.getPC(), "RETI Failed: PC<>0x1234"),
                () -> assertEquals((short) 0x0100, cpu.getSP(), "RETI Failed: SP<>0x0100")
        );
    }

    @Test
    void testRETN() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(256));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setSP((short) 0x00FE);
        compTest.poke(0x00FF, (byte) 0x78); // Return address low
        compTest.poke(0x00FE, (byte) 0x56); // Return address high
        compTest.poke(0x0000, (byte) 0xED); // ED prefix
        compTest.poke(0x0001, (byte) 0x45); // RETN
        cpu.fetch(); // ED prefix + RETN

        assertAll("RETN Group",
                () -> assertEquals((short) 0x7856, cpu.getPC(), "RETN Failed: PC<>0x7856"),
                () -> assertEquals((short) 0x0100, cpu.getSP(), "RETN Failed: SP<>0x0100")
        );
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
