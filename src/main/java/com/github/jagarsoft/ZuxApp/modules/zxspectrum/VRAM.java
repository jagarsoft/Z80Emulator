package com.github.jagarsoft.ZuxApp.modules.zxspectrum;

import com.github.jagarsoft.Memory;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class VRAM implements Memory {
    public static final int VRAM_ORIGIN = 0x4000;
    private final ZXSpectrumScreen screen;
    private final byte[] ram;
    public  final int size;

    public VRAM(ZXSpectrumScreen screen){
        this.screen = screen;
        this.ram = new byte[16 * 1024 - ZXSpectrumScreen.VRAM_BYTES - ZXSpectrumScreen.ATTR_BYTES ]; // 16Kb bank - bitmap - attrs
        //this.size = (short)ram.length + ZXSpectrumScreen.VRAM_BYTES + ZXSpectrumScreen.ATTR_BYTES;
        this.size = 16 * 1024;
    }

    @Override
    public void poke(int addr, byte data) {
        if( addr < ZXSpectrumScreen.VRAM_BYTES) { // pixels map
            screen.pokeBitmap(addr, data);
        } else if ( addr < ZXSpectrumScreen.VRAM_BYTES + ZXSpectrumScreen.ATTR_BYTES) { // attr map
            screen.pokeAttribute(addr - ZXSpectrumScreen.VRAM_BYTES, data);
        } else // rest of the bank
            ram[addr-(ZXSpectrumScreen.VRAM_BYTES + ZXSpectrumScreen.ATTR_BYTES)] = data;
    }

    @Override
    public byte peek(int addr) {
        if( addr < ZXSpectrumScreen.VRAM_BYTES) { // pixels map
            return screen.peekBitmap(addr);
        } else if ( addr < ZXSpectrumScreen.VRAM_BYTES + ZXSpectrumScreen.ATTR_BYTES) { // attr map
            return screen.peekAttribute(addr - ZXSpectrumScreen.VRAM_BYTES);
        } else
            return ram[addr-(ZXSpectrumScreen.VRAM_BYTES + ZXSpectrumScreen.ATTR_BYTES)];

    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public void load(InputStream dataStream, int dest, int size) {
        //unused
    }

    @Override
    public void load(RandomAccessFile dataStream, int dest, int size) {
        try {
            dataStream.read(ram, dest-(ZXSpectrumScreen.VRAM_BYTES + ZXSpectrumScreen.ATTR_BYTES), size);
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void movemem(short org, short dst, short count, MovememDirection dir) {
        switch (dir) {
            case FORWARD:
                while (count-- > 0)
                    //ram[dst++] = ram[org++];
                    poke(dst++, peek(org++));
                break;
            case REVERSE:
                while (count-- > 0)
                    //ram[dst--] = ram[org--];
                    poke(dst--, peek(org--));
                break;
        }

    }
}
