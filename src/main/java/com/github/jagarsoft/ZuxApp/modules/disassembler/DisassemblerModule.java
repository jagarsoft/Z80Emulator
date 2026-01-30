package com.github.jagarsoft.ZuxApp.modules.disassembler;

import com.github.jagarsoft.Computer;
import com.github.jagarsoft.Instruction;
import com.github.jagarsoft.Z80Disassembler;
import com.github.jagarsoft.ZuxApp.core.bus.UIEventHandler;
import com.github.jagarsoft.ZuxApp.infrastructure.module.BaseModule;
import com.github.jagarsoft.ZuxApp.modules.dataregion.DataRegion;
import com.github.jagarsoft.ZuxApp.modules.dataregion.events.DataBlockMapLoadedEvent;
import com.github.jagarsoft.ZuxApp.modules.debugger.events.CpuStateUpdatedEvent;
import com.github.jagarsoft.ZuxApp.modules.debugger.events.BinaryImageLoadedEvent;
import com.github.jagarsoft.ZuxApp.modules.disassembler.events.BreakpointToggledEvent;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.commands.AddJInternalFrameToDesktopPaneCommand;
import com.github.jagarsoft.ZuxApp.modules.memoryconfig.events.MemoryConfigChangedEvent;
import com.github.jagarsoft.ZuxApp.modules.disassembler.events.StepEvent;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DisassemblerModule extends BaseModule {
    DisassemblyTableModel disassemblyModel;
    JTable disassemblyTable;
    //SymbolTable ...
    DataRegion dataRegion;
    // Searching
    private JTextField searchField;
    private List<Map.Entry<Integer, Instruction>> searchResults;
    private int currentSearchIndex = -1;

    @Override
    public void configure() {
        eventBus.subscribe(BinaryImageLoadedEvent.class, (UIEventHandler<BinaryImageLoadedEvent>) (e) -> {
            list(e.getComputer(), e.getOrigin(), e.getLength());
            //disassemblyModel.fireTableDataChanged(); // TODO evitarlo si se usa fireTableRowsInserted abajo
        });

        eventBus.subscribe(DataBlockMapLoadedEvent.class, (Consumer<DataBlockMapLoadedEvent>) e->{
            dataRegion = e.getDataRegion();
        });

        /*eventBus.subscribe(MemoryConfigChangedEvent.class, (UIEventHandler<MemoryConfigChangedEvent>) (e) -> {
            // TODO no llega a actualizar la vista del Desensamblador
            GetComputerCommand computerCommand = new GetComputerCommand();
            commandBus.execute(computerCommand);
            Computer computer = computerCommand.getComputer();
            //list(computer, 0, computer.getMemorySize());
            list(computer, 0, e.getNumberPages() * e.getPageSize() * 1024);
            disassemblyModel.fireTableDataChanged();
        });*/

        //eventBus.subscribe(CpuStateUpdatedEvent.class, (Consumer<CpuStateUpdatedEvent>) ev -> {
        eventBus.subscribe(CpuStateUpdatedEvent.class, (UIEventHandler<CpuStateUpdatedEvent>) ev -> {
            int pc = ev.getCpu().getPC();
            /*if( ev.getCpu().isHalted() )
                disassemblyModel.setCurrentPC(pc-1);
            else*/
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

        // Log every single instruction stepped
        /* eventBus.subscribe(StepEvent.class, (Consumer<StepEvent>) (ev) -> {
            Instruction instruction = disassemblyModel.getInstructionByPC(ev.getPC());
            System.out.println(instruction.toString());
        });*/
    }

    private void list(Computer comp, int org, long size) {
        Z80Disassembler disassembler = new Z80Disassembler();
        //disassemblyModel.clear();
        disassembler.setComputer(comp);
        //disassembler.setSymbolTable(symbolTable);

        disassembler.setDataBlock(dataRegion);

        //comp.reset();
        //int oldPC = comp.getCPU().getPC();
        //comp.setOrigin(org);
        disassembler.setOrigin(org);
        int processed = 0;
        int lastProcessed = processed;
        if( disassemblyModel.getInstructionByPC(org) != null )
            processed = disassemblyModel.getInstructionByPC(org).index;
        do {
            final int pc = disassembler.getPC();
            final Instruction instruction = disassembler.fetchInstruction();
            instruction.index = processed; // assoc this row to PC
            disassemblyModel.add(pc, instruction);
            processed++;
            // Update UI every 100 instructions
            if (processed % 100 == 0) {
                //disassemblyModel.fireTableDataChanged();
                disassemblyModel.fireTableRowsInserted(processed-100, processed-1);
                lastProcessed = processed;
            }
        } while( (disassembler.getPC() - org) < size);
        disassemblyModel.fireTableRowsInserted(lastProcessed, processed);
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
                    int addr = disassemblyModel.getInstruction(row).getAddress();
                    disassemblyModel.toggleBreakpoint(addr);
                    disassemblyModel.fireTableCellUpdated(row, col);
                    eventBus.publish(new BreakpointToggledEvent(addr));
                }
            }
        });

        // Campo de búsqueda
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
searchField.setName("symbolSearchField");
        searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        JButton prevButton = new JButton("Prev");
        JButton nextButton = new JButton("Next");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);

        searchPanel.add(buttonPanel, BorderLayout.EAST);

        JInternalFrame frame = new JInternalFrame("Disassembly", true, true, true, true);
        frame.add(searchPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(disassemblyTable));

        //
        // Eventos
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateSearch(); }
            @Override public void removeUpdate(DocumentEvent e) { updateSearch(); }
            @Override public void changedUpdate(DocumentEvent e) { updateSearch(); }
        });

        prevButton.addActionListener(e -> showPreviousMatch());
        nextButton.addActionListener(e -> showNextMatch());
        //

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
                int pcRow = pcRowSupplier.get(); // Call to disassemblyModel::getCurrentPCRow
                if (row == pcRow) {
                    c.setBackground(Color.YELLOW);
                } else {
                    c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                }
                return c;
            }
        });
    }

    private void updateSearch() {
        searchResults = disassemblyModel.search(searchField.getText());
        currentSearchIndex = searchResults.isEmpty() ? -1 : 0;
        highlightCurrentMatch();
    }

    private void showNextMatch() {
        if (searchResults == null || searchResults.isEmpty()) return;
        currentSearchIndex = (currentSearchIndex + 1) % searchResults.size();
        highlightCurrentMatch();
    }

    private void showPreviousMatch() {
        if (searchResults == null || searchResults.isEmpty()) return;
        currentSearchIndex = (currentSearchIndex - 1 + searchResults.size()) % searchResults.size();
        highlightCurrentMatch();
    }

    private void highlightCurrentMatch() {
        if (currentSearchIndex < 0 || searchResults == null || searchResults.isEmpty())
            return;

        int address = searchResults.get(currentSearchIndex).getKey();
        Instruction instruction = disassemblyModel.getInstructionByPC(address);

        if( instruction != null ) {
            int i = instruction.getIndex();
            disassemblyTable.setRowSelectionInterval(i, i);
            disassemblyTable.scrollRectToVisible(disassemblyTable.getCellRect(i, 0, true));
        }
    }
}
