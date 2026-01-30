package com.github.jagarsoft.ZuxApp.modules.tape;

import com.github.jagarsoft.Computer;
import com.github.jagarsoft.ZuxApp.modules.debugger.events.BinaryImageLoadedEvent;

import java.io.IOException;

public class TAP {

/*
https://worldofspectrum.org/faq/reference/48kreference.htm#TapeDataStructure
https://wiki.speccy.org/doku.php?id=cursos:ensamblador:rutinas_save_load&rev=1705665538

TAP format

Byte    Length  Description
---------------------------
0       2       Length of header block (always 19 (0013h))
2       1       Byte Flag: 0=Header

The structure of the 17 byte tape header is as follows.

Byte    Length  Description
---------------------------
0       1       Type (0,1,2 or 3)
1       10      Filename (right-padded with blanks)
11      2       Length of data block
13      2       Parameter 1
15      2       Parameter 2
17      1       Checksum byte of header

Byte    Length  Description
---------------------------
0       2       Length of data block (Length of data block (from offset 11) + 2)
2       1       Byte Flag: FFh=Data
3       N       N bytes as indicated by offset 11 (Length of data block)
N+3     1       Checksum byte of data block

*/

    TAPHeader h = new TAPHeader();
    TAPData d = new TAPData();
    Seekable s;
    Computer currentComp;

    public TAP(Computer currentComp) {
        try {
            s = new RAFSeekable("C:\\Users\\fjgarrido\\Downloads\\zexall.tap", "r");
            //s = new RAFSeekable("C:\\Users\\fjgarrido\\Downloads\\PorompomPong.tap", "r");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.currentComp = currentComp;
    }

    public void SA_BYTES() {
    }

    public void LD_BYTES(Computer currentComp, byte a, short ix, short de) {
        if( a == 0 ) {
            try {
                loadHead(s);
                currentComp.loadBytes(((RAFSeekable) s).raf, ix, de);
                loadChkSumHead(s);
            } catch (IOException e) {
                //throw new RuntimeException(e);
                e.printStackTrace();
            }
        } else if ( a == (byte) 0x0FF ) {
            try {
                loadBlock(s);
                currentComp.loadBytes(((RAFSeekable) s).raf, ix, de);
                loadChkSumData(s);
                //(currentComp.getEventBus()).publish(new BinaryImageLoadedEvent(currentComp, ix, de));
            } catch (IOException e) {
                //throw new RuntimeException(e);
                e.printStackTrace();
            }
        }

        //Computer comp  = new Computer();
        //comp.movemem();
    }

    private void loadHead(Seekable s) throws IOException {
            // Leer longitud
            h.lengthHeaderBlock = s.getShortLE();
        return;

        // Leer flag
        //h.byteFlagHeader = s.getByte();
/*            h.type = s.getByte();

            // Leer datos
            h.filename = new byte[10];
            s.read(h.filename,0, 10);

            // Leer
            h.lengthDataBlock = s.getShortLE();

            h.parameter1 = s.getShortLE();
            h.parameter2 = s.getShortLE();

            h.checksumByteHeader = s.getByte();
  */  }

    private void loadChkSumHead(Seekable s) throws IOException {
        h.checksumByteHeader = s.getByte();
    }

    private void loadBlock(Seekable s) throws IOException {
        d.lengthDataBlock = s.getShortLE();
        return;

/*        d.byteFlagData = s.getByte();
        d.data = new byte[d.lengthDataBlock];
        d.checksumByteData = s.getByte();
*/    }

    private void loadChkSumData(Seekable s) throws IOException {
        d.checksumByteData = s.getByte();
    }
}

class TAPHeader {
    public int lengthHeaderBlock; // = 13h short
    public int byteFlagHeader; // = 0 byte
    public int type; // (0,1,2 or 3) byte
    public byte[] filename; // (right-padded with blanks)
    public int lengthDataBlock; // short
    public int parameter1; // short
    public int parameter2; // short
    public int checksumByteHeader; // byte
}

class TAPData {
    public int lengthDataBlock; // = TAPHeader.lengthHeaderBlock short
    public int byteFlagData; // = FFh byte
    public byte[] data;
    public int checksumByteData; // byte
}