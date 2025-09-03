package com.github.jagarsoft.ZuxApp.modules.computer;

import com.github.jagarsoft.Z80;
import com.github.jagarsoft.ZuxApp.modules.debugger.IZ80Cpu;
import com.github.jagarsoft.ZuxApp.modules.debugger.Z80State;

public class Z80Cpu implements IZ80Cpu {
    Z80 cpu;

    public Z80Cpu(Z80 cpu) {
        this.cpu = cpu;
    }

    @Override
    public void step() {
        System.out.println("Z80 step: "+getPC());
        cpu.fetch();
    }

    @Override
    public Z80State snapshot() {
        return new Z80State(cpu.getSnapshot());
    }

    @Override
    public int getPC() {
        return cpu.getPCnonIncrement();
    }

    @Override
    public boolean isHalted() {
        return cpu.isHalted();
    }

    @Override
    public void reset() {
        cpu.reset();
    }

    @Override
    public int getReg(String name) {
        int v;
        switch(name) {
            case "A": v = cpu.getA(); break;
            case "B": v = cpu.getB(); break;
            case "C": v = cpu.getC(); break;
            case "D": v = cpu.getD(); break;
            case "E": v = cpu.getE(); break;
            case "F": v = cpu.getF(); break;
            case "F_": v = cpu.getF_(); break;
            case "H": v = cpu.getH(); break;
            case "L": v = cpu.getL(); break;
            case "I": v = cpu.getI(); break;
            case "R": v = cpu.getR(); break;
            case "SP": v = cpu.getSP(); break;
            case "IX": v = cpu.getIX(); break;
            case "IY": v = cpu.getIY(); break;
            default:
                throw new IllegalArgumentException("Unknown reg: "+name);
        }
        return v;
    }
}
