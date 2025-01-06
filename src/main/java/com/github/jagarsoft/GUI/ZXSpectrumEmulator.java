package com.github.jagarsoft.GUI;

import com.github.jagarsoft.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.util.List;

public class ZXSpectrumEmulator {
    static byte[] rom = new byte[16 * 1024];
    /*static int[] data = new int[] {
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
    };*/
    /*static int[] data = new int[] {
            0x21, 0xEF, 0x48,   // 0000 LD HL, 0x48EF ; 18671
            0x11, 0x0F, 0x50,   // 0003 LD DE, 0x500F ; 20495
            0x3E, 0x80,         // 0006 LD A, 0x80
            0x06, 0x04,         // 0008 LD B, 4
            0x77,               // 000A LOOP:  LD (HL), A
            0x00,               // 000B NOP; LD (DE), A
            0x24,               // 000C INC H
            0x14,               // 000D INC D
            0x10, 0xFA,         // 000E DJNZ -6
            0X76                // 0010 HALT
    };*/

    /*static int[] data = new int[] {
            0x21, 0x00, 0x40,   // 0000 LD HL, 0x4000
            0x3E, 0x80,         // 0003 LD A, 0x80
            0x06, 0x1F,         // 0005 LD B, 0x1F
            0x48,               // 0007 L2: LD C, B
            0x06, 0x08,         // 0006 LD B, 8
            0x77,               // 0008 L1: LD (HL), A
            0x1F,               // 0009 RRA
            0x10, 0xFC,         // 000A DJNZ L1
            0x2C,               // INC L
            0x1F,               // 000C RRA
            0x41,               // 000E LD B, C
            0x10, 0xF4,         // 000A DJNZ L2
            0x45,               // 000B LD B, L
            0x04,               // INC B
            0x48,               // L4: LD C, B
            0x06, 0x08,         // 0006 LD B, 8
            0x77,               // 0008 L3: LD (HL), A
            0x17,               // RLA
            0x10, 0xFC,         // DJNZ L3
            0x17,               // RLA
            0x3D,               // DEC L
            0x41,               // LD B, C
            0x1, 0xF4,          // DJNZ L4
            0xC3, 0, 0,         // JMP L0
            0x76                // 0010 HALT
            // KEYPRESS: XOR A
            // IN A, (0xFE)
            // CPL
            // AND 0x1F
            // RET

            // K0: CALL KEYPRESS
            // JR Z, K0
            // K1: CALL KEYPRESS
            // JR NZ, K1
            // K2: CALL KEYPRESS
            // JR Z, K2
    };*/


    /*static int[] data = new int[] {
0x21, 0x00, 0x40, 0x3e, 0x80, 0x06, 0x1f, 0x48, 0x06, 0x09, 0x77, 0xcd, 0x3e, 0x00, 0x28, 0xfb
, 0xcd, 0x3e, 0x00, 0x20, 0xfb, 0x1f, 0x10, 0xf2, 0x2c, 0x1f, 0x41, 0x10, 0xea, 0x45, 0x04, 0x48
, 0x06, 0x08, 0x77, 0xcd, 0x3e, 0x00, 0x28, 0xfb, 0xcd, 0x3e, 0x00, 0x20, 0xfb, 0xcd, 0x3e, 0x00
, 0x28, 0xfb, 0x17, 0x10, 0xed, 0x17, 0x2d, 0x41, 0x10, 0xe5, 0xc3, 0x00, 0x00, 0x76, 0xf5, 0xaf
, 0xdb, 0xfe, 0x2f, 0xe6, 0x1f, 0xd1, 0x7a, 0xc9
};*/

static int[] data = new int[] {
  0x21, 0x00, 0x40, 0x3e, 0x80, 0x77, 0xcd, 0x36, 0x00, 0x28, 0xfb, 0xcd, 0x36, 0x00, 0x20, 0xfb
, 0xa7, 0x1f, 0x20, 0xf1, 0x2c, 0x7d, 0xfe, 0x20, 0x20, 0xe9, 0x2d, 0x3e, 0x01, 0x77, 0xcd, 0x36
, 0x00, 0x28, 0xfb, 0xcd, 0x36, 0x00, 0x20, 0xfb, 0xa7, 0x17, 0x20, 0xf1, 0x2d, 0x7d, 0xfe, 0xff
, 0x20, 0xe9, 0xc3, 0x00, 0x00, 0x76, 0xf5, 0xaf, 0xdb, 0xfe, 0x2f, 0xe6, 0x1f, 0xd1, 0x7a, 0xc9
};

/*static int[] data = new int[] {
      0x21, 0x00, 0x40, 0x3e, 0x80, 0x77, 0xa7, 0x1f, 0x20, 0xfb, 0x2c, 0x7d, 0xfe, 0x20, 0x20, 0xf3
    , 0x2d, 0x3e, 0x01, 0x77, 0xa7, 0x17, 0x20, 0xfb, 0x2d, 0x7d, 0xfe, 0xff, 0x20, 0xf3, 0xc3, 0x00
    , 0x00, 0x76
};*/

    static {
         for(int i=0; i<data.length; i++){
            rom[i] = (byte) data[i];
         }
     }
    
    public static void main(String[] args) {
        ZXSpectrumScreen screen = new ZXSpectrumScreen();
        IODevice keyboard = new ZXSpectrumKeyboard();
        Z80 cpu;
        Memory m;
        VRAM v;

        Computer spectrum = new Computer();
        spectrum.addCPU(cpu = new Z80());
        spectrum.addMemory(0x0000, m = new ROMMemory(rom));
        spectrum.addMemory(0x4000, v = new VRAM(screen));
        spectrum.addMemory(0x8000, new RAMMemory(16 * 1024));
        /*spectrum.addMemory(0xC000, new ROMMemory(16 * 1024))*/
        spectrum.addIODevice((short)0xFE, keyboard); // new ZXSpectrumIO(new ZXSpectrumKeyboard(), new ZXSpectrumBeeperTapeAndBorder())

        spectrum.reset();

		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame mf = new MainFrame(screen, (KeyListener) keyboard);
                mf.init("ZX Spectrum Emulator v0.2" /*, zx*/);
                mf.createMenuBar();
                mf.createPanels();
                mf.show();

                /*Computer zx = createZXSpectrum(screen);*/
            }
        });

        SwingWorker worker = new SwingWorker<Void, Rectangle>() {
            @Override
            protected Void doInBackground() {
                //spectrum.run();
                for (;;) {
                    int pc = cpu.getPC();
                    byte opC = m.peek(pc);
                    Rectangle r;
                    cpu.fetch(opC); // fetch opCode
                    r = v.getRectangle();
                    if( r != null ) {
                        publish(r);
                    }
                    if( opC == 0x76) break; // is HALT?
                }
                return null;
            }

            @Override
            protected void process(List<Rectangle> chunks) {
                for (Rectangle rect : chunks) {
                    screen.repaint(rect);
                }
            }
        };

        worker.execute();
    }
    
    private static /*Computer TODO*/ void createZXSpectrum(Screen screen) {



    }
}