package com.github.jagarsoft;

import java.util.BitSet;
import java.util.HashMap;

public class Computer {
    Z80 cpu;
    HashMap<Integer, Memory>   banks   = new HashMap<Integer, Memory>();
    HashMap<Integer, IODevice> ioBanks = new HashMap<Integer, IODevice>();
    
    //ArrayList<Integer, IODevice> IObanks = new ArrayList<Integer, IODevice>();
    //int[] ioBanks;
    //IODevice ioDev;

    short sizeMask = 0;

    public Computer(){
    }

    public void reset(){
        cpu.setComputer(this);
        cpu.reset();
    }

    public void run(){
        for (;;) {
            int pc = cpu.getPC();
            byte opC = this.peek(pc);
System.out.println("PC:"+Integer.toHexString(pc)+" opC:"+Integer.toHexString(opC));
            cpu.fetch(opC); // fetch opCode
            if( opC == 0x76) break; // is HALT?
        }
    }

    public void addCPU(Z80 cpu){
        this.cpu = cpu;
    }

    public void addIODevice(int port, IODevice device) {
        int[] ports = new int[]{port};
                
        bindPortsToDevice(ports, device);
    }

    public void addIODevice(int[] ports, IODevice device) {
        bindPortsToDevice(ports, device);
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
    
    private void bindPortsToDevice(int[] ports, IODevice device) {
        for(int port : ports) {
            ioBanks.put(port, device);
        }
    }

    public byte peek(int addr) {
        byte data = banks.get(addr & sizeMask).peek(addr - (addr & sizeMask));
System.out.println("peek addr:"+Integer.toHexString(addr)+" -> "+Integer.toHexString(data));
        return data;
    }
    
    public void poke(int addr, byte data) {
System.out.println("poke addr:"+Integer.toHexString(addr)+" -> "+Integer.toHexString(data));
        banks.get(addr & sizeMask).poke(addr - (addr & sizeMask), data);
    }

    public byte read(short addr) {
System.out.println(("Computer.read:"+Integer.toHexString(addr)));
        return ioBanks.get(addr).read(addr);
    }

    public void write(short addr, byte data) {
        ioBanks.get(addr).write(addr, (char)data);
    }
}
