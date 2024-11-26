package com.github.jagarsoft;

public class Disassembler extends Z80 implements Z80OpCode {

    public void NOP(byte z, byte y) {
        System.out.println("NOP");
    }

    public void EX_AF_AF_(byte z, byte y) {
        System.out.println("EX AF, AF'");
    }

    public void DJNZ(byte y, byte d) {
        System.out.println("DJNZ " + d);
    }

    public void JR(byte y, byte d) {
        System.out.println("JR " + d);
    }

    public void JR_cc(byte y, byte d) {
        System.out.println("JR " + cc[y-4] + ", " + d);
    }
}
