package com.github.jagarsoft.ZuxApp.modules.zxspectrum;

import com.github.jagarsoft.IODevice;
import com.github.jagarsoft.Logger;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.BitSet;

import static java.awt.event.KeyEvent.*;

public class ZXSpectrumKeyboard implements IODevice, Keyboard, KeyListener {
    protected BitSet A8 = new BitSet(5);
    protected BitSet A9 = new BitSet(5);
    protected BitSet A10 = new BitSet(5);
    protected BitSet A11 = new BitSet(5);
    protected BitSet A12 = new BitSet(5);
    protected BitSet A13 = new BitSet(5);
    protected BitSet A14 = new BitSet(5);
    protected BitSet A15 = new BitSet(5);

    public ZXSpectrumKeyboard() {
        forgetKeys();
    }

    private void forgetKeys() {
        A8.set(0, 5);
        A9.set(0, 5);
        A10.set(0, 5);
        A11.set(0, 5);
        A12.set(0, 5);
        A13.set(0, 5);
        A14.set(0, 5);
        A15.set(0, 5);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // void
    }

    @Override
    public void keyPressed(KeyEvent e) {
//System.out.println("getKeyCode:" + e.getKeyCode());
        switch(e.getKeyCode()) {
            case VK_1: A11.clear(0); break;
            case VK_2: A11.clear(1); break;
            case VK_3: A11.clear(2); break;
            case VK_4: A11.clear(3); break;
            case VK_5: A11.clear(4); break;

            case VK_6: A12.clear(4); break;
            case VK_7: A12.clear(3); break;
            case VK_8: A12.clear(2); break;
            case VK_9: A12.clear(1); break;
            case VK_0: A12.clear(0); break;

            case VK_Q: A10.clear(0); break;
            case VK_W: A10.clear(1); break;
            case VK_E: A10.clear(2); break;
            case VK_R: A10.clear(3); break;
            case VK_T: A10.clear(4); break;

            case VK_Y: A13.clear(4); break;
            case VK_U: A13.clear(3); break;
            case VK_I: A13.clear(2); break;
            case VK_O: A13.clear(1); break;
            case VK_P: A13.clear(0); break;
            
            case VK_A: A9.clear(0); break;
            case VK_S: A9.clear(1); break;
            case VK_D: A9.clear(2); break;
            case VK_F: A9.clear(3); break;
            case VK_G: A9.clear(4); break;

            case VK_H: A14.clear(4); break;
            case VK_J: A14.clear(3); break;
            case VK_K: A14.clear(2); break;
            case VK_L: A14.clear(1); break;
            case VK_ENTER: A14.clear(0); break;

            //case VK_CAPS_LOCK:
            case VK_CONTROL: A8.clear(0); break; // Caps Shift
            case VK_Z: A8.clear(1); break;
            case VK_X: A8.clear(2); break;
            case VK_C: A8.clear(3); break;
            case VK_V: A8.clear(4); break;

            case VK_B: A15.clear(4); break;
            case VK_N: A15.clear(3); break;
            case VK_M: A15.clear(2); break;
            case VK_SHIFT: A15.clear(1); break;
            case VK_SPACE: A15.clear(0); break;

            // Special combinations
            case VK_BACK_SPACE:
                A8.clear(0);         // Caps Shift
                A12.clear(0); break; // 0 key
            case VK_QUOTE:
                A15.clear(1);        // Shift
                A13.clear(0);        // P
                break;
            case VK_COMMA:
                A15.clear(1);        // Shift
                A15.clear(3);        // N
                break;
            case VK_PERIOD:
                A15.clear(1);        // Shift
                A15.clear(2);        // M
                break;
            case VK_SEMICOLON:
                A15.clear(1);        // Shift
                A13.clear(1);        // O
                break;
            case VK_COLON:
                A15.clear(1);        // Shift
                A8.clear(1);         // Z
                break;
            case VK_LEFT:
            case VK_KP_LEFT:
                A8.clear(0);         // Caps Shift
                A11.clear(4);        // 5
                break;
            case VK_DOWN:
            case VK_KP_DOWN:
                A8.clear(0);         // Caps Shift
                A12.clear(4);        // 6
                break;
            case VK_UP:
            case VK_KP_UP:
                A8.clear(0);         // Caps Shift
                A12.clear(3);        // 7
                break;
            case VK_RIGHT:
            case VK_KP_RIGHT:
                A8.clear(0);        // Caps Shift
                A12.clear(2);       // 8
                break;

            /*default:
                forgetKeys();*/
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
//System.out.println("keyReleased:" + e.getKeyCode());
        switch(e.getKeyCode()) {
            case VK_1: A11.set(0); break;
            case VK_2: A11.set(1); break;
            case VK_3: A11.set(2); break;
            case VK_4: A11.set(3); break;
            case VK_5: A11.set(4); break;

            case VK_6: A12.set(4); break;
            case VK_7: A12.set(3); break;
            case VK_8: A12.set(2); break;
            case VK_9: A12.set(1); break;
            case VK_0: A12.set(0); break;

            case VK_Q: A10.set(0); break;
            case VK_W: A10.set(1); break;
            case VK_E: A10.set(2); break;
            case VK_R: A10.set(3); break;
            case VK_T: A10.set(4); break;

            case VK_Y: A13.set(4); break;
            case VK_U: A13.set(3); break;
            case VK_I: A13.set(2); break;
            case VK_O: A13.set(1); break;
            case VK_P: A13.set(0); break;

            case VK_A: A9.set(0); break;
            case VK_S: A9.set(1); break;
            case VK_D: A9.set(2); break;
            case VK_F: A9.set(3); break;
            case VK_G: A9.set(4); break;

            case VK_H: A14.set(4); break;
            case VK_J: A14.set(3); break;
            case VK_K: A14.set(2); break;
            case VK_L: A14.set(1); break;
            case VK_ENTER: A14.set(0); break;

            //case VK_CAPS_LOCK:
            case VK_CONTROL: A8.set(0); break; // Caps Shift
            case VK_Z: A8.set(1); break;
            case VK_X: A8.set(2); break;
            case VK_C: A8.set(3); break;
            case VK_V: A8.set(4); break;

            case VK_B: A15.set(4); break;
            case VK_N: A15.set(3); break;
            case VK_M: A15.set(2); break;
            case VK_SHIFT: A15.set(1); break;
            case VK_SPACE: A15.set(0); break;

            // Special combinations
            case VK_BACK_SPACE:
                A8.set(0);           // Caps Shift
                A12.set(0);          // 0 key
                break;
            case VK_QUOTE:
                A15.set(1);         // Shift
                A13.set(0);         // P
                break;
            case VK_COMMA:
                A15.set(1);        // Shift
                A15.set(3);       // N
                break;
            case VK_PERIOD:
                A15.set(1);        // Shift
                A15.set(2);       // M
                break;
            case VK_SEMICOLON:
                A15.set(1);        // Shift
                A13.set(1);        // O
                break;
            case VK_COLON:
                A15.set(1);        // Shift
                A8.set(1);         // Z
                break;
            case VK_LEFT:
            case VK_KP_LEFT:
                A8.set(0);        // Caps Shift
                A11.set(4);       // 5
                break;
            case VK_DOWN:
            case VK_KP_DOWN:
                A8.set(0);        // Caps Shift
                A12.set(4);       // 6
                break;
            case VK_UP:
            case VK_KP_UP:
                A8.set(0);        // Caps Shift
                A12.set(3);       // 7
                break;
            case VK_RIGHT:
            case VK_KP_RIGHT:
                A8.set(0);        // Caps Shift
                A12.set(2);       // 8
                break;




            /*default:
                forgetKeys();*/
        }
    }

    @Override
    public void write(int addr, byte data) {
        Logger.write("keyboard was written!: " + addr + " " + data);
    }

    @Override
    public void write(int addr, byte data, int tstate) {
        this.write(addr, data);
    }

    @Override
    public byte  read(int addr) {
        int k = 0x1F;
        int mask = (addr >> 8) & 0xFF;

        if ((mask & 0x01) == 0) k &= toMask(A8);
        if ((mask & 0x02) == 0) k &= toMask(A9);
        if ((mask & 0x04) == 0) k &= toMask(A10);
        if ((mask & 0x08) == 0) k &= toMask(A11);
        if ((mask & 0x10) == 0) k &= toMask(A12);
        if ((mask & 0x20) == 0) k &= toMask(A13);
        if ((mask & 0x40) == 0) k &= toMask(A14);
        if ((mask & 0x80) == 0) k &= toMask(A15);

if( k != 0x1F ) {
    System.out.println("ZXSpectrumKeyboard.read:" + Integer.toHexString(addr));
    System.out.println("ZXSpectrumKeyboard.mask:" + Integer.toHexString(mask));
    System.out.println("k:" + Integer.toHexString(k));
}
        return (byte) (k & 0x0FF);
    }

    private int toMask(BitSet bs) {
        int m = 0;
        for (int i = 0; i < 5; i++) {
            if (bs.get(i)) {
                m |= (1 << i);
            }
        }
        return m;
    }

    //@Override
    public byte readORG(int addr) {
        //int row = ~((addr & 0x0000FF00) >> 8);
        int row = (~(addr >> 8)) & 0xFF;
        int k = 0x1F;

        if(( row & (~0xFE) & 0xFF) != 0 )
            k &= A8.toByteArray()[0];

        if(( row & (~0xFD) & 0xFF) != 0 )
            k &= A9.toByteArray()[0];

        if(( row & (~0xFB) & 0xFF) != 0 )
            k &= A10.toByteArray()[0];

        if(( row & (~0xF7) & 0xFF) != 0 )
            k &= A11.toByteArray()[0];

        if(( row & (~0xEF) & 0xFF) != 0 )
            k &= A12.toByteArray()[0];

        if(( row & (~0xDF) & 0xFF) != 0 )
            k &= A13.toByteArray()[0];

        if(( row & (~0xBF) & 0xFF) != 0 )
            k &= A14.toByteArray()[0];

        if(( row & (~0x7F) & 0xFF) != 0 )
            k &= A15.toByteArray()[0];

/* SOLO PARA VER QUE TECLA PULSO
if( k != 0x1F ) {
    System.out.println("ZXSpectrumKeyboard.read:"+Integer.toHexString(addr));
    System.out.println("ZXSpectrumKeyboard.row:"+Integer.toHexString(row));
    System.out.println("k:"+Integer.toHexString(k));
    // forgetKeys();
}*/
        return (byte) k;
    }

    public void testKey(int row) {
        read(row);
    }
}