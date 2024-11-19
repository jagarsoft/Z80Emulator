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
    public int getSize() { return this.rom.length; }

    public void poke(int addr, byte data) {
        ;
    }

    public byte peek(int addr){
        return this.rom[addr];
    }
}
