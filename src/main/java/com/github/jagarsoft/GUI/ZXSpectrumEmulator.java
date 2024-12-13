package com.github.jagarsoft.GUI;

import com.github.jagarsoft.Computer;
import com.github.jagarsoft.ROMMemory;
import com.github.jagarsoft.VRAM;
import com.github.jagarsoft.Z80;

import javax.swing.*;

public class ZXSpectrumEmulator {
    static byte[] rom = new byte[16 * 1024];
    static int[] data = new int[] {
            0x21, 0x00, 0x40,   // 0000 LD HL, 0x4000
            0x01, 0x00, 0xC0,   // 0003 LD BC, 256*192 // 0xC000
            0x16, 0xFF,         // 0006 LD D, 255
            0x72,               // 0008 LOOP_X: LD (HL), D
            0x15,               // 0009 DEC D
            0x23,               // 000A INC HL
            0x0B,               // 000B DEC BC
            0x78,               // 000C LD A, B
            0xB1,               // 000D OR C
            0xC2, 0x08, 0x00,   // 000E JP NZ, LOOP_X
            0x76                // 0011 HALT
    };

     static {
         for(int i=0; i<data.length; i++){
            rom[i] = (byte) data[i];
         }
     }
    
    public static void main(String[] args) {
        ZXSpectrumScreen screen = new ZXSpectrumScreen();

        Computer spectrum = new Computer();
        spectrum.addCPU(new Z80());
        //spectrum.addIODevice(new int[]{0}, new Console());
        spectrum.addMemory(0x0000, new ROMMemory(rom));
        spectrum.addMemory(0x4000, new VRAM(screen));
        /*spectrum.addMemory(0x8000, new ROMMemory(16 * 1024))
        spectrum.addMemory(0xC000, new ROMMemory(16 * 1024))*/

        spectrum.reset();

		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame mf = new MainFrame(screen);
                mf.init("ZX Spectrum Emulator v0.1" /*, zx*/);
                mf.createMenuBar();
                mf.createPanels();

                /*Computer zx = createZXSpectrum(screen);*/
            }
        });

        SwingWorker worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                spectrum.run();
                return null;
            }
        };

        worker.execute();
    }
    
    private static /*Computer TODO*/ void createZXSpectrum(Screen screen) {



    }
}