package com.github.jagarsoft.Zux;

import com.github.jagarsoft.Computer;
import com.github.jagarsoft.IODevice;

import static java.lang.String.format;

public class IOManagementUnit implements IODevice {
    final static short PHYS_COPY_CMD = 0x00EE;
    final static short PHYS_COPY_DAT = 0x00EF;

    Computer zux;
    private int data_cnt;
    private byte[] data = new byte[12];

    IOManagementUnit(Computer zux) {
        this.zux = zux;
    }

    @Override
    public void write(int addr, byte data) {
        switch (addr) {
            case PHYS_COPY_CMD:
                data_cnt = 11;
                break;
            case PHYS_COPY_DAT:
                this.data[data_cnt--] = data;
                if(data_cnt == -1)
                    do_phys_copy();
                break;
        }
    }

    @Override
    public byte read(int addr) {
        return (byte) 0xFF;
    }

    private void do_phys_copy() {
        // https://stackoverflow.com/a/70622171/2928048
        int cnt = ((data[11] << 24) & 0xFF000000)
                | ((data[10] << 16) & 0x00FF0000)
                | ((data[9] << 8) & 0x00FF00)
                | (data[8] & 0x00FF);

        int dst = ((data[7] << 24) & 0xFF00_0000)
                | ((data[6] << 16) & 0x00FF0000)
                | ((data[5] << 8) & 0x00FF00)
                | (data[4] & 0x00FF);

        int org = ((data[3] << 24) & 0xFF000000)
                | ((data[2] << 16) & 0x00FF0000)
                | ((data[1] << 8) & 0x00FF00)
                | (data[0]  & 0x00FF);

        System.out.println(format("%04X", cnt));
        System.out.println(format("%04X", dst));
        System.out.println(format("%04X", org));

        //zux.movemem(org, dst, cnt, Memory.MovememDirection.FORWARD);
    }
}
