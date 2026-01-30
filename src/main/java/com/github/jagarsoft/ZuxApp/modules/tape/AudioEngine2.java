package com.github.jagarsoft.ZuxApp.modules.tape;

public class AudioEngine2 {
    private final float sampleRate;
    private final float sampleDuration;
    private final float tToSec;

    private final float[] ring;
    private final int size;

    private int writeIdx = 0;
    private int readIdx = 0;

    private float lastLevel = -1f;
    private float accum = 0f;

    public AudioEngine2(int sampleRate, int ringSize) {
        this.sampleRate = sampleRate;
        this.sampleDuration = 1f / sampleRate;
        this.tToSec = 1f / 3_500_000f;
        this.size = ringSize;
        this.ring = new float[ringSize];
    }

    /* ===== PRODUCTOR ===== */

    public synchronized void onPulse(boolean high, int deltaTstates) {
        advanceTime(deltaTstates);
        lastLevel = high ? 1f : -1f;
    }

    public synchronized void advanceSilence(int deltaTstates) {
        advanceTime(deltaTstates);
    }

    private void advanceTime(int deltaTstates) {
        float dt = deltaTstates * tToSec;
        accum += dt;

        while (accum >= sampleDuration) {
            ring[writeIdx] = lastLevel;
            writeIdx = (writeIdx + 1) % size;

            // si alcanzamos al lector, descartamos la muestra más antigua
            if (writeIdx == readIdx) {
                readIdx = (readIdx + 1) % size;
            }
            accum -= sampleDuration;
        }
    }

    /* ===== CONSUMIDOR ===== */

    public synchronized int read(float[] dst, int max) {
        int count = 0;
        while (readIdx != writeIdx && count < max) {
            dst[count++] = ring[readIdx];
            readIdx = (readIdx + 1) % size;
        }
        return count;
    }

    public synchronized void stopAudio() {
        lastLevel = 0f;
        accum = 0f;
        // Llenar el buffer con silencio
        while (readIdx != writeIdx) {
            ring[readIdx] = 0f;
            readIdx = (readIdx + 1) % size;
        }
    }
}

