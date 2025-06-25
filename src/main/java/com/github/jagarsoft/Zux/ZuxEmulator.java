package com.github.jagarsoft.Zux;

import com.github.jagarsoft.*;
import com.github.jagarsoft.GUI.MainFrame;
import com.github.jagarsoft.GUI.Screen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import com.github.jagarsoft.Zux.Observable;

import static com.github.jagarsoft.Zux.MemoryManagementUnit.PHYS_COPY_CMD;
import static com.github.jagarsoft.Zux.MemoryManagementUnit.PHYS_COPY_DAT;

import static com.github.jagarsoft.Zux.ZuxLogger.LOGGER_CMD;
import static com.github.jagarsoft.Zux.ZuxLogger.LOGGER_DAT;

public class ZuxEmulator {
    static BlockingQueue<String> dbgCommands = new LinkedBlockingQueue<>();

    public static void main(String[] args) {
        ZuxScreen screen = new ZuxScreen();
        IODevice keyboard = null; //new ZuxKeyboard();
        FileInputStream dataStream;
        Z80 cpu;
        Z80Disassembler disassembler;
        int argc_opt = 0;
        boolean debugginOn;

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
        //MemoryManagementUnit MMU = new MemoryManagementUnit(zux);
        //zux.addIODevice(new byte[]{(byte)PHYS_COPY_CMD, (byte)PHYS_COPY_DAT}, new ZuxIO(MMU, MMU));

        ZuxLogger zuxLogger = new ZuxLogger(zux);
        zux.addIODevice(new byte[]{(byte) LOGGER_CMD, (byte) LOGGER_DAT}, new ZuxIO(zuxLogger, zuxLogger));

        disassembler = new Z80Disassembler(cpu);
        disassembler.setComputer(zux);

        if (args[argc_opt].equals("-d")) {
            debugginOn = true;
            argc_opt++;
        } else {
            debugginOn = false;
        }

        File file = new File(args[argc_opt]);
        try {
            dataStream = new FileInputStream(file);
            int size = (int) file.length();
            zux.load(dataStream, size);
            dataStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //System.out.println(FileSizeFormatter.stringifyFileSize(zux.getMemSize()*64L,4,1024));
        System.out.println(zux.getMemSize() * 64L + "Kb");
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

        if (debugginOn) {
            ZuxDebuggingRunner zuxDebuggingRunner = new ZuxDebuggingRunner(zux, screen);
            zuxDebuggingRunner.execute();
            DbgOrders dbgOrders = new DbgOrders(zuxDebuggingRunner);
            dbgOrders.start();
        } else {
            ZuxRunner zuxRunner = new ZuxRunner(zux, screen);
            zuxRunner.execute();
        }
    }
}

class ZuxRunner extends SwingWorker<Void, Rectangle> {
    private Screen screen;
    private Z80 cpu;
    final AtomicBoolean triggerFlag = new AtomicBoolean(false);

    ZuxRunner(Computer zux, Screen screen) {
        this.cpu = zux.getCPU();
        this.screen = screen;
    }

    @Override
    protected Void doInBackground() {
        long startTime, endTime;
        long startTick, endTick;
        startTime = startTick = System.nanoTime();
        //zux.run();
        for (int cont = 0; ; cont++) { // for(;;)
            //int pc = cpu.getPC();
            //byte opC = zux.peek(pc);
            //disassembler.fetch(opC);
            //cpu.fetch(opC); // fetch opCode
            cpu.fetch();

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
                publish(new Rectangle(0, 0, 8, 8));
                cpu.interrupt();
            }
        }
    }

    @Override
    protected void process(List<Rectangle> chunks) {
        for (Rectangle rect : chunks) {
            this.screen.repaint(rect);
        }
    }
}

class DbgOrders extends Thread {
    Observable eventsListener;

    DbgOrders(Observable eventsListener){
        this.eventsListener = eventsListener;
    }

    @Override
    public void run() {
        super.run();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("zux> ");

            String order = scanner.nextLine();

            if (order.equalsIgnoreCase("exit")) {
                break;
            }

            eventsListener.notify(order);
        }
    }
}
