package com.github.jagarsoft;

import java.io.FileInputStream;
import java.io.InputStream;

public interface Memory {
    enum MovememDirection { FORWARD, REVERSE }
    void poke(int addr, byte data);
    byte peek(int addr);
    int getSize();

    void load(InputStream dataStream, int dest, int size);
    //void load(FileInputStream dataStream, int dest, int size);

    void movemem(short org, short dst, short cont, MovememDirection dir);
}
