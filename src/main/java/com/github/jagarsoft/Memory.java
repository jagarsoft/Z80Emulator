package com.github.jagarsoft;

import java.io.InputStream;

public interface Memory {
    void poke(int addr, byte data);
    byte peek(int addr);
    short getSize();

    void load(InputStream dataStream, int dest, int size);
}
