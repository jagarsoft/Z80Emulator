package com.github.jagarsoft.ZuxApp.infrastructure.module;

import com.github.jagarsoft.ZuxApp.core.bus.CommandBus;
import com.github.jagarsoft.ZuxApp.core.bus.EventBus;
import com.github.jagarsoft.ZuxApp.core.events.ModuleLoadedEvent;
import com.github.jagarsoft.ZuxApp.core.module.Module;

import javax.swing.*;

public abstract class BaseModule implements Module {

    protected CommandBus commandBus;
    protected EventBus eventBus;
    //protected JDesktopPane desktopPane;

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public final void register(CommandBus commandBus, EventBus eventBus/*, JDesktopPane desktopPane*/) {
        this.commandBus = commandBus;
        this.eventBus = eventBus;
        //this.desktopPane = desktopPane;

        configure();

        // Emitir evento global
        this.eventBus.publish(new ModuleLoadedEvent(this.getName()));
    }

    public abstract void initUI();

    // TODO: Move into Command in MainModule
    /*protected void openInternalFrame(JInternalFrame frame) {
        desktopPane.add(frame);
        frame.setVisible(true);
    }*/
}
