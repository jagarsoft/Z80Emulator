package com.github.jagarsoft;

public class Console implements IODevice {

    /*Console(InputChannel in, OutputChannel out) {

    }*/

    @Override
    public void write(int addr, char data) {
        System.out.print((char)data);
    }

    @Override
    public byte read(int addr) {
        return 0;
    }
}
