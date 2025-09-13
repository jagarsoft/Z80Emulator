package com.github.jagarsoft.ZuxApp.modules.computer;

import com.github.jagarsoft.*;
import com.github.jagarsoft.Zux.ZuxIO;
import com.github.jagarsoft.Zux.ZuxLogger;
import com.github.jagarsoft.Zux.ZuxScreen;
import com.github.jagarsoft.Zux.ZuxTerminal;
import com.github.jagarsoft.ZuxApp.core.bus.CommandHandler;
import com.github.jagarsoft.ZuxApp.infrastructure.bus.BroadCastEvents;
import com.github.jagarsoft.ZuxApp.infrastructure.module.BaseModule;
import com.github.jagarsoft.ZuxApp.modules.computer.commands.ComputerLoadImageCommand;
import com.github.jagarsoft.ZuxApp.modules.computer.commands.GetComputerCommand;
import com.github.jagarsoft.ZuxApp.modules.logger.events.LogEvent;
import com.github.jagarsoft.ZuxApp.modules.memoryconfig.commands.GetMemoryConfiguration;
import com.github.jagarsoft.ZuxApp.modules.memoryconfig.events.MemoryConfigChangedEvent;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.function.Consumer;

import static com.github.jagarsoft.Zux.ZuxLogger.LOGGER_CMD;
import static com.github.jagarsoft.Zux.ZuxLogger.LOGGER_DAT;

public class ComputerModule extends BaseModule {
    private final Computer computer;
    Z80 cpu;
    ZuxScreen screen;

    public ComputerModule() {
        computer = new Computer();
        computer.addCPU(cpu = new Z80());
        cpu.setComputer(computer);
    }

    private void allocMemory() {
        int pageSizeK;
        GetMemoryConfiguration memConfiguration = new GetMemoryConfiguration();
        commandBus.execute(memConfiguration);
        pageSizeK = memConfiguration.pageSize * 1024;
        eventBus.publish(new LogEvent("Allocating memory..." + memConfiguration.numberPages + " pages of "
                + memConfiguration.pageSize + "K = " + (memConfiguration.numberPages * pageSizeK) + " bytes"));

        for(int bank = 0; bank < memConfiguration.numberPages; bank++) {
            System.out.println("Bank: " + String.format("0x%08X", (long)(bank * pageSizeK)));
            computer.addMemory(bank * pageSizeK, new RAMMemory(pageSizeK));
        }
    }

    @Override
    public void configure() {
        allocMemory();

        screen = new ZuxScreen();
        IODevice keyboard = null; // new ZuxKeyboard();
        computer.addIODevice((byte) 0xCC, new BroadCastEvents(eventBus, new ZuxIO(keyboard, new ZuxTerminal(screen))));
        //computer.addIODevice((byte) 0xFE, new BroadCastEvents(eventBus, new ZuxIO(keyboard, new ZuxTerminal(screen))));
        ZuxLogger zuxLogger = new ZuxLogger(computer);
        computer.addIODevice(new byte[]{(byte)LOGGER_CMD, (byte)LOGGER_DAT}, new BroadCastEvents(eventBus, new ZuxIO(zuxLogger, zuxLogger)));
        // TODO computer.addIODevice((byte)0xFE, new ZXSpectrumIO(keyboard, new ZXBorder(screen)));

        //disassembler = new Z80Disassembler(cpu);
        //disassembler.setComputer(computer);

        eventBus.subscribe(MemoryConfigChangedEvent.class, (Consumer<MemoryConfigChangedEvent>) event -> {
            computer.freeMemory();
            allocMemory();
        });

        commandBus.registerHandler(GetComputerCommand.class, new CommandHandler<GetComputerCommand>() {
            @Override
            public void handle(GetComputerCommand command) {
                command.setComputer(computer);
                //command.setCpu(cpu);
            }
        });

        commandBus.registerHandler(ComputerLoadImageCommand.class, new CommandHandler<ComputerLoadImageCommand>() {
            @Override
            public void handle(ComputerLoadImageCommand command) {
                FileInputStream dataStream;
                File file = command.file;
                int size;

                try {
                    dataStream = new FileInputStream(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                size = (int)file.length();
                if( size > computer.getMemorySize() ) {
                    //eventBus.publish(new ImageExceedsMemory(size, computer.getMemSize()));
                    System.out.println("ImageExceedsMemory " + size + " " + computer.getBankSize());
                    return;
                }

                try {
                    computer.load(dataStream, size);
                    dataStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                //eventBus.publish(new ImageLoadedEvent(computer, size));
            }
        });
    }

    @Override
    public void initUI() {
        //JInternalFrame frame = new JInternalFrame("Debugger", true, true, true, true);
        //screen.createScreen(frame.getFr);
    }
}
