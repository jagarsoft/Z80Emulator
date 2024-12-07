package com.github.jagarsoft;

public interface IODevice {
    void write(int addr, char data);
    byte read(int addr);

    // short getSize(); TODO?
}
