package com.github.jagarsoft.Zux;

import com.github.jagarsoft.IODevice;

public class ZuxIO implements IODevice {
    IODevice in;
    IODevice out;

    public ZuxIO(IODevice keyboard, IODevice zuxTerminal) {
        in = keyboard;
        out = zuxTerminal;
    }

    @Override
    public void write(int addr, byte data) {
        out.write(addr, data);
    }

    @Override
    public void write(int addr, byte data, int tstate) {
        this.write(addr, data);
    }

    @Override
    public byte read(int addr) {
        return in.read(addr);
    }
}