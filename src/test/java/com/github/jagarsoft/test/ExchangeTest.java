package com.github.jagarsoft.test;

import com.github.jagarsoft.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExchangeTest {
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

        cpu.setA((byte) 0x00);
        cpu.setF((byte) 0x00); // this case tests a bug implementation in BitSet where all bits are clear

        cpu.setA_((byte) 0x00);
        cpu.setF_((byte) 0x00);

        cpu.EX_AF_AF_();

        assertAll("EX_AF_AF' Group",
                () -> assertEquals(0x00, cpu.getA(), "EX_AF_AF_ Failed (A)"),
                () -> assertEquals(0x00, cpu.getA_(), "EX_AF_AF_ Failed (A')"),

                () -> assertEquals(0x00, cpu.getF(), "EX_AF_AF_ Failed (F)"),
                () -> assertEquals(0x00, cpu.getF_(), "EX_AF_AF_ Failed (F')")
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
    void testEX_SP_xx() {
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
                () -> assertEquals((short)0x0FF00, cpu.getHL(), "EX (SP), HL Failed HL="+ Integer.toHexString(cpu.getHL())),
                () -> assertEquals((byte) 0x34, compTest.peek(0x0000), "EX (SP), HL Failed (SP)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertEquals((byte) 0x12, compTest.peek(0x0001), "EX (SP), HL Failed (SP+1)="+ Integer.toHexString(compTest.peek(0x0001))),

                () -> assertNotEquals((short)0x1234, cpu.getHL(), "EX (SP), HL Failed HL="+ Integer.toHexString(cpu.getHL())),
                () -> assertNotEquals((byte) 0x00, compTest.peek(0x0000), "EX (SP), HL Failed (SP)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertNotEquals((byte) 0xFF, compTest.peek(0x0001), "EX (SP), HL Failed (SP+1)="+ Integer.toHexString(compTest.peek(0x0001)))
        );


        compTest.poke(0x0000, (byte) 0);
        compTest.poke(0x0001, (byte) 0xFF);

        cpu.setIX((short) 0x1234);

        cpu.EX_SP_IX();

        assertAll("EX (SP), IX Group",
                () -> assertEquals((short)0x0FF00, cpu.getIX(), "EX (SP), IX Failed IX="+ Integer.toHexString(cpu.getIX())),
                () -> assertEquals((byte) 0x34, compTest.peek(0x0000), "EX (SP), IX Failed (SP)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertEquals((byte) 0x12, compTest.peek(0x0001), "EX (SP), IX Failed (SP+1)="+ Integer.toHexString(compTest.peek(0x0001))),

                () -> assertNotEquals((short)0x1234, cpu.getIX(), "EX (SP), IX Failed IX="+ Integer.toHexString(cpu.getIX())),
                () -> assertNotEquals((byte) 0x00, compTest.peek(0x0000), "EX (SP), HL Failed (SP)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertNotEquals((byte) 0xFF, compTest.peek(0x0001), "EX (SP), HL Failed (SP+1)="+ Integer.toHexString(compTest.peek(0x0001)))
        );

        compTest.poke(0x0000, (byte) 0);
        compTest.poke(0x0001, (byte) 0xFF);

        cpu.setIY((short) 0x1234);

        cpu.EX_SP_IY();

        assertAll("EX (SP), IY Group",
                () -> assertEquals((short)0x0FF00, cpu.getIY(), "EX (SP), IY Failed IX="+ Integer.toHexString(cpu.getIY())),
                () -> assertEquals((byte) 0x34, compTest.peek(0x0000), "EX (SP), IY Failed (SP)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertEquals((byte) 0x12, compTest.peek(0x0001), "EX (SP), IY Failed (SP+1)="+ Integer.toHexString(compTest.peek(0x0001))),

                () -> assertNotEquals((short)0x1234, cpu.getIX(), "EX (SP), IY Failed IY="+ Integer.toHexString(cpu.getIY())),
                () -> assertNotEquals((byte) 0x00, compTest.peek(0x0000), "EX (SP), IY Failed (SP)="+ Integer.toHexString(compTest.peek(0x0000))),
                () -> assertNotEquals((byte) 0xFF, compTest.peek(0x0001), "EX (SP), IY Failed (SP+1)="+ Integer.toHexString(compTest.peek(0x0001)))
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
}
