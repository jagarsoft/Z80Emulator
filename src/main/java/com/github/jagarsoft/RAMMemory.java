package com.github.jagarsoft;

public class RAMMemory implements Memory {
        private byte[] ram;
        public short size;

        public RAMMemory(int size){
            this.ram = new byte[size];
            this.size = (short)size;
        }

        public void poke(int addr, byte data) { ram[addr] = data; }

        public short getSize(){ return size; }

        public byte peek(int addr) { return ram[addr]; }
}
