package com.github.jagarsoft;

public class Console implements IODevice {

    /*Console(InputChannel in, OutputChannel out) {

    }*/

    @Override
    public void write(int addr, byte data) {
        System.out.print((char)data);
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
