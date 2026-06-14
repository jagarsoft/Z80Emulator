package com.github.jagarsoft.ZuxApp.modules.tape;

import com.github.jagarsoft.Computer;
import com.github.jagarsoft.Logger;
import com.github.jagarsoft.ZuxApp.modules.debugger.events.BinaryImageLoadedEvent;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import static java.lang.System.exit;

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

    public TAPHeader h = new TAPHeader();
    public TAPData d = new TAPData();
    Seekable s;
    Computer currentComp; // TODO NO USADO!

    byte[] buffer = new byte[1024];

    public TAP() {}

    // https://archive.org/download/zx_spectrum_tosec_set_september_2023
    public TAP(Computer currentComp) {
        try {
            //s = new RAFSeekable("C:\\Users\\fjgarrido\\Documents\\Private\\Repositorio\\Github\\jagasoft\\Z80Emulator\\src\\main\\resources\\zexall.tap", "r");
            //s = new RAFSeekable("zexall2.tap", "r");
            //s = new RAFSeekable("z80memptr.tap", "r");
            //s = new RAFSeekable("z80full.tap", "r");
            //s = new RAFSeekable("z80doc.tap", "r");
            //s = new RAFSeekable("z80flags.tap", "r");
            //s = new RAFSeekable("z80docflags.tap", "r");
            //s = new RAFSeekable("z80memptr-ZEsarUX.tap", "r");
            s = new RAFSeekable("C:\\Users\\fjgarrido\\Documents\\Private\\Repositorio\\Github\\jagasoft\\Z80Emulator\\src\\main\\resources\\PorompomPong.tap", "r");
            //s = new RAFSeekable("TresEnRaya.tap", "r");
            //s = new RAFSeekable("BatallaEspacial.tap", "r");
            //s = new RAFSeekable("tState-test.tap", "r");
            //s = new RAFSeekable("hnyf2023.tap", "r");
            //s = new RAFSeekable("Celtic Music (1997)(Chezron).tap", "r");
            //s = new RAFSeekable("Harmonic Horror (1986)(Sinclair User)(16K).tap", "r");
            //s = new RAFSeekable("membertro.tap", "r");
            //s = new RAFSeekable("ManicMiner.tap", "r");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.currentComp = currentComp;
    }

    public void SA_BYTES() {
    }

    public void LD_BYTES(Computer currentComp, int a, int ix, int de) {
        if( a == 0 ) {
            try {
                loadHead(s);
                currentComp.loadBytes(((RAFSeekable) s).raf, ix, de);
                loadChkSumHead(s);
            } catch (IOException e) {
                //throw new RuntimeException(e);
                e.printStackTrace();
            }
        } else if ( a == (int) 0x0FF ) {
            try {
                loadBlock(s);
                currentComp.loadBytes(((RAFSeekable) s).raf, ix, de);
                loadChkSumData(s);
                if( h.type == 3 ) {
                    if(currentComp.getEventBus() != null)
                        currentComp.getEventBus().publish(new BinaryImageLoadedEvent(currentComp, ix, de));
                }
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
        //return;

        // Leer flag
        h.byteFlagHeader = s.getByte();
        h.type = s.getByte();
        s.seek(s.position()-1); // unread

        return;
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

        d.byteFlagData = s.getByte();
        return;

/*        d.data = new byte[d.lengthDataBlock];
        d.checksumByteData = s.getByte();
*/    }

    private void loadChkSumData(Seekable s) throws IOException {
        d.checksumByteData = s.getByte();
    }

    public String createTAP(String file, int init, int offset) throws IOException, URISyntaxException {
        RAFSeekable inFile = new RAFSeekable(file, "r");
        String dirname = PathUtils.getDirectoryPath(file);
        String basename = PathUtils.getBaseNameWithoutExtension(file);
        String ext = PathUtils.getExtension(file);
        String outFileName = dirname + '\\' + basename + ".tap";
        RAFSeekable outFile = new RAFSeekable(outFileName, "rw");
        int size = inFile.size();
        //TAPHeader h = new TAPHeader();
        int i;

        if( size == 0 ) {
            System.out.printf("%s has 0 bytes of size", outFileName);
            exit(1);
        }

        h.lengthHeaderBlock = 0x0013;
        h.byteFlagHeader = 0x00;
        h.type = 0x03;

        h.filename = new byte[10];
        for (i = 0; i < basename.length(); i++) {
            h.filename[i] = (byte) (basename.charAt(i));
        }
        for (; i < 10; i++) {
            h.filename[i] = (byte) (' ');
        }

        h.lengthDataBlock = size;
        h.parameter1 = init;
        h.parameter2 = offset;
        h.checksumByteHeader = 0xAA;

        // Header block

        outFile.putShortLE(0, (short) h.lengthHeaderBlock);
        outFile.putByte(2, (byte) h.byteFlagHeader);
        outFile.putByte(3, (byte) h.type);
        outFile.write(h.filename, 0, 10);
        outFile.putShortLE(14, (short) h.lengthDataBlock);
        outFile.putShortLE(16, (short) h.parameter1);
        outFile.putShortLE(18, (short) h.parameter2);
        outFile.putByte(20, (byte) h.checksumByteHeader);

        // Data block

        d.lengthDataBlock = (short)(size+2);
        d.byteFlagData = (byte) 0xFF;
        d.checksumByteData = 0xBB;

        outFile.putShortLE((short)d.lengthDataBlock);
        outFile.putByte((byte)d.byteFlagData);

        int read;

        do {
            read = inFile.read(buffer, 0, 1024);
            outFile.write(buffer, 0, read);
        } while( read == 1024 );

        outFile.putByte((byte)d.checksumByteData);

        outFile.close();

        return dirname + '\\' + basename + ".tap";
    }

    public RAFSeekable loadTAP(String fileName) throws IOException, URISyntaxException {
        RAFSeekable inFile = new RAFSeekable(fileName, "r");

        if( ! getNextComponent(inFile) )
            return null;

        return inFile;
    }

    public boolean getNextComponent(RAFSeekable inFile) {
        try {
            h.lengthHeaderBlock = inFile.getShortLE();
            h.byteFlagHeader = inFile.getByte();
            h.type = inFile.getByte();
            h.filename = new byte[10];
            for ( int i = 0; i < 10; i++ ) {
                h.filename[i] = (byte) inFile.getByte();
            }
            h.lengthDataBlock = inFile.getShortLE();
            h.parameter1 = inFile.getShortLE();
            h.parameter2 = inFile.getShortLE();
            h.checksumByteHeader = inFile.getByte();

            Logger.info(Arrays.toString(h.filename));
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
