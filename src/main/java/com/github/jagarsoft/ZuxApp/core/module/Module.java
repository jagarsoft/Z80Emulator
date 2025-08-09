package com.github.jagarsoft.ZuxApp.core.module;

import com.github.jagarsoft.ZuxApp.core.bus.CommandBus;
import com.github.jagarsoft.ZuxApp.core.bus.EventBus;

public interface Module {
    String getName();
    void register(CommandBus commandBus, EventBus eventBus/*, JDesktopPane desktopPane*/);
    void configure();
    void initUI();
}
