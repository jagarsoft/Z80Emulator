package com.github.jagarsoft;

import java.util.ArrayList;

public class Computer {
    Z80 cpu;
    ArrayList<Memory> banks = new ArrayList<Memory>();

    Dispatcher d;

    public Computer(){
        d = new Dispatcher(new Z80());
    }

    public void addCPU(Z80 cpu){
        this.cpu = cpu;
    }

    public void addMemory(Memory memory) {
        banks.add(memory);
    }

    public void reset(){
        cpu.setComputer(this);
        cpu.reset();
    }

    public void run(){
        for (;;) {
            d.execute(this.peek(cpu.getPC())); // fetch opCode
        }
    }

    public byte peek(int addr) {
        int d = addr;
        Memory mm = null;

        for (Memory m : this.banks) {
            if( d > m.getSize() ) {
                d -= m.getSize();
            } else {
                mm = m;
                break;
            }
        }

        return mm.peek(d);
    }
    
    public void poke(int addr, byte data) {
        int d = addr;
        Memory mm = null;

        for (Memory m : this.banks) {
            if( d > m.getSize() ) {
                d -= m.getSize();
            } else {
                mm = m;
                break;
            }
        }

        mm.poke(d, data);
    }
}
