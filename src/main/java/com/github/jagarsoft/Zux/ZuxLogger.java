package com.github.jagarsoft.Zux;

import com.github.jagarsoft.Computer;
import com.github.jagarsoft.IODevice;
import com.github.jagarsoft.Logger;

public class ZuxLogger implements IODevice {
    final static int LOGGER_CMD = 0x00E0;
    final static int LOGGER_DAT = 0x00E1;
    final static int LOG_INT = 0;
    final static int LOG_STR = 1;
    private final Computer zux;
    private byte log_cmd;
    private byte word = 0;
    private byte proc_ptr_low;
    private byte proc_ptr_hi;
    private int proc_ptr;
    private byte[] byteString = new byte[80];

    ZuxLogger(Computer zux) {
        this.zux = zux;
    }

    @Override
    public void write(int addr, byte data) {
        switch (addr) {
            case LOGGER_CMD:
                log_cmd = data;
                break;
            case LOGGER_DAT:
                switch (log_cmd) {
                    case LOG_INT:
                        if (word++ == 0) {
                            proc_ptr_low = data;
                        } else {
                            proc_ptr_hi = data;
                            word = 0;
                            proc_ptr = proc_ptr_hi * 256 + proc_ptr_low;
                            log(proc_ptr);
                        }
                        break;
                    case LOG_STR:
                        byteString[word++] = data;
                        if (data == '\n') {
                            byteString[word] = 0;
                            log(new String(byteString, 0, word));
                            word=0;
                        }
                        break;
                }
                break;
        }
    }

    @Override
    public byte read(int addr) {
        return 0;
    }

    private void log(int data) {
        Logger.intvalue(data);
    }

    private void log(String str) {
        Logger.write(str);
    }
}
