package com.github.jagarsoft.ZuxApp.modules.tape;

public class TAPHeader {
    public int lengthHeaderBlock; // = 13h short
    public int byteFlagHeader; // = 0 byte
    public int type; // (0,1,2 or 3) byte
    public byte[] filename; // (right-padded with blanks)
    public int lengthDataBlock; // short
    public int parameter1; // short
    public int parameter2; // short
    public int checksumByteHeader; // byte
}