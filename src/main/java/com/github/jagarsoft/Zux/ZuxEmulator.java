package com.github.jagarsoft.Zux;

import com.github.jagarsoft.*;
import com.github.jagarsoft.GUI.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.jagarsoft.Zux.MemoryManagementUnit.PHYS_COPY_CMD;
import static com.github.jagarsoft.Zux.MemoryManagementUnit.PHYS_COPY_DAT;

public class ZuxEmulator {
    public static void main(String[] args) {
        ZuxScreen screen = new ZuxScreen();
        IODevice keyboard = null; //new ZuxKeyboard();
        FileInputStream dataStream;
        final AtomicBoolean triggerFlag = new AtomicBoolean(false);
        Z80 cpu;
        Z80Disassembler disassembler;

        Computer zux = new Computer();
        zux.addCPU(cpu = new Z80());
        zux.addMemory(0x0000_0000, new RAMMemory(64 * 1024));
        zux.addMemory(0x0001_0000, new RAMMemory(64 * 1024));
        // 2^16 * 64K = 4096K
        /*for(long bank = 0; bank < 0x0010_0000L /#*0x1_0000_0000L*#/; bank += 0x1_0000) {
            //System.out.println("Bank: " + String.format("0x%08X", bank));
            zux.addMemory((int)bank, new RAMMemory(64 * 1024));
        }*/

        zux.addIODevice((byte) 0xCC, new ZuxIO(keyboard, new ZuxTerminal(screen)));
        MemoryManagementUnit MMU = new MemoryManagementUnit(zux);
        zux.addIODevice(new byte[]{(byte)PHYS_COPY_CMD, (byte)PHYS_COPY_DAT}, new ZuxIO(MMU, MMU));

        disassembler = new Z80Disassembler(cpu);
        disassembler.setComputer(zux);

        File file = new File(args[0]);
        try {
            dataStream = new FileInputStream(file);
            int size = (int) file.length();
            zux.load(dataStream, size);
            dataStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //System.out.println(FileSizeFormatter.stringifyFileSize(zux.getMemSize()*64L,4,1024));
        System.out.println(zux.getMemSize()*64L+"Kb");
        zux.reset();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame mf = new MainFrame(screen, (KeyListener) keyboard);
                mf.init("Zux Emulator v0.1" /*, zx*/);
                mf.createMenuBar();
                mf.createPanels();
                mf.show();
            }
        });

        SwingWorker worker = new SwingWorker<Void, Rectangle>() {
            @Override
            protected Void doInBackground() {
                long startTime, endTime;
                long startTick, endTick;
                startTime = startTick = System.nanoTime();
                //zux.run();
                for (int cont = 0; ; cont++) { // for(;;)
                    int pc = cpu.getPC();
                    Logger.info("fetch ");
                    byte opC = zux.peek(pc);
                    disassembler.fetch(opC);
                    cpu.fetch(opC); // fetch opCode

                    //endTime = System.nanoTime();
                    //long duration = endTime - startTime;
                    //if (duration > 20_000_000) {
                    /*if (duration > 19_999_000) {
                        Logger.tick("Tiempo de ejecución: " + duration + " nanosegundos (" + cont + ")");
                        Logger.tick("Tiempo de ejecución: " + (duration / 1_000_000.0) + " milisegundos");
                        cont = 0;
                        startTime = System.nanoTime();
                    }*/
                    if (triggerFlag.getAndSet(false)) {
                        endTick = System.nanoTime();
                        Logger.tick("TICK !!!");
                        long durationTick = endTick - startTick;
                        Logger.tick("Tiempo TICK: " + durationTick + " nanosegundos (" + cont + ")");
                        Logger.tick("Tiempo TICK: " + (durationTick / 1_000_000.0) + " milisegundos");
                        startTick = System.nanoTime();
                        publish(new Rectangle(0,0,8,8));
                        cpu.interrupt();
                    }
                }
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
}
