package com.github.jagarsoft.GUI;

import com.github.jagarsoft.IODevice;
import com.github.jagarsoft.Logger;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.BitSet;

import static java.awt.event.KeyEvent.*;

class ZXSpectrumKeyboard implements IODevice, Keyboard, KeyListener {
    protected BitSet A8 = new BitSet(5);
    protected BitSet A9 = new BitSet(5);
    protected BitSet A10 = new BitSet(5);
    protected BitSet A11 = new BitSet(5);
    protected BitSet A12 = new BitSet(5);
    protected BitSet A13 = new BitSet(5);
    protected BitSet A14 = new BitSet(5);
    protected BitSet A15 = new BitSet(5);

    ZXSpectrumKeyboard() {
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

    }

    @Override
    public void keyPressed(KeyEvent e) {
System.out.println("getKeyCode:" + e.getKeyCode());
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
            case VK_J: A15.clear(3); break;
            case VK_K: A14.clear(2); break;
            case VK_L: A14.clear(1); break;
            case VK_ENTER: A14.clear(0); break;
            
            case VK_SHIFT: A8.clear(0); break;
            case VK_Z: A8.clear(1); break;
            case VK_X: A8.clear(2); break;
            case VK_C: A8.clear(3); break;
            case VK_V: A8.clear(4); break;

            case VK_B: A15.clear(4); break;
            case VK_N: A15.clear(3); break;
            case VK_M: A15.clear(2); break;
            case VK_ALT:
            case VK_ALT_GRAPH:
                       A15.clear(1); break;
            case VK_SPACE: A15.clear(0); break;
            default:
                A8.set(0, 5);
                A9.set(0, 5);
                A10.set(0, 5);
                A11.set(0, 5);
                A12.set(0, 5);
                A13.set(0, 5);
                A14.set(0, 5);
                A15.set(0, 5);
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
System.out.println("keyReleased:" + e.getKeyCode());
/*        switch(e.getKeyCode()) {
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
            case VK_J: A15.set(3); break;
            case VK_K: A14.set(2); break;
            case VK_L: A14.set(1); break;
            case VK_ENTER: A14.set(0); break;

            case VK_SHIFT: A8.set(0); break;
            case VK_Z: A8.set(1); break;
            case VK_X: A8.set(2); break;
            case VK_C: A8.set(3); break;
            case VK_V: A8.set(4); break;

            case VK_B: A15.set(4); break;
            case VK_N: A15.set(3); break;
            case VK_M: A15.set(2); break;
            case VK_ALT:
            case VK_ALT_GRAPH:
                A15.set(1); break;
            case VK_SPACE: A15.set(0); break;
            default:
                A8.set(0, 5);
                A9.set(0, 5);
                A10.set(0, 5);
                A11.set(0, 5);
                A12.set(0, 5);
                A13.set(0, 5);
                A14.set(0, 5);
                A15.set(0, 5);
        }*/
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
    public byte read(int addr) {
        int row = ~((addr & 0xFF00) >> 8);
        int k = 0xFF;
System.out.println("ZXSpectrumKeyboard.read:"+Integer.toHexString(addr));
System.out.println("ZXSpectrumKeyboard.row:"+Integer.toHexString(row));
        if(( row & ~0xFE) != 0 )
            k &= A8.toByteArray()[0];

        if(( row & ~0xFD) != 0 )
            k &= A9.toByteArray()[0];

        if(( row & ~0xFB) != 0 )
            k &= A10.toByteArray()[0];

        if(( row & ~0xF7) != 0 )
            k &= A11.toByteArray()[0];

        if(( row & ~0xEF) != 0 )
            k &= A12.toByteArray()[0];

        if(( row & ~0xDF) != 0 )
            k &= A13.toByteArray()[0];

        if(( row & ~0xBF) != 0 )
            k &= A14.toByteArray()[0];

        if(( row & ~0x7F) != 0 )
            k &= A15.toByteArray()[0];
System.out.println("k:"+Integer.toHexString(k));
        return (byte) k;
    }
}