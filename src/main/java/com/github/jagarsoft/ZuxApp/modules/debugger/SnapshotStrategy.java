package com.github.jagarsoft.ZuxApp.modules.debugger;

import com.github.jagarsoft.ZuxApp.core.bus.EventBus;
import com.github.jagarsoft.ZuxApp.modules.debugger.events.CpuStateUpdatedEvent;

public class SnapshotStrategy implements NotificationStrategy {
    Z80State oldState = null;

    public void onInstruction(IZ80Cpu cpu, EventBus eventBus) {
        Z80State currentState = cpu.snapshot();
        if( oldState == null || currentState.hasChanged(oldState) ) {
            oldState = currentState;
            eventBus.publish(new CpuStateUpdatedEvent(cpu, currentState));
        }
    }
    public void onPause(IZ80Cpu cpu, EventBus eventBus) {
        Z80State currentState = cpu.snapshot();
        if( oldState == null || currentState.hasChanged(oldState) ) {
            oldState = currentState;
            eventBus.publish(new CpuStateUpdatedEvent(cpu, currentState));
        }
    }
}
