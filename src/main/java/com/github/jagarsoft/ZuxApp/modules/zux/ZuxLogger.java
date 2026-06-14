package com.github.jagarsoft.ZuxApp.modules.zux;

import com.github.jagarsoft.Computer;
import com.github.jagarsoft.IODevice;
import com.github.jagarsoft.Logger;

public class ZuxLogger implements IODevice {
    public final static short LOGGER_CMD = 0x00E0; // 0x70CD
    public final static short LOGGER_DAT = 0x00E1; // 0X70D7
    public final static byte LOG_INT = 0;
    public final static byte LOG_STR = 1;
    public final static byte LOG_CHAR = 2;
    private final Computer zux;
    private byte log_cmd;
    private byte word = 0;
    private int proc_ptr_low;
    private int proc_ptr_hi;
    private int proc_ptr;
    private byte[] byteString = new byte[80];

    public ZuxLogger(Computer zux) {
        this.zux = zux;
    }

    /*
        1.  log for a number (integer):
            LD  BC, LOGGER_CMD
            LD  A, LOG_INT
            OUT (C), A
            LD  BC, LOGGER_DAT
            LD  A, L; low first
            OUT (C), A
            LD  A, H; then high, two bytes always
            OUT (C), A
            RET

        2.  log for a string (one character at a time, ASCIIZ, \0 flush)
        logSTR: LD  BC, LOGGER_CMD
                LD  A, LOG_STR
                OUT (C), A
                LD  BC, LOGGER_DAT
        nchar:  LD  A, (HL)
                OUT (C), A
                OR  A
                RET Z
                INC HL
                JR  nchar

        3. log for a char
        logCHAR:PUSH    AF
                LD  BC, LOGGER_CMD
                LD  A, LOG_CHAR
                OUT (C), A
                LD  BC, LOGGER_DAT
                POP AF
                OUT (C), A
                RET

     */
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
                            proc_ptr_low = ((short)data&0x00FF);
                        } else {
                            proc_ptr_hi = ((short)data&0x00FF);
                            word = 0;
                            proc_ptr = proc_ptr_hi * 256 + proc_ptr_low;
                            log(proc_ptr);
                        }
                        break;
                    case LOG_STR:
                        byteString[word++] = data;
                        if (data == '\0') {
                            log(new String(byteString, 0, word-1));
                            word=0;
                        }
                        if (data == '\n') {
                            log(new String(byteString, 0, word));
                            word=0;
                        }
                        break;
                    case LOG_CHAR:
                        byteString[0] = data;
                        log(new String(byteString, 0, 1));
                        break;
                }
                break;
        }
    }

    @Override
    public void write(int addr, byte data, int tstate) {
        this.write(addr, data);
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
