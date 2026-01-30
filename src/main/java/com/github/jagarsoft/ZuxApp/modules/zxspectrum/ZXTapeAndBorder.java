package com.github.jagarsoft.ZuxApp.modules.zxspectrum;

import com.github.jagarsoft.IODevice;
import com.github.jagarsoft.ZuxApp.modules.tape.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class ZXTapeAndBorder implements IODevice {
    private static final byte MIC = 8;
    private final ZXSpectrumScreen screen;
    String nombreArchivoOUT = "archivoOUT.bin";
    String nombreArchivoIN = "archivoIN.bin";
    FileOutputStream fos, fis;
    long  lastTick;
    int lastTstate;
    byte data = 0;

    AudioEngine2 engine;
    AudioPlayer2 player;
    
    private Timer beepTimer;
    private TimerTask beepStopTask;
    private static final long BEEP_TIMEOUT_MS = 1000;

    public ZXTapeAndBorder(ZXSpectrumScreen screen) {
        this.screen = screen;
        engine = new AudioEngine2(44100, 44100*10);
        player = new AudioPlayer2(engine, 44100);

        new Thread(player, "AudioPlayer").start();
        
        beepTimer = new Timer("BeepTimer", true);

        lastTick = System.nanoTime();

        /*try {
            fos = new FileOutputStream(nombreArchivoOUT);
            fis = new FileOutputStream(nombreArchivoIN);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void write(int addr, byte data) {
        throw new UnsupportedOperationException("Old write.");
    }

    @Override
    public void write(int addr, byte data, int thisTstate) {
        /*try {
            long thisTick = System.nanoTime();
            System.out.println("OUT: " + data +", "+ thisTick + ", "+ (thisTick-lastTick) + ", TState " + thisTstate + ", " + (thisTstate-lastTstate));
            fos.write(data);
            fos.flush();*/
        System.out.println("OUT("+ Integer.toHexString(addr) + ")," + Integer.toHexString(data) + ", " + thisTstate);

            //if( lastTstate != 0)
                engine.onPulse((data & MIC) == 0, /*thisTstate*/6628 /*2168*/);
        /*int deltaTstates = (lastTstate == 0) ? 0 : thisTstate - lastTstate;
        engine.onPulse((data & MIC) == 0, deltaTstates);
        lastTstate = thisTstate;*/

            scheduleBeepStop();

            //lastTick = thisTick;
            //lastTstate = thisTstate;
        /*} catch (IOException e) {
            e.printStackTrace();
        }*/

        screen.setBorderColor(data);
    }

    @Override
    public byte read(int addr) {
        if( data == 2)
            data = 0x0d;
        else
            data = 2;
        System.out.println("IN("+ Integer.toHexString(addr) + ")," + Integer.toHexString(data));// + ", " + thisTstate);
        return data;
    }
    
    private void scheduleBeepStop() {
        if (beepStopTask != null) {
            beepStopTask.cancel();
        }
        
        beepStopTask = new TimerTask() {
            @Override
            public void run() {
                engine.stopAudio();
                player.flush();
                System.out.println("BEEP stopped - timeout reached");
            }
        };
        
        beepTimer.schedule(beepStopTask, BEEP_TIMEOUT_MS);
    }
    
    public void dispose() {
        if (beepTimer != null) {
            beepTimer.cancel();
        }
        if (player != null) {
            player.stop();
        }
    }
}
