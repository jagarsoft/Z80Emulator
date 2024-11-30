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

    @Override
    public void LD_rp_p_nn() {
        byte Z = currentComp.peek(PC++);
        byte W = currentComp.peek(PC++);
        System.out.println("LD "+rp[p]+", "+getWord(W, Z));
    }

    @Override
    public void ADD_HL_rp_p() {
        System.out.println("ADD HL, "+rp[p]);
    }

    @Override
    public void LD_BC_A() {
        System.out.println("LD (BC), A");
    }
    @Override
    public void LD_DE_A() {
        System.out.println("LD (DE), A");
    }

    public void LD_nn_HL() {
        byte Z = currentComp.peek(PC++);
        byte W = currentComp.peek(PC++);
        System.out.println("LD ("+getWord(W, Z)+"), HL");
    }

    public void LD_nn_A() {
        byte Z = currentComp.peek(PC++);
        byte W = currentComp.peek(PC++);
        System.out.println("LD ("+getWord(W, Z)+"), A");
    }

    public void LD_A_BC() {
        System.out.println("LD A, (BC)");
    }

    public void LD_A_DE() {
        System.out.println("LD A, (DE)");
    }

    public void LD_HL_nn() {
        byte Z = currentComp.peek(PC++);
        byte W = currentComp.peek(PC++);
        System.out.println("LD HL, (" + getWord(W, Z) + ")");
    }

    public void LD_A_nn() {
        byte Z = currentComp.peek(PC++);
        byte W = currentComp.peek(PC++);
        System.out.println("LD A, (" + getWord(W, Z) + ")");
    }

    public void INC_rp_p() {
        System.out.println("INC "+rp[p]);
    }

    public void DEC_rp_p() {
        System.out.println("DEC "+rp[p]);
    }
}
