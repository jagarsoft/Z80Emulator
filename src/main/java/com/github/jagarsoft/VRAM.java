package com.github.jagarsoft;

import com.github.jagarsoft.GUI.Screen;

import java.awt.*;

public class VRAM implements Memory {
    private Screen screen;
    private byte[] ram;
    public short size;

    public VRAM(Screen screen){
        this.screen = screen;
        this.ram = new byte[16 * 1024];
        this.size = (short)ram.length;
    }

    public void poke(int addr, byte data) {
        ram[addr] = data;
        if( addr < 0x5B00 - 0x4000) {
            int[] point = new int[2]; // (x, y)
            transform(addr, point);
            System.out.println("HL="+Integer.toHexString(addr)+" x="+point[0]+" y="+point[1]+" "+Integer.toHexString((data)));
            for(int i = 0; i<8; i++, data<<=1) {
                if( ((data & 0x1000_0000) != 0) )
                    screen.drawPixel(point[0]+i, point[1], Color.RED);
                else
                    screen.drawPixel(point[0]+i, point[1], Color.YELLOW);
            }
        }
   }

    public short getSize(){ return size; }

    public byte peek(int addr) { return ram[addr]; }

    // Lineal: sin tercios
    private void transform(int addr, int[] point) {
        int x, y;
        byte h, l;

        h = (byte) ((addr & 0xFF00) >> 8);
        l = (byte) (addr & 0x00FF);

        y = ((h & 0b0001_1111) << 3)
                | ((l & 0b1110_0000) >> 5);
        x = l & 0b0001_1111;

        point[0] = x;
        point[1] = y;
    }
    private void transformZX(int addr, int[] point) {
        int x, y;
        byte h, l;

            h = (byte) ((addr & 0xFF00) >> 8);
            l = (byte) (addr & 0x00FF);

            y = ((h & 0b0001_1000) << 3)
              | ((l & 0b1110_0000) >> 3)
              | (h & 0b0000_0111);
            x = l & 0b0001_1111;

            point[0] = x;
            point[1] = y;
    }

    /*private void transform(int addr, int[] point) {
        int x, y;
        byte h, l;

            h = (byte) ((addr & 0xFF00) >> 8);
            l = (byte) (addr & 0x00FF);
            y = h & 0b0001_1000;
            x = (l & 0b1110_0000) >> 5;
            y |= x;
            y <<= 3;
            y |= h & 0b0000_0111;
            x = l & 0b0001_1111;

            point[0] = x;
            point[1] = y;
    }*/
}
