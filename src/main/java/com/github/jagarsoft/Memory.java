package com.github.jagarsoft;

interface Memory {
    int getSize();
    void poke(int addr, byte data);
    byte peek(int addr);
}
