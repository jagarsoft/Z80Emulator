package com.github.jagarsoft.ZuxApp.modules.disassembler;

import com.github.jagarsoft.Computer;
import com.github.jagarsoft.Instruction;
import com.github.jagarsoft.Z80Disassembler;
import com.github.jagarsoft.ZuxApp.core.bus.UIEventHandler;
import com.github.jagarsoft.ZuxApp.infrastructure.module.BaseModule;
import com.github.jagarsoft.ZuxApp.modules.debugger.events.CpuStateUpdatedEvent;
import com.github.jagarsoft.ZuxApp.modules.debugger.events.ImageLoadedEvent;
import com.github.jagarsoft.ZuxApp.modules.disassembler.events.BreakpointToggledEvent;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.commands.AddJInternalFrameToDesktopPaneCommand;
import com.github.jagarsoft.ZuxApp.modules.memoryconfig.events.MemoryConfigChangedEvent;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Supplier;

public class DisassemblerModule extends BaseModule {
    static Z80Disassembler disassembler = new Z80Disassembler();
    DisassemblyTableModel disassemblyModel;
    JTable disassemblyTable;

    /*UIEventHandler<ImageLoadedEvent>  imageLoadedEventUIEventHandler = new  UIEventHandler<ImageLoadedEvent>() {

        @Override
        public void handle(ImageLoadedEvent event) {
            event.get
        }
    };*/

    @Override
    public void configure() {
        eventBus.subscribe(ImageLoadedEvent.class, (UIEventHandler<ImageLoadedEvent>) (e) -> {
            list(e.getComputer(), 0, e.getLength());
            disassemblyModel.fireTableDataChanged(); // TODO evitarlo si se usa fireTableRowsInserted abajo
        });
        eventBus.subscribe(MemoryConfigChangedEvent.class, (UIEventHandler<MemoryConfigChangedEvent>) (e) -> {
            // TODO no llega a actualizar la vista del Desensamblador
            /*GetComputerCommand computerCommand = new GetComputerCommand();
            commandBus.execute(computerCommand);
            Computer computer = computerCommand.getComputer();
            //list(computer, 0, computer.getMemorySize());
            list(computer, 0, e.getNumberPages() * e.getPageSize() * 1024);
            disassemblyModel.fireTableDataChanged();*/
        });

        //eventBus.subscribe(CpuStateUpdatedEvent.class, (Consumer<CpuStateUpdatedEvent>) ev -> {
        eventBus.subscribe(CpuStateUpdatedEvent.class, (UIEventHandler<CpuStateUpdatedEvent>) ev -> {
            int pc = ev.getCpu().getPC();
            if( ev.getCpu().isHalted() )
                disassemblyModel.setCurrentPC(pc-1);
            else
                disassemblyModel.setCurrentPC(pc);

            int index = disassemblyModel.getCurrentPCRow();
            if (index >= 0 && index < disassemblyTable.getRowCount()) {
                //Rectangle cellRect = disassemblyTable.getCellRect(index, 0, true);
                //disassemblyTable.scrollRectToVisible(cellRect);
                disassemblyTable.getSelectionModel().setSelectionInterval(index, index);
                disassemblyTable.scrollRectToVisible(disassemblyTable.getCellRect(index, 0, true));
            }

            //disassemblyModel.fireTableRowsUpdated(index, index);
            disassemblyModel.fireTableDataChanged();
        });
    }

    private void list(Computer comp, int org, long size) {
        //disassemblyModel.clear();
        disassembler.setComputer(comp);
        //disassembler.setSymbolTable(symbolTable);

        comp.reset();
        comp.setOrigin(org);
        int processed = 0;
        do {
            final int pc = disassembler.getPC();
            final Instruction instruction = disassembler.fetchInstruction();
            instruction.index = processed; // assoc this row to PC
            disassemblyModel.add(pc, instruction);
            processed++;
            // Update UI every 100 instructions
            if (processed % 100 == 0) {
                disassemblyModel.fireTableDataChanged();
                //disassemblyModel.fireTableRowsInserted(processed-100, processed-1);
            }
        } while( (disassembler.getPC() - org) < size);
    }

    @Override
    public void initUI() {
        disassemblyModel = new DisassemblyTableModel();
        disassemblyTable = new JTable(disassemblyModel);
        highlightCurrentRow(disassemblyTable, disassemblyModel::getCurrentPCRow);
        disassemblyTable.getColumnModel().getColumn(0).setCellRenderer(new BreakpointCellRenderer(disassemblyModel));
        disassemblyTable.getColumnModel().getColumn(0).setMaxWidth(30);

        disassemblyTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = disassemblyTable.rowAtPoint(e.getPoint());
                int col = disassemblyTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 0) { // primera columna
                    int addr = disassemblyModel.get(row).getAddress();
                    disassemblyModel.toggleBreakpoint(addr);
                    disassemblyModel.fireTableCellUpdated(row, col);
                    eventBus.publish(new BreakpointToggledEvent(addr));
                }
            }
        });

        JInternalFrame frame = new JInternalFrame("Disassembly", true, true, true, true);
        frame.add(new JScrollPane(disassemblyTable));

        frame.setSize(500, 300);
        frame.setLocation(10, 10);

        this.commandBus.execute(new AddJInternalFrameToDesktopPaneCommand(frame));
        frame.setVisible(true);
    }

    private static void highlightCurrentRow(JTable table, Supplier<Integer> pcRowSupplier) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                int pcRow = pcRowSupplier.get();
                if (row == pcRow) {
                    c.setBackground(Color.YELLOW);
                } else {
                    c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                }
                return c;
            }
        });
    }
}
