package com.github.jagarsoft.ZuxApp.modules.zxspectrum.commands;

import com.github.jagarsoft.ZuxApp.core.bus.Command;
import com.github.jagarsoft.Computer;

public class SetZXSpectrumDeviceBanksCommand implements Command {
    private Computer computer;

    public SetZXSpectrumDeviceBanksCommand(Computer computer) {
        setComputer(computer);
    }

    public void setComputer(Computer computer) {
        this.computer = computer;
    }

    public Computer getComputer() {
        return this.computer;
    }
}
