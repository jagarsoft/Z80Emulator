package com.github.jagarsoft.test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.jagarsoft.Z80;

public class Z80Test {

    @Test
    void testEX_AF_AF_() {
        Z80 cpu = new Z80();

        cpu.setA((byte)0x01);
        cpu.setF((byte)0x02);

        cpu.setA_((byte)0x03);
        cpu.setF_((byte)0x04);

        cpu.EX_AF_AF_((byte)0,(byte)0);

        assertEquals(0x03, cpu.getA(), "EX_AF_AF_ Failed (A)");
        assertEquals(0x01, cpu.getA_(), "EX_AF_AF_ Failed (A')");

        assertEquals(0x04, cpu.getF(), "EX_AF_AF_ Failed (F)");
        assertEquals(0x02, cpu.getF_(), "EX_AF_AF_ Failed (F')");
        /*System.out.println("A:"+String.format("0x%02X", cpu.getA()));
        System.out.println("F:"+String.format("0x%02X", cpu.getF()));
        System.out.println("A_:"+String.format("0x%02X", cpu.getA_()));
        System.out.println("F_:"+String.format("0x%02X", cpu.getF_()));*/
    }
}
