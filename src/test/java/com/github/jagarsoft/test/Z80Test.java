package com.github.jagarsoft.test;

import com.github.jagarsoft.Computer;
import com.github.jagarsoft.RAMMemory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Z80Test {

    @Test
    void testEX_AF_AF_() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte)0x01);
        cpu.setF((byte)0x02);

        cpu.setA_((byte)0x03);
        cpu.setF_((byte)0x04);

        cpu.EX_AF_AF_((byte)0,(byte)0);

        assertAll("EX_AF_AF' Group",
            () -> assertEquals(0x03, cpu.getA(), "EX_AF_AF_ Failed (A)"),
            () -> assertEquals(0x01, cpu.getA_(), "EX_AF_AF_ Failed (A')"),

            () -> assertEquals(0x04, cpu.getF(), "EX_AF_AF_ Failed (F)"),
            () -> assertEquals(0x02, cpu.getF_(), "EX_AF_AF_ Failed (F')")
        );
    }
    
    @Test
    void testDJNZ_B_NotZero_MustJumpBackward() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0001, (byte)-1);

        
        cpu.setB((byte)0xFF);
        cpu.setPC(0x0001);

        cpu.DJNZ((byte) 0, (byte) 0);

        assertAll("DJNZ Group",
            () -> assertEquals((byte)0xFE, cpu.getB(), "DJNZ Failed: B<>0 (B=" + cpu.getB() + ")"),
            () -> assertEquals(0x0001, cpu.getPC(), "DJNZ Failed: PC was not modified (PC=" + cpu.getPC() + ")")
        );
    }
    
    @Test
    void testDJNZ_B_Zero_MustNotJump() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0001, (byte)-10);

        cpu.setB((byte)1);
        cpu.setPC(0x0001);

        cpu.DJNZ((byte) 0, (byte) 0);

        assertAll("DJNZ Group",
            () -> assertEquals(0, (int)cpu.getB(), "DJNZ Failed: B=0 (B=" + cpu.getB() + ")"),
            () -> assertEquals(0x0002, cpu.getPC(), "DJNZ Failed: PC was modified (PC=" + cpu.getPC() + ")")
        );
    }

    @Test
    void testJR_MustJumpBackward() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0001, (byte)-0x0001);

        cpu.setPC(0x0001);

        cpu.JR((byte) 0, (byte) 0);

        assertEquals(0x0001, cpu.getPC(), "JR Failed: PC was not modified (PC=" + cpu.getPC() + ")");
    }

    @Test
    void testJR_cc_MustJumpBackward() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0001, (byte)-1);

        cpu.setPC(0x0001);
        cpu.resZF();

        cpu.JR_cc((byte) 4, (byte) 0); // NZ

        assertEquals(0x0001, cpu.getPC(), "JR NZ Failed: PC was not modified (PC=" + cpu.getPC() + ")");

        cpu.setPC(0x0001);
        cpu.setZF();

        cpu.JR_cc((byte) 5, (byte) 0); // Z

        assertEquals(0x0001, cpu.getPC(), "JR Z Failed: PC was not modified (PC=" + cpu.getPC() + ")");

        cpu.setPC(0x0001);
        cpu.resCF();

        cpu.JR_cc((byte) 6, (byte) 0); // NC

        assertEquals(0x0001, cpu.getPC(), "JR NC Failed: PC was not modified (PC=" + cpu.getPC() + ")");

        cpu.setPC(0x0001);
        cpu.setCF();

        cpu.JR_cc((byte) 7, (byte) 0); // C

        assertEquals(0x0001, cpu.getPC(), "JR C Failed: PC was not modified (PC=" + cpu.getPC() + ")");
    }

    @Test
    void testJR_cc_MustNotJump() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0001, (byte)-0x0001);

        cpu.setPC(0x0001);
        cpu.setZF();

        cpu.JR_cc((byte) 4, (byte) 0); // NZ

        assertEquals(0x0002, cpu.getPC(), "JR NZ Failed: PC was modified (PC=" + cpu.getPC() + ")");

        cpu.setPC(0x0001);
        cpu.resZF();

        cpu.JR_cc((byte) 5, (byte) 0); // Z

        assertEquals(0x0002, cpu.getPC(), "JR Z Failed: PC was modified (PC=" + cpu.getPC() + ")");

        cpu.setPC(0x0001);
        cpu.setCF();

        cpu.JR_cc((byte) 6, (byte) 0); // NC

        assertEquals(0x0002, cpu.getPC(), "JR NC Failed: PC was modified (PC=" + cpu.getPC() + ")");

        cpu.setPC(0x0001);
        cpu.resCF();

        cpu.JR_cc((byte) 7, (byte) 0); // C

        assertEquals(0x0002, cpu.getPC(), "JR C Failed: PC was modified (PC=" + cpu.getPC() + ")");
    }
    
    @Test
    void testLD_BC_A(){
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(new RAMMemory(1));
        cpu.setComputer(compTest);
        compTest.poke(0, (byte)0x80);
        
        cpu.setBC((short)0);
        cpu.setA((byte)0xFF);
        
        cpu.LD_BC_A((byte) 0, (byte)0);
        
        assertAll("LD (BC), A Group",
            () -> assertEquals((byte)0xFF, (byte)compTest.peek(0) , "LD (BC), A Failed: (BC)<>0xFF ((BC)=" + compTest.peek(0) + ")"),
            () -> assertNotEquals((byte)0x80, (byte)compTest.peek(0) , "LD (BC), A Failed: (BC) still 0x80 ((BC)=" + compTest.peek(0) + ")")
        );
    }
    
    @Test
    void testLD_DE_A(){
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(new RAMMemory(1));
        cpu.setComputer(compTest);
        compTest.poke(0, (byte)0x80);
        
        cpu.setDE((short)0);
        cpu.setA((byte)0xFF);
        
        cpu.LD_DE_A((byte) 0, (byte)0);
        
        assertAll("LD (DE), A Group",
            () -> assertEquals((byte)0xFF, (byte)compTest.peek(0) , "LD (DE), A Failed: (DE)<>0xFF ((DE)=" + compTest.peek(0) + ")"),
            () -> assertNotEquals((byte)0x80, (byte)compTest.peek(0) , "LD (DE), A Failed: (DE) still 0x80 ((DE)=" + compTest.peek(0) + ")")
        );
    }

    @Test
    void testLD_nn_HL(){
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte)0x02);
        compTest.poke(0x0001, (byte)0x00);
        compTest.poke(0x0002, (byte)0x56);
        compTest.poke(0x0003, (byte)0x78);

        cpu.setPC(0);
        cpu.setHL((short)0x1234);

        cpu.LD_nn_HL((byte) 0, (byte)0); // LD (0x0002), HL

        assertAll("LD_nn_HL Group",
            () -> assertEquals((byte)0x34, (byte)compTest.peek(2) , "LD (0x0002), HL Failed: (0x0002)<>0x34=" + Integer.toHexString(compTest.peek(2)) ),
            () -> assertNotEquals((byte)0x56, (byte)compTest.peek(2) , "LD (0x002), HL Failed: (0x0002) still 0x56=" + Integer.toHexString(compTest.peek(2)) ),
            () -> assertEquals((byte)0x12, (byte)compTest.peek(3) , "LD (0x0002), HL Failed: (0x0003)<>0x12" + Integer.toHexString(compTest.peek(3)) ),
            () -> assertNotEquals((byte)0x78, (byte)compTest.peek(3) , "LD (0x0002), HL Failed: (0x0003) still 0x78=" + Integer.toHexString(compTest.peek(3)) ),
            () -> assertEquals((byte)0x0002, cpu.getPC() , "LD (0x0002), HL Failed: PC no was modified PC<>0x0002 PC=" + Integer.toHexString(cpu.getPC()) )
        );
    }
}
