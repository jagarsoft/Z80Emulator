package com.github.jagarsoft;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;

public interface Memory {
    enum MovememDirection { FORWARD, REVERSE }
    void poke(int addr, byte data);
    byte peek(int addr);
    int getSize();

    void load(InputStream dataStream, int dest, int size);
    void load(RandomAccessFile dataStream, int dest, int size);
    //void load(FileInputStream dataStream, int dest, int size);

    //PRECOND: origin and destination are in the same bank throughout the entire count
    void movemem(short org, short dst, short count, MovememDirection dir);
}
