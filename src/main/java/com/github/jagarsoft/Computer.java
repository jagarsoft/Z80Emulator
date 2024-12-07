package com.github.jagarsoft;

import java.util.BitSet;
import java.util.HashMap;

public class Computer {
    Z80 cpu;
    HashMap<Integer, Memory> banks = new HashMap<Integer, Memory>();

    //HashMap<Integer, IODevice> IObanks = new HashMap<Integer, IODevice>();
    //ArrayList<Integer, IODevice> IObanks = new ArrayList<Integer, IODevice>();
    int[] IObanks;
    IODevice ioDev;

    short sizeMask = 0;

    public Computer(){
    }

    public void reset(){
        cpu.setComputer(this);
        cpu.reset();
    }

    public void run(){
        for (;;) {
            byte opC = this.peek(cpu.getPC());
            cpu.fetch(opC); // fetch opCode
            if( opC == 0x76) break; // is HALT?
        }
    }

    public void addCPU(Z80 cpu){
        this.cpu = cpu;
    }

    public void addIODevice(int port, IODevice device) {

    }

    public void addIODevice(int[] ports, IODevice device) {
        IObanks = ports;
        ioDev = device;
    }

    public void addMemory(int base, Memory memory) {
        short s = memory.getSize();
        if( ! powerOf2(s) )
            throw new IllegalArgumentException("Size must be a power of 2. "+s+" given");

        if( sizeMask == 0 )
            sizeMask = makeSizeMask(s);
        else if( sizeMask != makeSizeMask(s) )
            throw new IllegalArgumentException("All banks must be the same size as the first one. "+s+" given");

        int key = base2key(base);

        banks.put(key, memory);
    }

    private boolean powerOf2(short size){
        BitSet s = BitSet.valueOf(new long[]{size});
        return s.cardinality() == 1;
    }

    private short makeSizeMask(short s) {
        return (short) ~(s - 1);
    }

    private int base2key(int addr) {
        return addr & sizeMask;
    }

    public byte peek(int addr) { return banks.get(addr & sizeMask).peek(addr); }
    
    public void poke(int addr, byte data) {
        banks.get(addr & sizeMask).poke(addr, data);
    }

    public void write(byte data) {
        ioDev.write(0, (char)data);
    }
}
