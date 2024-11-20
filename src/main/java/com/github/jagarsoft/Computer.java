package com.github.jagarsoft;

import java.util.ArrayList;

public class Computer {
    Z80 cpu;
    ArrayList<Memory> banks = new ArrayList<Memory>();

    Dispatcher d;

    Computer(){
        d = new Dispatcher(new Z80());
    }

    void addCPU(Z80 cpu){
        this.cpu = cpu;
    }

    void addMemory(Memory memory) {
        banks.add(memory);
    }

    void reset(){
        cpu.reset();
    }

    void run(){
        for (;;) {
            int operands = d.execute(this.peek(cpu.getPC())); // peek opCode
            while(operands-- > 0) {
                d.execute(this.peek(cpu.getPC())); // peek data
            }
        }
    }

    private byte peek(int addr) {
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
}
