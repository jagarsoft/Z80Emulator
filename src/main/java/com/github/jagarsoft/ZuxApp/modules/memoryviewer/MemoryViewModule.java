package com.github.jagarsoft.ZuxApp.modules.memoryviewer;

import com.github.jagarsoft.ZuxApp.core.bus.UIEventHandler;
import com.github.jagarsoft.ZuxApp.core.module.Module;
import com.github.jagarsoft.ZuxApp.infrastructure.module.BaseModule;
import com.github.jagarsoft.ZuxApp.modules.debugger.events.BinaryImageLoadedEvent;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.commands.AddJInternalFrameToDesktopPaneCommand;

import javax.swing.*;
import java.awt.*;

public class MemoryViewModule extends BaseModule implements Module {
    MemoryTableModel memoryModel;
    JTable memoryTable;

    @Override
    public void configure() {
        eventBus.subscribe(BinaryImageLoadedEvent.class, (UIEventHandler<BinaryImageLoadedEvent>) (e) -> {
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
        memoryTable.setFont(new Font("Monospaced", Font.PLAIN, 10));

        JInternalFrame frame = new JInternalFrame("Memory Dump", true, true, true, true);
        JScrollPane memScroll = new JScrollPane(memoryTable);
        frame.add(memScroll);
        frame.pack();
        frame.setSize(500, 300);
        frame.setLocation(10, 320);

        this.commandBus.execute(new AddJInternalFrameToDesktopPaneCommand(frame));
        frame.setVisible(true);
    }
}
