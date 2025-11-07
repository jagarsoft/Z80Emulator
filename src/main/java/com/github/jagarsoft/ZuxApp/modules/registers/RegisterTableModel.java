package com.github.jagarsoft.ZuxApp.modules.registers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.table.AbstractTableModel;

//import static java.lang.StringUTF16.indexOf;

public class RegisterTableModel extends AbstractTableModel {
    //private final List<String> names = Arrays.asList("AF", "BC", "DE", "HL", "IX", "IY", "SP", "PC");
    private final List<String> names = Arrays.asList("A", "B", "C", "D", "E", "H", "L", "I", "R", "IX", "IY", "SP", "F", "PC");
    private final Map<String, Integer> values = new HashMap<>();

    @Override
    public int getRowCount() {
        return names.size(); // as rows as registers there are in names
    }

    @Override
    public int getColumnCount() {
        return 2; // Name & Value
    }

    @Override
    public Object getValueAt(int row, int col) {
        String regName = names.get(row);
        int value = values.getOrDefault(regName, 0);

        if(col == 0) {
            return regName;
        } else if( row == names.indexOf("F") ) {
            return flags();
        } else if( row < names.indexOf("IX") ) {
                    return String.format("%02X", (byte)(value & 0x00FF));
                } else  {
                    return String.format("%04X", (short)(value & 0x0000FFFF));
                }
    }

    /*
    Bit	7	6	 5	4	 3	 2	1	0
   Flag	S	Z	F5	H	F3	P/V	N	C
     */

    private String flags() {
        int value = values.getOrDefault("F", 0);
        return ((value & 0x80) != 0 ? "S" : "-")
              +((value & 0x40) != 0 ? "Z" : "-")
              +((value & 0x20) != 0 ? "5" : "-")
              +((value & 0x10) != 0 ? "H" : "-")
              +((value & 0x08) != 0 ? "3" : "-")
              +((value & 0x04) != 0 ? "P" : "V")
              +((value & 0x02) != 0 ? "N" : "-")
              +((value & 0x01) != 0 ? "C" : "-");
    }

    @Override
    public String getColumnName(int col) {
        return col == 0 ? "Register" : "Value";
    }

    public void setRegister(String regName, int value) {
        values.put(regName, value);
        setValueAt(value, names.indexOf(regName), 1);
        //fireTableDataChanged();
        fireTableRowsUpdated(names.indexOf(regName), names.indexOf(regName));

        /*registerModel.fireTableRowsUpdated(index, index);
          registerModel.fireTableRowsUpdated();*/
    }
}
