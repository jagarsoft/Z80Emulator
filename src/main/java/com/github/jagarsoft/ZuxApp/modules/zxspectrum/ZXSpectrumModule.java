package com.github.jagarsoft.ZuxApp.modules.zxspectrum;

import com.github.jagarsoft.*;
import com.github.jagarsoft.ZuxApp.core.bus.CommandHandler;
import com.github.jagarsoft.ZuxApp.infrastructure.bus.BroadCastEvents;
import com.github.jagarsoft.ZuxApp.infrastructure.module.BaseModule;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.commands.AddJInternalFrameToDesktopPaneCommand;
import com.github.jagarsoft.ZuxApp.modules.zxspectrum.commands.SetZXSpectrumDeviceBanksCommand;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.HexFormat;

public class ZXSpectrumModule extends BaseModule {
    JInternalFrame frame;
    ZXSpectrumScreen screen;
    KeyListener keyboard;

    @Override
    public void configure() {
        commandBus.registerHandler(SetZXSpectrumDeviceBanksCommand.class, new CommandHandler<SetZXSpectrumDeviceBanksCommand>() {
            @Override
            public void handle(SetZXSpectrumDeviceBanksCommand command) {
                Computer computer = command.getComputer();

                short[] ports = new short[8+256];

                ports[0] = (short)0x0FEFE;
                ports[1] = (short)0x0FDFE;
                ports[2] = (short)0x0FBFE;
                ports[3] = (short)0x0F7FE;
                ports[4] = (short)0x0EFFE;
                ports[5] = (short)0x0DFFE;
                ports[6] = (short)0x0BFFE;
                ports[7] = (short)0x07FFE;

                for(int i = 0; i <= 255; i++) {
                    ports[i+8] = (short)((i<<8) | 0x0FE);
                }

                computer.addMemory(0x0000, new ROMMemory(16 * 1024));
                computer.addMemory(0x4000, new VRAM(screen));
                computer.addMemory(0x8000, new RAMMemory(16 * 1024));
                computer.addMemory(0xC000, new RAMMemory(16 * 1024));
                computer.addIODevice(ports,
                        new BroadCastEvents(eventBus, new ZXSpectrumIO((IODevice) keyboard, new ZXTapeAndBorder(screen))));
                /*IODevice ktb = new ZXKeyboardTapeAndBorder(keyboard, screen);
                computer.addIODevice(ports,
                        new BroadCastEvents(eventBus, new ZXSpectrumIO(ktb, ktb)));*/

            }
        });
    }

    @Override
    public void initUI() {
        /*JFrame frame = new JFrame("ZXSpectrum Screen");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);

        JDesktopPane desktop = new JDesktopPane();
        frame.setContentPane(desktop);
        */

        // crear frame frame y añadir nuestro panel
        frame = new JInternalFrame("ZXSpectrum Screen", true, true, true, true);
        keyboard = new ZXSpectrumKeyboard();
        screen = new ZXSpectrumScreen();
        screen.setFocusable(true);
        screen.addKeyListener(keyboard);
        frame.setContentPane(screen);
        frame.pack();
        frame.setVisible(true);
        frame.setLocation(20, 20);

        this.commandBus.execute(new AddJInternalFrameToDesktopPaneCommand(frame));
        frame.setVisible(true);

        try {
            //screen.loadSCRFile(new File("C:\\Users\\fjgarrido\\Downloads\\Alstrad.scr"));
            //screen.loadSCRFile(new File("C:\\Users\\fjgarrido\\Downloads\\Amaurote.scr"));

            // https://atornblad.github.io/zx-spectrum-bitmap/
            screen.loadSCRFile(new File("C:\\Users\\fjgarrido\\Downloads\\reset.tap"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /*SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                while(true){
                    for(int row = 0xFEFF; row >= 0x7FFF; row <<= 1) {
                        ((ZXSpectrumKeyboard) keyboard).testKey(row);
                    }
                }
            }
        });*/


        /*new Thread(() -> {
            try {
                // llenar bitmap con un patrón (cada byte alternado)
                for (int addr = 0; addr < VRAM_BYTES; addr++) {
                    byte pattern = (byte) (((addr % 2) == 0) ? 0xAA : 0x55);
                    screen.pokeBitmap(addr, pattern);
                }
                // llenar atributos
                for (int r = 0; r < 24; r++) {
                    for (int c = 0; c < 32; c++) {
                        int idx = r * 32 + c;
                        // crear varios atributos para que se aprecie flash/bright
                        int ink = (c + r) % 8;
                        int paper = (7 - ink);
                        boolean bright = ((r + c) % 2) == 0;
                        boolean flash = ((r + c) % 5) == 0;
                        byte attr = (byte) ((flash ? 0x80 : 0) | (bright ? 0x40 : 0) | ((paper & 0x07) << 3) | (ink & 0x07));
                        screen.pokeAttribute(idx, attr);
                    }
                }
                // cambiar borde alternando colores
                int idx = 0;
                while (true) {
                    screen.out254(idx & 0x07);
                    idx++;
                    Thread.sleep(300);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }).start();*/
    }
}