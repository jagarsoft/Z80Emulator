package com.github.jagarsoft.ZuxApp.modules.memory;

import com.github.jagarsoft.ZuxApp.core.bus.UIEventHandler;
import com.github.jagarsoft.ZuxApp.core.module.Module;
import com.github.jagarsoft.ZuxApp.infrastructure.module.BaseModule;
import com.github.jagarsoft.ZuxApp.modules.debugger.events.ImageLoadedEvent;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.commands.AddJInternalFrameToDesktopPaneCommand;

import javax.swing.*;

public class MemoryViewModule extends BaseModule implements Module {
    MemoryTableModel memoryModel;
    JTable memoryTable;

    @Override
    public void configure() {
        eventBus.subscribe(ImageLoadedEvent.class, (UIEventHandler<ImageLoadedEvent>) (e) -> {
            memoryModel.dump(e.getComputer(), 0, (int)e.getLength());
        });

        /*eventBus.subscribe(CpuStateUpdatedEvent.class, (Consumer<CpuStateUpdatedEvent>) ev -> {
            int pc = ev.getCpu().getPC();
            if( ev.getCpu().isHalted() )
                disassemblyModel.setCurrentPC(pc-1);
            else
                disassemblyModel.setCurrentPC(pc);

            int index = disassemblyModel.getCurrentPCRow();
            //disassemblyModel.fireTableRowsUpdated(index, index);
            disassemblyModel.fireTableDataChanged();
        });*/
    }

    @Override
    public void initUI() {
        memoryModel = new MemoryTableModel();
        memoryTable = new JTable(memoryModel);

        JInternalFrame frame = new JInternalFrame("Volcado de Memoria", true, true, true, true);
        JScrollPane memScroll = new JScrollPane(memoryTable);
        frame.add(memScroll);
        frame.pack();
        frame.setSize(500, 300);
        frame.setLocation(10, 320);

        this.commandBus.execute(new AddJInternalFrameToDesktopPaneCommand(frame));
        frame.setVisible(true);
    }
}
