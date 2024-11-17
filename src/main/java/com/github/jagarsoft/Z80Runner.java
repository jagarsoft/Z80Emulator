package com.github.jagarsoft;

public class Z80Runner extends Z80 implements Z80OpCode {
    static int offset = 0;
    
    String cc[] = new String[] {
            "NZ", "Z", "NC", "C", "PO", "PE", "P", "M" 
    };

    public int NOP(byte y, byte z) {
        System.out.println("NOP");
        return 0;
    }

    public int EX_AF_AF_(byte y, byte z) {
        System.out.println("EX AF, AF'");
        return 0;
    }

    public int DJNZ(byte d, byte z) {
        if( offset == 0 ){
            return ++offset;
        }

        System.out.println("DJNZ " + d);
        return offset = 0;
    }

    public int JR(byte d, byte z) {
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
