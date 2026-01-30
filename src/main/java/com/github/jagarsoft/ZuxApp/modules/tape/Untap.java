package com.github.jagarsoft.ZuxApp.modules.tape;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.System.exit;

public class Untap {
    static class Block {
        int size;
        byte[] data;
        TAPHeader h;
    }

    static class TAPHeader {
        /*
    Byte    Length  Description
    ---------------------------
    0       1       Byte Flag: 0=Header
    1       1       Type (0,1,2 or 3)
    2       10      Filename (right-padded with blanks)
    12      2       Length of data block
    14      2       Parameter 1
    16      2       Parameter 2
    18      1       Checksum byte of header
        */
        //public short lengthHeaderBlock; // = 13h: the same as Block.size
        public byte byteFlagHeader; // = 0
        public byte type; // (0,1,2 or 3)
        public byte[] filename; // (right-padded with blanks)
        public int lengthDataBlock; // short
        public int parameter1; // short
        public int parameter2; // short
        public byte checksumByteHeader;
    }

    static class TAPData {
        //public short lengthDataBlock; // = TAPHeader.lengthHeaderBlock
        public byte byteFlagData; // = FFh
        public byte[] data;
        public byte checksumByteData;
    }

    private static enum BLOCKTYPE {
        IS_HEADER(0),
        IS_DATA(-1),
        IS_UNKNOWN(2);
        private int value;
        BLOCKTYPE(int value) {
            this.value = value;
        }
    };
    static RAFSeekable tap;
    static ArrayList<Block> blocks = new ArrayList<Block>();
    static String[] tokens = new String[256];
    static {
        // ZX Spectrum BASIC Tokens (0xA5-0xFF)
        tokens[0xA5] = "RND";
        tokens[0xA6] = "INKEY$";
        tokens[0xA7] = "PI";
        tokens[0xA8] = "FN";
        tokens[0xA9] = "POINT";
        tokens[0xAA] = "SCREEN$";
        tokens[0xAB] = "ATTR";
        tokens[0xAC] = "AT";
        tokens[0xAD] = "TAB";
        tokens[0xAE] = "VAL$";
        tokens[0xAF] = "CODE";
        tokens[0xB0] = "VAL";
        tokens[0xB1] = "LEN";
        tokens[0xB2] = "SIN";
        tokens[0xB3] = "COS";
        tokens[0xB4] = "TAN";
        tokens[0xB5] = "ASN";
        tokens[0xB6] = "ACS";
        tokens[0xB7] = "ATN";
        tokens[0xB8] = "LN";
        tokens[0xB9] = "EXP";
        tokens[0xBA] = "INT";
        tokens[0xBB] = "SQR";
        tokens[0xBC] = "SGN";
        tokens[0xBD] = "ABS";
        tokens[0xBE] = "PEEK";
        tokens[0xBF] = "IN";
        tokens[0xC0] = "USR";
        tokens[0xC1] = "STR$";
        tokens[0xC2] = "CHR$";
        tokens[0xC3] = "NOT";
        tokens[0xC4] = "BIN";
        tokens[0xC5] = "OR";
        tokens[0xC6] = "AND";
        tokens[0xC7] = "<=";
        tokens[0xC8] = ">=";
        tokens[0xC9] = "<>";
        tokens[0xCA] = "LINE";
        tokens[0xCB] = "THEN";
        tokens[0xCC] = "TO";
        tokens[0xCD] = "STEP";
        tokens[0xCE] = "DEF FN";
        tokens[0xCF] = "CAT";
        tokens[0xD0] = "FORMAT";
        tokens[0xD1] = "MOVE";
        tokens[0xD2] = "ERASE";
        tokens[0xD3] = "OPEN #";
        tokens[0xD4] = "CLOSE #";
        tokens[0xD5] = "MERGE";
        tokens[0xD6] = "VERIFY";
        tokens[0xD7] = "BEEP";
        tokens[0xD8] = "CIRCLE";
        tokens[0xD9] = "INK";
        tokens[0xDA] = "PAPER";
        tokens[0xDB] = "FLASH";
        tokens[0xDC] = "BRIGHT";
        tokens[0xDD] = "INVERSE";
        tokens[0xDE] = "OVER";
        tokens[0xDF] = "OUT";
        tokens[0xE0] = "LPRINT";
        tokens[0xE1] = "LLIST";
        tokens[0xE2] = "STOP";
        tokens[0xE3] = "READ";
        tokens[0xE4] = "DATA";
        tokens[0xE5] = "RESTORE";
        tokens[0xE6] = "NEW";
        tokens[0xE7] = "BORDER";
        tokens[0xE8] = "CONTINUE";
        tokens[0xE9] = "DIM";
        tokens[0xEA] = "REM";
        tokens[0xEB] = "FOR";
        tokens[0xEC] = "GO TO";
        tokens[0xED] = "GO SUB";
        tokens[0xEE] = "INPUT";
        tokens[0xEF] = "LOAD";
        tokens[0xF0] = "LIST";
        tokens[0xF1] = "LET";
        tokens[0xF2] = "PAUSE";
        tokens[0xF3] = "NEXT";
        tokens[0xF4] = "POKE";
        tokens[0xF5] = "PRINT";
        tokens[0xF6] = "PLOT";
        tokens[0xF7] = "RUN";
        tokens[0xF8] = "SAVE";
        tokens[0xF9] = "RANDOMIZE";
        tokens[0xFA] = "IF";
        tokens[0xFB] = "CLS";
        tokens[0xFC] = "DRAW";
        tokens[0xFD] = "CLEAR";
        tokens[0xFE] = "RETURN";
        tokens[0xFF] = "COPY";
    }


    public static void main(String[] args) throws IOException {
        Block blk;
        int blockIndex = -1;
        String fileTap;

        int argIndex = 0;
        String arg = args[argIndex];
        boolean blockOpt = false;
        boolean listOpt = false;

        switch (arg) {
            case "-b":
                blockOpt = true;
                blockIndex = Integer.parseInt(args[argIndex+1]);
                argIndex += 2;
                break;

            case "-l":
                listOpt = true;
                argIndex++;
        }

        fileTap = args[argIndex];
        System.out.println(fileTap);

        tap = new RAFSeekable(fileTap, "r");

        blockIndex = 0;
        while (true) {
            try {
                blk = readBlock();
                blocks.add(blk);
                showBlock(blk, blockIndex++); // remove
            } catch (EOFException e) {
                break;
            }
        }

        System.out.println(tap.size() + " bytes");
        System.out.println(blocks.size() + " blocks");

        if( blockOpt && (blockIndex < 0 || blockIndex > blocks.size()) ) {
            System.out.println("Invalid block index: " + blockIndex);
            exit(1);
        }

        if( listOpt ) {
            blockIndex = 0;
            while(blockIndex < blocks.size()) {
                blk = blocks.get(blockIndex);
                //showBlock(blk);
                showBlock(blk, blockIndex++);
            }
        } else if (blockOpt ) {
            //showBlock(blockIndex);
        }
    }

    private static void showBlock(Block blk, int blockIndex) {
    //private static void showBlock(int blockIndex) {
        //Block blk = blocks.get(blockIndex); // TODO recover
        BLOCKTYPE typeBlock = getBlockType(blk);

        System.out.println("Block " +  blockIndex);
        showTypeBlock(typeBlock);

        switch (typeBlock) {
            case IS_HEADER:
                showHeader(blk);
                break;
            case IS_DATA:
                showData(blocks.get(blockIndex-1), blk);
                break;
        }

        System.out.println("Total " + blk.size + " bytes");

        System.out.println();
    }

    private static void showData(Block headerBlk, Block dataBlk) {
        byte typeData = headerBlk.data[1];

        System.out.println("Type Data " +  getHeaderType(typeData) );

        switch (typeData) {
            case 0: // 0 (Program)
                getProgram(headerBlk, dataBlk);
                break;
            case 1: // 1 (Number array)
//                dumpNumberArray();
                break;
            case 2: // 2 (Character array)
//                dumpCharArray();
                break;
            case 3: // 3 (CODE)
//                dumpCode();
                break;
        }
    }

    private static void getProgram(Block headerBlk, Block dataBlk) {
        checkProgramIntegrity(headerBlk, dataBlk);

        byte[] program = new byte[headerBlk.h.lengthDataBlock];
        System.arraycopy(dataBlk.data, 1, program, 0, headerBlk.h.lengthDataBlock);

        System.out.println( Arrays.toString( program ));
        dumpProgram(program);
    }

    private static void dumpProgram(byte[] program) {
        for (int i = 0; i < program.length; i++) {
            printLine(program[i], program[i + 1]);
            i+=4; // skip line (processed above) and length of this line
            while(program[i] != 0x0D ) {
                if (program[i] == 0x0E ) {
                    i += 6; // skip number in FP format (5 bytes) prefixed by 0x0E
                    continue;
                }
                printToken(program[i]);
                i++;
            }
            System.out.println();
        }
    }

    private static void printToken(byte b) {
        int i = b;
        if( b < 0) i = 256 + b;
        if( i < 0xA5 )
            System.out.print((char) i);
        else // token
            System.out.print(tokens[i]+" ");
    }

    private static void printLine(byte b, byte b1) {
        System.out.printf("%4d ", b*256 + b1);
    }

    private static void checkProgramIntegrity(Block headerBlk, Block dataBlk) {
        if( dataBlk.data[0] != -1 ) {
            System.out.println("Program corrupted");
        }
    }

    private static void showHeader(Block blk) {
        blk.h = new TAPHeader();

        //blk.h.lengthHeaderBlock = blk.size;
        blk.h.byteFlagHeader = getByte(blk.data, 0);// (byte) BLOCKTYPE.IS_HEADER.value;
        blk.h.type = getByte(blk.data, 1);
        blk.h.filename = new byte[10];
        System.arraycopy(blk.data, 2, blk.h.filename, 0, 10);
        blk.h.lengthDataBlock = getShort(blk.data, 12);
        blk.h.parameter1 = getShort(blk.data, 14);
        blk.h.parameter2 = getShort(blk.data, 16);
        blk.h.checksumByteHeader = getByte(blk.data, 18);

        System.out.println("Header for: " + getHeaderType(blk.h.type));
        System.out.println("Filename: \"" + new String(blk.h.filename) + "\"");
        System.out.println("Length Data Block: " + blk.h.lengthDataBlock);
        System.out.println("Parameter 1: " + blk.h.parameter1);
        System.out.println("Parameter 2: " + blk.h.parameter2);
        System.out.println("Checksum Byte Header: " + blk.h.checksumByteHeader);
    }

    private static String getHeaderType(byte type) {
        return switch (type) {
            case 0 -> "0 (Program)";
            case 1 -> "1 (Number array)";
            case 2 -> "2 (Character array)";
            case 3 -> "3 (CODE)";
            default -> "Unexpected value: " + type;
        };
    }

    public static int getShort(byte[] data, int offset) {
        int lo = data[offset] & 0x0FF;
        int hi = data[offset+1] & 0x0FF;
        return (hi << 8) | lo;
    }

    public static byte getByte(byte[] data, int offset) {
        return (byte) (data[offset] & 0x0FF);
    }

    private static void showTypeBlock(BLOCKTYPE typeBlock) {
        switch( typeBlock ) {
            case IS_HEADER:
                System.out.print("Header block: ");
                break;
            case IS_DATA:
                System.out.print("Data block: ");
                break;
            case IS_UNKNOWN:
                System.out.println("Unknown type block");
                exit(BLOCKTYPE.IS_UNKNOWN.value);
        }
    }

    private static BLOCKTYPE getBlockType(Block blk) {
        return switch(blk.data[0]) {
            case 0 ->
                    BLOCKTYPE.IS_HEADER;
            case -1 ->
                    BLOCKTYPE.IS_DATA;
            default ->
                    BLOCKTYPE.IS_UNKNOWN;
        };
    }

    static Block readBlock() throws IOException {
        Block blk = new Block();

        blk.size = tap.getShortLE();
        blk.data = new byte[blk.size];
        tap.read(blk.data, 0, blk.size);

        return blk;
    }
}
