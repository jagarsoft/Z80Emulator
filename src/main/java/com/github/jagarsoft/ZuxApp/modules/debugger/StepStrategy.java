package com.github.jagarsoft.ZuxApp.modules.debugger;

import com.github.jagarsoft.ZuxApp.core.bus.EventBus;
import com.github.jagarsoft.ZuxApp.modules.debugger.events.CpuStateUpdatedEvent;

public class StepStrategy extends BaseStrategy implements NotificationStrategy {
    public void onInstruction(IZ80Cpu cpu, EventBus eventBus) {
        checkStateChanged(cpu, eventBus);
    }

    public void onPause(IZ80Cpu cpu, EventBus eventBus) {
        // void
    }
}
