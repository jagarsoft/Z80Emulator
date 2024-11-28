package com.github.jagarsoft;

class ROMMemory implements Memory {
    private byte[] rom;

    ROMMemory(int size){
        this.rom = new byte[size];
    }

    ROMMemory(byte[] bank){
        this.rom = bank;
    }

    @Override
    public void poke(int addr, byte data) { /* Read-Only Memory */; }

    @Override
    public byte peek(int addr){ return rom[addr]; }
    
    @Override
    public short getSize() { return (short)rom.length; }
}
