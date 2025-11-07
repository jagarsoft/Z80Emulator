package com.github.jagarsoft.test;

import com.github.jagarsoft.IODevice;

public class ZXSpectrumIOForTesting implements IODevice {
    private byte data;

    @Override
    public void write(int addr, byte data) {
        this.data = data; // Echoes output ...
    }

    @Override
    public byte read(int addr) {
        return this.data; // ... to the input
    }
}