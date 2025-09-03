package com.github.jagarsoft.ZuxApp.modules.computer;

import com.github.jagarsoft.*;
import com.github.jagarsoft.ZuxApp.core.bus.CommandHandler;
import com.github.jagarsoft.ZuxApp.infrastructure.module.BaseModule;
import com.github.jagarsoft.ZuxApp.modules.computer.commands.ComputerLoadImageCommand;
import com.github.jagarsoft.ZuxApp.modules.computer.commands.GetComputerCPUCommand;
import com.github.jagarsoft.ZuxApp.modules.logger.events.LogEvent;
import com.github.jagarsoft.ZuxApp.modules.memoryconfig.commands.GetMemoryConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ComputerModule extends BaseModule {
    private final Computer computer;
    Z80 cpu;

    public ComputerModule() {
        computer = new Computer();
        computer.addCPU(cpu = new Z80());
        cpu.setComputer(computer);

        //disassembler = new Z80Disassembler(cpu);
        //disassembler.setComputer(computer);
    }

    private void allocMemory() {
        GetMemoryConfiguration memConfiguration = new GetMemoryConfiguration();
        commandBus.execute(memConfiguration);
        eventBus.publish(new LogEvent("Allocating memory..." + memConfiguration.numberPages + " pages of "
                + memConfiguration.pageSize + " = " + (memConfiguration.numberPages*memConfiguration.pageSize*1024) + " bytes"));

        for(int bank = 0; bank < memConfiguration.numberPages; bank++) {
            System.out.println("Bank: " + String.format("0x%08X", (long)bank));
            computer.addMemory(bank * memConfiguration.pageSize, new RAMMemory(memConfiguration.pageSize*1024));
        }
    }

    @Override
    public void configure() {
        allocMemory();
        // TODO computer.addIODevice((byte)0xFE, new ZXSpectrumIO(keyboard, new ZXBorder(screen)));

        commandBus.registerHandler(GetComputerCPUCommand.class, new CommandHandler<GetComputerCPUCommand>() {
            @Override
            public void handle(GetComputerCPUCommand command) {
                command.setCpu(cpu);
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

                try {
                    computer.load(dataStream, size);
                    dataStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void initUI() {
        // void
    }
}
