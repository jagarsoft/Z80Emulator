package com.github.jagarsoft.ZuxApp.modules.tape;

public class AudioEngine {
    private final float sampleRate;
    private final float sampleDuration;
    private final float tToSec;

    private final float[] ring;
    private int writeIdx = 0;

    private float lastLevel = -1f;
    private float accum = 0f;

    public AudioEngine(int sampleRate, int ringSize) {
        this.sampleRate = sampleRate;
        this.sampleDuration = 1f / sampleRate;
        this.tToSec = 1f / 3_500_000f; // 3.5 MHz
        this.ring = new float[ringSize];
    }

    // Llamado por cada OUT que altere el bit del altavoz
    public void onPulse(boolean high, int deltaTstates) {
        float dt = deltaTstates * tToSec;
        accum += dt;
        while (accum >= sampleDuration) {
            ring[writeIdx] = lastLevel;
            writeIdx = (writeIdx + 1) % ring.length;
            accum -= sampleDuration;
        }
        lastLevel = high ? 1f : -1f;
    }

    // Si pasa tiempo sin OUT, añade tramo continuo
    public void advanceSilence(int deltaTstates) {
        float dt = deltaTstates * tToSec;
        accum += dt;
        while (accum >= sampleDuration) {
            ring[writeIdx] = lastLevel;
            writeIdx = (writeIdx + 1) % ring.length;
            accum -= sampleDuration;
        }
    }

    /** Lee muestras crudas para consumo */
    public int read(float[] dst, int max) {
        int available = (writeIdx >= 0 ? writeIdx : 0); // entrega cíclica simplificada
        int take = Math.min(available, max);
        for (int i = 0; i < take; i++) {
            dst[i] = ring[i];
        }
        return take;
    }
}

