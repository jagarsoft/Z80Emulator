package com.github.jagarsoft.ZuxApp.modules.console;

import com.github.jagarsoft.Z80;
import com.github.jagarsoft.ZuxApp.infrastructure.module.BaseModule;
import com.github.jagarsoft.ZuxApp.modules.debugger.IZ80Cpu;
import com.github.jagarsoft.ZuxApp.modules.debugger.Z80State;
import com.github.jagarsoft.ZuxApp.modules.debugger.events.CpuStateUpdatedEvent;
import com.github.jagarsoft.ZuxApp.modules.debugger.events.ExecutionPausedEvent;

import java.util.function.Consumer;

public class ConsoleModule extends BaseModule {
    @Override
    public String getName() {
        return "Console";
    }

    @Override
    public void configure() {
        eventBus.subscribe(CpuStateUpdatedEvent.class, (Consumer<CpuStateUpdatedEvent>) event -> {
            IZ80Cpu cpu = event.getCpu();
            Z80State currentState = event.getState();
            for(Z80.RegTouched r : Z80.RegTouched.values()) {
                if( currentState.isTouched(r) ) {
                    System.out.println(r.name() + ": cambio " + cpu.getReg(r.name()) );
                }
            }
        });
    }

    @Override
    public void initUI() {

    }
}
