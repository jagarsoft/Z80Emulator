package com.github.jagarsoft;

public class Disassembler extends Z80 implements Z80OpCode {

    public void NOP() {
        System.out.println("NOP");
    }

    public void EX_AF_AF_() {
        System.out.println("EX AF, AF'");
    }

    public void DJNZ() {
        byte d = currentComp.peek(PC++);
        System.out.println("DJNZ " + d);
    }

    public void JR() {
        byte d = currentComp.peek(PC++);
        System.out.println("JR " + d);
    }

    public void JR_cc() {
        byte d = currentComp.peek(PC++);
        System.out.println("JR " + cc[y-4] + ", " + d);
    }
}
