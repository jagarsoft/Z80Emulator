package com.github.jagarsoft;

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
}
