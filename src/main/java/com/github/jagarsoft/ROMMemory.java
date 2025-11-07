package com.github.jagarsoft;

import java.io.IOException;
import java.io.InputStream;

public class ROMMemory implements Memory {
    private byte[] rom;

    public ROMMemory(int size){
        this.rom = new byte[size];
    }

    public ROMMemory(byte[] bank){
        this.rom = bank;
    }

    @Override
    //public void poke(int addr, byte data) { /* Read-Only Memory */; }
    public void poke(int addr, byte data) { if( addr == 0x1303 ) rom[addr] = data; }

    @Override
    public byte peek(int addr){ return rom[addr]; }
    
    @Override
    public int getSize() { return rom.length; }

    @Override
    public void load(InputStream dataStream, int dest, int size) {
        try {
            dataStream.read(rom, dest, size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void movemem(short org, short dst, short count, MovememDirection dir) {
        // omit silently
     }
}
