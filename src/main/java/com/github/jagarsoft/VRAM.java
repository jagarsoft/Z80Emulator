package com.github.jagarsoft;

import com.github.jagarsoft.GUI.Screen;

import java.awt.*;

public class VRAM implements Memory {
    private int k = 0;
    private Screen screen;
    private byte[] ram;
    private Rectangle r = null;
    public short size;

    public VRAM(Screen screen){
        this.screen = screen;
        this.ram = new byte[16 * 1024];
        this.size = (short)ram.length;
    }

    public void poke(int addr, byte data) {
        Color c[] = {Color.YELLOW, Color.GREEN, Color.BLUE, Color.CYAN};
        ram[addr] = data;
        if( addr < 0x5800 - 0x4000 ) { // pixels map
            int[] point = new int[2]; // (x, y) of the pixel
            int[] attr  = new int[2]; // (paper, ink)
            int[] pointLR = new int[2]; // (c, r) of the char
            transformZX(addr, point);
            //transformZXattr(point, attr, pointLR);
System.out.println(++k+": HL = "+Integer.toHexString(addr)+" x="+point[0]+" y="+point[1]+" -> "+Integer.toHexString(((byte)(data&0xFF))));
            for(int i = 0, j = 0; i<8; i++, data<<=1, j%=4) {
                if( ((data & 0x1000_0000) != 0) )
                    screen.drawPixel(point[0] + i, point[1], Color.RED);//attr[1]); // set pixel to Ink
                else
                    screen.drawPixel(point[0] + i, point[1], c[j]); //attr[0]); // res pixel to Paper
                System.out.println(j++);
            }
            this.r = new Rectangle(point[0], point[1], 8, 1);
        } else if ( addr < 0x5B00 - 0x4000 ) { // attr map
            /*for(int i = 0; i < 8; i++)
                for(int j = 0; j < 8; j++) {
                    OBTENER LA DIR DEL 1ER BYTE DEL BLOQUE Y REPINTAR LOS 64 PIXELES QUE LO COMPONEN
                }*/
            //screen.drawAttr(data, attr);
            //this.r = new Rectangle(point[0], point[1], 8, 8);
        }
   }

    public short getSize(){ return size; }

    public byte peek(int addr) { return ram[addr]; }

    public Rectangle getRectangle() {
        Rectangle or = r;
        r = null;
        return or;
    }

    // Lineal: sin tercios
    private void transform(int addr, int[] point) {
        int x, y;
        byte h, l;

        h = (byte) ((addr & 0xFF00) >> 8);
        l = (byte) (addr & 0x00FF);

        y = ((h & 0b0001_1111) << 3)
          | ((l & 0b1110_0000) >> 5);
        x = l & 0b0001_1111;

        point[0] = x*8;
        point[1] = y;
    }
    private void transformZX(int addr, int[] point) {
        int x, y;
        byte h, l;

            h = (byte) ((addr & 0xFF00) >> 8);
            l = (byte) (addr & 0x00FF);

            y = ((h & 0b0001_1000) << 3)
              | ((l & 0b1110_0000) >> 2)
              |  (h & 0b0000_0111);
            x = l & 0b0001_1111;

            point[0] = x*8;
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

    private void transformZXattr(int[] point, int[] attr, int[] pointLR) {
        int c, r;

            c = point[0] / 32;
            r = point[1] / 24;

            pointLR[0] = c;
            pointLR[1] = r;

            attr[0] = ram[0x5B00 + r*32 + c] & 0b0011_1000; // Paper
            attr[1] = ram[0x5B00 + r*32 + c] & 0b0000_0111; // Ink
    }
}
