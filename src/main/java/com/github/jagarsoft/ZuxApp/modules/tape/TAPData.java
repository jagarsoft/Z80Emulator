package com.github.jagarsoft.ZuxApp.modules.tape;

public class TAPData {
    public int lengthDataBlock; // = TAPHeader.lengthHeaderBlock short
    public int byteFlagData; // = FFh byte
    public byte[] data;
    public int checksumByteData; // byte
}