package com.github.jagarsoft.ZuxApp.core.module;

import com.github.jagarsoft.ZuxApp.core.bus.CommandBus;
import com.github.jagarsoft.ZuxApp.core.bus.EventBus;

public interface Module {

    String getName();

    /**
     * 1st step: Called from BootstrapDebug.initialize(). Then it calls to configure()
     */
    void register(CommandBus commandBus, EventBus eventBus);

    /**
     * Callback from register()
     * Here subscribe to all events and all register command handlers this module needs
     */
    void configure();

    /**
     * 2nd step: Called from BootstrapDebug.initialize() after be registered and configured
     */
    void initUI();
}
