package com.github.jagarsoft.ZuxApp.modules.disassembler;

import javax.swing.table.AbstractTableModel;

import com.github.jagarsoft.Instruction;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DisassemblyTableModel extends AbstractTableModel {
    private final InstructionList<Instruction> instructions = new InstructionList<>();
    private int currentPCIndex = 0;

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
            case 0 -> "";
            case 1 -> String.format("%04X", instr.getAddress());
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

    public Instruction getInstruction(int row) {
        return instructions.get(row);
    }

    public Integer getCurrentPCRow() {
        return currentPCIndex;
    }

    public void setCurrentPC(int pc) {
        currentPCIndex = instructions.getByAddress(pc).getIndex();
    }


    public boolean isBreakpoint(int addr) {
        return instructions.getByAddress(addr).hasBreakPoint;
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
}

