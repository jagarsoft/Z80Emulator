package com.github.jagarsoft.ZuxApp.modules.debugger;

import com.github.jagarsoft.ZuxApp.core.bus.EventBus;

public class SnapshotStrategy extends BaseStrategy implements NotificationStrategy {
    public void onInstruction(IZ80Cpu cpu, EventBus eventBus) {
        //checkStateChanged(cpu, eventBus);
        lastState = cpu.snapshot();
    }

    public void onPause(IZ80Cpu cpu, EventBus eventBus) {
        checkStateChanged(cpu, eventBus);
    }
}
