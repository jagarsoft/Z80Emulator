package com.github.jagarsoft.test;

import com.github.jagarsoft.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.*;

public class ArithmeticTest {
/*
| HL     | rr     | Result | YF | H | XF | N | C | What test?  | Cases
| ------ | ------ | ------ | -- | - | -- | - | - | ----------- | -----
| 0x1234 | 0x0001 | 0x1235 |  0 | 0 |  0 | 0 | 0 | Clean case  | Case 1
| 0x0FFF | 0x0001 | 0x1000 |  0 | 1 |  0 | 0 | 0 | half carry  | Case 2
| 0xFFFF | 0x0001 | 0x0000 |  1 | 1 |  1 | 0 | 1 | carry       | Case 3
*/
    @Test
    void testADD_HL_rp_p() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setHL((short)0x1234);
        cpu.setBC((short)0x0001);
        cpu.setDE((short)0x0001);
        cpu.setSP((short)0x0001);
        cpu.setF((byte) 0xFF); // All flags set to test they're not affected

        long initTState = cpu.getTState();
        cpu.fetch((byte)0x09); // ADD HL, BC

        assertAll("ADD HL, rp_p Group",
                () -> assertEquals((short) 0x1235, cpu.getHL(), "ADD HL, BC Failed: HL<>0x1235 HL=" + Integer.toHexString(cpu.getHL())),
                () -> assertNotEquals((short) 0x1234, cpu.getHL(), "ADD HL, BC Failed: HL Still 0x1234 HL=" + Integer.toHexString(cpu.getHL())),

                () -> assertTrue(cpu.getSF(), "Sign flag was affected (S=" + cpu.getSF() + ")"),
                () -> assertTrue(cpu.getZF(), "Zero flag was affected (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF must be OFF (YF=" + cpu.getYF() + ")"),
                () -> assertFalse(cpu.getHF(), "Half-carry must be OFF"),
                () -> assertFalse(cpu.getXF(), "XF must be OFF (XF=" + cpu.getXF() + ")"),
                () -> assertTrue(cpu.getVF(), "Overflow was affected (VF=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getNF(), "N must be OFF"),
                () -> assertFalse(cpu.getCF(), "Carry must be OFF"),

                () -> assertEquals(11, cpu.getTState()-initTState, "ADD HL, BC TState Failed")
        );

        cpu.setHL((short)0x0FFF);
        cpu.setF((byte) 0xFF); // All flags set to test they're not affected
        cpu.fetch((byte)0x19); // ADD HL, DE

        assertAll("ADD HL, rp_p Group",
                () -> assertEquals((short) 0x1000, cpu.getHL(), "ADD HL, DE Failed: HL<>0x0001 HL=" + Integer.toHexString(cpu.getHL())),
                () -> assertNotEquals((short) 0x0FFF, cpu.getHL(), "ADD HL, DE Failed: HL Still 0x0FFF HL=" + Integer.toHexString(cpu.getHL())),

                () -> assertTrue(cpu.getSF(), "Sign flag was affected (S=" + cpu.getSF() + ")"),
                () -> assertTrue(cpu.getZF(), "Zero flag was affected (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF must be OFF (YF=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Half-carry must be ON"),
                () -> assertFalse(cpu.getXF(), "XF must be OFF (XF=" + cpu.getXF() + ")"),
                () -> assertTrue(cpu.getVF(), "Overflow was affected (VF=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getNF(), "N must be OFF"),
                () -> assertFalse(cpu.getCF(), "Carry must be OFF")
        );

        /*        cpu.fetch((byte)0x29); // ADD HL, HL
        assertEquals((short) 0x246E, cpu.getHL(), "ADD HL, HL Failed: HL<>0x246E HL=" + Integer.toHexString(cpu.getHL()));
        assertNotEquals((short) 0x1237, cpu.getHL(), "ADD HL, HL Failed: HL Still 0x1237 HL=" + Integer.toHexString(cpu.getHL()));
        assertEquals((short) 0x256D, cpu.getHL(), "ADD HL, SP Failed: HL<>0x256D HL=" + Integer.toHexString(cpu.getHL()));
        assertNotEquals((short) 0x246E, cpu.getHL(), "ADD HL, SP Failed: HL Still 0x246E HL=" + Integer.toHexString(cpu.getHL()));
*/
        cpu.setHL((short)0xFFFF);
        cpu.setF((byte) 0xFF); // All flags set to test they're not affected

        cpu.fetch((byte)0x39); // ADD HL, SP

        assertAll(
                () -> assertEquals((short)0x0000, cpu.getHL(), "ADD HL, SP Failed: HL<>0x0000"+Integer.toHexString(cpu.getHL())),
                () -> assertNotEquals((short)0xFFFF, cpu.getHL(), "ADD HL, SP Failed: HL Still 0xFFFF HL="+Integer.toHexString(cpu.getHL())),

                () -> assertTrue(cpu.getSF(), "Sign flag was affected (S=" + cpu.getSF() + ")"),
                () -> assertTrue(cpu.getZF(), "Zero flag was affected (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF must be OFF (YF=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Half-carry must be ON"),
                () -> assertFalse(cpu.getXF(), "XF must be OFF (XF=" + cpu.getXF() + ")"),
                () -> assertTrue(cpu.getVF(), "Overflow was affected (VF=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getNF(), "N must be OFF"),
                () -> assertTrue(cpu.getCF(), "Carry must be ON")
        );
    }


/*
| HL     | rr     | C | Result | S | Z | YF | H | XF | p/V | N | C | What test?     | Cases
| ------ | ------ | - | ------ | - | - | -- | - | -- | --- | _ | - | -------------- | -----
| 0x7FFF | 0x0001 | 0 | 0x8000 | 1 | 0 | 0  | 1 | 0  |  1  | 0 | 0 | overflow (+→−) | Case 1
| 0x8000 | 0x8000 | 0 | 0x0000 | 0 | 1 | 0  | 0 | 0  |  1  | 0 | 1 | overflow (−→+) | Case 2
| 0x0FFF | 0x0001 | 0 | 0x1000 | 0 | 0 | 0  | 1 | 0  |  0  | 0 | 0 | half carry     | Case 3
| 0x0000 | 0x0000 | 1 | 0x0001 | 0 | 0 | 0  | 0 | 0  |  0  | 0 | 0 | carry          | Case 4
| 0x2800 | 0x0000 | 0 | 0x2800 | 0 | 0 | 1  | 0 | 1  |  0  | 0 | 0 | YF/XF          | Case 5
*/

    @Test
    void testADC_HL_rp_p() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xED);
        compTest.poke(0x0001, (byte) 0x4A); // ADC HL, BC

        cpu.setPC(0x0000);
        cpu.setHL((short)0x7FFF);
        cpu.setBC((short)0x0001);
        cpu.resCF();

        long initTState = cpu.getTState();
        cpu.fetch();

        assertAll("ADC HL, rp_p Group",
            () -> assertEquals((short) 0x8000, cpu.getHL(), "ADC HL, BC Failed: HL<>0x8000 HL=" + Integer.toHexString(cpu.getHL())),
            () -> assertNotEquals((short) 0x7FFF, cpu.getHL(), "ADC HL, BC Failed: HL Still 0x7FFF HL=" + Integer.toHexString(cpu.getHL())),

            () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
            () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
            () -> assertFalse(cpu.getYF(), "YF must be OFF (YF=" + cpu.getYF() + ")"),
            () -> assertTrue(cpu.getHF(), "Half-carry must be ON (HF=" + cpu.getHF() + ")"),
            () -> assertFalse(cpu.getXF(), "XF must be OFF (XF=" + cpu.getXF() + ")"),
            () -> assertTrue(cpu.getVF(), "Overflow must be ON (VF=" + cpu.getVF() + ")"),
            () -> assertFalse(cpu.getNF(), "N must be OFF (NF=" + cpu.getNF() + ")"),
            () -> assertFalse(cpu.getCF(), "Carry must be OFF (CF=" + cpu.getCF() + ")"),

            () -> assertEquals(15, cpu.getTState()-initTState, "ADC HL, BC TState Failed")
        );

        compTest.poke(0x0001, (byte) 0x6A); // ADC HL, HL
        cpu.setHL((short)0x8000);
        cpu.setPC(0x0000);
        cpu.resCF();

        cpu.fetch();

        assertAll("ADC HL, rp_p Group",
                () -> assertEquals((short) 0x0000, cpu.getHL(), "ADC HL, HL Failed: HL<>0x0000 HL=" + Integer.toHexString(cpu.getHL())),
                () -> assertNotEquals((short) 0x8000, cpu.getHL(), "ADC HL, HL Failed: HL Still 0x8000 HL=" + Integer.toHexString(cpu.getHL())),

                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF must be OFF (YF=" + cpu.getYF() + ")"),
                () -> assertFalse(cpu.getHF(), "Half-carry must be OFF (HF=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getXF(), "XF must be OFF (XF=" + cpu.getXF() + ")"),
                () -> assertTrue(cpu.getVF(), "Overflow must be ON (VF=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getNF(), "N must be OFF (NF=" + cpu.getNF() + ")"),
                () -> assertTrue(cpu.getCF(), "Carry must be ON (CF=" + cpu.getCF() + ")")
        );


        compTest.poke(0x0001, (byte) 0x5A); // ADC HL, DE
        cpu.setHL((short) 0x0FFF);
        cpu.setDE((short) 0x0001);
        cpu.setPC(0x0000);
        cpu.resCF();

        cpu.fetch();

        assertAll("ADC HL, rp_p Group",
                () -> assertEquals((short) 0x1000, cpu.getHL(), "ADC HL, HL Failed: HL<>0x1000 HL=" + Integer.toHexString(cpu.getHL())),
                () -> assertNotEquals((short) 0x0FFF, cpu.getHL(), "ADC HL, HL Failed: HL Still 0x0FFF HL=" + Integer.toHexString(cpu.getHL())),

                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF must be OFF (YF=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Half-carry must be ON (HF=\" + cpu.getHF() + \")\"),"),
                () -> assertFalse(cpu.getXF(), "XF must be OFF (XF=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow must be OFF (VF=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getNF(), "N must be OFF (NF=" + cpu.getNF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry must be ON (CF=" + cpu.getCF() + ")")
        );

        compTest.poke(0x0001, (byte) 0x7A); // ADC HL, SP
        cpu.setHL((short)0x0000);
        cpu.setSP((short)0x0000);
        cpu.setPC(0x0000);
        cpu.setCF();

        cpu.fetch();

        assertAll("ADC HL, rp_p Group",
                () -> assertEquals((short) 0x0001, cpu.getHL(), "ADC HL, SP Failed: HL<>0x0001 HL=" + Integer.toHexString(cpu.getHL())),
                () -> assertNotEquals((short) 0x0000, cpu.getHL(), "ADC HL, SP Failed: HL Still 0x0000 HL=" + Integer.toHexString(cpu.getHL())),

                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF must be OFF (YF=" + cpu.getYF() + ")"),
                () -> assertFalse(cpu.getHF(), "Half-carry must be ON (HF=\" + cpu.getHF() + \")\"),"),
                () -> assertFalse(cpu.getXF(), "XF must be OFF (XF=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow must be OFF (VF=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getNF(), "N must be OFF (NF=" + cpu.getNF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry must be ON (CF=" + cpu.getCF() + ")")
        );

        compTest.poke(0x0001, (byte) 0x7A); // ADC HL, SP
        cpu.setHL((short)0x2800);
        cpu.setSP((short)0x0000);
        cpu.setPC(0x0000);
        cpu.resCF();

        cpu.fetch();

        assertAll("ADC HL, rp_p Group",
                () -> assertEquals((short) 0x2800, cpu.getHL(), "ADC HL, SP Failed: HL<>0x2800 HL=" + Integer.toHexString(cpu.getHL())),

                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getYF(), "YF must be ON (YF=" + cpu.getYF() + ")"),
                () -> assertFalse(cpu.getHF(), "Half-carry must be ON (HF=\" + cpu.getHF() + \")\"),"),
                () -> assertTrue(cpu.getXF(), "XF must be ON (XF=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow must be OFF (VF=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getNF(), "N must be OFF (NF=" + cpu.getNF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry must be OFF (CF=" + cpu.getCF() + ")")
        );
    }

    @Test
    void testADD_IX_rp_p() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xDD);
        compTest.poke(0x0001, (byte) 0x09); // ADD IX, BC

        cpu.setPC(0x0000);

        cpu.setIX((short)0x1234);
        cpu.setBC((short)0x0001);

        long initTState = cpu.getTState();
        cpu.fetch();

        assertAll("ADD IX, rp_p Group",
                () -> assertEquals((short) 0x1235, cpu.getIX(), "ADD IX, BC Failed: IX<>0x1235 IX=" + Integer.toHexString(cpu.getIX())),
                () -> assertNotEquals((short) 0x1234, cpu.getIX(), "ADD IX, BC Failed: IX Still 0x1234 IX=" + Integer.toHexString(cpu.getIX())),

                //() -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                //() -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF must be OFF (YF=" + cpu.getYF() + ")"),
                () -> assertFalse(cpu.getHF(), "Half-carry must be OFF (HF=\" + cpu.getHF() + \")\"),"),
                () -> assertFalse(cpu.getXF(), "XF must be OFF (XF=" + cpu.getXF() + ")"),
                //() -> assertFalse(cpu.getVF(), "Overflow must be OFF (VF=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getNF(), "N must be OFF (NF=" + cpu.getNF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry must be OFF (CF=" + cpu.getCF() + ")"),

                () -> assertEquals(15, cpu.getTState()-initTState, "ADD IX, BC TState Failed")
        );

        compTest.poke(0x0001, (byte) 0x19);
        cpu.setIX((short)0x0FFF);
        cpu.setDE((short)0x0001);
        cpu.setPC(0x0000);

        cpu.fetch(); // ADD IX, DE

        assertAll("ADD IX, rp_p Group",
                () -> assertEquals((short) 0x1000, cpu.getIX(), "ADD IX, DE Failed: IX<>0x1000 IX=" + Integer.toHexString(cpu.getIX())),
                () -> assertNotEquals((short) 0x0FFF, cpu.getIX(), "ADD IX, DE Failed: IX Still 0x0FFF IX=" + Integer.toHexString(cpu.getIX())),

                //() -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                //() -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF must be OFF (YF=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Half-carry must be ON (HF=\" + cpu.getHF() + \")\"),"),
                () -> assertFalse(cpu.getXF(), "XF must be OFF (XF=" + cpu.getXF() + ")"),
                //() -> assertFalse(cpu.getVF(), "Overflow must be OFF (VF=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getNF(), "N must be OFF (NF=" + cpu.getNF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry must be OFF (CF=" + cpu.getCF() + ")")
        );


        compTest.poke(0x0001, (byte) 0x29);
        cpu.setIX((short)0x0001);
        cpu.setPC(0x0000);

        cpu.fetch(); // ADD IX, IX

        assertAll("ADD IX, rp_p Group",
                () -> assertEquals((short) 0x0002, cpu.getIX(), "ADD IX, IX Failed: IX<>0x0002 IX=" + Integer.toHexString(cpu.getIX())),
                () -> assertNotEquals((short) 0x0001, cpu.getIX(), "ADD IX, IX Failed: IX Still 0x0001 IX=" + Integer.toHexString(cpu.getIX())),

                //() -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                //() -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF must be OFF (YF=" + cpu.getYF() + ")"),
                () -> assertFalse(cpu.getHF(), "Half-carry must be OFF (HF=\" + cpu.getHF() + \")\"),"),
                () -> assertFalse(cpu.getXF(), "XF must be OFF (XF=" + cpu.getXF() + ")"),
                //() -> assertFalse(cpu.getVF(), "Overflow must be OFF (VF=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getNF(), "N must be OFF (NF=" + cpu.getNF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry must be OFF (CF=" + cpu.getCF() + ")")
        );

        compTest.poke(0x0001, (byte) 0x39);
        cpu.setIX((short)0xFFFF);
        cpu.setSP((short)0x0001);
        cpu.setPC(0x0000);

        cpu.fetch();  // ADD IX, SP

        assertAll("ADD IX, rp_p Group",
                () -> assertEquals((short) 0x0000, cpu.getIX(), "ADD IX, DE Failed: IX<>0x0000 IX=" + Integer.toHexString(cpu.getIX())),
                () -> assertNotEquals((short) 0xFFFF, cpu.getIX(), "ADD IX, DE Failed: IX Still 0xFFFF IX=" + Integer.toHexString(cpu.getIX())),

                //() -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                //() -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF must be ON (YF=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Half-carry must be ON (HF=\" + cpu.getHF() + \")\"),"),
                () -> assertFalse(cpu.getXF(), "XF must be ON (XF=" + cpu.getXF() + ")"),
                //() -> assertFalse(cpu.getVF(), "Overflow must be OFF (VF=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getNF(), "N must be OFF (NF=" + cpu.getNF() + ")"),
                () -> assertTrue(cpu.getCF(), "Carry must be ON (CF=" + cpu.getCF() + ")")
        );
    }

    @Test
    void testADD_IY_rp_p() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xFD);
        compTest.poke(0x0001, (byte) 0x09); // ADD IY, BC

        cpu.setPC(0x0000);

        cpu.setIY((short)0x1234);
        cpu.setBC((short)0x0001);

        long initTState = cpu.getTState();
        cpu.fetch();

        assertAll("ADD IY, rp_p Group",
                () -> assertEquals((short) 0x1235, cpu.getIY(), "ADD IY, BC Failed: IY<>0x1235 IY=" + Integer.toHexString(cpu.getIY())),
                () -> assertNotEquals((short) 0x1234, cpu.getIY(), "ADD IY, BC Failed: IY Still 0x1234 IY=" + Integer.toHexString(cpu.getIY())),

                //() -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                //() -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF must be OFF (YF=" + cpu.getYF() + ")"),
                () -> assertFalse(cpu.getHF(), "Half-carry must be OFF (HF=\" + cpu.getHF() + \")\"),"),
                () -> assertFalse(cpu.getXF(), "XF must be OFF (XF=" + cpu.getXF() + ")"),
                //() -> assertFalse(cpu.getVF(), "Overflow must be OFF (VF=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getNF(), "N must be OFF (NF=" + cpu.getNF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry must be OFF (CF=" + cpu.getCF() + ")"),

                () -> assertEquals(15, cpu.getTState()-initTState, "ADD IY, BC TState Failed")
        );

        compTest.poke(0x0001, (byte) 0x19);
        cpu.setIY((short)0x0FFF);
        cpu.setDE((short)0x0001);
        cpu.setPC(0x0000);

        cpu.fetch(); // ADD IY, DE

        assertAll("ADD IY, rp_p Group",
                () -> assertEquals((short) 0x1000, cpu.getIY(), "ADD IY, DE Failed: IY<>0x1000 IY=" + Integer.toHexString(cpu.getIY())),
                () -> assertNotEquals((short) 0x0FFF, cpu.getIY(), "ADD IY, DE Failed: IY Still 0x0FFF IY=" + Integer.toHexString(cpu.getIY())),

                //() -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                //() -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF must be OFF (YF=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Half-carry must be ON (HF=\" + cpu.getHF() + \")\"),"),
                () -> assertFalse(cpu.getXF(), "XF must be OFF (XF=" + cpu.getXF() + ")"),
                //() -> assertFalse(cpu.getVF(), "Overflow must be OFF (VF=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getNF(), "N must be OFF (NF=" + cpu.getNF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry must be OFF (CF=" + cpu.getCF() + ")")
        );


        compTest.poke(0x0001, (byte) 0x29);
        cpu.setIY((short)0x0001);
        cpu.setPC(0x0000);

        cpu.fetch(); // ADD IY, IY

        assertAll("ADD IY, rp_p Group",
                () -> assertEquals((short) 0x0002, cpu.getIY(), "ADD IY, IY Failed: IY<>0x0002 IY=" + Integer.toHexString(cpu.getIY())),
                () -> assertNotEquals((short) 0x0001, cpu.getIY(), "ADD IY, IY Failed: IY Still 0x0001 IY=" + Integer.toHexString(cpu.getIY())),

                //() -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                //() -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF must be OFF (YF=" + cpu.getYF() + ")"),
                () -> assertFalse(cpu.getHF(), "Half-carry must be OFF (HF=\" + cpu.getHF() + \")\"),"),
                () -> assertFalse(cpu.getXF(), "XF must be OFF (XF=" + cpu.getXF() + ")"),
                //() -> assertFalse(cpu.getVF(), "Overflow must be OFF (VF=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getNF(), "N must be OFF (NF=" + cpu.getNF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry must be OFF (CF=" + cpu.getCF() + ")")
        );

        compTest.poke(0x0001, (byte) 0x39);
        cpu.setIY((short)0xFFFF);
        cpu.setSP((short)0x0001);
        cpu.setPC(0x0000);

        cpu.fetch();  // ADD IY, SP

        assertAll("ADD IY, rp_p Group",
                () -> assertEquals((short) 0x0000, cpu.getIY(), "ADD IY, DE Failed: IY<>0x0000 IY=" + Integer.toHexString(cpu.getIY())),
                () -> assertNotEquals((short) 0xFFFF, cpu.getIY(), "ADD IY, DE Failed: IY Still 0xFFFF IY=" + Integer.toHexString(cpu.getIY())),

                //() -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                //() -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF must be ON (YF=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Half-carry must be ON (HF=\" + cpu.getHF() + \")\"),"),
                () -> assertFalse(cpu.getXF(), "XF must be ON (XF=" + cpu.getXF() + ")"),
                //() -> assertFalse(cpu.getVF(), "Overflow must be OFF (VF=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getNF(), "N must be OFF (NF=" + cpu.getNF() + ")"),
                () -> assertTrue(cpu.getCF(), "Carry must be ON (CF=" + cpu.getCF() + ")")
        );
    }

    @Test
    void testADD_A_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte)0x7F);
        cpu.setB((byte)0x01);
        cpu.resCF();

        long initTState = cpu.getTState();
        cpu.fetch((byte)0x80); // ADD A, B

        assertEquals(4, cpu.getTState()-initTState, "ADD A, B TState Failed");

        ADC_A_case1(cpu, "ADD A, B Failed: A<>0x80 = ");

        cpu.setA((byte)0xFF);
        cpu.setC((byte)0x01);
        cpu.resCF();

        cpu.fetch((byte)0x81); // ADD A, C

        ADC_A_case2(cpu, "ADD A, C Failed: A<>0x00 = ");

        cpu.setA((byte)0x0F);
        cpu.setD((byte)0x01);
        cpu.resCF();

        cpu.fetch((byte)0x82); // ADD A, D

        ADC_A_case3(cpu, "ADD A, D Failed: A<>0x10 = ");

        cpu.setA((byte)0x1F);
        cpu.setE((byte)0x01);
        cpu.resCF();

        cpu.fetch((byte)0x83); // ADD A, E

        ADC_A_case4(cpu, "ADD A, E Failed: A<>0x20 = ");

        cpu.setA((byte)0x80);
        cpu.setH((byte)0x80);
        cpu.resCF();

        cpu.fetch((byte)0x84); // ADD A, H

        ADC_A_case5(cpu, "ADD A, H Failed: A<>00= ");

        cpu.setA((byte)0x04);
        cpu.setL((byte)0x04);
        cpu.resCF();

        cpu.fetch((byte)0x85); // ADD A, L

        ADC_A_case6(cpu, "ADD A, L Failed: A<>00= ");

        /*assertAll(
                () -> assertEquals((byte) 0x43, cpu.getA(), "ADD A, B Failed: A<>0x43 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")"),
                () -> assertTrue(cpu.getVF(), "Overflow flag must be ON (V=" + cpu.getVF() + ")")
        );

        cpu.setA((byte)0);
        cpu.setB((byte)0);

        cpu.fetch((byte)0x80); // ADD A, B

        assertAll(
                () -> assertEquals((byte) 0, cpu.getA(), "ADD A, B Failed: A<>0 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")")
        );

        cpu.setA((byte)0x0E);
        cpu.setB((byte)2);

        cpu.fetch((byte)0x80); // ADD A, B

        assertAll(
                () -> assertEquals((byte) 0x10, cpu.getA(), "ADD A, B Failed: A<>0x10 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON (H=" + cpu.getHF() + ")")
        );*/
    }

    @Test
    void testADC_A_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte)0x7F);
        cpu.setB((byte)0x00);
        cpu.setCF();

        long initTState = cpu.getTState();
        cpu.fetch((byte)0x88); // ADC A, B

        assertEquals(4, cpu.getTState()-initTState, "ADC A, B TState Failed");

        ADC_A_case1(cpu, "ADC A, B; C=1, Failed: A<>0x80 = ");

        cpu.setA((byte)0xFF);
        cpu.setC((byte)0x00);
        cpu.setCF();

        cpu.fetch((byte)0x89); // ADC A, C

        ADC_A_case2(cpu, "ADC A, C Failed: A<>0x00 = ");

        cpu.setA((byte)0x0F);
        cpu.setD((byte)0x00);
        cpu.setCF();

        cpu.fetch((byte)0x8A); // ADC A, D

        ADC_A_case3(cpu, "ADC A, D Failed: A<>0x10 = ");

        cpu.setA((byte)0x1F);
        cpu.setE((byte)0x01);
        cpu.resCF();

        cpu.fetch((byte)0x8B); // ADC A, E

        ADC_A_case4(cpu, "ADC A, B Failed: A<>0x03 = ");

        cpu.setA((byte)0x80);
        cpu.setH((byte)0x80);
        cpu.resCF();

        cpu.fetch((byte)0x8C); // ADC A, H

        ADC_A_case5(cpu, "ADC A, H Failed: A<>0x20 = ");

        cpu.setA((byte)0x04);
        cpu.resCF();

        cpu.fetch((byte)0x8F); // ADC A, A TODO usar (HL)

        ADC_A_case6(cpu, "ADC A, A Failed: A<>0x08 = ");
    }

    @Test
    void testADD_A_XY() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);

        compTest.poke(0, (byte) 0xDD);
        compTest.poke(1, (byte) 0x84); // ADD A, IXH

        cpu.setPC(0x0000);
        cpu.setA((byte)0x7F);
        cpu.setIXH((byte) 0x01);

        long initTState = cpu.getTState();
        cpu.fetch();

        assertEquals(8, cpu.getTState()-initTState, "ADD A, IXH TState Failed");

        ADC_A_case1(cpu, "ADD A, IXH Failed: A<>0x80 = ");

        compTest.poke(0, (byte) 0xFD);
        compTest.poke(1, (byte) 0x84); // ADD A, IYH

        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);
        cpu.setIYH((byte)0x01);

        cpu.fetch();

        ADC_A_case2(cpu, "ADD A, IYH Failed: A<>0 = ");

        compTest.poke(0, (byte) 0xDD);
        compTest.poke(1, (byte) 0x85); // ADD A, IXL

        cpu.setPC(0x0000);
        cpu.setA((byte)0x0F);
        cpu.setIXL((byte) 0x01);

        long initTState2 = cpu.getTState();
        cpu.fetch();

        assertEquals(8, cpu.getTState()-initTState2, "ADD A, IXL TState Failed");

        ADC_A_case3(cpu, "ADD A, IXL Failed: A<>0 = ");

        compTest.poke(0, (byte) 0xFD);
        compTest.poke(1, (byte) 0x85); // ADD A, IYL

        cpu.setPC(0x0000);
        cpu.setA((byte)0x1F);
        cpu.setIYL((byte)0x01);

        cpu.fetch();

        ADC_A_case4(cpu, "ADD A, IYL Failed: A<>0x20 = ");
    }

    @Test
    void testADD_A_n() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0x01);

        cpu.setPC(0x0000);
        cpu.setA((byte)0x7F);

        long initTState = cpu.getTState();
        cpu.fetch((byte)0xC6); // ADD A, 0x01

        assertEquals(7, cpu.getTState()-initTState, "ADD A, n TState Failed");

        ADC_A_case1(cpu, "ADD A, 0x01 Failed: A<>0x80 = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);

        cpu.fetch((byte)0xC6); // ADD A, 0x01

        ADC_A_case2(cpu, "ADD A, 0x10 Failed: A<>0x10 = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0x0F);

        cpu.fetch((byte)0xC6); // ADD A, 0x01

        ADC_A_case3(cpu, "ADD A, 0x01 Failed: A<>0x10 = ");

        compTest.poke(0x0000, (byte) 0x01);
        cpu.setPC(0x0000);
        cpu.setA((byte)0x1F);

        cpu.fetch((byte)0xC6); // ADD A, 0x01

        ADC_A_case4(cpu, "ADD A, 0x01 Failed: A<>0x20 = ");

        compTest.poke(0x0000, (byte) 0x80);
        cpu.setPC(0x0000);
        cpu.setA((byte)0x80);

        cpu.fetch((byte)0xC6); // ADD A, 0x80

        ADC_A_case5(cpu, "ADD A, 0x80 Failed: A<>0x0 = ");

        compTest.poke(0x0000, (byte) 0x04);
        cpu.setPC(0x0000);
        cpu.setA((byte)0x04);

        cpu.fetch((byte)0xC6); // ADD A, 0x04

        ADC_A_case6(cpu, "ADD A, 0x04 Failed: A<>0x08 = ");
    }

    @Test
    void testADC_A_n() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0x00);

        cpu.setPC(0x0000);
        cpu.setA((byte)0x7F);
        cpu.setCF();

        long initTState = cpu.getTState();
        cpu.fetch((byte)0xCE); // ADC A, 0x00

        assertEquals(7, cpu.getTState()-initTState, "ADC A, n TState Failed");

        ADC_A_case1(cpu,"ADC A, 0x0; C=1 Failed: A<>0x80 = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);
        cpu.setCF();

        cpu.fetch((byte)0xCE);

        ADC_A_case2(cpu, "ADC A, 0x00; C=1 Failed: A<>0x00 = ");

        compTest.poke(0x0000, (byte) 0x00);
        cpu.setPC(0x0000);
        cpu.setA((byte)0x0F);
        cpu.setCF();

        cpu.fetch((byte)0xCE); // ADC A, 0x00

        ADC_A_case3(cpu, "ADC A, 0x00 Failed: A<>0x10 = ");

        compTest.poke(0x0000, (byte) 0x01);
        cpu.setPC(0x0000);
        cpu.setA((byte)0x01F);
        cpu.resCF();

        cpu.fetch((byte)0xCE); // ADC A, 0x01

        ADC_A_case4(cpu, "ADC A, 0x01, C=0, Failed: A<>0x20 = ");

        compTest.poke(0x0000, (byte) 0x80);
        cpu.setPC(0x0000);
        cpu.setA((byte)0x80);
        cpu.resCF();

        cpu.fetch((byte)0xCE); // ADC A, 0x80

        ADC_A_case5(cpu, "ADC A, 0x80, C=0, Failed: A<>0x00 = ");

        compTest.poke(0x0000, (byte) 0x04);
        cpu.setPC(0x0000);
        cpu.setA((byte)0x04);
        cpu.resCF();

        cpu.fetch((byte)0xCE); // ADC A, 0x04

        ADC_A_case6(cpu, "ADC A, 0x04, C=0, Failed: A<>0x08 = ");
    }

    @Test
    void testADD_A_IX_d() {
        long initTState;
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xDD); // ADD A, (IX+3)
        compTest.poke(0x0001, (byte) 0x86);
        compTest.poke(0x0002, (byte) 0x03);
        compTest.poke(0x0003, (byte) 0x01);

        cpu.setPC(0x0000);
        cpu.setIX((short) 0x0000);
        cpu.setA((byte)0x7F);
        initTState = cpu.getTState();
        cpu.fetch();// ADD A, (IX+3)

        assertEquals(19, cpu.getTState()-initTState, "ADC A, (IX+3) TState Failed");
        
        ADC_A_case1(cpu, "ADD A, (IX+3); C=1, Failed: A<>0x80 = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);
        cpu.setCF();

        cpu.fetch();

        ADC_A_case2(cpu, "ADD A, (IX+3); C=1 Failed: A<>0x00 = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0x0F);
        cpu.setCF();

        cpu.fetch();

        ADC_A_case3(cpu, "ADD A, (IX+3); C=1, Failed: A<>0x10 = ");

        compTest.poke(0x0003, (byte) 0x01);
        cpu.setPC(0x0000);
        cpu.setA((byte)0x1F);
        cpu.resCF();

        cpu.fetch();

        ADC_A_case4(cpu, "ADD A, (IX+3); C=0, Failed: A<>0x20 = ");

        compTest.poke(0x0003, (byte) 0x80);
        cpu.setPC(0x0000);
        cpu.setA((byte)0x80);
        cpu.resCF();

        cpu.fetch();

        ADC_A_case5(cpu, "ADD A, (IX+3); C=0, Failed: A<>0x00 = ");

        compTest.poke(0x0003, (byte) 0x04);
        cpu.setPC(0x0000);
        cpu.setA((byte)0x04);
        cpu.resCF();

        cpu.fetch();

        ADC_A_case6(cpu, "ADD A, (IX+3); C=0, Failed: A<>0x08 = ");
    }

    @Test
    void testADC_A_IX_d() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xDD); // ADC A, (IX+3)
        compTest.poke(0x0001, (byte) 0x8E);
        compTest.poke(0x0002, (byte) 0x03);
        compTest.poke(0x0003, (byte) 0x00);

        cpu.setPC(0x0000);
        cpu.setA((byte)0x7F);
        cpu.setIX((short)0x0000);
        cpu.setCF();

        long initTState = cpu.getTState();
        cpu.fetch(); // ADC A, (IX+3)

        assertEquals(19, cpu.getTState()-initTState, "ADC A, (IX+3) TState Failed");

        ADC_A_case1(cpu, "ADC A, (IX+3); C=1, Failed: A<>0x80 = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);
        cpu.setCF();

        cpu.fetch();

        ADC_A_case2(cpu, "ADC A, (IX+3); C=1 Failed: A<>0x00 = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0x0F);
        cpu.setCF();

        cpu.fetch();

        ADC_A_case3(cpu, "ADC A, (IX+3); C=1, Failed: A<>0x10 = ");

        compTest.poke(0x0003, (byte) 0x01);
        cpu.setPC(0x0000);
        cpu.setA((byte)0x1F);
        cpu.resCF();

        cpu.fetch();

        ADC_A_case4(cpu, "ADC A, (IX+3); C=0, Failed: A<>0x20 = ");

        compTest.poke(0x0003, (byte) 0x80);
        cpu.setPC(0x0000);
        cpu.setA((byte)0x80);
        cpu.resCF();

        cpu.fetch();

        ADC_A_case5(cpu, "ADC A, (IX+3); C=0, Failed: A<>0x00 = ");

        compTest.poke(0x0003, (byte) 0x04);
        cpu.setPC(0x0000);
        cpu.setA((byte)0x04);
        cpu.resCF();

        cpu.fetch();

        ADC_A_case6(cpu, "ADC A, (IX+3); C=0, Failed: A<>0x08 = ");
    }

    @Test
    void testADC_A_IY_d() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xFD); // ADC A, (IY+3)
        compTest.poke(0x0001, (byte) 0x8E);
        compTest.poke(0x0002, (byte) 0x03);
        compTest.poke(0x0003, (byte) 0x00);

        cpu.setPC(0x0000);
        cpu.setA((byte)0x7F);
        cpu.setIY((short)0x0000);
        cpu.setCF();

        long initTState = cpu.getTState();
        cpu.fetch(); // ADC A, (IY+3)

        assertEquals(19, cpu.getTState()-initTState, "ADC A, (IY+3) TState Failed");

        ADC_A_case1(cpu, "ADC A, (IY+3); C=1, Failed: A<>0x80 = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);
        cpu.setCF();

        cpu.fetch();

        ADC_A_case2(cpu, "ADC A, (IY+3); C=1 Failed: A<>0x00 = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0x0F);
        cpu.setCF();

        cpu.fetch();

        ADC_A_case3(cpu, "ADC A, (IY+3); C=1, Failed: A<>0x10 = ");

        compTest.poke(0x0003, (byte) 0x01);
        cpu.setPC(0x0000);
        cpu.setA((byte)0x1F);
        cpu.resCF();

        cpu.fetch();

        ADC_A_case4(cpu, "ADC A, (IY+3); C=0, Failed: A<>0x20 = ");

        compTest.poke(0x0003, (byte) 0x80);
        cpu.setPC(0x0000);
        cpu.setA((byte)0x80);
        cpu.resCF();

        cpu.fetch();

        ADC_A_case5(cpu, "ADC A, (IY+3); C=0, Failed: A<>0x00 = ");

        compTest.poke(0x0003, (byte) 0x04);
        cpu.setPC(0x0000);
        cpu.setA((byte)0x04);
        cpu.resCF();

        cpu.fetch();

        ADC_A_case6(cpu, "ADC A, (IY+3); C=0, Failed: A<>0x08 = ");
    }

    @Test
    void testADD_A_IY_d() {
        long initTState;
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xFD); // ADD A, (IY+3)
        compTest.poke(0x0001, (byte) 0x86);
        compTest.poke(0x0002, (byte) 0x03);
        compTest.poke(0x0003, (byte) 0x01);

        cpu.setPC(0x0000);
        cpu.setIY((short) 0x0000);
        cpu.setA((byte)0x7F);
        initTState = cpu.getTState();
        cpu.fetch();// ADD A, (IY+3)

        assertEquals(19, cpu.getTState()-initTState, "ADC A, (IY+3) TState Failed");

        ADC_A_case1(cpu, "ADD A, (IY+3); C=1, Failed: A<>0x80 = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);
        cpu.setCF();

        cpu.fetch();

        ADC_A_case2(cpu, "ADD A, (IY+3); C=1 Failed: A<>0x00 = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0x0F);
        cpu.setCF();

        cpu.fetch();

        ADC_A_case3(cpu, "ADD A, (IY+3); C=1, Failed: A<>0x10 = ");

        compTest.poke(0x0003, (byte) 0x01);
        cpu.setPC(0x0000);
        cpu.setA((byte)0x1F);
        cpu.resCF();

        cpu.fetch();

        ADC_A_case4(cpu, "ADD A, (IY+3); C=0, Failed: A<>0x20 = ");

        compTest.poke(0x0003, (byte) 0x80);
        cpu.setPC(0x0000);
        cpu.setA((byte)0x80);
        cpu.resCF();

        cpu.fetch();

        ADC_A_case5(cpu, "ADD A, (IY+3); C=0, Failed: A<>0x00 = ");

        compTest.poke(0x0003, (byte) 0x04);
        cpu.setPC(0x0000);
        cpu.setA((byte)0x04);
        cpu.resCF();

        cpu.fetch();

        ADC_A_case6(cpu, "ADD A, (IY+3); C=0, Failed: A<>0x08 = ");
    }

    @Test
    void testSUB_n() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0x01);

        cpu.setPC(0x0000);
        cpu.setA((byte)0x0);

        long initTState = cpu.getTState();
        cpu.fetch((byte)0xD6); // SUB 0x01

        SBC_A_case1(cpu, "SUB 0x01; C=0 Failed: A<>0xFF = ");

        assertEquals(7, cpu.getTState()-initTState, "SUB n TState Failed");

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x01);

        cpu.fetch((byte) 0xD6);

        SBC_A_case2(cpu, "SUB 0x01; C=0 Failed: A<>0x00 = ");

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x10);

        cpu.fetch((byte) 0xD6);

        SBC_A_case3(cpu, "SUB 0x01; C=0 Failed: A<>0x0F = ");

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x80);

        cpu.fetch((byte) 0xD6);

        SBC_A_case4(cpu, "SUB 0x01; C=0 Failed: A<>0x7F = ");

        /*cpu.setPC(0x0000);
        cpu.setA((byte) 0x1E);
        compTest.poke(0x0000, (byte) 0x0F);

        cpu.fetch((byte) 0xD6);

        assertAll(
                () -> assertEquals((byte) 0x0F, cpu.getA(), "SUB 0x0F Failed: A<>0x0F = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x80); // -128
        compTest.poke(0x0000, (byte) 0x80); // -128

        cpu.fetch((byte) 0xD6);

        assertAll(
                () -> assertEquals((byte) 0x00, cpu.getA(), "SUB 0x80 Failed: A<>0x00 = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")")
        );*/
    }

    @Test
    void testSBC_A_n() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0x01);

        cpu.setPC(0x0000);
        cpu.setA((byte)0x00);
        cpu.resCF();

        long initTState = cpu.getTState();
        cpu.fetch((byte)0xDE); // SBC A, 0x01

        SBC_A_case1(cpu, "SBC A, 0x01; C=0 Failed: A<>0xFF = ");

        assertEquals(7, cpu.getTState()-initTState, "SBC A, n TState Failed");

        cpu.setPC(0x0000);
        cpu.setA((byte)0x01);
        cpu.resCF();

        cpu.fetch((byte)0xDE);

        SBC_A_case2(cpu, "SBC A, 0x01; C=0 Failed: A<>0x00 = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0x10);
        cpu.resCF();

        cpu.fetch((byte)0xDE);

        SBC_A_case3(cpu, "SBC A, 0x01; C=0 Failed: A<>0x0F = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0x80);
        cpu.resCF();

        cpu.fetch((byte)0xDE);

        SBC_A_case4(cpu, "SBC A, 0x01; C=0 Failed: A<>0x7F = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0x00);
        cpu.setCF();
        compTest.poke(0x0000, (byte) 0x00);

        cpu.fetch((byte)0xDE);

        SBC_A_case5(cpu, "SBC A, 0x00; C=1 Failed: A<>0xFF = ");
    }

/*
SUB
SBC
n/r_z/(IX+d)/(IY+d)
| A    | n    |  C  | Result | S | Z | YF | H | XF | p/V | N | C | What test?       | Cases
| ---- | ---- | --- | ------ | - | - | -- | - | -- | --- | - | - | ---------------- | -----
| 0x00 | 0x01 |  0  |  0xFF  | 1 | 0 |  1 | 1 |  1 |  0  | 1 | 1 | borrow + sign    | Case 1
| 0x01 | 0x01 |  0  |  0x00  | 0 | 1 |  0 | 0 |  0 |  0  | 1 | 0 | zero clean       | Case 2
| 0x10 | 0x01 |  0  |  0x0F  | 0 | 0 |  0 | 1 |  1 |  0  | 1 | 0 | half borrow      | Case 3
| 0x80 | 0x01 |  0  |  0x7F  | 0 | 0 |  1 | 1 |  1 |  1  | 1 | 0 | overflow         | Case 4
| 0x00 | 0x00 |  1  |  0xFF  | 1 | 0 |  1 | 1 |  1 |  0  | 1 | 1 | borrow for carry | Case 5
*/
    @Test
    void testSUB_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte) 0x00);
        cpu.setB((byte) 0x01);
        //cpu.resCF();

        long initTState = cpu.getTState();
        cpu.fetch((byte) 0x90); // SUB B

        assertEquals(4, cpu.getTState()-initTState, "SUB B TState Failed");

        SBC_A_case1(cpu, "SUB B; C=0 Failed: A<>0xFF = ");

        cpu.setA((byte) 0x01);
        cpu.setC((byte) 0x01);
        //cpu.resCF();

        cpu.fetch((byte) 0x91); // SUB C

        SBC_A_case2(cpu, "SUB C; C=0 Failed: A<>0x00 = ");

        cpu.setA((byte) 0x10);
        cpu.setD((byte) 0x01);

        cpu.fetch((byte) 0x92);

        SBC_A_case3(cpu, "SUB D; C=0 Failed: A<>0x0F = ");

        cpu.setA((byte) 0x80);
        cpu.setE((byte) 0x01);

        cpu.fetch((byte) 0x93);

        SBC_A_case4(cpu, "SUB E; C=0 Failed: A<>0x7F = ");
    }

    @Test
    void testSBC_A_r_z() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte)0x00);
        cpu.setB((byte)0x01);
        cpu.resCF();

        long initTState = cpu.getTState();
        cpu.fetch((byte)0x98); // SBC A,B

        SBC_A_case1(cpu, "SBC A, B; C=0 Failed: A<>0xFF = ");

        assertEquals(4, cpu.getTState()-initTState, "SBC A, B TState Failed");

        cpu.setA((byte)0x01);
        cpu.setC((byte)0x01);
        cpu.resCF();

        cpu.fetch((byte)0x99); // SBC A,C

        SBC_A_case2(cpu, "SBC A, C; C=0 Failed: A<>0x00 = ");

        cpu.setA((byte)0x10);
        cpu.setD((byte)0x01);
        cpu.resCF();

        cpu.fetch((byte)0x9A); // SBC A,D

        SBC_A_case3(cpu, "SBC A, D; C=0 Failed: A<>0x0F = ");

        cpu.setA((byte)0x80);
        cpu.setE((byte)0x01);
        cpu.resCF();

        cpu.fetch((byte)0x9B); // SBC A,E

        SBC_A_case4(cpu, "SBC A, E; C=0 Failed: A<>0x7F = ");

        cpu.setA((byte)0x00);
        cpu.setH((byte)0x00);
        cpu.setCF();

        cpu.fetch((byte)0x9C); // SBC A,H

        SBC_A_case5(cpu, "SBC A, H; C=1 Failed: A<>0xFF = ");
    }

/*
SBC
| HL       | rr       | C   | Resultado | S | Z | H | p/V | C | Qué cubre        |
| -------- | -------- | --- | --------- | - | - | - | --- | - | ---------------- |
| `0x0000` | `0x0001` | `0` | `0xFFFF`  | 1 | 0 | 1 | 0   | 1 | borrow + sign    | 4
| `0x0001` | `0x0001` | `0` | `0x0000`  | 0 | 1 | 0 | 0   | 0 | zero limpio      | 3
| `0x1000` | `0x0001` | `0` | `0x0FFF`  | 0 | 0 | 1 | 0   | 0 | half borrow      | 5
| `0x8000` | `0x0001` | `0` | `0x7FFF`  | 0 | 0 | 1 | 1   | 0 | overflow         | 1
| `0x0000` | `0x0000` | `1` | `0xFFFF`  | 1 | 0 | 1 | 0   | 1 | borrow por carry | 6
F3/F5 desde el byte alto (H) ⚠️ (igual que ADC)
| HL     | rr     | C | Result | S | Z | YF | H | XF | p/V | N | C | What test?       | Cases
| ------ | ------ | - | ------ | - | - | -- | - | -- | --- | - | - | ---------------- | ------
| 0x8000 | 0x0001 | 0 | 0x7FFF | 0 | 0 |  1 | 1 |  1 |  1  | 1 | 0 | overflow         | Case 1
| 0x7FFF | 0xFFFF | 0 | 0x8000 | 1 | 0 |  0 | 0 |  0 |  1  | 1 | 1 | reverse overflow | Case 2
| 0x0001 | 0x0001 | 0 | 0x0000 | 0 | 1 |  0 | 0 |  0 |  0  | 1 | 0 | zero             | Case 3
| 0x0000 | 0x0001 | 0 | 0xFFFF | 1 | 0 |  1 | 1 |  1 |  0  | 1 | 1 | borrow           | Case 4
| 0x1000 | 0x0001 | 0 | 0x0FFF | 0 | 0 |  0 | 1 |  1 |  0  | 1 | 0 | half borrow      | Case 5
| 0x0000 | 0x0000 | 1 | 0xFFFF | 1 | 0 |  1 | 1 |  1 |  0  | 1 | 1 | carry            | Case 6
*/
    @Test
    void testSBC_HL_rp_p() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xED);
        compTest.poke(0x0001, (byte) 0x42);

        cpu.setPC(0x0000);
        cpu.setHL((short) 0x8000);
        cpu.setBC((short) 0x0001);
        cpu.resCF();

        long initTState = cpu.getTState();
        cpu.fetch(); // SBC HL, BC

        assertAll("SBC_HL_rp_p Group",
                () -> assertEquals((short)0x7FFF, cpu.getHL(), "SBC_HL, BC Failed: HL<>0X7FFF (HL=" + cpu.getHL() + ")"),
                () -> assertNotEquals((short)0x8000, cpu.getHL(), "SBC_HL, BC Failed: HL still 0x8000 (HL=" + Integer.toHexString(cpu.getHL()) + ")"),
                () -> assertFalse(cpu.getSF(), "Sign must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getYF(), "YF flag must be ON (YF=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertTrue(cpu.getXF(), "XF flag must be ON (XF=" + cpu.getXF() + ")"),
                () -> assertTrue(cpu.getVF(), "Overflow flag must be ON (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (V=" + cpu.getNF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")"),

                () -> assertEquals(15, cpu.getTState()-initTState, "SBC HL, rp TState Failed")
        );

        cpu.setPC(0x0000);
        cpu.setHL((short) 0x7FFF);
        cpu.setDE((short) 0xFFFF);
        compTest.poke(0x0001, (byte) 0x52);
        cpu.resCF();

        cpu.fetch(); // SBC HL, DE

        assertAll("SBC_HL_rp_p Group",
                () -> assertEquals((short)0x8000, cpu.getHL(), "SBC_HL, DE Failed: HL<>0x8000 (HL=" + cpu.getHL() + ")"),
                () -> assertNotEquals((short)0x7FFF, cpu.getHL(), "SBC_HL, DE Failed: HL still 0x7FFF (HL=" + Integer.toHexString(cpu.getHL()) + ")"),
                () -> assertTrue(cpu.getSF(), "Sign must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF flag must be OFF (YF=" + cpu.getYF() + ")"),
                () -> assertFalse(cpu.getHF(), "Half-carry flag must be OFF (H=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getXF(), "XF flag must be OFF (XF=" + cpu.getXF() + ")"),
                () -> assertTrue(cpu.getVF(), "Overflow flag must be ON (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (V=" + cpu.getNF() + ")"),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setHL((short) 0x0001);
        compTest.poke(0x0001, (byte) 0x62);
        cpu.resCF();

        cpu.fetch(); // SBC HL, HL

        assertAll("SBC_HL_rp_p Group",
                () -> assertEquals((short)0x0000, cpu.getHL(), "SBC HL, HL Failed: HL<>0x0000 (HL=" + cpu.getHL() + ")"),
                () -> assertNotEquals((short)0x0001, cpu.getHL(), "SBC HL, HL Failed: HL still 0x0001 (HL=" + Integer.toHexString(cpu.getHL()) + ")"),
                () -> assertFalse(cpu.getSF(), "Sign must be ON (S=" + cpu.getSF() + ")"),
                () -> assertTrue(cpu.getZF(), "Zero must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF flag must be OFF (YF=" + cpu.getYF() + ")"),
                () -> assertFalse(cpu.getHF(), "Half-carry flag must be OFF (H=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getXF(), "XF flag must be OFF (XF=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (V=" + cpu.getNF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setHL((short) 0x0000);
        cpu.setSP((short) 0x0001);
        compTest.poke(0x0001, (byte) 0x72);
        cpu.resCF();

        cpu.fetch(); // SBC HL, SP

        assertAll("SBC_HL_rp_p Group",
                () -> assertEquals((short)0xFFFF, cpu.getHL(), "SBC HL, SP Failed: HL<>0xFFFF (HL=" + cpu.getHL() + ")"),
                () -> assertNotEquals((short)0x0000, cpu.getHL(), "SBC HL, SP Failed: HL still 0x0000 (HL=" + Integer.toHexString(cpu.getHL()) + ")"),
                () -> assertTrue(cpu.getSF(), "Sign must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getYF(), "YF flag must be ON (YF=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertTrue(cpu.getXF(), "XF flag must be ON (XF=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (V=" + cpu.getNF() + ")"),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setHL((short) 0x1000);
        cpu.setBC((short) 0x0001);
        compTest.poke(0x0001, (byte) 0x42);
        cpu.resCF();

        cpu.fetch(); // SBC HL, BC

        assertAll("SBC_HL_rp_p Group",
                () -> assertEquals((short)0x0FFF, cpu.getHL(), "SBC HL, BC Failed: HL<>0x0FFF (HL=" + cpu.getHL() + ")"),
                () -> assertNotEquals((short)0x1000, cpu.getHL(), "SBC HL, BC Failed: HL still 0x1000 (HL=" + Integer.toHexString(cpu.getHL()) + ")"),
                () -> assertFalse(cpu.getSF(), "Sign must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF flag must be OFF (YF=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertTrue(cpu.getXF(), "XF flag must be ON (XF=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (V=" + cpu.getNF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setHL((short) 0x0000);
        compTest.poke(0x0001, (byte) 0x62);
        cpu.setCF();

        cpu.fetch(); // SBC HL, HL

        assertAll("SBC_HL_rp_p Group",
                () -> assertEquals((short)0xFFFF, cpu.getHL(), "SBC HL, HL; Carry = 1 Failed: HL<>0xFFFF (HL=" + cpu.getHL() + ")"),
                () -> assertNotEquals((short)0x0000, cpu.getHL(), "SBC HL, HL Failed: HL still 0x0000 (HL=" + Integer.toHexString(cpu.getHL()) + ")"),
                () -> assertTrue(cpu.getSF(), "Sign must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getYF(), "YF flag must be ON (YF=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertTrue(cpu.getXF(), "XF flag must be ON (XF=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (V=" + cpu.getNF() + ")"),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")")
        );
    }

    @Test
    void testSUB_IX_d() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xDD); // SUB (IX+3)
        compTest.poke(0x0001, (byte) 0x96);
        compTest.poke(0x0002, (byte) 0x03);

        cpu.setIX((short) 0);

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x00);
        compTest.poke(0x0003, (byte) 0x01);

        long initTState = cpu.getTState();
        cpu.fetch();

        assertEquals(19, cpu.getTState()-initTState, "SUB (IX+3) TState Failed");

        SBC_A_case1(cpu, "SUB (IX+3) Failed: A<>0xFF = ");

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x01);

        cpu.fetch();

        SBC_A_case2(cpu, "SUB C; C=0 Failed: A<>0x00 = ");

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x10);

        cpu.fetch();

        SBC_A_case3(cpu, "SUB D; C=0 Failed: A<>0x0F = ");

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x80);

        cpu.fetch();

        SBC_A_case4(cpu, "SUB E; C=0 Failed: A<>0x7F = ");
    }

    @Test
    void testSUB_IY_d() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xFD); // SUB (IY+3)
        compTest.poke(0x0001, (byte) 0x96);
        compTest.poke(0x0002, (byte) 0x03);

        cpu.setIY((short) 0);

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x00);
        compTest.poke(0x0003, (byte) 0x01);

        long initTState = cpu.getTState();
        cpu.fetch();

        assertEquals(19, cpu.getTState()-initTState, "SUB (IY+3) TState Failed");

        SBC_A_case1(cpu, "SUB (IY+3) Failed: A<>0xFF = ");

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x01);

        cpu.fetch();

        SBC_A_case2(cpu, "SUB C; C=0 Failed: A<>0x00 = ");

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x10);

        cpu.fetch();

        SBC_A_case3(cpu, "SUB D; C=0 Failed: A<>0x0F = ");

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x80);

        cpu.fetch();

        SBC_A_case4(cpu, "SUB E; C=0 Failed: A<>0x7F = ");
    }

    @Test
    void testSBC_A_IX_d() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xDD); // SBC A, (IX+3)
        compTest.poke(0x0001, (byte) 0x9E);
        compTest.poke(0x0002, (byte) 0x03);
        compTest.poke(0x0003, (byte) 0x01);

        cpu.setIX((short) 0);

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x00);
        cpu.resCF();

        long initTState = cpu.getTState();
        cpu.fetch();

        assertEquals(19, cpu.getTState()-initTState, "SBC A, (IX+3) TState Failed");

        SBC_A_case1(cpu, "SBC A, (IX+3); C=0 Failed: A<>0xFF = ");

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x01);
        cpu.resCF();

        cpu.fetch();

        SBC_A_case2(cpu, "SBC A, (IX+3); C=0 Failed: A<>0x00 = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0x10);
        cpu.resCF();

        cpu.fetch();

        SBC_A_case3(cpu, "SBC A, (IX+3); C=0 Failed: A<>0x0F = ");
        
        cpu.setPC(0x0000);
        cpu.setA((byte) 0x80);
        cpu.resCF();

        cpu.fetch();

        SBC_A_case4(cpu, "SBC A, (IX+3); C=0 Failed: A<>0x7F = ");
        
        cpu.setPC(0x0000);
        cpu.setA((byte) 0x00);
        cpu.setCF();
        compTest.poke(0x0003, (byte) 0x00);

        cpu.fetch();

        SBC_A_case5(cpu, "SBC A, (IX+3); C=1 Failed: A<>0xFF = ");
    }

    @Test
    void testSBC_A_IY_d() {
        Z80ForTesting cpu = new Z80ForTesting();

        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xFD); // SBC A, (IY+3)
        compTest.poke(0x0001, (byte) 0x9E);
        compTest.poke(0x0002, (byte) 0x03);
        compTest.poke(0x0003, (byte) 0x01);

        cpu.setIY((short) 0);

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x00);
        cpu.resCF();

        long initTState = cpu.getTState();
        cpu.fetch();

        SBC_A_case1(cpu, "SBC A, (IY+3); C=0 Failed: A<>0xFF = ");

        assertEquals(19, cpu.getTState()-initTState, "SBC A, (IY+3) TState Failed");

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x01);
        cpu.resCF();

        cpu.fetch();

        SBC_A_case2(cpu, "SBC A, (IY+3); C=0 Failed: A<>0x00 = ");

        cpu.setPC(0x0000);
        cpu.setA((byte)0x10);
        cpu.resCF();

        cpu.fetch();

        SBC_A_case3(cpu, "SBC A, (IY+3); C=0 Failed: A<>0x0F = ");

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x80);
        cpu.resCF();

        cpu.fetch();

        SBC_A_case4(cpu, "SBC A, (IY+3); C=0 Failed: A<>0x7F = ");

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x00);
        cpu.setCF();
        compTest.poke(0x0003, (byte) 0x00);

        cpu.fetch();

        SBC_A_case5(cpu, "SBC A, (IY+3); C=1 Failed: A<>0xFF = ");
    }

    @Test
    void testINC_rp_p() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setBC((short)0x1234);
        cpu.setDE((short)0x1234);
        cpu.setHL((short)0x1234);
        cpu.setSP((short)0x1234);
        cpu.setF((byte) 0xFF); // All flags set to test they're not affected

        long initTState = cpu.getTState();
        cpu.fetch((byte)0x03); // INC BC
        cpu.fetch((byte)0x13); // INC DE
        cpu.fetch((byte)0x23); // INC HL
        cpu.fetch((byte)0x33); // INC SP

        assertAll("INC rp[p] Group",
                () -> assertEquals((short)0x1235, cpu.getBC(), "INC BC Failed: BC<>0x1235 = " + Integer.toHexString(cpu.getBC())),
                () -> assertNotEquals((short)0x1234, cpu.getBC(), "INC BC Failed: BC still 0x1234 = " + Integer.toHexString(cpu.getBC())),

                () -> assertEquals((short)0x1235, cpu.getDE(), "INC DE Failed: DE<>0x1235 = " + Integer.toHexString(cpu.getDE())),
                () -> assertNotEquals((short)0x1234, cpu.getDE(), "INC DE Failed: DE still 0x1234 = " + Integer.toHexString(cpu.getDE())),

                () -> assertEquals((short)0x1235, cpu.getHL(), "INC HL Failed: HL<>0x1235 = " + Integer.toHexString(cpu.getHL())),
                () -> assertNotEquals((short)0x1234, cpu.getHL(), "INC HL Failed: HL still 0x1234 = " + Integer.toHexString(cpu.getHL())),

                () -> assertEquals((short)0x1235, cpu.getSP(), "INC SP Failed: SP<>0x1235 = " + Integer.toHexString(cpu.getSP())),
                () -> assertNotEquals((short)0x1234, cpu.getSP(), "INC SP Failed: SP still 0x1234 = " + Integer.toHexString(cpu.getSP())),

                () -> assertEquals((byte) 0xFF, cpu.getF(), "INC rp_p Failed: Flags were affected"),

                () -> assertEquals(6*4, cpu.getTState()-initTState, "INC rp TState Failed")
        );

        cpu.setHL((short)0x00FF); // test half-carry

        cpu.fetch((byte)0x23); // INC HL

        assertAll("INC rp[p] Group",
                () -> assertEquals((short)0x0100, cpu.getHL(), "INC HL Failed: HL<>0x0100 = " + Integer.toHexString(cpu.getHL())),
                () -> assertNotEquals((short)0x00FF, cpu.getHL(), "INC HL Failed: HL still 0x00FF = " + Integer.toHexString(cpu.getHL()))
        );
    }

    @Test
    void testDEC_rp_p() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setBC((short)0x1234);
        cpu.setDE((short)0x1234);
        cpu.setHL((short)0x1234);
        cpu.setSP((short)0x1234);
        cpu.setF((byte) 0xFF); // All flags set to test they're not affected

        long initTState = cpu.getTState();
        cpu.fetch((byte)0x0B); // DEC BC
        cpu.fetch((byte)0x1B); // DEC DE
        cpu.fetch((byte)0x2B); // DEC HL
        cpu.fetch((byte)0x3B); // DEC SP

        assertAll("DEC rp[p] Group",
                () -> assertEquals((short)0x1233, cpu.getBC(), "DEC BC Failed: BC<>0x1233 = " + Integer.toHexString(cpu.getBC())),
                () -> assertNotEquals((short)0x1234, cpu.getBC(), "DEC BC Failed: BC still 0x1234 = " + Integer.toHexString(cpu.getBC())),

                () -> assertEquals((short)0x1233, cpu.getDE(), "DEC DE Failed: DE<>0x1233 = " + Integer.toHexString(cpu.getDE())),
                () -> assertNotEquals((short)0x1234, cpu.getDE(), "DEC DE Failed: DE still 0x1234 = " + Integer.toHexString(cpu.getDE())),

                () -> assertEquals((short)0x1233, cpu.getHL(), "DEC HL Failed: HL<>0x1233 = " + Integer.toHexString(cpu.getHL())),
                () -> assertNotEquals((short)0x1234, cpu.getHL(), "DEC HL Failed: HL still 0x1234 = " + Integer.toHexString(cpu.getHL())),

                () -> assertEquals((short)0x1233, cpu.getSP(), "DEC SP Failed: SP<>0x1233 = " + Integer.toHexString(cpu.getSP())),
                () -> assertNotEquals((short)0x1234, cpu.getSP(), "DEC SP Failed: SP still 0x1234 = " + Integer.toHexString(cpu.getSP())),

                () -> assertEquals((byte) 0xFF, cpu.getF(), "DEC rp_p Failed: Flags were affected"),

                () -> assertEquals(6*4, cpu.getTState()-initTState, "DEC rp TState Failed")
        );

        cpu.setHL((short)0x0200); // test half-carry

        cpu.fetch((byte)0x2B); // DEC HL

        assertAll("DEC rp[p] Group",
                () -> assertEquals((short)0x01FF, cpu.getHL(), "DEC HL Failed: HL<>0x01FF = " + Integer.toHexString(cpu.getHL())),
                () -> assertNotEquals((short)0x0200, cpu.getHL(), "DEC HL Failed: HL still 0x0200 = " + Integer.toHexString(cpu.getHL())),
                () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON")
        );
    }

/*
INC
r_z/(IX+d)/(IY+d)
| Value | Result | S | Z | YF | H | XF | p/V | N | What test?      | Cases
| ----- | ------ | - | - | -- | - | -- | --- | - | --------------- | -----
|  0x7F |  0x80  | 1 | 0 |  0 | 1 |  0 |  1  | 0 | overflow + sign | Case 1
|  0xFF |  0x00  | 0 | 1 |  0 | 1 |  0 |  0  | 0 | zero            | Case 2bis
|  0x0F |  0x10  | 0 | 0 |  0 | 1 |  0 |  0  | 0 | half carry      | Case 3
|  0x1F |  0x20  | 0 | 0 |  1 | 1 |  0 |  0  | 0 | YF set          | Case 4
|  0x07 |  0x08  | 0 | 0 |  0 | 0 |  1 |  0  | 0 | XF set          | Case 6
*/
    @Test
    void testINC_r_y() {
        Z80ForTesting cpu = new Z80ForTesting();
        //Computer compTest = new Computer();
        //compTest.addCPU(cpu);
        //compTest.addMemory(0x0000, new RAMMemory(2));
        //cpu.setComputer(compTest);

        cpu.setB((byte)0x7F);
        long initTState = cpu.getTState();
        cpu.fetch((byte)0x04); // INC B

        assertEquals(4, cpu.getTState()-initTState, "INC B TState Failed");

        cpu.setA(cpu.getB());
        ADC_A_case1(cpu, "INC B Failed: B<>0x80 = ");

        cpu.setC((byte)0xFF);

        cpu.fetch((byte)0x0C); // INC C

        cpu.setA(cpu.getC());
        ADC_A_case2bis(cpu, "INC C Failed: C<>0x00 = ", setRandomCarry(cpu));

        cpu.setD((byte)0x0F);

        cpu.fetch((byte)0x14); // INC D

        cpu.setA(cpu.getD());
        ADC_A_case3bis(cpu, "INC D Failed: D<>0x10 = ", setRandomCarry(cpu));

        cpu.setE((byte)0x1F);

        cpu.fetch((byte)0x1C); // INC E

        cpu.setA(cpu.getE());
        ADC_A_case4bis(cpu, "INC E Failed: E<>0x20 = ", setRandomCarry(cpu));

        cpu.setH((byte)0x07);

        cpu.fetch((byte)0x24); // INC H

        cpu.setA(cpu.getH());
        ADC_A_case6bis(cpu, "INC H Failed: H<>0x08 = ", setRandomCarry(cpu));
    }


    @Test
    void testINC_IX_d() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xDD); // INC (IX+3)
        compTest.poke(0x0001, (byte) 0x34);
        compTest.poke(0x0002, (byte) 0x03);

        cpu.setIX((short) 0x0000);
        cpu.setPC(0x0000);

        compTest.poke(0x0003, (byte) 0x7F);
        long initTState = cpu.getTState();
        cpu.fetch();

        assertEquals(23, cpu.getTState()-initTState, "INC (IX+3) TState Failed");

        cpu.setA(compTest.peek(0x0003));
        ADC_A_case1(cpu, "INC (IX+3) Failed<>0x80");

        compTest.poke(0x0003, (byte) 0xFF);
        cpu.setPC(0x0000);
        cpu.fetch();

        cpu.setA(compTest.peek(0x0003));
        ADC_A_case2bis(cpu, "INC (IX+3) Failed<>0x00", setRandomCarry(cpu));

        compTest.poke(0x0003, (byte) 0x0F);
        cpu.setPC(0x0000);
        cpu.fetch();

        cpu.setA(compTest.peek(0x0003));
        ADC_A_case3bis(cpu, "INC (IX+3) Failed<>0x10", setRandomCarry(cpu));

        compTest.poke(0x0003, (byte) 0x1F);
        cpu.setPC(0x0000);
        cpu.fetch();

        cpu.setA(compTest.peek(0x0003));
        ADC_A_case4bis(cpu, "INC (IX+3) Failed<>0x20", setRandomCarry(cpu));

        compTest.poke(0x0003, (byte) 0x07);
        cpu.setPC(0x0000);
        cpu.fetch();

        cpu.setA(compTest.peek(0x0003));
        ADC_A_case6bis(cpu, "INC (IX+3) Failed<>0x08", setRandomCarry(cpu));
    }

    /*
    Set mínimo de tests (4 valores) para DEC de 8 bits
Valor	Resultado	S	Z	H	p/V	N	Qué se prueba
inicial
0x01	    0x00	0	1	0	0	1	Zero
0x10	    0x0F	0	0	1	0	1	Half-borrow
0x80	    0x7F	0	0	1	1	1	Overflow
0x00	    0xFF	1	0	1	0	1	Sign
     */

    @Test
    void testDEC_IX_d() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xDD); // DEC (IX+3)
        compTest.poke(0x0001, (byte) 0x35);
        compTest.poke(0x0002, (byte) 0x03);

        cpu.setIX((short) 0x0000);
        cpu.setPC(0x0000);

        compTest.poke(0x0003, (byte) 0x00);
        long initTState = cpu.getTState();
        cpu.fetch();

        assertEquals(23, cpu.getTState()-initTState, "DEC (IX+3) TState Failed");

        cpu.setA(compTest.peek(0x0003));
        SBC_A_case1bis(cpu, "DEC (IX+3) Failed: A<>0xFF = ", setRandomCarry(cpu));

        compTest.poke(0x0003, (byte) 0x01);
        cpu.setPC(0x0000);
        cpu.fetch();

        cpu.setA(compTest.peek(0x0003));
        SBC_A_case2bis(cpu, "DEC (IX+3) Failed<>0x00", setRandomCarry(cpu));

        compTest.poke(0x0003, (byte) 0x10);
        cpu.setPC(0x0000);
        cpu.fetch();

        cpu.setA(compTest.peek(0x0003));
        SBC_A_case3bis(cpu, "DEC (IX+3) Failed<>0x0F", setRandomCarry(cpu));

        compTest.poke(0x0003, (byte) 0x80);
        cpu.setPC(0x0000);
        cpu.fetch();

        cpu.setA(compTest.peek(0x0003));
        SBC_A_case4bis(cpu, "DEC (IX+3) Failed<>0x7F", setRandomCarry(cpu));
    }

    @Test
    void testINC_IY_d() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xFD); // INC (IY+3)
        compTest.poke(0x0001, (byte) 0x34);
        compTest.poke(0x0002, (byte) 0x03);

        cpu.setIY((short) 0x0000);
        cpu.setPC(0x0000);

        compTest.poke(0x0003, (byte) 0x7F);
        long initTState = cpu.getTState();
        cpu.fetch();

        assertEquals(23, cpu.getTState()-initTState, "INC (IY+3) TState Failed");

        cpu.setA(compTest.peek(0x0003));
        ADC_A_case1(cpu, "INC (IY+3) Failed<>0x80");

        compTest.poke(0x0003, (byte) 0xFF);
        cpu.setPC(0x0000);
        cpu.fetch();

        cpu.setA(compTest.peek(0x0003));
        ADC_A_case2bis(cpu, "INC (IY+3) Failed<>0x00", setRandomCarry(cpu));

        compTest.poke(0x0003, (byte) 0x0F);
        cpu.setPC(0x0000);
        cpu.fetch();

        cpu.setA(compTest.peek(0x0003));
        ADC_A_case3bis(cpu, "INC (IY+3) Failed<>0x10", setRandomCarry(cpu));

        compTest.poke(0x0003, (byte) 0x1F);
        cpu.setPC(0x0000);
        cpu.fetch();

        cpu.setA(compTest.peek(0x0003));
        ADC_A_case4bis(cpu, "INC (IY+3) Failed<>0x20", setRandomCarry(cpu));

        compTest.poke(0x0003, (byte) 0x07);
        cpu.setPC(0x0000);
        cpu.fetch();

        cpu.setA(compTest.peek(0x0003));
        ADC_A_case6bis(cpu, "INC (IY+3) Failed<>0x08", setRandomCarry(cpu));
    }

    @Test
    void testDEC_IY_d() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(4));
        cpu.setComputer(compTest);
        compTest.poke(0x0000, (byte) 0xFD); // DEC (IY+3)
        compTest.poke(0x0001, (byte) 0x35);
        compTest.poke(0x0002, (byte) 0x03);

        cpu.setIY((short) 0x0000);
        cpu.setPC(0x0000);

        compTest.poke(0x0003, (byte) 0x00);
        long initTState = cpu.getTState();
        cpu.fetch();

        assertEquals(23, cpu.getTState()-initTState, "DEC (IY+3) TState Failed");

        cpu.setA(compTest.peek(0x0003));
        SBC_A_case1bis(cpu, "DEC (IY+3) Failed: A<>0xFF = ", setRandomCarry(cpu));

        compTest.poke(0x0003, (byte) 0x01);
        cpu.setPC(0x0000);
        cpu.fetch();

        cpu.setA(compTest.peek(0x0003));
        SBC_A_case2bis(cpu, "DEC (IY+3) Failed<>0x00", setRandomCarry(cpu));

        compTest.poke(0x0003, (byte) 0x10);
        cpu.setPC(0x0000);
        cpu.fetch();

        cpu.setA(compTest.peek(0x0003));
        SBC_A_case3bis(cpu, "DEC (IY+3) Failed<>0x0F", setRandomCarry(cpu));

        compTest.poke(0x0003, (byte) 0x80);
        cpu.setPC(0x0000);
        cpu.fetch();

        cpu.setA(compTest.peek(0x0003));
        SBC_A_case4bis(cpu, "DEC (IY+3) Failed<>0x7F", setRandomCarry(cpu));
    }

/*
DEC
r_z/(IX+d)/(IY+d)
| Value | Result | S | Z | YF | H | XF | p/V | N | C | What test?         | Cases
| ----- | ------ | - | - | -- | - | -- | --- | - | - | ------------------ | -----
|  0x00 |  0xFF  | 1 | 0 |  1 | 1 |  1 |  0  | 1 | - | sign + YF set      | Case 1
|  0x01 |  0x00  | 0 | 1 |  0 | 0 |  0 |  0  | 1 | - | zero               | Case 2
|  0x10 |  0x0F  | 0 | 0 |  0 | 1 |  1 |  0  | 1 | - | half borrow + XF   | Case 3
|  0x80 |  0x7F  | 0 | 0 |  1 | 1 |  1 |  1  | 1 | - | overflow + YF set  | Case 4
*/
    @Test
    void testDEC_r_y() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte) 0x00);

        long initTState = cpu.getTState();
        cpu.fetch((byte) 0x3D); // DEC A

        assertEquals(4, cpu.getTState()-initTState, "INC A TState Failed");

        SBC_A_case1bis(cpu, "DEC A Failed: A<>0xFF = ", setRandomCarry(cpu));

        cpu.setB((byte) 0x01);

        cpu.fetch((byte) 0x05); // DEC B

        cpu.setA(cpu.getB());
        SBC_A_case2bis(cpu, "DEC A Failed: A<>0x00 = ", setRandomCarry(cpu));

        cpu.setC((byte) 0x10);

        cpu.fetch((byte) 0x0D); // DEC C

        cpu.setA(cpu.getC());
        SBC_A_case3bis(cpu, "DEC A Failed: A<>0x0F = ", setRandomCarry(cpu));

        cpu.setD((byte) 0x80);

        cpu.fetch((byte) 0x15); // DEC D

        cpu.setA(cpu.getD());
        SBC_A_case4bis(cpu, "DEC D Failed: A<>0x7F = ", setRandomCarry(cpu));
    }

    @Test
    void testINC_IX() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(256));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setIX((short) 0xFFFF);
        cpu.setF((byte) 0xFF); // All flags set to test they're not affected
        compTest.poke(0x0000, (byte) 0xDD); // DD prefix
        compTest.poke(0x0001, (byte) 0x23); // INC IX
        long initTState = cpu.getTState();
        cpu.fetch(); // DD prefix + INC IX

        assertAll("INC IX Group",
                () -> assertEquals((short) 0x0000, cpu.getIX(), "INC IX Failed: IX<>0x0000"),
                () -> assertEquals((byte) 0xFF, cpu.getF(), "INC IX Failed: Flags were affected"),

                () -> assertEquals(10, cpu.getTState()-initTState, "INC IX TState Failed")
        );
    }

    @Test
    void testINC_IY() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setIY((short) 0xFFFF);
        cpu.setF((byte) 0xFF); // All flags set to test they're not affected
        compTest.poke(0x0000, (byte) 0xFD); // FD prefix
        compTest.poke(0x0001, (byte) 0x23); // INC IY
        long initTState = cpu.getTState();
        cpu.fetch(); // DD prefix + INC IY

        assertAll("INC IY Group",
                () -> assertEquals((short) 0x0000, cpu.getIY(), "INC IY Failed: IY<>0x0000"),
                () -> assertEquals((byte) 0xFF, cpu.getF(), "INC IY Failed: Flags were affected"),

                () -> assertEquals(10, cpu.getTState()-initTState, "INC IY TState Failed")
        );
    }

    @Test
    void testDEC_IX() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(256));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setIX((short) 0x0001);
        cpu.setF((byte) 0xFF); // All flags set to test they're not affected
        compTest.poke(0x0000, (byte) 0xDD); // DD prefix
        compTest.poke(0x0001, (byte) 0x2B); // DEC IX
        long initTState = cpu.getTState();
        cpu.fetch(); // DD prefix + DEC IX

        assertAll("DEC IX Group",
                () -> assertEquals((short) 0x0000, cpu.getIX(), "DEC IX Failed: IX<>0x0001"),
                () -> assertEquals((byte) 0xFF, cpu.getF(), "DEC IX Failed: Flags were affected"),

                () -> assertEquals(10, cpu.getTState()-initTState, "DEC IX TState Failed")
        );
    }

    @Test
    void testDEC_IY() {
        long initTState;
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setIY((short) 0x0001);
        cpu.setF((byte) 0xFF); // All flags set to test they're not affected
        compTest.poke(0x0000, (byte) 0xFD); // FD prefix
        compTest.poke(0x0001, (byte) 0x2B); // DEC IY
        initTState = cpu.getTState();
        cpu.fetch(); // DD prefix + DEC IY

        assertAll("DEC IY Group",
                () -> assertEquals((short) 0x0000, cpu.getIY(), "DEC IY Failed: IY<>0x0000"),
                () -> assertEquals((byte) 0xFF, cpu.getF(), "DEC IY Failed: Flags were affected"),

                () -> assertEquals(10, cpu.getTState()-initTState, "DEC IY TState Failed")
        );
    }

    @Test
    void testINC_XY() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);

        cpu.setPC(0x0000);
        cpu.setIXH((byte) 0x7F);
        compTest.poke(0x0000, (byte) 0xDD); // DD prefix
        compTest.poke(0x0001, (byte) 0x24); // INC IXH
        long initTState = cpu.getTState();
        cpu.fetch(); // DD prefix + INC IXH

        cpu.setA(cpu.getIXH());
        assertEquals(8, cpu.getTState()-initTState, "INC IXH TState Failed");

        ADC_A_case1(cpu, "INC IXH Failed: IXH<>0x80");

        cpu.setPC(0x0000);
        cpu.setIXL((byte) 0xFF);
        compTest.poke(0x0000, (byte) 0xDD); // DD prefix
        compTest.poke(0x0001, (byte) 0x2C); // INC IXL
        cpu.fetch(); // DD prefix + INC IXL

        cpu.setA(cpu.getIXL());
        ADC_A_case2bis(cpu, "INC IXL Failed: IXL<>00", setRandomCarry(cpu));

        cpu.setPC(0x0000);
        cpu.setIYH((byte) 0x0F);
        compTest.poke(0x0000, (byte) 0xFD); // FD prefix
        compTest.poke(0x0001, (byte) 0x24); // INC IYH
        cpu.fetch(); // FD prefix + INC IYH

        cpu.setA(cpu.getIYH());
        ADC_A_case3bis(cpu, "INC IYH Failed: IYH<>0x10", setRandomCarry(cpu));

        cpu.setPC(0x0000);
        cpu.setIYL((byte) 0x1F);
        compTest.poke(0x0000, (byte) 0xFD); // FD prefix
        compTest.poke(0x0001, (byte) 0x2C); // INC IYL
        cpu.fetch(); // FD prefix + INC IYL

        cpu.setA(cpu.getIYL());
        ADC_A_case4bis(cpu, "INC IYL Failed: IYH<>0x20", setRandomCarry(cpu));

        cpu.setPC(0x0000);
        cpu.setIYL((byte) 0x07);
        compTest.poke(0x0000, (byte) 0xFD); // FD prefix
        compTest.poke(0x0001, (byte) 0x2C); // INC IYL
        cpu.fetch(); // FD prefix + INC IYL

        cpu.setA(cpu.getIYL());
        ADC_A_case6bis(cpu, "INC IYL Failed: IYH<>0x08", setRandomCarry(cpu));
    }

    @Test
    void testDEC_XY() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setIXH((byte) 0x00);
        compTest.poke(0x0000, (byte) 0xDD); // DD prefix
        compTest.poke(0x0001, (byte) 0x25); // DEC IXH
        long initTState = cpu.getTState();
        cpu.fetch(); // DD prefix + DEC IXH

        assertEquals(8, cpu.getTState()-initTState, "DEC IXH TState Failed");

        cpu.setA(cpu.getIXH());
        SBC_A_case1bis(cpu, "DEC IXH Failed: IXH<>0xFF = ", setRandomCarry(cpu));

        cpu.setPC(0x0000);
        cpu.setIXL((byte) 0x01);
        compTest.poke(0x0000, (byte) 0xDD); // DD prefix
        compTest.poke(0x0001, (byte) 0x2D); // DEC IXL
        long initTState2 = cpu.getTState();
        cpu.fetch(); // DD prefix + DEC IXL

        assertEquals(8, cpu.getTState()-initTState2, "DEC IXL TState Failed");

        cpu.setA(cpu.getIXL());
        SBC_A_case2bis(cpu, "DEC IXL Failed: IXL<>0x00 = ", setRandomCarry(cpu));

        cpu.setPC(0x0000);
        cpu.setIYH((byte) 0x10);
        compTest.poke(0x0000, (byte) 0xFD); // FD prefix
        compTest.poke(0x0001, (byte) 0x25); // DEC IYH
        long initTState3 = cpu.getTState();
        cpu.fetch(); // FD prefix + DEC IYH

        assertEquals(8, cpu.getTState()-initTState3, "DEC IYH TState Failed");

        cpu.setA(cpu.getIYH());
        SBC_A_case3bis(cpu, "DEC IYH Failed: IYH<>0x0F", setRandomCarry(cpu));

        cpu.setPC(0x0000);
        cpu.setIYL((byte) 0x80);
        compTest.poke(0x0000, (byte) 0xFD); // FD prefix
        compTest.poke(0x0001, (byte) 0x2D); // DEC IYL
        long initTState4 = cpu.getTState();
        cpu.fetch(); // FD prefix + INC IYL

        assertEquals(8, cpu.getTState()-initTState4, "DEC IYL TState Failed");

        cpu.setA(cpu.getIYL());
        SBC_A_case4bis(cpu, "DEC IYL Failed: IYL<>7F", setRandomCarry(cpu));
    }

/*
NEG
|   A  | Result | S | Z | YF | H | XF | P/v | C | What test?   | Cases
| ---- | ------ | - | - | -- | - | -- | --- | - | ------------ | -----
| 0x00 |  0x00  | 0 | 1 |  0 | 0 |  0 |  0  | 0 | zero         | Case 1
| 0x01 |  0xFF  | 1 | 0 |  1 | 1 |  1 |  0  | 1 | general case | Case 2
| 0x80 |  0x80  | 1 | 0 |  0 | 0 |  0 |  1  | 1 | overflow     | Case 3
| 0x08 |  0xF8  | 1 | 0 |  1 | 1 |  1 |  0  | 1 | XH=1         | Case 4
| 0x20 |  0xE0  | 1 | 0 |  1 | 0 |  0 |  0  | 1 | YH=1         | Case 5
*/
    @Test
    void testNEG() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(16));
        compTest.poke(0x0000, (byte) 0xED); // ED prefix
        compTest.poke(0x0001, (byte) 0x44); // NEG
        cpu.setComputer(compTest);
        cpu.reset();

        cpu.setA((byte) 0x00);

        long initTState = cpu.getTState();
        cpu.fetch(); // ED prefix + NEG

        assertAll("NEG Group Case 1",
                () -> assertEquals((byte) 0x00, cpu.getA(), "NEG Failed: A<>0x00 (A=" + cpu.getA() + ")"),
                () -> assertFalse(cpu.getSF(), "NEG Failed: S Flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertTrue(cpu.getZF(), "NEG Failed: Z Flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "NEG Failed: YF Flag must be OFF (YF=" + cpu.getYF() + ")"),
                () -> assertFalse(cpu.getHF(), "NEG Failed: H Flag must be OFF (H=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getXF(), "NEG Failed: XF Flag must be OFF (XF=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getPF(), "NEG Failed: P Flag must be OFF (P=" + cpu.getPF() + ")"),
                () -> assertFalse(cpu.getCF(), "NEG Failed: C Flag must be OFF (C=" + cpu.getCF() + ")"),

                () -> assertEquals(8, cpu.getTState()-initTState, "NEG TState Failed")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x01);

        cpu.fetch(); // ED prefix + NEG

        assertAll("NEG Group Case 2",
                () -> assertEquals((byte) 0xFF, cpu.getA(), "NEG Failed: A<>0xFF (A=" + cpu.getA() + ")"),
                () -> assertTrue(cpu.getSF(), "NEG Failed: S Flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "NEG Failed: Z Flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getYF(), "NEG Failed: YF Flag must be ON (YF=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "NEG Failed: H Flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertTrue(cpu.getXF(), "NEG Failed: XF Flag must be ON (XF=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getPF(), "NEG Failed: P Flag must be OFF (P=" + cpu.getPF() + ")"),
                () -> assertTrue(cpu.getCF(), "NEG Failed: C Flag must be ON (C=" + cpu.getCF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x80);

        cpu.fetch(); // ED prefix + NEG

        assertAll("NEG Group Case 3",
                () -> assertEquals((byte) 0x80, cpu.getA(), "NEG Failed: A<>0x80 (A=" + cpu.getA() + ")"),
                () -> assertTrue(cpu.getSF(), "NEG Failed: S Flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "NEG Failed: Z Flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "NEG Failed: YF Flag must be OFF (YF=" + cpu.getYF() + ")"),
                () -> assertFalse(cpu.getHF(), "NEG Failed: H Flag must be OFF (H=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getXF(), "NEG Failed: XF Flag must be OFF (XF=" + cpu.getXF() + ")"),
                () -> assertTrue(cpu.getPF(), "NEG Failed: P Flag must be ON (P=" + cpu.getPF() + ")"),
                () -> assertTrue(cpu.getCF(), "NEG Failed: C Flag must be ON (C=" + cpu.getCF() + ")")
        );

        cpu.setPC(0x0000);
        cpu.setA((byte) 0x08);

        cpu.fetch(); // ED prefix + NEG

        assertAll("NEG Group Case 4",
                () -> assertEquals((byte) 0xF8, cpu.getA(), "NEG Failed: A<>0xF8 (A=" + cpu.getA() + ")"),
                () -> assertTrue(cpu.getSF(), "NEG Failed: S Flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "NEG Failed: Z Flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getYF(), "NEG Failed: YF Flag must be ON (YF=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "NEG Failed: H Flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertTrue(cpu.getXF(), "NEG Failed: XF Flag must be ON (XF=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getPF(), "NEG Failed: P Flag must be OFF (P=" + cpu.getPF() + ")"),
                () -> assertTrue(cpu.getCF(), "NEG Failed: C Flag must be ON (C=" + cpu.getCF() + ")")
        );

/*        cpu.setPC(0x0000);
        cpu.setA((byte) 0x20);

        cpu.fetch(); // ED prefix + NEG

        assertAll("NEG Group Case 4",
                () -> assertEquals((byte) 0x, cpu.getA(), "NEG Failed: A<>0xF8 (A=" + cpu.getA() + ")"),
                () -> assertTrue(cpu.getSF(), "NEG Failed: S Flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "NEG Failed: Z Flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getYF(), "NEG Failed: YF Flag must be ON (YF=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "NEG Failed: H Flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertTrue(cpu.getXF(), "NEG Failed: XF Flag must be ON (XF=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getVF(), "NEG Failed: V Flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getCF(), "NEG Failed: C Flag must be ON (C=" + cpu.getCF() + ")")
        );*/
    }

    @Test
    void testCPL() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setA((byte) 0x0F);
        cpu.setF((byte) 0x00);

        long initTState = cpu.getTState();
        cpu.CPL();

        assertAll("CPL Group",
                () -> assertEquals((byte) 0xF0, cpu.getA(), "CPL Failed: A<>0xF0 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0x0F, cpu.getA(), "CPL Failed: A still 0x0F = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getHF(), "CPL Failed: H Flag not set"),
                () -> assertTrue(cpu.getNF(), "CPL Failed: N Flag not set"),
                () -> assertEquals(0, cpu.getF() & 0b1110_1101, "CPL Failed: F Flags affected: " + Integer.toHexString(cpu.getF())),
                () -> assertEquals(4, cpu.getTState()-initTState, "CPL TState Failed")
        );

        cpu.setA((byte) 0xFF);
        cpu.setF((byte) 0x00);

        cpu.CPL();

        assertAll("CPL Group Z must not be affected",
                () -> assertEquals((byte) 0x00, cpu.getA(), "CPL Failed: A<>0x00 = " + Integer.toHexString(cpu.getA())),
                () -> assertNotEquals((byte) 0xFFF, cpu.getA(), "CPL Failed: A still 0xFF = " + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getHF(), "CPL Failed: H Flag not set"),
                () -> assertTrue(cpu.getNF(), "CPL Failed: N Flag not set"),
                () -> assertEquals(0, cpu.getF() & 0b1110_1101, "CPL Failed: F Flags affected: " + Integer.toHexString(cpu.getF()))
        );
    }

    @Test
    void testSCF() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setCF();

        long initTState = cpu.getTState();
        cpu.SCF();

        assertTrue(cpu.getCF(), "SCF Failed: Carry flag must be ON");
        assertEquals(4, cpu.getTState()-initTState, "INC IXH TState Failed");
    }

    @Test
    void testCCF() {
        Z80ForTesting cpu = new Z80ForTesting();

        cpu.setCF();

        long initTState = cpu.getTState();
        cpu.CCF();

        assertFalse(cpu.getCF(), "CCF Failed: Carry flag must be OFF");

        assertEquals(4, cpu.getTState()-initTState, "CCF TState Failed");

        cpu.resCF();

        cpu.CCF();

        assertTrue(cpu.getCF(), "CCF Failed: Carry flag must be ON");
    }

    @Test
    void testADC_A_XY() {
        long initTState;
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);

        compTest.poke(0, (byte) 0xDD);
        compTest.poke(1, (byte) 0x8C); // ADC A, IXH

        cpu.setPC(0x0000);
        cpu.setA((byte)0x7F);
        cpu.setIXH((byte) 0x00);
        cpu.setCF();
        initTState = cpu.getTState();
        cpu.fetch();

        assertEquals(8, cpu.getTState()-initTState, "ADC A, IXH TState Failed");

        ADC_A_case1(cpu, "ADC A, IXH; C=1 Failed: A<>0x80 = ");

        compTest.poke(0, (byte) 0xFD);
        compTest.poke(1, (byte) 0x8C); // ADC A, IYH

        cpu.setPC(0x0000);
        cpu.setA((byte)0xFF);
        cpu.setIYH((byte)0x00);
        cpu.setCF();

        cpu.fetch();

        ADC_A_case2(cpu, "ADC A, IYH; C=1 Failed: A<>0x00");

        compTest.poke(0, (byte) 0xDD);
        compTest.poke(1, (byte) 0x8D); // ADC A, IXL

        cpu.setPC(0x0000);
        cpu.setA((byte)0x0F);
        cpu.setIXL((byte) 0x00);
        cpu.setCF();

        cpu.fetch();

        ADC_A_case3(cpu, "ADC A, IXL; C=1 Failed: A<>0x10 = ");

        compTest.poke(0, (byte) 0xFD);
        compTest.poke(1, (byte) 0x8D); // ADC A, IYL

        cpu.setPC(0x0000);
        cpu.setA((byte)0x1F);
        cpu.setIYL((byte)0x01);
        cpu.resCF();

        cpu.fetch();

        ADC_A_case4(cpu, "ADC A, IYL; C=0 Failed: A<>0x20");
    }

    @Test
    void testDAA() {
        long initTState;
        Z80ForTesting cpu = new Z80ForTesting();

// --- Caso 1: A=0x09 (sin ajustes, resultado 0x09) ---
        cpu.setA((byte) 0x09);
        cpu.resNF();
        cpu.resHF();
        cpu.resCF();
        initTState = cpu.getTState();
        cpu.fetch((byte)0x27);

        assertAll(
                () -> assertEquals((byte) 0x09, cpu.getA(), "DAA Failed: A<>0x09"),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF"),
                () -> assertFalse(cpu.getYF(), "YF flag must be OFF"),
                () -> assertFalse(cpu.getHF(), "Half-carry flag must be OFF"),
                () -> assertTrue(cpu.getXF(), "XF flag must be ON"),
                () -> assertFalse(cpu.getNF(), "N flag must be OFF"),
                () -> assertTrue(cpu.getPF(), "Parity flag must be ON (0x09 has even bits=2)"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF"),
                
                () -> assertEquals(4, cpu.getTState()-initTState, "DAA TState Failed")
        );

// --- Caso 2: A=0x0A (ajuste +6, resultado 0x10) ---
        cpu.setA((byte) 0x0A);
        cpu.resNF();
        cpu.resHF();
        cpu.resCF();
        cpu.fetch((byte)0x27);

        assertAll(
                () -> assertEquals((byte) 0x10, cpu.getA(), "DAA Failed: A<>0x10"),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF"),
                () -> assertFalse(cpu.getYF(), "YF flag must be OFF"),
                () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON"),
                () -> assertFalse(cpu.getXF(), "XF flag must be OFF"),
                () -> assertFalse(cpu.getNF(), "N flag must be OFF"),
                () -> assertFalse(cpu.getPF(), "Parity flag must be OFF (0x10 has odd bits=1)"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF")
        );

// --- Caso 3: A=0x9A (ADD con Carry final, resultado 0x00, CF=1) ---
        cpu.setA((byte) 0x9A);
        cpu.resNF();
        cpu.resHF();
        cpu.resCF();
        cpu.fetch((byte)0x27);

        assertAll(
                () -> assertEquals((byte) 0x00, cpu.getA(), "DAA Failed: A<>0x00"),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF"),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON"),
                () -> assertFalse(cpu.getYF(), "YF flag must be OFF"),
                () -> assertTrue(cpu.getPF(), "Parity flag must be ON (0x00 has even parity)"),
                () -> assertFalse(cpu.getXF(), "XF flag must be OFF"),
                () -> assertFalse(cpu.getNF(), "N flag must be OFF"),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON")
        );

// --- Caso 4: SUB sin half ni carry (resultado inalterado) ---
        cpu.setA((byte) 0x99);
        cpu.setNF();
        cpu.resHF();
        cpu.resCF();
        cpu.fetch((byte)0x27);

        assertAll(
                () -> assertEquals((byte) 0x99, cpu.getA(), "DAA Failed: A<>0x99"),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (bit7=1)"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF"),
                () -> assertFalse(cpu.getYF(), "YF flag must be OFF"),
                () -> assertFalse(cpu.getHF(), "Half-carry flag must be OFF"),
                () -> assertTrue(cpu.getXF(), "XF flag must be ON"),
                () -> assertTrue(cpu.getNF(), "N flag must be ON"),
                () -> assertTrue(cpu.getPF(), "Parity flag must be ON (0x99 has odd bits=4?)"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF")
        );

// --- Caso 5: SUB con half carry (resultado 0x94) ---
        cpu.setA((byte) 0x9A);
        cpu.setNF();
        cpu.setHF();
        cpu.resCF();
        cpu.fetch((byte)0x27);

        assertAll(
                () -> assertEquals((byte) 0x34, cpu.getA(), "DAA Failed: A<>0x94"),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF"),
                () -> assertTrue(cpu.getYF(), "YF flag must be ON"),
                () -> assertFalse(cpu.getHF(), "Half-carry flag must be OFF"),
                () -> assertFalse(cpu.getXF(), "XF flag must be OFF"),
                () -> assertTrue(cpu.getNF(), "N flag must be ON"),
                () -> assertFalse(cpu.getPF(), "Parity flag must be OFF (0x94 has odd bits)"),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON")
        );

// --- Caso 6: SUB con carry + half carry (resultado 0x00, CF=1) ---
        cpu.setA((byte) 0x9A);
        cpu.setNF();
        cpu.setHF();
        cpu.setCF();
        cpu.fetch((byte)0x27);

        assertAll(
                () -> assertEquals((byte) 0x34, cpu.getA(), "DAA Failed: A<>0x00"),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF"),
                //() -> assertTrue(cpu.getZF(), "Zero flag must be ON"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF"),
                () -> assertTrue(cpu.getYF(), "YF flag must be ON"),
                //() -> assertTrue(cpu.getHF(), "Half-carry flag must be ON"),
                () -> assertFalse(cpu.getHF(), "Half-carry flag must be OFF"),
                () -> assertFalse(cpu.getXF(), "XF flag must be OFF"),
                () -> assertTrue(cpu.getNF(), "N flag must be ON"),
                //() -> assertTrue(cpu.getPF(), "Parity flag must be ON (0x00 has even bits)"),
                () -> assertFalse(cpu.getPF(), "Parity flag must be ON (0x00 has even bits)"),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON")
        );
    }

    @Test
    void testSUB_XY() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);

        compTest.poke(0, (byte) 0xDD);
        compTest.poke(1, (byte) 0x94); // SUB IXH

        cpu.setPC(0x0000);
        cpu.setA((byte)0x00);
        cpu.setIXH((byte) 0x01);

        long initTState = cpu.getTState();
        cpu.fetch();

        assertEquals(8, cpu.getTState()-initTState, "SUB IXH TState Failed");

        SBC_A_case1(cpu, "SUB IXH Failed: A<>0xFF");

        compTest.poke(0, (byte) 0xFD);
        compTest.poke(1, (byte) 0x94); // SUB IYH

        cpu.setPC(0x0000);
        cpu.setA((byte)0x01);
        cpu.setIYH((byte)0x01);
        
        cpu.fetch();

        SBC_A_case2(cpu, "SUB IYH Failed: A<>0xFF");

        compTest.poke(0, (byte) 0xDD);
        compTest.poke(1, (byte) 0x95); // SUB IXL

        cpu.setPC(0x0000);
        cpu.setA((byte)0x10);
        cpu.setIXL((byte) 0x01);

        long initTState2 = cpu.getTState();
        cpu.fetch();

        assertEquals(8, cpu.getTState()-initTState2, "SUB IXL TState Failed");

        SBC_A_case3(cpu, "SUB IXL Failed: A<>0x0F");

        compTest.poke(0, (byte) 0xFD);
        compTest.poke(1, (byte) 0x95); // SUB IYL

        cpu.setPC(0x0000);
        cpu.setA((byte)0x80);
        cpu.setIYL((byte)0x01);

        cpu.fetch();

        SBC_A_case4(cpu, "SUB IYL Failed: A<>0x7F");
    }

    @Test
    void testSBC_XY() {
        Z80ForTesting cpu = new Z80ForTesting();
        Computer compTest = new Computer();
        compTest.addCPU(cpu);
        compTest.addMemory(0x0000, new RAMMemory(2));
        cpu.setComputer(compTest);

        compTest.poke(0, (byte) 0xDD);
        compTest.poke(1, (byte) 0x9C); // SBC A, IXH

        cpu.setPC(0x0000);
        cpu.setA((byte)0x00);
        cpu.setIXH((byte) 0x01);
        cpu.resCF();
        long initTState3 = cpu.getTState();

        cpu.fetch();

        assertEquals(8, cpu.getTState()-initTState3, "SBC A, IXH TState Failed");

        SBC_A_case1(cpu, "SBC A, IXH Failed: A<>0xFF");

        compTest.poke(0, (byte) 0xFD);
        compTest.poke(1, (byte) 0x9C); // SBC A, IYH

        cpu.setPC(0x0000);
        cpu.setA((byte)0x01);
        cpu.setIYH((byte)0x01);
        cpu.resCF();

        cpu.fetch();

        SBC_A_case2(cpu, "SBC A, IYH Failed: A<>0x00");

        compTest.poke(0, (byte) 0xDD);
        compTest.poke(1, (byte) 0x9D); // SBC A, IXL

        cpu.setPC(0x0000);
        cpu.setA((byte)0x10);
        cpu.setIXL((byte) 0x01);
        cpu.resCF();

        cpu.fetch();

        SBC_A_case3(cpu, "SBC A, IXL Failed: A<>0x0F");

        compTest.poke(0, (byte) 0xFD);
        compTest.poke(1, (byte) 0x9D); // SBC A, IYL

        cpu.setPC(0x0000);
        cpu.setA((byte)0x80);
        cpu.setIYL((byte)0x01);
        cpu.resCF();

        cpu.fetch();

        SBC_A_case4(cpu, "SBC A, IYL Failed: A<>0x7F");

        compTest.poke(0, (byte) 0xFD);
        compTest.poke(1, (byte) 0x9D); // SBC A, IYL

        cpu.setPC(0x0000);
        cpu.setA((byte)0x00);
        cpu.setIYL((byte)0x00);
        cpu.setCF();

        cpu.fetch();

        SBC_A_case5(cpu, "SBC A, IYL Failed: A<>0xFF");
    }

    private static void SBC_A_case1(Z80ForTesting cpu, String msg) {
        assertAll(
                () -> assertEquals((byte) 0xFF, cpu.getA(),  msg + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getYF(), "YF flag must be ON (Y=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Halfcarry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertTrue(cpu.getXF(), "XF flag must be ON (X=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "Negative flag must be ON (N=" + cpu.getNF() + ")"),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")")
        );
    }

    private static void SBC_A_case1bis(Z80ForTesting cpu, String msg, int randomCarry) {
        assertAll(
                () -> assertEquals((byte) 0xFF, cpu.getA(),  msg + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getYF(), "YF flag must be ON (Y=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Halfcarry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertTrue(cpu.getXF(), "XF flag must be ON (X=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "N flag must be ON (N=" + cpu.getNF() + ")"),
                () -> assertEquals(0, randomCarry ^ (cpu.getCF()?1:0), "Carry flag was affected (C=" + cpu.getCF() + ")")
        );
    }

    private static void SBC_A_case2(Z80ForTesting cpu, String msg) {
        assertAll(
                () -> assertEquals((byte) 0x00, cpu.getA(), msg + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF flag must be OFF (Y=" + cpu.getYF() + ")"),
                () -> assertFalse(cpu.getHF(), "Halfcarry flag must be OFF (H=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getXF(), "XF flag must be OFF (X=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")")
        );
    }

    private static void SBC_A_case2bis(Z80ForTesting cpu, String msg, int randomCarry) {
        assertAll(
                () -> assertEquals((byte) 0x00, cpu.getA(), msg + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF flag must be OFF (Y=" + cpu.getYF() + ")"),
                () -> assertFalse(cpu.getHF(), "Halfcarry flag must be OFF (H=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getXF(), "XF flag must be OFF (X=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "N flag must be ON (N=" + cpu.getNF() + ")"),
                () -> assertEquals(0, randomCarry ^ (cpu.getCF()?1:0), "Carry flag was affected (C=" + cpu.getCF() + ")")
        );
    }

    private static void SBC_A_case3(Z80ForTesting cpu, String msg) {
        assertAll(
                () -> assertEquals((byte) 0x0F, cpu.getA(), msg + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF flag must be OFF (Y=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Halfcarry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertTrue(cpu.getXF(), "XF flag must be ON (X=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")")
        );
    }

    private static void SBC_A_case3bis(Z80ForTesting cpu, String msg, int randomCarry) {
        assertAll(
                () -> assertEquals((byte) 0x0F, cpu.getA(), msg + " = (" + Integer.toHexString(cpu.getA()) + ")"),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF flag must be OFF (Y=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Halfcarry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertTrue(cpu.getXF(), "XF flag must be ON (X=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "N flag must be ON (N=" + cpu.getNF() + ")"),
                () -> assertEquals(0, randomCarry ^ (cpu.getCF()?1:0), "Carry flag was affected (C=" + cpu.getCF() + ")")
        );
    }

    private static void SBC_A_case4(Z80ForTesting cpu, String msg) {
        assertAll(
                () -> assertEquals((byte) 0x7F, cpu.getA(), msg + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getYF(), "YF flag must be ON (Y=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Halfcarry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertTrue(cpu.getXF(), "XF flag must be ON (X=" + cpu.getXF() + ")"),
                () -> assertTrue(cpu.getVF(), "Overflow flag must be ON (V=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")")
        );
    }

    private static void SBC_A_case4bis(Z80ForTesting cpu, String msg, int randomCarry) {
        assertAll(
                () -> assertEquals((byte) 0x7F, cpu.getA(), msg + Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getYF(), "YF flag must be ON (Y=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Halfcarry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertTrue(cpu.getXF(), "XF flag must be ON (X=" + cpu.getXF() + ")"),
                () -> assertTrue(cpu.getVF(), "Overflow flag must be ON (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getNF(), "N flag must be ON (N=" + cpu.getNF() + ")"),
                () -> assertEquals(0, randomCarry ^ (cpu.getCF()?1:0), "Carry flag was affected (C=" + cpu.getCF() + ")")
        );
    }

    private static void SBC_A_case5(Z80ForTesting cpu, String msg) {
        assertAll(
                () -> assertEquals((byte) 0xFF, cpu.getA(), msg + Integer.toHexString(cpu.getA())),
                () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getYF(), "YF flag must be ON (Y=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Halfcarry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertTrue(cpu.getXF(), "XF flag must be ON (X=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")")
        );
    }

/*
ADD/ADC
n/r_z/(IX+d)/(IY+d)
| A    |   n  |  C  | Result | S | Z | YF | H | XF | p/V | N | C | What test?         | Cases
| ---- | ---- | --- | ------ | - | - | -- | - | -- | --- | - | - | ------------------ | -----
| 0x7F | 0x00 |  1  |  0x80  | 1 | 0 |  0 | 1 |  0 |  1  | 0 | 0 | overflow por carry | Case 1
| 0xFF | 0x00 |  1  |  0x00  | 0 | 1 |  0 | 1 |  0 |  0  | 0 | 1 | zero + carry       | Case 2
| 0x0F | 0x00 |  1  |  0x10  | 0 | 0 |  0 | 1 |  0 |  0  | 0 | 0 | half carry         | Case 3
| 0x1F | 0x01 |  0  |  0x20  | 0 | 0 |  1 | 1 |  0 |  0  | 0 | 0 | YF set             | Case 4
| 0x80 | 0x80 |  0  |  0x00  | 0 | 1 |  0 | 0 |  0 |  1  | 0 | 1 | overflow + carry   | Case 5
| 0x04 | 0x04 |  0  |  0x08  | 0 | 0 |  0 | 0 |  1 |  0  | 0 | 0 | XF set             | Case 6
 */
        private static void ADC_A_case1(Z80ForTesting cpu, String msg) {
            assertAll(
                    () -> assertEquals((byte) 0x80, cpu.getA(), Integer.toHexString(cpu.getA())),
                    () -> assertTrue(cpu.getSF(), "Sign flag must be ON (S=" + cpu.getSF() + ")"),
                    () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                    () -> assertFalse(cpu.getYF(), "YF flag must be OFF (YF=" + cpu.getYF() + ")"),
                    () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON (H=" + cpu.getHF() + ")"),
                    () -> assertFalse(cpu.getXF(), "XF flag must be OFF (XF=" + cpu.getXF() + ")"),
                    () -> assertTrue(cpu.getVF(), "Overflow flag must be ON (V=" + cpu.getVF() + ")"),
                    () -> assertFalse(cpu.getNF(), "N flag must be OFF (NF=" + cpu.getNF() + ")"),
                    () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")")
            );
        }

        private static void ADC_A_case2(Z80ForTesting cpu, String msg) {
            assertAll(
                    () -> assertEquals((byte) 0x00, cpu.getA(), Integer.toHexString(cpu.getA())),
                    () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                    () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                    () -> assertFalse(cpu.getYF(), "YF flag must be OFF (YF=" + cpu.getYF() + ")"),
                    () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON (H=" + cpu.getHF() + ")"),
                    () -> assertFalse(cpu.getXF(), "XF flag must be OFF (XF=" + cpu.getXF() + ")"),
                    () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                    () -> assertFalse(cpu.getNF(), "N flag must be OFF (NF=" + cpu.getNF() + ")"),
                    () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")")
            );
        }

    private static void ADC_A_case2bis(Z80ForTesting cpu, String msg, int randomCarry) {
        assertAll(
                () -> assertEquals((byte) 0x00, cpu.getA(), Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF flag must be OFF (YF=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getXF(), "XF flag must be OFF (XF=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getNF(), "N flag must be OFF (NF=" + cpu.getNF() + ")"),
                () -> assertEquals(0, randomCarry ^ (cpu.getCF()?1:0), "Carry flag was affected (C=" + cpu.getCF() + ")")
        );
    }

        private static void ADC_A_case3(Z80ForTesting cpu, String msg) {
            assertAll(
                    () -> assertEquals((byte) 0x10, cpu.getA(), Integer.toHexString(cpu.getA())),
                    () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                    () -> assertFalse(cpu.getZF(), "Zero flag must be OF (Z=" + cpu.getZF() + ")"),
                    () -> assertFalse(cpu.getYF(), "YF flag must be OFF (YF=" + cpu.getYF() + ")"),
                    () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON (H=" + cpu.getHF() + ")"),
                    () -> assertFalse(cpu.getXF(), "XF flag must be OFF (XF=" + cpu.getXF() + ")"),
                    () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                    () -> assertFalse(cpu.getNF(), "N flag must be OFF (NF=" + cpu.getNF() + ")"),
                    () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")")
            );
        }

    private static void ADC_A_case3bis(Z80ForTesting cpu, String msg, int randomCarry) {
        assertAll(
                () -> assertEquals((byte) 0x10, cpu.getA(), Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OF (Z=" + cpu.getZF() + ")"),
                () -> assertFalse(cpu.getYF(), "YF flag must be OFF (YF=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON (H=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getXF(), "XF flag must be OFF (XF=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getNF(), "N flag must be OFF (NF=" + cpu.getNF() + ")"),
                () -> assertEquals(0, randomCarry ^ (cpu.getCF()?1:0), "Carry flag was affected (C=" + cpu.getCF() + ")")
        );
    }

        private static void ADC_A_case4(Z80ForTesting cpu, String msg) {
            assertAll(
                    () -> assertEquals((byte) 0x20, cpu.getA(), Integer.toHexString(cpu.getA())),
                    () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                    () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                    () -> assertTrue(cpu.getYF(), "YF flag must be ON (YF=" + cpu.getYF() + ")"),
                    () -> assertTrue(cpu.getHF(), "Half-carry flag must be ON (H=" + cpu.getHF() + ")"),
                    () -> assertFalse(cpu.getXF(), "XF flag must be OFF (XF=" + cpu.getXF() + ")"),
                    () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                    () -> assertFalse(cpu.getNF(), "N flag must be OFF (NF=" + cpu.getNF() + ")"),
                    () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")")
            );
        }

    private static void ADC_A_case4bis(Z80ForTesting cpu, String msg, int randomCarry) {
        assertAll(
                () -> assertEquals((byte) 0x20, cpu.getA(), Integer.toHexString(cpu.getA())),
                () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                () -> assertTrue(cpu.getYF(), "YF flag must be ON (YF=" + cpu.getYF() + ")"),
                () -> assertTrue(cpu.getHF(), "Half-carry flag must be OFF (H=" + cpu.getHF() + ")"),
                () -> assertFalse(cpu.getXF(), "XF flag must be OFF (XF=" + cpu.getXF() + ")"),
                () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                () -> assertFalse(cpu.getNF(), "N flag must be OFF (NF=" + cpu.getNF() + ")"),
                () -> assertEquals(0, randomCarry ^ (cpu.getCF()?1:0), "Carry flag was affected (C=" + cpu.getCF() + ")")
        );
    }

        private static void ADC_A_case5(Z80ForTesting cpu, String msg) {
            assertAll(
                    () -> assertEquals((byte) 0x00, cpu.getA(), Integer.toHexString(cpu.getA())),
                    () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                    () -> assertTrue(cpu.getZF(), "Zero flag must be ON (Z=" + cpu.getZF() + ")"),
                    () -> assertFalse(cpu.getYF(), "YF flag must be OFF (YF=" + cpu.getYF() + ")"),
                    () -> assertFalse(cpu.getHF(), "Half-carry flag must be OFF (H=" + cpu.getHF() + ")"),
                    () -> assertFalse(cpu.getXF(), "XF flag must be OFF (XF=" + cpu.getXF() + ")"),
                    () -> assertTrue(cpu.getVF(), "Overflow flag must be ON (V=" + cpu.getVF() + ")"),
                    () -> assertFalse(cpu.getNF(), "N flag must be OFF (NF=" + cpu.getNF() + ")"),
                    () -> assertTrue(cpu.getCF(), "Carry flag must be ON (C=" + cpu.getCF() + ")")
            );
        }

        private static void ADC_A_case6(Z80ForTesting cpu, String msg) {
            assertAll(
                    () -> assertEquals((byte) 0x08, cpu.getA(), Integer.toHexString(cpu.getA())),
                    () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                    () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                    () -> assertFalse(cpu.getYF(), "YF flag must be OFF (YF=" + cpu.getYF() + ")"),
                    () -> assertFalse(cpu.getHF(), "Half-carry flag must be OFF (H=" + cpu.getHF() + ")"),
                    () -> assertTrue(cpu.getXF(), "XF flag must be ON (XF=" + cpu.getXF() + ")"),
                    () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                    () -> assertFalse(cpu.getNF(), "N flag must be OFF (NF=" + cpu.getNF() + ")"),
                    () -> assertFalse(cpu.getCF(), "Carry flag must be OFF (C=" + cpu.getCF() + ")")
            );
        }

        private static void ADC_A_case6bis(Z80ForTesting cpu, String msg, int randomCarry) {
            assertAll(
                    () -> assertEquals((byte) 0x08, cpu.getA(), Integer.toHexString(cpu.getA())),
                    () -> assertFalse(cpu.getSF(), "Sign flag must be OFF (S=" + cpu.getSF() + ")"),
                    () -> assertFalse(cpu.getZF(), "Zero flag must be OFF (Z=" + cpu.getZF() + ")"),
                    () -> assertFalse(cpu.getYF(), "YF flag must be OFF (YF=" + cpu.getYF() + ")"),
                    () -> assertFalse(cpu.getHF(), "Half-carry flag must be OFF (H=" + cpu.getHF() + ")"),
                    () -> assertTrue(cpu.getXF(), "XF flag must be ON (XF=" + cpu.getXF() + ")"),
                    () -> assertFalse(cpu.getVF(), "Overflow flag must be OFF (V=" + cpu.getVF() + ")"),
                    () -> assertFalse(cpu.getNF(), "N flag must be OFF (NF=" + cpu.getNF() + ")"),
                    () -> assertEquals(0, randomCarry ^ (cpu.getCF()?1:0), "Carry flag was affected (C=" + cpu.getCF() + ")")
            );
        }

        private int setRandomCarry(Z80ForTesting cpu) {
            boolean randomCarry = Math.random() < 0.5;
            if(randomCarry) {
                cpu.setCF();
                return 1;
            } else {
                cpu.resCF();
                return 0;
            }
        }

        @Test
        @Disabled("testParity")
        void testParity() {
            for(byte i = 0; i<255; i++) {
                int c = Integer.bitCount(i);

                BitSet pA = BitSet.valueOf(new byte[]{i}); // new heap object cada vez
                System.out.println(i + ": " + c + " " + ((pA.cardinality() % 2) == 0));
            }
        }
    }