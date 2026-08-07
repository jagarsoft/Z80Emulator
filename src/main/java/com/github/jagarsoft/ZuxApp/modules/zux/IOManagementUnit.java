package com.github.jagarsoft.ZuxApp.modules.zux;

import com.github.jagarsoft.Computer;
import com.github.jagarsoft.IODevice;
import com.github.jagarsoft.Memory;
import com.github.jagarsoft.ZuxApp.core.bus.EventBus;
import com.github.jagarsoft.ZuxApp.modules.kernel.events.SetKernelProcTableEvent;

import static java.lang.String.format;

public class IOManagementUnit implements IODevice {
    public final static short SET_KERNEL_PROC_TABLE_CMD = 0x0060;
    public final static short SET_KERNEL_PROC_TABLE_DAT = 0x0061;
    public final static short SET_TEXT_BANK_CMD = 0x007B; // 0xC07B
    public final static short SET_TEXT_BANK_DAT = 0x007C; // 0xC07C
    public final static short SET_BOTH_BANKS_CMD = 0x00BB; // 0xC0BB
    public final static short SET_BOTH_BANKS_DAT = 0x00BC; // 0xC0BC
    public final static short SET_DATA_BANK_INM_CMD = 0x00DB; // 0xC0DB
    public final static short SET_DATA_BANK_INM_DAT = 0x00DC; // 0xC0DC
    public final static short GET_TOT_MEM_CMD = 0x00EC; // get_tot_mem() in getutil.asm
    public final static short GET_TOT_MEM_DAT = 0x00ED; // get_tot_mem() in getutil.asm
    public final static short PHYS_COPY_CMD = 0x00EE; // 0xC0CD
    public final static short PHYS_COPY_DAT = 0x00EF; // 0xC0D7

    public final static short VDISK_CMD = 0x00A0; // 0xC0A0
    public final static short VDISK_DAT = 0x00A1; // 0xC0A1

    public final static short PEEK_BYTE_ON_BANK_CMD = 0x00C0; // 0xC0C0
    public final static short PEEK_BYTE_ON_BANK_DAT = 0x00C1; // 0xC0C1
    public final static short POKE_BYTE_ON_BANK_CMD = 0x00C2; // 0xC0C2
    public final static short POKE_BYTE_ON_BANK_DAT = 0x00C3; // 0xC0C3

    private Computer zuxComputer;
    private int data_cnt;
    private byte[] data = new byte[12];
    private int get_tot_mem;
    private EventBus eventBus;
    //private Computer computer;

    IOManagementUnit(Computer zuxComputer) {
        this.zuxComputer = zuxComputer;
        /* TODO
        el constructor implementa el Get_Tot_Mem_Command
         */
    }


    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void write(int addr, byte data) {
        switch (addr) {
            case GET_TOT_MEM_CMD:
                /*
                Executar el Get_Tot_Mem_Command y dejar el resultado en get_tot_mem data[1] & data[0]
                 */
                break;
            case PHYS_COPY_CMD:
                data_cnt = 12; // 3 words of 32 bits
                System.out.println("[IOManagementUnit] PHYS_COPY_CMD: waiting for 12 bytes");
                break;
            case PHYS_COPY_DAT:
                --data_cnt;
                this.data[data_cnt] = data;
                System.out.println(format("[IOManagementUnit] PHYS_COPY_DAT byte %d: 0x%02X (data_cnt now=%d)", 12-data_cnt, data & 0xFF, data_cnt));
                if(data_cnt == 0) {
                    System.out.println("[IOManagementUnit] PHYS_COPY: all 12 bytes received, calling do_phys_copy()");
                    do_phys_copy();
                    System.out.println("[IOManagementUnit] PHYS_COPY: do_phys_copy() completed");
                }
                break;
            case SET_TEXT_BANK_CMD:
            case SET_DATA_BANK_INM_CMD:
                data_cnt = 2;   // 1 word = 2 bytes
                break;
            case SET_TEXT_BANK_DAT:
                --data_cnt;
                this.data[data_cnt]= data;
                if(data_cnt == 0)
                    do_set_text_bank();
                break;
            case SET_BOTH_BANKS_CMD:
            case PEEK_BYTE_ON_BANK_CMD:
            case SET_KERNEL_PROC_TABLE_CMD:
                data_cnt = 4;   // 2 words = 4 bytes
                break;
            case POKE_BYTE_ON_BANK_CMD:
                data_cnt = 5;   // 2 words + 1 byte = 5 bytes
                break;
            case SET_DATA_BANK_INM_DAT:
                --data_cnt;
                this.data[data_cnt] = data;
                if(data_cnt == 0)
                    do_set_data_bank();
                break;
            case SET_BOTH_BANKS_DAT:
                --data_cnt;
                this.data[data_cnt] = data;
                if(data_cnt == 0)
                    do_set_both_banks();
                break;
            case PEEK_BYTE_ON_BANK_DAT:
                --data_cnt;
                this.data[data_cnt] = data;
                if(data_cnt == 0) {
                    this.data[data_cnt] = peek_byte_on_bank();
                }
                break;
            case POKE_BYTE_ON_BANK_DAT:
                --data_cnt;
                this.data[data_cnt] = data;
                if(data_cnt == 0) {
                    poke_byte_on_bank(this.data[data_cnt]);
                }
                break;
            case SET_KERNEL_PROC_TABLE_DAT:
                --data_cnt;
                this.data[data_cnt] = data;
                if(data_cnt == 0) {
                    do_set_kernel_proc_table();
                }
                break;
            case VDISK_CMD:
                data_cnt = 9;   // 4 words + 1 byte = 9 bytes
                break;
            case VDISK_DAT:
                --data_cnt;
                this.data[data_cnt] = data;
                if(data_cnt == 0) {
                    do_set_vdisk_params();
                }
                break;
            default:
                System.out.println(format("IOManagementUnit:write: Unrecognized address: 0x%04X", addr));
        }
    }

    @Override
    public void write(int addr, byte data, int tstate) {
        this.write(addr, data);
    }

    @Override
    public byte read(int addr) // { return (byte) 0xFF; }
    {
        byte r = (byte) 0xFF; // Pull-up!

        switch (addr) {
            /* TODO
            case GET_TOT_MEM_DAT:
                     if data_cnt = 0;
                     return LOW get_tot_mem
                     if data_cnt = 1;
                     return HI get_tot_mem;
                break;*/
            case PEEK_BYTE_ON_BANK_DAT:
                r = data[0];
                break;
            case VDISK_DAT:
                //r = -1;
        }

        return r;
    }


    private void do_phys_copy() {
        // https://stackoverflow.com/a/70622171/2928048
        int cnt = ((data[11] << 24) & 0xFF00_0000) // Z88DK's long (32 bits) are Java's int
                | ((data[10] << 16) & 0x00FF_0000)
                | ((data[9] << 8) & 0x00_FF00)
                | (data[8] & 0x00FF);

        int dst = ((data[7] << 24) & 0xFF00_0000)
                | ((data[6] << 16) & 0x00FF_0000)
                | ((data[5] << 8) & 0x00_FF00)
                | (data[4] & 0x00FF);

        int org = ((data[3] << 24) & 0xFF00_0000)
                | ((data[2] << 16) & 0x00FF_0000)
                | ((data[1] << 8) & 0x00_FF00)
                | (data[0]  & 0x00FF);

        System.out.println(format("[IOManagementUnit] do_phys_copy() called"));
        System.out.println(format("[IOManagementUnit]   cnt=0x%08X (bytes to copy)", cnt+2));
        System.out.println(format("[IOManagementUnit]   dst=0x%08X (bank=%04X, offset=%04X)", dst-2, (dst-2)>>>16, (dst-2)&0x0FFFF));
        System.out.println(format("[IOManagementUnit]   org=0x%08X (bank=%04X, offset=%04X)", org-2, (org-2)>>>16, (org-2)&0x0FFFF));

        try {
            zuxComputer.movemem_between_banks(org, dst, cnt, Memory.MovememDirection.FORWARD);
            System.out.println("[IOManagementUnit] movemem_between_banks() completed successfully");

            // Debug: print ALL bytes of destination after copy
            System.out.println(format("[IOManagementUnit] Destination bytes after copy (%d bytes):", cnt+2));
            int old_bank = zuxComputer.getDataBank();
            int dst_bank = ((dst-2)>>> 16) & 0x0FFFF;
            int dst_offset = (dst-2) & 0x0FFFF;
            zuxComputer.setDataBankImmediate(dst_bank);
            StringBuilder hex_dump = new StringBuilder();
            for (int i = 0; i < cnt+2; i++) {
                int addr = dst_offset + i;
                byte val = zuxComputer.peek(addr);
                hex_dump.append(format("%02X ", val & 0xFF));
                if ((i + 1) % 16 == 0) {
                    System.out.println(format("[IOManagementUnit]   [%04X-%04X]: %s",
                        dst_offset + i - 15, dst_offset + i, hex_dump.toString()));
                    hex_dump = new StringBuilder();
                }
            }
            if (hex_dump.length() > 0) {
                System.out.println(format("[IOManagementUnit]   [%04X-%04X]: %s",
                    dst_offset + (cnt/16)*16, dst_offset + cnt - 1, hex_dump.toString()));
            }
            zuxComputer.setDataBankImmediate(old_bank);
        } catch (Exception e) {
            System.out.println("[IOManagementUnit] ERROR in movemem_between_banks(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void do_set_text_bank() {
        int bank_low = (data[1]&0x00FF);
        int bank_hi = (data[0]&0x00FF);
        int bank = bank_hi << 8 | ( bank_low & 0x00FF );
        zuxComputer.setTextBank(bank);
    }

    private void do_set_data_bank() {
        int bank_low = (data[1]&0x00FF);
        int bank_hi = (data[0]&0x00FF);
        int bank = bank_hi << 8 | ( bank_low & 0x00FF );;
        zuxComputer.setDataBankImmediate(bank);
    }

    private void do_set_both_banks() {
        int text_bank_low = (data[3]&0x00FF);
        int text_bank_hi = (data[2]&0x00FF);
        int data_bank_low = (data[1]&0x00FF);
        int data_bank_hi = (data[0]&0x00FF);
        int textbank = text_bank_hi << 8 | ( text_bank_low & 0x00FF );;
        int databank = data_bank_hi << 8 | ( data_bank_low & 0x00FF );;
        zuxComputer.setBothBanks(textbank, databank);
    }

    private byte peek_byte_on_bank() {
        int hl_offset_low = (data[3]&0x00FF);
        int hl_offset_hi = (data[2]&0x00FF);
        int de_bank_low = (data[1]&0x00FF);
        int de_bank_hi = (data[0]&0x00FF);
        int hl_offset = hl_offset_hi << 8 | ( hl_offset_low & 0x00FF );;
        int de_bank = de_bank_hi << 8 | ( de_bank_low & 0x00FF );;

System.out.println("peek_byte_on_bank");
System.out.println(format("de_bank=%04X", de_bank));
System.out.println(format("hl_offset=%04X", hl_offset));

        int dataBank = zuxComputer.getDataBank();
        zuxComputer.setDataBankImmediate(de_bank);
        byte a = zuxComputer.peek(hl_offset);
        zuxComputer.setDataBankImmediate(dataBank);
System.out.println(format("a=%02X", a));
        return a;
    }

    private void poke_byte_on_bank(byte a) {
        int hl_offset_low = (data[4]&0x00FF);
        int hl_offset_hi = (data[3]&0x00FF);
        int de_bank_low = (data[2]&0x00FF);
        int de_bank_hi = (data[1]&0x00FF);
        //int a = (data[0]&0x00FF);
        int hl_offset = hl_offset_hi << 8 | ( hl_offset_low & 0x00FF );;
        int de_bank = de_bank_hi << 8 | ( de_bank_low & 0x00FF );;

System.out.println("poke_byte_on_bank");
System.out.println(format("de_bank=%04X", de_bank));
System.out.println(format("hl_offset=%04X", hl_offset));
System.out.println(format("a=%02X", a));

        int dataBank = zuxComputer.getDataBank();
        zuxComputer.setDataBankImmediate(de_bank);
        zuxComputer.poke(hl_offset, a);
        zuxComputer.setDataBankImmediate(dataBank);
    }

    private void do_set_kernel_proc_table() {
        int procTable_low = (data[3]&0x00FF);
        int procTable_hi = (data[2]&0x00FF);
        int sizeTable_low = (data[1]&0x00FF);   // NR_TASKS+NR_PROCS
        int sizeTable_hi = (data[0]&0x00FF);    // sizeof(struct proc)
        int procTable = procTable_hi << 8 | ( procTable_low & 0x00FF );
        int sizeTable = sizeTable_hi * sizeTable_low;
        eventBus.publish(new SetKernelProcTableEvent(procTable, sizeTable));
        System.out.println("SetKernelProcTableEvent("+String.format("%04X", procTable)+", "+String.format("%04X", sizeTable)+")");
    }

    private void do_set_vdisk_params() {
        int minor = (data[8]&0x00FF);
        int blk_lo = (data[7]&0x00FF);
        int blk_hi = (data[6]&0x00FF);
        int bank_lo = (data[5]&0x00FF);
        int bank_hi = (data[4]&0x00FF);
        int off_lo = (data[3]&0x00FF);
        int off_hi = (data[2]&0x00FF);
        int cnt_lo = (data[1]&0x00FF);
        int cnt_hi = (data[0]&0x00FF);
        int block = blk_hi << 8 | ( blk_lo & 0x00FF );
        int bank = bank_hi << 8 | ( bank_lo & 0x00FF );
        int offset = off_hi << 8 | ( off_lo & 0x00FF );
        int count = cnt_hi << 8 | ( cnt_lo & 0x00FF );

        System.out.println("minor = "+minor);
        System.out.println("block = "+block);
        System.out.println("bank = "+bank);
        System.out.println("offset = "+offset);
        System.out.println("count = "+count);
    }
}
