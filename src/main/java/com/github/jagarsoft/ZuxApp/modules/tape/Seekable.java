package com.github.jagarsoft.ZuxApp.modules.tape;

// Seekable.java
import java.io.Closeable;
import java.io.IOException;

public interface Seekable extends Closeable {
    void seek(long pos) throws IOException;
    long position() throws IOException;
    int read(byte[] dst, int off, int len) throws IOException;
    void write(byte[] src, int off, int len) throws IOException;
    byte getByte(long pos) throws IOException;
    int getByte() throws IOException;
    void putByte(long pos, byte b) throws IOException;
    short getShortLE(long pos) throws IOException;
    int getShortLE() throws IOException;
    void putShortLE(long pos, short value) throws IOException;
    int size() throws IOException;
}
