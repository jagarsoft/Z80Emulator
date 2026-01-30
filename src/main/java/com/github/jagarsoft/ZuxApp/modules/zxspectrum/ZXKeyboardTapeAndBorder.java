package com.github.jagarsoft.ZuxApp.modules.zxspectrum;

import com.github.jagarsoft.IODevice;

public class ZXKeyboardTapeAndBorder implements IODevice {


    @Override
    public void write(int addr, byte data) {

    }

    @Override
    public void write(int addr, byte data, int tstate) {

    }

    @Override
    public byte read(int addr) {
        return 0;
    }
}
