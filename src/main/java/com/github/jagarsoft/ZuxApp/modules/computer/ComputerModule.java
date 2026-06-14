package com.github.jagarsoft.ZuxApp.modules.computer;

import com.github.jagarsoft.*;
import com.github.jagarsoft.Computer;
import com.github.jagarsoft.ZuxApp.core.bus.CommandHandler;
import com.github.jagarsoft.ZuxApp.infrastructure.module.BaseModule;
import com.github.jagarsoft.ZuxApp.modules.computer.commands.ComputerLoadImageCommand;
import com.github.jagarsoft.ZuxApp.modules.computer.commands.GetComputerCommand;
import com.github.jagarsoft.ZuxApp.modules.debugger.events.BinaryImageLoadedEvent;
import com.github.jagarsoft.ZuxApp.modules.logger.events.LogEvent;
import com.github.jagarsoft.ZuxApp.modules.memoryconfig.commands.GetMemoryConfiguration;
import com.github.jagarsoft.ZuxApp.modules.memoryconfig.events.MemoryConfigChangedEvent;
import com.github.jagarsoft.ZuxApp.modules.tape.PathUtils;
import com.github.jagarsoft.ZuxApp.modules.tape.RAFSeekable;
import com.github.jagarsoft.ZuxApp.modules.tape.TAP;
import com.github.jagarsoft.ZuxApp.modules.zux.SetZuxDeviceBanksCommand;


import java.io.*;
import java.net.URISyntaxException;
import java.util.function.Consumer;

public class ComputerModule extends BaseModule {
    private final Computer computer;
    private final String image;
    private final String dataRegion;
    Z80 cpu;

    /*public ComputerModule() {
        computer = new Computer();
        computer.addCPU(cpu = new Z80());
        computer.setEventBus(eventBus); // TODO refactor
        cpu.setComputer(computer);
        this.image = null;
        this.dataRegion = null;
    }*/

    public ComputerModule(String image, String dataRegion) {
        computer = new Computer();
        computer.addCPU(cpu = new Z80());
        computer.setEventBus(eventBus); // TODO refactor. Computer must not raise events
        cpu.setComputer(computer);
        this.image = image;
        this.dataRegion = dataRegion;
    }

    private void allocMemory() {
        int pageSizeK;
        GetMemoryConfiguration memConfiguration = new GetMemoryConfiguration();
        commandBus.execute(memConfiguration);
        pageSizeK = memConfiguration.pageSize * 1024;
        eventBus.publish(new LogEvent("Allocating memory..." + memConfiguration.numberPages + " pages of "
                + memConfiguration.pageSize + "K = " + (memConfiguration.numberPages * pageSizeK) + " bytes"));

        /*for(int bank = 0; bank < memConfiguration.numberPages; bank++) {
            System.out.println("Bank: " + String.format("0x%08X", (long)(bank * pageSizeK)));
            computer.addMemory(bank * pageSizeK, new RAMMemory(pageSizeK));
        }*/

        /*SetZXSpectrumDeviceBanksCommand command = new SetZXSpectrumDeviceBanksCommand(computer,
                                                                memConfiguration.numberPages, memConfiguration.pageSize);*/
        SetZuxDeviceBanksCommand command = new SetZuxDeviceBanksCommand(computer,
                memConfiguration.numberPages, memConfiguration.pageSize);

        commandBus.execute(command);
    }

    @Override
    public void configure() {
        allocMemory();

        //screen = new ZuxScreen();
        //IODevice keyboard = new ZXSpectrumKeyboard();
        //computer.addIODevice((byte) 0xCC, new BroadCastEvents(eventBus, new ZuxIO(keyboard, new ZuxTerminal(screen))));
        //computer.addIODevice((byte) 0xFE, new BroadCastEvents(eventBus, new ZuxIO(keyboard, new ZuxTerminal(screen))));
        //ZuxLogger zuxLogger = new ZuxLogger(computer);
        //computer.addIODevice(new byte[]{(byte)LOGGER_CMD, (byte)LOGGER_DAT}, new BroadCastEvents(eventBus, new ZuxIO(zuxLogger, zuxLogger)));
        //computer.addIODevice((byte)0xFE, new ZXSpectrumIO(keyboard, new ZXTapeAndBorder(screen)));


        //disassembler = new Z80Disassembler(cpu);
        //disassembler.setComputer(computer);

        eventBus.subscribe(MemoryConfigChangedEvent.class, (Consumer<MemoryConfigChangedEvent>) event -> {
            if( !event.isExecOnLoad() ) { // TODO remove
                computer.freeMemory();
                allocMemory();
            }
        });

        commandBus.registerHandler(GetComputerCommand.class, new CommandHandler<GetComputerCommand>() {
            @Override
            public void handle(GetComputerCommand command) {
                computer.setEventBus(eventBus);
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

                String extension = PathUtils.getExtension(file.getName());

                if( extension.equals("tap") ) {
                    try {
                        loadTAP(file.getAbsolutePath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }

                try {
                    dataStream = new FileInputStream(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                size = (int)file.length();
                if( size == 0x4000 ) // TODO isROMLoading()
                    size = computer.getMemorySize();
                if( size > computer.getMemorySize() ) {
                    //eventBus.publish(new ImageExceedsMemory(size, computer.getMemorySize()));
                    System.out.println("A:ImageExceedsMemory " + size + " > " + computer.getMemorySize());
                    return;
                }

                /*GetLoadExecConfiguration loadExecCommand = new GetLoadExecConfiguration();
                commandBus.execute(loadExecCommand);*/

                try {
                    //computer.load(dataStream, size);
                    computer.loadBytes(dataStream, 0, size); // loadExecCommand.init
                    dataStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                /*if( loadExecCommand.execOnLoad ) {
                    computer.setEventBus(eventBus);
                    computer.setPC(loadExecCommand.init);
                }*/

                //eventBus.publish(new ImageLoadedEvent(computer, size));
                /*computer.poke(0x0F44, (byte) 0);
                computer.poke(0x0F45, (byte) 0);
                computer.poke(0x0F46, (byte) 0);*/
                //computer.poke(0x1303, (byte) 0); // Overwrite HALT TODO interrupts must continue

                // AQUI TODO delegar en ComputerLoadImageCommand si la carga tiene exito
                eventBus.publish(new BinaryImageLoadedEvent(computer, 0, size)); // loadExecCommand.init
            }
        });

        /*commandBus.registerHandler(LoadRawCodeAndRunCommand.class, new CommandHandler<LoadRawCodeAndRunCommand>() {
            @Override
            public void handle(LoadRawCodeAndRunCommand command) {
                FileInputStream dataStream;
                File file = new File(command.binName);
                int size;

                try {
                    dataStream = new FileInputStream(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                size = (int)file.length();
                if( size > computer.getMemorySize() ) {
                    //eventBus.publish(new ImageExceedsMemory(size, computer.getMemSize()));
                    System.out.println("B:ImageExceedsMemory " + size + " " + computer.getBankSize());
                    return;
                }

                int org = Integer.parseInt(command.init);
                try {
                    computer.loadBytes(dataStream, org, size);
                    dataStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                //eventBus.publish(new ImageLoadedEvent(computer, size));
                /*computer.poke(0x0F44, (byte) 0);
                computer.poke(0x0F45, (byte) 0);
                computer.poke(0x0F46, (byte) 0)

                //computer.getCPU().setHL((short)org);
                //computer.getCPU().JP_HL();
            }
        });*/
    }

    private void loadTAP(String fileName) throws IOException, URISyntaxException {
        TAP tap = new TAP();
        RAFSeekable raf = tap.loadTAP(fileName);
        /*FileDescriptor fd = raf.raf.getFD();
        FileInputStream fis = new FileInputStream(fd);*/

        do {
            int init = tap.h.parameter1;
            int offset = tap.h.parameter2;
            long size = tap.h.lengthDataBlock;
            try {
                raf.getShortLE(); // skip len TAP
                raf.getByte(); // skip type
                computer.loadBytes(raf.raf, init, offset, size);
                raf.getByte(); // skip checksum

                // AQUI TODO delegar en ComputerLoadImageCommand si la carga tiene exito
                eventBus.publish(new BinaryImageLoadedEvent(computer, init, size)); // loadExecCommand.init
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } while(tap.getNextComponent(raf));
    }

    @Override
    public void initUI() {
        // It's a service

        //File currentFile = new File(dataRegion);
        //commandBus.execute(new DataRegionModule());

        if( image != null ) {
            File imageFile = new File(image);
            commandBus.execute(new ComputerLoadImageCommand(imageFile));
            computer.run();
        }
    }
}
