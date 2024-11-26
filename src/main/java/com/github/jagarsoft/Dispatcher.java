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
        // z = 1 skipped
        // z = 2
        opCodes[0][2][0b000] = opC::LD_BC_A;
        opCodes[0][2][0b010] = opC::LD_DE_A;
        opCodes[0][2][0b100] = opC::LD_nn_HL;
        opCodes[0][2][0b110] = opC::LD_nn_A;
        opCodes[0][2][0b001] = opC::LD_A_BC;
        opCodes[0][2][0b011] = opC::LD_A_DE;
        opCodes[0][2][0b101] = opC::LD_HL_nn;
        opCodes[0][2][0b111] = opC::LD_A_nn;

    }

    void execute(byte opC){
        x = (byte) ((opC & (byte)0b11000000) >> 6);
        y = (byte) ((opC & (byte)0b00111000) >> 3);
        z = (byte) (opC & (byte)0b111);

        if (opCodes[x][z][y] != null) {
            opCodes[x][z][y].execute(z, y);
        }
    }
}
