package com.github.jagarsoft;

public interface IODevice {
    void write(int addr, byte data);
    byte read(int addr);

    // short getSize(); TODO?
}
