package com.github.jagarsoft.ZuxApp.modules.zxspectrum.commands;

import com.github.jagarsoft.ZuxApp.core.bus.Command;
import com.github.jagarsoft.Computer;

public class SetZXSpectrumDeviceBanksCommand implements Command {
    public long numberPages;
    public int pageSize;
    private Computer computer;

    public SetZXSpectrumDeviceBanksCommand(Computer computer, long numberPages, int pageSize) {
        setComputer(computer);
        this.numberPages = numberPages;
        this.pageSize = pageSize;
    }

    public void setComputer(Computer computer) {
        this.computer = computer;
    }

    public Computer getComputer() {
        return this.computer;
    }
}
