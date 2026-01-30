package com.github.jagarsoft.ZuxApp.modules.tape;

// RAFSeekable.java (RandomAccessFile)
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class RAFSeekable implements Seekable {
    private long pos;

    public final RandomAccessFile raf;

    public RAFSeekable(String tap, String mode) throws IOException, URISyntaxException {
        URL resource = getClass().getClassLoader().getResource(tap);
        if (resource != null) {
            this.raf = new RandomAccessFile(new File(resource.toURI()), mode);
        } else this.raf = null;
        this.pos = 0;
    }

    @Override
    public void seek(long pos) throws IOException {
        raf.seek(pos);
        this.pos = pos;
    }

    @Override
    public long position() throws IOException {
        return this.pos = raf.getFilePointer();
    }

    @Override
    public int read(byte[] dst, int off, int len) throws IOException {
        this.pos += len;
        return raf.read(dst, off, len);
    }

    @Override
    public void write(byte[] src, int off, int len) throws IOException {
        raf.write(src, off, len);
    }

    @Override
    public byte getByte(long pos) throws IOException {
        raf.seek(pos);
        this.pos++;
        return raf.readByte();
    }

    @Override
    public int getByte() throws IOException {
        this.pos++;
        return raf.readByte() & 0x00FF;
    }

    @Override
    public void putByte(long pos, byte b) throws IOException {
        raf.seek(pos);
        raf.writeByte(b & 0xFF);
    }

    @Override
    public short getShortLE(long pos) throws IOException {
        this.pos = pos;
        raf.seek(pos);
        int lo = raf.readUnsignedByte();
        int hi = raf.readUnsignedByte();
        this.pos+=2;
        return (short)((hi << 8) | lo);
    }

    @Override
    public int getShortLE() throws IOException { // LE = Little Endian
        int lo = raf.readUnsignedByte();
        int hi = raf.readUnsignedByte();
        this.pos+=2;
        return ((hi << 8) | lo) & 0x0000FFFF;
    }

    @Override
    public void putShortLE(long pos, short value) throws IOException {
        raf.seek(pos);
        raf.writeByte(value & 0xFF);
        raf.writeByte((value >> 8) & 0xFF);
    }

    @Override
    public int size() throws IOException {
        return (int)raf.length();
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }
}
