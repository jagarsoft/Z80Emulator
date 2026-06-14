package com.github.jagarsoft.ZuxApp.modules.zux;

import com.github.jagarsoft.IODevice;

public class ZuxTerminal implements IODevice {
    TerminalModel terminalModel;
    TerminalPanel terminalPanel;

    public ZuxTerminal(TerminalModel terminalModel,
                       TerminalPanel terminalPanel) {
        this.terminalModel = terminalModel;
        this.terminalPanel = terminalPanel;
    }

    @Override
    public void write(int addr, byte data) {
        terminalModel.putChar((char) data);
    }

    @Override
    public void write(int addr, byte data, int tstate) {
        this.write(addr, data);
    }

    @Override
    public byte read(int addr) {
        return 0;
    }
}
