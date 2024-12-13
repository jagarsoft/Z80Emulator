package com.github.jagarsoft;

public class ROMMemory implements Memory {
    private byte[] rom;

    public ROMMemory(int size){
        this.rom = new byte[size];
    }

    public ROMMemory(byte[] bank){
        this.rom = bank;
    }

    @Override
    public void poke(int addr, byte data) { /* Read-Only Memory */; }

    @Override
    public byte peek(int addr){ return rom[addr]; }
    
    @Override
    public short getSize() { return (short)rom.length; }
}
