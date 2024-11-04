package com.github.jagarsoft;

class RAMMemory implements Memory {
        private byte[] ram;

        RAMMemory(int size){
            this.ram = new byte[size];
        }

        public void poke(int addr, byte data) {
            this.ram[addr] = data;
        }

        public byte peek(int addr){
            return this.ram[addr];
        }
}
