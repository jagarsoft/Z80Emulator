package com.github.jagarsoft;

public interface IODevice {
    void write(int addr, byte data);
    void write(int addr, byte data, int tstate);
    byte read(int addr);

    // short getSize(); TODO?
}
