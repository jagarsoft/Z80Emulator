package com.github.jagarsoft;

public class Dispatcher {
    static int offset;
    static byte x;
    static byte y;
    static byte z;

    // Array to store reference to methods of different implementations
    static OpCode[][][] opCodes = new OpCode[4][8][8];

    public Dispatcher(Z80OpCode opC) {
        // According to http://www.z80.info/decoding.htm
        //     [x][z][y]
        opCodes[0][0][0] = opC::NOP;
        opCodes[0][0][1] = opC::EX_AF_AF_;
        opCodes[0][0][2] = opC::DJNZ;
        opCodes[0][0][3] = opC::JR;
        opCodes[0][0][4] = opC::JR_cc;
        opCodes[0][0][5] = opC::JR_cc;
        opCodes[0][0][6] = opC::JR_cc;
        opCodes[0][0][7] = opC::JR_cc;
    }

    int execute(byte opC){
            if( offset == 0 ) {
                x = (byte) ((opC & (byte)0b11000000) >> 6);
                y = (byte) ((opC & (byte)0b00111000) >> 3);
                z = (byte) (opC & (byte)0b111);

                if (opCodes[x][z][y] != null) {
                    offset = opCodes[x][z][y].execute(z, y);
                }
                // else: ignore opCode
            } else {
                opCodes[x][z][y].execute(y, opC); // opC === data
                offset--;
            }

            return offset;
        }
}
