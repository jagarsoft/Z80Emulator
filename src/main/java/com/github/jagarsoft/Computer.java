package com.github.jagarsoft;

import com.github.jagarsoft.ZuxApp.core.bus.EventBus;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.BitSet;
import java.util.HashMap;

public class Computer {
    Z80 cpu;
    HashMap<Integer, Memory> banks   = new HashMap<Integer, Memory>();
    HashMap<Short, IODevice> ioBanks = new HashMap<Short, IODevice>();

    int sizeMask = 0;
    private EventBus eventBus;

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
        new Thread(()->{
            for (;;) {
            /* debugger purpose only
            int pc = cpu.getPC();
            byte opC = this.peek(pc);
//Logger.info("PC:"+Integer.toHexString(pc)+" opC:"+Integer.toHexString(opC));
            cpu.fetch(opC); // fetch opCode
            //if( opC == 0x76) break; // is HALT?
             */
                cpu.fetch();
            }
        }).start();
    }

    public Z80 getCPU() {
        return this.cpu;
    }

    public void addCPU(Z80 cpu){
        this.cpu = cpu;
    }

    public void addIODevice(short port, IODevice device) {
        short[] ports = new short[]{port};
                
        bindPortsToDevice(ports, device);
    }

    public void addIODevice(short[] ports, IODevice device) {
        bindPortsToDevice(ports, device);
    }

    public void addMemory(int base, Memory memory) {
        int s = memory.getSize();
        if( ! powerOf2(s) ) {
            System.out.println("Size must be a power of 2. " + s + " given");
            throw new IllegalArgumentException("Size must be a power of 2. " + s + " given");
        }

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
    
    private void bindPortsToDevice(short[] ports, IODevice device) {
        for(short port : ports) {
            ioBanks.put(port, device);
        }
    }

    public byte peek(int addr) {
        addr = addr & 0x0000FFFF;
        if( banks.containsKey( base2key(addr) ) ) {
            return banks.get(base2key(addr)).peek(addr - (addr & sizeMask));
        } else
            return (byte) 0xFF; // Pull-up!
    }

    public void poke(int addr, byte data) {
        addr = addr & 0x0000FFFF;
        if( banks.containsKey( base2key(addr) ) )
            banks.get(base2key(addr)).poke(addr - (addr & sizeMask), data);
        else
            throw  new IllegalArgumentException("No such bank address " + addr);
    }

    public byte read(short addr) {
        if( ioBanks.containsKey(addr) ) {
            return ioBanks.get(addr /*& 0xFF*/).read(addr);
        } else
            return (byte) 0xFF; // Pull-up!
    }

    public void write(short addr, byte data) {
        if( ioBanks.containsKey(addr) )
            ioBanks.get(addr /*& 0x00FF*/).write(addr /*& 0x00FF*/, data);
    }

    public void write(short addr, byte data, int tstate) {
        if( ioBanks.containsKey(addr) )
            ioBanks.get(addr /*& 0x00FF*/).write(addr /*& 0x00FF*/, data, tstate);
    }

    public void load(InputStream dataStream) throws IOException {
assert dataStream != null;
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

    public void loadBytes(FileInputStream dataStream, int init, long length) throws IOException {
        assert dataStream != null;

        int bank_size = banks.get(base2key(0)).getSize();

        do {
            long left_to_load = length; // sentinel = end condition
            int base = base2key(init);
            Memory bank = banks.get(base);

            int relative = init - base; // != 0 for 1st bank only, else 0
            if (relative + length > bank_size)
                left_to_load = bank_size - relative;

            bank.load(dataStream, relative, (int)left_to_load);

            if (left_to_load == length) return;

            length -= left_to_load;
            init += left_to_load; // next bank
        } while(true); // length > 0
    }

    public void loadBytes(RandomAccessFile dataStream, int init, long length) throws IOException {
        assert dataStream != null;

        if( init < 0 )
            init *= -1;

        int bank_size = banks.get(base2key(0)).getSize();

        do {
            long left_to_load = length; // sentinel = end condition
            int base = base2key(init);
            if( base < 0 )
                base *= -1;
            Memory bank = banks.get(base);
assert bank != null;
            int relative = init - base; // != 0 for 1st bank only, else 0
            if (relative + length > bank_size)
                left_to_load = bank_size - relative;

            bank.load(dataStream, relative, (int)left_to_load);

            if (left_to_load == length) return;

            length -= left_to_load;
            init += left_to_load; // next bank
        } while(true); // length > 0
    }

    public boolean isSameBank(short org, short dst, short cont) {
        return false;
        /*return base2key(org) == base2key(dst)
            && base2key(org+cont) == base2key(dst+cont);*/
    }

    // PRECOND: origin and destination are in the same bank throughout the entire count
    public void movemem(short org, short dst, short count, Memory.MovememDirection dir) {
        banks.get(base2key(org)).movemem((short) (org - (org & sizeMask)), (short) (dst - (dst & sizeMask)), count, dir);
    }

    public void dump(int org, int count) {
        System.out.print(Integer.toHexString(org)+" ");
        for(int n = 0; n < count; n+=16) {
            for (int i = 0; i < 16; i++)
                System.out.print(Integer.toHexString(this.peek(org+i)).toUpperCase()+" ");
            System.out.print(" ");
            for (int i = 0; i < 16; i++)
                System.out.print((char)this.peek(org+i)+" ");

            if( n + 16 > count)
                System.out.print("\n"+Integer.toHexString(org)+" ");
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

    public void setPC(int init) {
        cpu.PC = init;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public EventBus getEventBus() {
        return this.eventBus;
    }
}
