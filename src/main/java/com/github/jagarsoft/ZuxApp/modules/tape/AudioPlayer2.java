package com.github.jagarsoft.ZuxApp.modules.tape;

import javax.sound.sampled.*;

public class AudioPlayer2 implements Runnable {
    private final AudioEngine2 engine;
    private SourceDataLine line = null;
    private volatile boolean running = true;

    public AudioPlayer2(AudioEngine2 engine, int sampleRate) {
        this.engine = engine;
        AudioFormat fmt = new AudioFormat(sampleRate, 8, 1, true, false);
        try {
            line = AudioSystem.getSourceDataLine(fmt);
            line.open(fmt, 4096);
            line.start();
        } catch (LineUnavailableException e) {
            //throw new RuntimeException(e);
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
    }

    public void flush() {
        if (line != null) {
            line.flush();
        }
    }

    @Override
    public void run() {
        float[] fbuf = new float[1024];
        byte[] bbuf = new byte[1024];

        while (running) {
            int n = engine.read(fbuf, fbuf.length);

            if (n > 0) {
                for (int i = 0; i < n; i++) {
                    bbuf[i] = (byte) (fbuf[i] * 127f);
                }
                line.write(bbuf, 0, n);
            } else {
                try {
                	Thread.sleep(1);
                } catch (InterruptedException ignored) {}
            }
        }

        line.drain();
        line.stop();
        line.close();
    }
}

