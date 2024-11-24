package com.github.jagarsoft.test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Z80Test {

    @Test
    void testEX_AF_AF_() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte)0x01);
        cpu.setF((byte)0x02);

        cpu.setA_((byte)0x03);
        cpu.setF_((byte)0x04);

        cpu.EX_AF_AF_((byte)0,(byte)0);

        assertEquals(0x03, cpu.getA(), "EX_AF_AF_ Failed (A)");
        assertEquals(0x01, cpu.getA_(), "EX_AF_AF_ Failed (A')");

        assertEquals(0x04, cpu.getF(), "EX_AF_AF_ Failed (F)");
        assertEquals(0x02, cpu.getF_(), "EX_AF_AF_ Failed (F')");
    }
    
    @Test
    void testDJNZ_B_NotZero_MustJumpBackward() {
        Z80ForTesting cpu = new Z80ForTesting();

        
        cpu.setB((byte)0xFF);
        cpu.setPC(0x800A);

        cpu.DJNZ((byte) 0, (byte) 0); // Skip offset
        cpu.DJNZ((byte) 0, (byte) -10);

        assertEquals((byte)0xFE, cpu.getB(), "DJNZ Failed: B<>0 (B=" + cpu.getB() + ")");
        assertEquals(0x8000, cpu.getPC(), "DJNZ Failed: PC was not modified (PC=" + cpu.getPC() + ")");
    }
    
    @Test
    void testDJNZ_B_Zero_MustNotJump() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setB((byte)1);
        cpu.setPC(0x8000);

        cpu.DJNZ((byte) 0, (byte) 0); // Skip offset
        cpu.DJNZ((byte) 0, (byte) -2);

        assertEquals(0, (int)cpu.getB(), "DJNZ Failed: B=0 (B=" + cpu.getB() + ")");
        assertEquals(0x8000, cpu.getPC(), "DJNZ Failed: PC was modified (PC=" + cpu.getPC() + ")");
    }
}
