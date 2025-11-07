package com.github.jagarsoft.ZuxApp.modules.zxspectrum;

import com.github.jagarsoft.IODevice;

import java.io.FileOutputStream;
import java.io.IOException;

public class ZXTapeAndBorder implements IODevice {
    private final ZXSpectrumScreen screen;
    String nombreArchivoOUT = "archivoOUT.bin";
    String nombreArchivoIN = "archivoIN.bin";
    FileOutputStream fos, fis;
    long  lastTick;

    public ZXTapeAndBorder(ZXSpectrumScreen screen) {
        this.screen = screen;
        lastTick = System.nanoTime();

        try {
            fos = new FileOutputStream(nombreArchivoOUT);
            fis = new FileOutputStream(nombreArchivoIN);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(int addr, byte data) {
        try {
            long thisTick = System.nanoTime();
            System.out.println("OUT: " + data +", "+ thisTick + ", "+ (thisTick-lastTick));
            lastTick = thisTick;
            fos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        screen.setBorderColor(data);
    }

    @Override
    public byte read(int addr) {
        return 0;
    }
}
