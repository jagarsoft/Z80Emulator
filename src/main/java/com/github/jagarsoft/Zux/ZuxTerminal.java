package com.github.jagarsoft.Zux;

import com.github.jagarsoft.IODevice;

public class ZuxTerminal implements IODevice {
    ZuxScreen screen;

    public ZuxTerminal(ZuxScreen screen) {
        this.screen = screen;
    }

    @Override
    public void write(int addr, byte data) {
        // TODO out 8x8 pixels to BufferImage
        screen.putchar(data);
    }

    @Override
    public void write(int addr, byte data, int tstate) {
        this.write(addr, data);
    }

    @Override
    public byte read(int addr) {
        return 0;
    }
}