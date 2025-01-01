package com.github.jagarsoft;

public interface Memory {
    void poke(int addr, byte data);
    byte peek(int addr);
    short getSize();
}
