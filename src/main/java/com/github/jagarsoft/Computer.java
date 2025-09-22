package com.github.jagarsoft;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.BitSet;
import java.util.HashMap;

public class Computer {
    Z80 cpu;
    HashMap<Integer, Memory> banks   = new HashMap<Integer, Memory>();
    HashMap<Byte, IODevice> ioBanks = new HashMap<Byte, IODevice>();
    
    //ArrayList<Integer, IODevice> IObanks = new ArrayList<Integer, IODevice>();
    //int[] ioBanks;
    //IODevice ioDev;

    int sizeMask = 0;

    public Computer(){
    }

    public void reset(){
        //cpu.setComputer(this);
        cpu.reset();
    }

    public void setOrigin(int origin) {
        cpu.PC = origin;
    }

    public void run(){
        for (;;) {
            int pc = cpu.getPC();
            byte opC = this.peek(pc);
//Logger.info("PC:"+Integer.toHexString(pc)+" opC:"+Integer.toHexString(opC));
            cpu.fetch(opC); // fetch opCode
            //if( opC == 0x76) break; // is HALT?
        }
    }

    public Z80 getCPU() {
        return this.cpu;
    }

    public void addCPU(Z80 cpu){
        this.cpu = cpu;
    }

    public void addIODevice(byte port, IODevice device) {
        byte[] ports = new byte[]{port};
                
        bindPortsToDevice(ports, device);
    }

    public void addIODevice(byte[] ports, IODevice device) {
        bindPortsToDevice(ports, device);
    }

    public void addMemory(int base, Memory memory) {
        int s = memory.getSize();
        if( ! powerOf2(s) ) {
            System.out.println("Size must be a power of 2. " + s + " given");
            throw new IllegalArgumentException("Size must be a power of 2. " + s + " given");
        }
//System.out.println("Size:"+s);
        if( sizeMask == 0 )
            sizeMask = makeSizeMask(s);
        else if( sizeMask != makeSizeMask(s) ) {
            System.out.println("All banks must be the same size as the first one (" + Integer.toHexString(sizeMask) + "). " + Integer.toHexString(s) + " given");
            throw new IllegalArgumentException("All banks must be the same size as the first one (" + Integer.toHexString(sizeMask) + "). " + Integer.toHexString(s) + " given");
        }

        int key = base2key(base);

        banks.put(key, memory);
    }

    private boolean powerOf2(int size){
        BitSet s = BitSet.valueOf(new long[]{size});
        return s.cardinality() == 1;
    }

    // https://stackoverflow.com/a/466278/2928048
    public int upper_power_of_two(int v)
    {
        v--;
        v |= v >> 1;
        v |= v >> 2;
        v |= v >> 4;
        v |= v >> 8;
        v |= v >> 16;
        v++;
        return v;

    }

    // int s is a power of 2
    private int makeSizeMask(int s) {
        return ~(s - 1);
    }

    private int base2key(int addr) {
        return addr & sizeMask;
    }
    
    private void bindPortsToDevice(byte[] ports, IODevice device) {
        for(byte port : ports) {
            ioBanks.put(port, device);
        }
    }

    public byte peek(int addr) {
addr = addr&0xFFFF;
//Logger.compMem("peek addr:"+Integer.toHexString(addr));
        byte data = banks.get(base2key(addr)).peek(addr - (addr & sizeMask));
//Logger.compMem(" -> "+Integer.toHexString(data & 0xFF));
        return data;
    }

    public void poke(int addr, byte data) {
addr = addr&0xFFFF;
//Logger.compMem("poke addr:"+Integer.toHexString(addr)+" -> "+Integer.toHexString(data));
        banks.get(base2key(addr&0xFFFF)).poke(addr - (addr & sizeMask), data);
    }

    public byte read(short addr) {
Logger.info(("Computer.read ("+Integer.toHexString(addr)+")"));
        byte data = ioBanks.get((byte)(addr & 0xFF)).read(addr);
Logger.info(" -> "+Integer.toHexString(0 & 0xFF));
        return data;
    }

    public void write(short addr, byte data) {
Logger.info("Computer.write ("+Integer.toHexString(addr)+"):"+Integer.toHexString(data));
Logger.info(" addr: "+Integer.toHexString(addr & 0xFF));
        ioBanks.get((byte)(addr & 0x00FF)).write(addr & 0x00FF, data);
    }

    public void load(InputStream dataStream) throws IOException {
assert dataStream != null;
Logger.info("Computer.load dataStream: "+dataStream.toString());
        banks.get(base2key(0)).load(dataStream, 0,16*1024);
    }

    /*
    public void load(FileInputStream dataStream, long length) throws IOException {
        assert dataStream != null;
        Logger.info("Computer.load dataStream: "+dataStream.toString());
        banks.get(base2key(0)).load(dataStream, 0, (int)length);
    }
    */

    /**
     * Load Image into Memory chunks.
     * Each chunk is the size of the first bank
     *
     * @param dataStream
     * @param length
     * @throws IOException
     */
    public void load(FileInputStream dataStream, long length) throws IOException {
        assert dataStream != null;
        Logger.info("Computer.load dataStream: "+dataStream.toString());
        long left_to_load;
        int bank_size = banks.get(base2key(0)).getSize();
        int i = 0;
        while( length > bank_size ) {
            left_to_load = length - bank_size;
            banks.get(base2key(i)).load(dataStream, 0, bank_size);
            length = left_to_load;
            i += bank_size;
        }
        banks.get(base2key(i)).load(dataStream, 0, (int)length);
    }

    public boolean isSameBank(short org, short dst, short cont) {
        return false;
        /*return base2key(org) == base2key(dst)
            && base2key(org+cont) == base2key(dst+cont);*/
    }

    // PRECOND: origin and destination are in the same bank throughout the entire size
    public void movemem(short org, short dst, short size, Memory.MovememDirection dir) {
        banks.get(base2key(org)).movemem((short) (org - (org & sizeMask)), (short) (dst - (dst & sizeMask)), size, dir);
    }

    public void dump(int org, int count) {
        int i = 0;

        Logger.info(Integer.toHexString(org)+": ");
        for(int n = 0; n < count; n++) {
            Logger.info(Integer.toHexString(this.peek(org+n))+" ");

            if ((n % 8) == 0)
                Logger.info("\n"+Integer.toHexString(org)+": ");
        }
    }

    public int getMemorySize() {
        return banks.size() * getBankSize();
    }

    public int getBankSize() {
        if( !banks.isEmpty() )
            return banks.get(0).getSize();
        else
            return 0;
    }

    public void freeMemory() {
        sizeMask = 0;
        banks.clear();
    }
}
