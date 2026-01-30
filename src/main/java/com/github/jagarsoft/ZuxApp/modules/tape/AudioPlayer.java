package com.github.jagarsoft.ZuxApp.modules.tape;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioPlayer implements Runnable {
    private final AudioEngine engine;
    private SourceDataLine line = null;
    private final float[] tmp = new float[1024];
    private volatile boolean running = true;

    public AudioPlayer(AudioEngine engine, int sampleRate) /*throws Exception*/ {
        this.engine = engine;
        AudioFormat fmt = new AudioFormat(sampleRate, 8, 1, true, false);
        try {
            line = AudioSystem.getSourceDataLine(fmt);
            line.open(fmt, 4096);
            line.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        byte[] out = new byte[1024];
        if( line != null ) {
            while (running) {
                int n = engine.read(tmp, tmp.length);
                for (int i = 0; i < n; i++) {
                    float f = tmp[i];
                    out[i] = (byte) (f * 127f);
                }
                line.write(out, 0, n);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    //throw new RuntimeException(e);
                }
            }
            line.stop();
            line.close();
        }
    }
}

