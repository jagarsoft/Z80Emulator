package com.github.jagarsoft;

import java.util.concurrent.locks.LockSupport;

/**
 * Optimized timing system for Z80 emulator using frame-based execution.
 * Implements LockSupport.parkNanos with busy-wait fallback for precise timing.
 * Avoids allocations in the hot loop for better performance.
 */
public class Z80FrameTiming {
    
    // ZX Spectrum timing constants
    private static final int TSTATES_PER_FRAME = 69888;
    private static final long FRAME_DURATION_NS = 20_000_000L; // 50Hz = 20ms per frame
    private static final long CPU_FREQ_HZ = 3_500_000L;
    private static final long NANOS_PER_SECOND = 1_000_000_000L;
    
    // Thresholds for different timing strategies
    private static final long PARK_THRESHOLD_NS = 2_000_000L; // 2ms
    private static final long BUSY_THRESHOLD_NS = 50_000L;     // 50μs
    
    // Pre-computed values to avoid divisions in hot loop
    private static final double NS_PER_TSTATE = (double) NANOS_PER_SECOND / CPU_FREQ_HZ;
    private static final int GARBAGECOLLECTOR = -1; //32000;

    // Timing state - reused to avoid allocations
    private long frameStartTime;
    private long currentTStates;
    protected long targetTStates;
    private long frameNumber;
    private int garbageCollector = GARBAGECOLLECTOR;

    public Z80FrameTiming() {
        reset();
    }
    
    public void reset() {
        frameStartTime = System.nanoTime();
        currentTStates = 0;
        targetTStates = TSTATES_PER_FRAME;
        frameNumber = 0;
    }
    
    /**
     * Execute instructions until frame completion, then sync timing.
     * Returns true if frame is complete and interrupt should be triggered.
     */
    public boolean executeFrame(Z80 cpu) {
        //System.out.println("currentTStates="+currentTStates);
        if (currentTStates >= targetTStates) {
            // Frame complete - sync timing and prepare next frame
            syncFrameTiming();
            currentTStates = 0;
            targetTStates = TSTATES_PER_FRAME;
            frameNumber++;
            return true; // Trigger interrupt
        }
        return false;
    }
    
    /**
     * Add T-states for executed instruction.
     */
    public void addTStates(long tStates) {
        currentTStates += tStates;
    }
    
    /**
     * Get current T-states within frame.
     */
    public long getCurrentTStates() {
        return currentTStates;
    }
    
    /**
     * Check if frame is complete.
     */
    public boolean isFrameComplete() {
        return currentTStates >= targetTStates;
    }
    
    /**
     * Precise timing synchronization using LockSupport.parkNanos + busy-wait.
     * Uses different strategies based on remaining time to optimize performance.
     */
    private void syncFrameTiming() {
        final long currentTime = System.nanoTime();
        final long elapsedTime = currentTime - frameStartTime;
        final long remainingTime = FRAME_DURATION_NS - elapsedTime;
        
        if (remainingTime > 0) {
            waitPrecise(remainingTime);
        }
        
        // Start next frame
        frameStartTime = System.nanoTime();
    }
    
    /**
     * Optimized waiting strategy:
     * - Long waits: LockSupport.parkNanos (most of the wait)
     * - Medium waits: Thread.yield() loop  
     * - Short waits: busy-wait loop for precision
     */
    private void waitPrecise(long remainingNanos) {
        final long startWait = System.nanoTime();
        
        // Phase 1: Use parkNanos for bulk of the wait (if > 2ms)
        if (remainingNanos > PARK_THRESHOLD_NS) {
            final long parkTime = remainingNanos - PARK_THRESHOLD_NS;
            LockSupport.parkNanos(parkTime);
        }
        
        // Phase 2: yield() loop for medium precision (if > 50μs remaining)
        long now = System.nanoTime();
        long remaining = remainingNanos - (now - startWait);
        
        while (remaining > BUSY_THRESHOLD_NS) {
            if(garbageCollector-- == 0) {
                Runtime.getRuntime().gc();
                garbageCollector = GARBAGECOLLECTOR;
            }
            Thread.yield();
            now = System.nanoTime();
            remaining = remainingNanos - (now - startWait);
        }
        
        // Phase 3: busy-wait for final precision
        final long endTime = startWait + remainingNanos;
        while (System.nanoTime() < endTime) {
            // Tight loop for maximum precision
        }
    }
    
    /**
     * Calculate expected T-states based on elapsed time.
     * Useful for catching up if emulation is running slow.
     */
    public long getExpectedTStates() {
        final long elapsedTime = System.nanoTime() - frameStartTime;
        return Math.min((long)(elapsedTime / NS_PER_TSTATE), TSTATES_PER_FRAME);
    }
    
    /**
     * Check if emulation is running behind schedule.
     */
    public boolean isRunningBehind() {
        return currentTStates < getExpectedTStates();
    }
    
    /**
     * Get current frame number.
     */
    public long getFrameNumber() {
        return frameNumber;
    }
    
    /**
     * Get progress within current frame (0.0 to 1.0).
     */
    public double getFrameProgress() {
        return (double) currentTStates / TSTATES_PER_FRAME;
    }
}