package com.github.jagarsoft;

public class Z80Runner extends Z80 implements Z80OpCode {
    // mover a interface Z80OpCode
    static int offset = 0;

    String[] cc = new String[] {
            "NZ", "Z", "NC", "C", "PO", "PE", "P", "M"
    };
    //

    // Mover los metodos de accion a Z80, entonces Z80Runner quedaria vacio y donde se instancie usar z80 directamente
    public int NOP(byte z, byte y) {
        return 0;
    }

    public int EX_AF_AF_(byte z, byte y) {
        z = this.A;
        this.A = A_;
        this.A_ = z;
        return 0;
    }

    public int DJNZ(byte y, byte d) {
        if( offset == 0 ){
            return ++offset;
        }

        if( --this.B != 0 )
            this.PC += d;

        return offset = 0;
    }

    public int JR(byte y, byte d) {
        if( offset == 0 ){
            return ++offset;
        }

        this.PC += d;

        return offset = 0;
    }

    public int JR_cc(byte y, byte d) {
        boolean t = false; // True?
        if( offset == 0 ){
            return ++offset;
        }

        switch (cc[y-4]) {
            case "NZ":  t = ! this.ZF; break;
            case "Z":   t = this.ZF; break;
            case "NC":  t = ! this.CF; break;
            case "C":   t = this.CF; break;
        }

        if( t ) this.PC += d;

        return offset = 0;
    }
}
