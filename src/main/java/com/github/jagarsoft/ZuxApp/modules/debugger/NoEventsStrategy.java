package com.github.jagarsoft.ZuxApp.modules.debugger;

import com.github.jagarsoft.ZuxApp.core.bus.EventBus;

public class NoEventsStrategy implements NotificationStrategy {
    public void onInstruction(IZ80Cpu cpu, EventBus eventBus) {
        // Silence
    }
    public void onPause(IZ80Cpu cpu, EventBus eventBus) {
        // Silence
    }
}
