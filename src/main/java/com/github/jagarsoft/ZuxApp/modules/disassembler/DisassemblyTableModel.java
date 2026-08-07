package com.github.jagarsoft.ZuxApp.modules.disassembler;

import javax.swing.table.AbstractTableModel;

import com.github.jagarsoft.Instruction;
import com.github.jagarsoft.ZuxApp.modules.debugger.DebuggerModule;

import java.util.List;
import java.util.Map;

public class DisassemblyTableModel extends AbstractTableModel {
    private final InstructionList<Instruction> instructions = new InstructionList<>();
    private int currentPCIndex = 0;

    //public boolean isBankingOn = false;
    //public int bank = 0;

    @Override
    public int getRowCount() {
        return instructions.size();
    }

    @Override
    public int getColumnCount() { return 6; }

    @Override
    public Object getValueAt(int row, int col) {
        Instruction instr = instructions.get(row);
        return switch (col) {
            case 0 -> ""; // gap for BreakPoint
            case 1 -> ( !instr.isBankingOn() ? String.format("%04X", instr.getAddress()) :
                              String.format("%04X:%04X", instr.bank, instr.getAddress()));
            case 2 -> instr.getHexBytes();
            case 3 -> instr.getLabel();
            case 4 -> instr.getMnemonic();
            case 5 -> instr.getComment();
            default -> "";
        };
    }

    @Override
    public String getColumnName(int col) {
        return switch (col) {
            case 0 -> "BP";
            case 1 -> "Addr";
            case 2 -> "OpCodes";
            case 3 -> "Label";
            case 4 -> "Mnemonic";
            case 5 -> "Comment";
            default -> "";
        };
    }

    public void add(int pc, Instruction instruction) {
        instructions.add(pc, instruction);
    }

    public void removeFrom(int pc) {
        instructions.remove(pc);
    }

    public Instruction getInstruction(int row) {
        return instructions.get(row);
    }

    public Integer getCurrentPCRow() {
        return currentPCIndex;
    }

    public void setCurrentPC(int pc) {
        currentPCIndex = instructions.getByAddress(pc).getIndex();
    }

    public int getLastProcessed() { return instructions.getLastProcessed(); }

    // TODO Refactor pending!
    // All breakpoints concern must be BreakPointManager's responsibility
    // shared with DisassemblyTableModel and DebuggerModule.
    // BreakPointManager is a Model for Breakpoints
    public boolean isBreakpoint(int addr) {
        Instruction instr = instructions.getByAddress(addr);
        if( instr == null ) // TODO no debería de ocurrir
            return false;
        return instr.hasBreakPoint; // TODO bring flag to Manager
    }

    public void toggleBreakpoint(int addr) {
        instructions.getByAddress(addr).hasBreakPoint = !instructions.getByAddress(addr).hasBreakPoint;
    }

    public void clear() {
        instructions.clear();
    }

    public Instruction getInstructionByPC(int pc) {
        return instructions.getByAddress(pc);
    }

    public List<Map.Entry<Integer, Instruction>> search(String query) {
        return instructions.search(query);
    }

    public void dump() {
        for (Instruction i : instructions.getInstructions())
            System.out.println(i.index + ": " + i);
    }
}

