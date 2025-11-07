package com.github.jagarsoft;

public class ZXSpectrumIO implements IODevice {
    IODevice in;
    IODevice out;

    public ZXSpectrumIO(IODevice keyboard, IODevice zxBorder) {
        in = keyboard;
        out = zxBorder;
    }

    @Override
    public void write(int addr, byte data) {
        out.write(addr, data);
    }

    @Override
    public byte read(int addr) { return in.read(addr); }
}
