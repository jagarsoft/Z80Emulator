package com.github.jagarsoft;

class RAMMemory implements Memory {
        private byte[] ram;
        public int size;

        RAMMemory(int size){
            this.ram = new byte[size];
            this.size = size;
        }

        public int getSize(){
            return this.size;
        }

        public void poke(int addr, byte data) {
            this.ram[addr] = data;
        }

        public byte peek(int addr) {
            return this.ram[addr];
        }
}
