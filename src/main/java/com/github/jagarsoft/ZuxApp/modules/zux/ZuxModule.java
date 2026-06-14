package com.github.jagarsoft.ZuxApp.modules.zux;

import com.github.jagarsoft.*;
import com.github.jagarsoft.ZuxApp.core.bus.CommandHandler;
import com.github.jagarsoft.ZuxApp.infrastructure.module.BaseModule;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.commands.AddJInternalFrameToDesktopPaneCommand;

import static com.github.jagarsoft.ZuxApp.modules.zux.IOManagementUnit.*;
import static com.github.jagarsoft.ZuxApp.modules.zux.ZuxLogger.LOGGER_CMD;
import static com.github.jagarsoft.ZuxApp.modules.zux.ZuxLogger.LOGGER_DAT;

import javax.swing.*;

public class ZuxModule extends BaseModule {
    JInternalFrame frame;
    TerminalModel terminalModel;
    TerminalPanel terminalPanel;

    @Override
    public void configure() {
        commandBus.registerHandler(SetZuxDeviceBanksCommand.class, new CommandHandler<SetZuxDeviceBanksCommand>() {
            @Override
            public void handle(SetZuxDeviceBanksCommand command) {
                Computer zuxComputer = command.getComputer();

                // 2^16 * 64K = 4096K = 4M
                for(long bank = 0; bank < 0x0010 /*_0000L*/ /*0x1_0000_0000L*/; bank += 0x1/*_0000*/) {
                    //System.out.println("Bank: " + String.format("0x%08X", bank));
                    zuxComputer.addMemory((int)bank, new RAMMemory(64 * 1024));
                }

                zuxComputer.addIODevice((short) 0x00CC, new ZuxIO(null, new ZuxTerminal(terminalModel, terminalPanel)));

                IOManagementUnit ioMU = new IOManagementUnit(zuxComputer);
                zuxComputer.addIODevice(new short[]{
                        GET_TOT_MEM_CMD, GET_TOT_MEM_DAT,
                        PHYS_COPY_CMD, PHYS_COPY_DAT},
                        new ZuxIO(ioMU, ioMU));

                ZuxLogger zuxLogger = new ZuxLogger(zuxComputer);
                zuxComputer.addIODevice(new short[]{
                        LOGGER_CMD, LOGGER_DAT},
                        new ZuxIO(zuxLogger, zuxLogger));

                //MemoryManagementUnit MMU = new MemoryManagementUnit(zux);
                //zux.addIODevice(new byte[]{(byte)PHYS_COPY_CMD, (byte)PHYS_COPY_DAT}, new ZuxIO(MMU, MMU));

                Z80OpCode customRETI = Z80OpCodeInterceptor.createInterceptor(
                        zuxComputer.getCPU(),
                        "RETI",
                        () -> {
                            System.out.println("Custom RETI implementation!");
                            zuxComputer.getCPU().RETI();
                            // Conmutar el bank aqui
                        }
                );

                Z80OpCode customRST20 = Z80OpCodeInterceptor.createInterceptor(
                        zuxComputer.getCPU(),
                        "RST20",
                        () -> {
                            System.out.println("Custom RST 20h implementation!");
                            zuxComputer.getCPU().RST_y_8();
                            // Conmutar el bank aqui
                        }
                );

                // Replace entry on table
                zuxComputer.getCPU().dispatcherFor((byte) 0xED, (byte) 0x4D, customRETI);
                zuxComputer.getCPU().dispatcherFor((byte) 0x00, (byte) 0xE7, customRST20);

                zuxComputer.poke(0, (byte) 0x00);
                zuxComputer.poke(1, (byte) 0xED);
                zuxComputer.poke(2, (byte) 0x4D);
                zuxComputer.getCPU().fetch();
                zuxComputer.getCPU().fetch();

                zuxComputer.poke(3, (byte) 0xE7);
                zuxComputer.getCPU().fetch();

                /*computer.addIODevice(ports,
                        new BroadCastEvents(eventBus, new ZXSpectrumIO((IODevice) keyboard, new ZXTapeAndBorder(screen))));*/
                /*IODevice ktb = new ZXKeyboardTapeAndBorder(keyboard, screen);
                computer.addIODevice(ports,
                        new BroadCastEvents(eventBus, new ZXSpectrumIO(ktb, ktb)));*/

            }
        });
    }

    @Override
    public void initUI() {
        frame = new JInternalFrame("Zux Terminal", true, true, true, true);
        /*keyboard = new ZXSpectrumKeyboard();
        screen = new ZXSpectrumScreen();
        screen.setFocusable(true);
        screen.addKeyListener(keyboard);
        frame.setContentPane(screen);*/
        terminalModel = new TerminalModel(25, 80);
        terminalPanel = new TerminalPanel(terminalModel);
        frame.add(terminalPanel);
        frame.pack();
        frame.setLocation(20, 20);

        this.commandBus.execute(new AddJInternalFrameToDesktopPaneCommand(frame));
        frame.setVisible(true);
    }
}
