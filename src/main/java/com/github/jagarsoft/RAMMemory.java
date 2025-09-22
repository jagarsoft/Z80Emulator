package com.github.jagarsoft;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RAMMemory implements Memory {
        private byte[] ram;
        public int size;

        public RAMMemory(int size){
            this.ram = new byte[size];
            this.size = size;
        }

        public void poke(int addr, byte data) { ram[addr] = data; }

        public int getSize(){ return size; }

        public byte peek(int addr) { return ram[addr]; }

        @Override
        public void load(InputStream dataStream, int dest, int size) {
                try {
                        dataStream.read(ram, dest, size);
                } catch (
                        IOException e) {
                        throw new RuntimeException(e);
                }
        }

        @Override
        public void movemem(short org, short dst, short count, MovememDirection dir) {
                switch (dir) {
                        case FORWARD:
                                while (count-- > 0)
                                        ram[dst++] = ram[org++];
                                break;
                        case REVERSE:
                                while (count-- > 0)
                                        ram[dst--] = ram[org--];
                                break;
                }

        }
}
