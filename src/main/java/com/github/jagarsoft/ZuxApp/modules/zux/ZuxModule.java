package com.github.jagarsoft.ZuxApp.modules.zux;

import com.github.jagarsoft.*;
import com.github.jagarsoft.ZuxApp.core.bus.CommandHandler;
import com.github.jagarsoft.ZuxApp.infrastructure.bus.BroadCastMemoryEvents;
import com.github.jagarsoft.ZuxApp.infrastructure.bus.BroadCastIODeviceEvents;
import com.github.jagarsoft.ZuxApp.infrastructure.commands.GetBootstrapCommand;
import com.github.jagarsoft.ZuxApp.infrastructure.module.BaseModule;
import com.github.jagarsoft.ZuxApp.modules.kernel.commands.GetKernelBankCommand;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.commands.AddJInternalFrameToDesktopPaneCommand;

import static com.github.jagarsoft.ZuxApp.modules.zux.IOManagementUnit.*;
import static com.github.jagarsoft.ZuxApp.modules.zux.ZuxLogger.LOGGER_CMD;
import static com.github.jagarsoft.ZuxApp.modules.zux.ZuxLogger.LOGGER_DAT;

import javax.swing.*;

public class ZuxModule extends BaseModule {
    public static final short CONSOLE_OUT = 0x00CC;
    JInternalFrame frame;
    TerminalModel terminalModel;
    TerminalPanel terminalPanel;

    @Override
    public void configure() {
        commandBus.registerHandler(SetZuxDeviceBanksCommand.class, new CommandHandler<SetZuxDeviceBanksCommand>() {
            @Override
            public void handle(SetZuxDeviceBanksCommand command) {
                Computer zuxComputer = command.getComputer();
                GetBootstrapCommand bootstrapCommand;
                commandBus.execute(bootstrapCommand = new GetBootstrapCommand());
                boolean isDebugOn = bootstrapCommand.getBootstrap().isDebugOn();
                Memory kernelBank = null;

                // 2^16 * 64K = 4096K = 4M
                if( isDebugOn ) // only catches memory events for kernel
                    zuxComputer.addMemory(0,
                            new BroadCastMemoryEvents(eventBus, commandBus,
                                    kernelBank = new RAMMemory(64 * 1024)));
                else
                    zuxComputer.addMemory(0,
                            new RAMMemory(64 * 1024));
                for(long bank = 1; bank < 0x0010 /*_0000L*/ /*0x1_0000_0000L*/; bank += 0x1/*_0000*/) {
                    //System.out.println("Bank: " + String.format("0x%08X", bank));
                    zuxComputer.addMemory((int)bank, new RAMMemory(64 * 1024));
                }

                final Memory finalKernelBank = kernelBank;
                commandBus.registerHandler(GetKernelBankCommand.class, new CommandHandler<GetKernelBankCommand>() {
                            @Override
                            public void handle(GetKernelBankCommand cmd) {
                                cmd.kernelBank = finalKernelBank;
                            }
                        });

                // Control Console
                if( isDebugOn )
                    zuxComputer.addIODevice(CONSOLE_OUT,
                            new BroadCastIODeviceEvents(eventBus,
                                new ZuxIO(null /* TODO connect keyboard to terminal */, new ZuxTerminal(terminalModel, terminalPanel))));
                else
                    zuxComputer.addIODevice(CONSOLE_OUT, new ZuxIO(null, new ZuxTerminal(terminalModel, terminalPanel)));

                /*computer.addIODevice(ports,
                        new BroadCastIODeviceEvents(eventBus, new ZXSpectrumIO((IODevice) keyboard, new ZXTapeAndBorder(screen))));*/
                /*IODevice ktb = new ZXKeyboardTapeAndBorder(keyboard, screen);
                computer.addIODevice(ports,
                        new BroadCastIODeviceEvents(eventBus, new ZXSpectrumIO(ktb, ktb)));*/

                //MemoryManagementUnit MMU = new MemoryManagementUnit(zux);
                //zux.addIODevice(new byte[]{(byte)PHYS_COPY_CMD, (byte)PHYS_COPY_DAT}, new ZuxIO(MMU, MMU));

                IOManagementUnit ioMU = new IOManagementUnit(zuxComputer);
                final short[] ports = {
                        SET_KERNEL_PROC_TABLE_CMD,  SET_KERNEL_PROC_TABLE_DAT,
                        GET_TOT_MEM_CMD,            GET_TOT_MEM_DAT,
                        PHYS_COPY_CMD,              PHYS_COPY_DAT,
                        SET_TEXT_BANK_CMD,          SET_TEXT_BANK_DAT,
                        SET_DATA_BANK_INM_CMD,      SET_DATA_BANK_INM_DAT,
                        SET_BOTH_BANKS_CMD,         SET_BOTH_BANKS_DAT,
                        PEEK_BYTE_ON_BANK_CMD,      PEEK_BYTE_ON_BANK_DAT,
                        POKE_BYTE_ON_BANK_CMD,      POKE_BYTE_ON_BANK_DAT,
                        VDISK_CMD,                  VDISK_DAT
                };
                if( isDebugOn )
                    zuxComputer.addIODevice(ports,
                            new BroadCastIODeviceEvents(eventBus,
                                new ZuxIO(ioMU, ioMU)));
                else
                    zuxComputer.addIODevice(ports,
                        new ZuxIO(ioMU, ioMU));

                ZuxLogger zuxLogger = new ZuxLogger(zuxComputer);
                if( isDebugOn )
                    zuxComputer.addIODevice(new short[]{
                                    LOGGER_CMD, LOGGER_DAT},
                                    new BroadCastIODeviceEvents(eventBus,
                                        new ZuxIO(zuxLogger, zuxLogger)));
                else
                    zuxComputer.addIODevice(new short[]{
                                    LOGGER_CMD, LOGGER_DAT},
                                        new ZuxIO(zuxLogger, zuxLogger));

                ioMU.setEventBus(eventBus);

                // Create interceptors to enable banking
                Z80OpCode customRETI = Z80OpCodeInterceptor.createInterceptor(
                        zuxComputer.getCPU(),
                        "RETI",
                        () -> {
                            System.out.println("Custom RETI implementation!");
                            zuxComputer.switchBanks();
                            zuxComputer.getCPU().RETI();
                        }
                );

                Z80OpCode customRST20 = Z80OpCodeInterceptor.createInterceptor(
                        zuxComputer.getCPU(),
                        "RST_y_8",
                        () -> {
                            System.out.println("Custom RST 20h implementation!");
                            zuxComputer.getCPU().RST_y_8();
                            zuxComputer.switchBanks();
                        }
                );

                // Replace entry on table
                zuxComputer.getCPU().dispatcherFor((byte) 0xED, (byte) 0x4D, customRETI);
                zuxComputer.getCPU().dispatcherFor((byte) 0x00, (byte) 0xE7, customRST20);
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
