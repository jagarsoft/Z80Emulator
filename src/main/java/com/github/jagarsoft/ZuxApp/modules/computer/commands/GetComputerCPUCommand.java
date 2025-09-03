package com.github.jagarsoft.ZuxApp.modules.computer.commands;

import com.github.jagarsoft.Z80;
import com.github.jagarsoft.ZuxApp.core.bus.Command;

public class GetComputerCPUCommand implements Command {
    private Z80 cpu;

    /*public GetCPUCommand(Z80 cpu) {
        this.cpu = cpu;
    }*/

    public Z80 getCpu() {
        return cpu;
    }

    public void setCpu(Z80 cpu) {
        this.cpu = cpu;
    }
}
