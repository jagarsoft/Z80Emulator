package com.github.jagarsoft.ZuxApp.modules.computer.commands;

import com.github.jagarsoft.ZuxApp.core.bus.Command;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.commands.GetFileSelectedCommand;

import java.io.File;

public class ComputerLoadImageCommand implements Command {
    public File file;

    public ComputerLoadImageCommand(File currentFile) {
        this.file = currentFile;
    }
}
