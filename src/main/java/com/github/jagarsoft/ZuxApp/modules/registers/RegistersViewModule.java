package com.github.jagarsoft.ZuxApp.modules.registers;

import com.github.jagarsoft.Z80;
import com.github.jagarsoft.ZuxApp.core.bus.UIEventHandler;
import com.github.jagarsoft.ZuxApp.infrastructure.module.BaseModule;
import com.github.jagarsoft.ZuxApp.modules.debugger.IZ80Cpu;
import com.github.jagarsoft.ZuxApp.modules.debugger.Z80State;
import com.github.jagarsoft.ZuxApp.modules.debugger.events.CpuStateUpdatedEvent;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.commands.AddJInternalFrameToDesktopPaneCommand;

import javax.swing.*;
import java.util.function.Consumer;

public class RegistersViewModule extends BaseModule {
    RegisterTableModel registerModel;

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public void configure() {
        eventBus.subscribe(CpuStateUpdatedEvent.class, (Consumer<CpuStateUpdatedEvent>) event -> {
        //eventBus.subscribe(CpuStateUpdatedEvent.class, (UIEventHandler<CpuStateUpdatedEvent>) event -> {
            IZ80Cpu cpu = event.getCpu();
            Z80State currentState = event.getState();
            for(Z80.RegTouched r : Z80.RegTouched.values()) {
                if( currentState.isTouched(r) ) {
                    //System.out.println(r.name() + ": cambio " + cpu.getReg(r.name()) );
                    registerModel.setRegister(r.name(), cpu.getReg(r.name()));
                }
            }
            registerModel.setRegister("PC", cpu.getPC());
        });
    }

    @Override
    public void initUI() {
        registerModel = new RegisterTableModel();
        JTable registerTable = new JTable(registerModel);

        JInternalFrame frame = new JInternalFrame("Registers", true, true, true, true);
        frame.setSize(160, 265);
        frame.add(new JScrollPane(registerTable));

        this.commandBus.execute(new AddJInternalFrameToDesktopPaneCommand(frame));
        frame.setVisible(true);
    }
}
