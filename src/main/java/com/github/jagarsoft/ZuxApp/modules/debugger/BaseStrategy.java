package com.github.jagarsoft.ZuxApp.modules.debugger;

import com.github.jagarsoft.ZuxApp.core.bus.EventBus;
import com.github.jagarsoft.ZuxApp.modules.debugger.events.CpuStateUpdatedEvent;

public class BaseStrategy {
    protected Z80State lastState = null;

    protected void checkStateChanged(IZ80Cpu cpu, EventBus eventBus) {
        Z80State currentState = cpu.snapshot();
        if( lastState == null || currentState.hasChanged(lastState) || cpu.isHalted() ) {
            lastState = currentState;
            eventBus.publish(new CpuStateUpdatedEvent(cpu, currentState));
        }
    }
}
