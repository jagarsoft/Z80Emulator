package com.github.jagarsoft;

interface Memory {
    void poke(int addr, byte data);
    byte peek(int addr);
    int getSize();
}
