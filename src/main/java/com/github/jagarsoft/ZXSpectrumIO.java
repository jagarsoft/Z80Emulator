package com.github.jagarsoft;

public class ZXSpectrumIO implements IODevice {
    IODevice in;
    IODevice out;

    public ZXSpectrumIO(IODevice keyboard, IODevice zxSpectrumBeeperTapeAndBorder) {
        in = keyboard;
        out = zxSpectrumBeeperTapeAndBorder;
    }

    @Override
    public void write(int addr, char data) {
        out.write(addr, data);
    }

    @Override
    public byte read(int addr) {
        return in.read(addr);
    }
}
