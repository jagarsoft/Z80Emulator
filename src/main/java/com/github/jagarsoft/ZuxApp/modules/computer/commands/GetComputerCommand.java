package com.github.jagarsoft.ZuxApp.modules.computer.commands;

import com.github.jagarsoft.Computer;
import com.github.jagarsoft.Z80;
import com.github.jagarsoft.ZuxApp.core.bus.Command;

public class GetComputerCommand implements Command {
    private Computer computer;

    /*public GetCPUCommand(Z80 cpu) {
        this.cpu = cpu;
    }*/

    public Computer getComputer() { return computer; }

    public Z80 getCpu() { return computer.getCPU(); }

    public void setCpu(Z80 cpu) { computer.addCPU(cpu); }

    public void setComputer(Computer computer) { this.computer = computer; }
}
