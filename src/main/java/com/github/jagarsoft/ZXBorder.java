package com.github.jagarsoft;

import com.github.jagarsoft.GUI.ZXSpectrumScreen;

public class ZXBorder implements IODevice {
    ZXSpectrumScreen screen;

    long tA, tB;
    long duration;

    public ZXBorder(ZXSpectrumScreen screen) {
        this.screen = screen;
        tA = tB = 0;
    }

    @Override
    public void write(int addr, byte data) {
        screen.setBorder((byte) (data & 0b111));

        Logger.write("Border:"+(byte) (data & 0b111));
        if( (data & 0b1000) == 0 ) {
            tA = System.nanoTime();
            duration = tA - tB;
            tB = tA;
            System.out.println("duration:"+duration);
            Logger.write("OUT:  127,");
        } else {
            tA = System.nanoTime();
            duration = tA - tB;
            tB = tA;
            System.out.println("duration"+duration);
            Logger.write("OUT: -127,");
        }
    }

    @Override
    public void write(int addr, byte data, int tstate) {
        this.write(addr, data);
    }

    @Override
    public byte read(int addr) {
        Logger.read("Border was read!: " + addr);
        return 0;
    }
}
