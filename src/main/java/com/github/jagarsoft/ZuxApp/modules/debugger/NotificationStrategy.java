package com.github.jagarsoft.ZuxApp.modules.debugger;

import com.github.jagarsoft.ZuxApp.core.bus.EventBus;

public interface NotificationStrategy {
    void onInstruction(IZ80Cpu cpu, EventBus eventBus);
    void onPause(IZ80Cpu cpu, EventBus eventBus);
}
