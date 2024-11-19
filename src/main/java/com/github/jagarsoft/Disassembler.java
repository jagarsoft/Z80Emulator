package com.github.jagarsoft;

public class Disassembler extends Z80 implements Z80OpCode {
    static int offset = 0;

    // cuando extienda de z80 todos los metodos de abajo sobrecargaran a los de la clase z80
    public int NOP(byte z, byte y) {
        System.out.println("NOP");
        return 0;
    }

    public int EX_AF_AF_(byte z, byte y) {
        System.out.println("EX AF, AF'");
        return 0;
    }

    public int DJNZ(byte y, byte d) {
        if( offset == 0 ){
            return ++offset;
        }

        System.out.println("DJNZ " + d);
        return offset = 0;
    }

    public int JR(byte y, byte d) {
        if( offset == 0 ){
            return ++offset;
        }
        
        System.out.println("JR " + d);
        return offset = 0;
    }

    public int JR_cc(byte y, byte d) {
        if( offset == 0 ){
            return ++offset;
        }
        
        System.out.println("JR " + cc[y-4] + ", " + d);
        return offset = 0;
    }
}
