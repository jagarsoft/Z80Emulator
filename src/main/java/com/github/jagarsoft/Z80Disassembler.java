package com.github.jagarsoft;

import com.github.jagarsoft.ZuxApp.modules.dataregion.DataRegion;

import java.nio.charset.StandardCharsets;

public class Z80Disassembler extends Z80 implements Z80OpCode {

    Instruction instruction = new Instruction();
    Z80 cpu;

    private DataRegion dataRegion;

    public Z80Disassembler() {
        dispatcher(this);
        this.cpu = null;
        //this.reset();
    }

    public Z80Disassembler(Z80 cpu) {
        dispatcher(this);
        this.cpu = cpu;
        //this.reset();
    }

    /*public Disassembler(Z80OpCode opC) {
        dispatcher(opC);
        this.reset();
    }*/

    public void setComputer(Computer theComp) {
        this.cpu = theComp.getCPU();
        super.setComputer(theComp);
    }

    public void dump(int org, int size) {
        currentComp.setOrigin(org);
        do {
            this.fetch();
        } while( (this.getPC() - org) < size);
    }

    public void CB_prefix() {
        byte opC = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = opC;
        fetchCB(opC);
    }

    public void ED_prefix() {
        byte opC = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = opC;
        fetchED(opC);
    }

    public void DD_prefix() {
        byte opC = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = opC;
        fetchDD(opC);
    }

    public void FD_prefix() {
        byte opC = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = opC;
        fetchFD(opC);
    }

    public void FDCB_prefix() {
        d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = (byte)d;
        fetchFDCB((byte)0); // dummy param
    }

    public void fetchFDCB(byte d) { // d is dummy
        byte opC = currentComp.peek(PC); // Exception: not skip yet

        opCMasked(opC);

        if (FDCBopCodes[x][z][y] != null) {
            FDCBopCodes[x][z][y].execute();
        } else {
            throw new IllegalArgumentException("FDCB+OpCode not implemented yet: " + Integer.toHexString(opC));
        }
    }

    public void setOrigin(int org) {
        PC = org;
    }

    public int getPC() {
assert cpu != null;
        if(cpu != null)
            return cpu.PC;
        else
            return PC;
    }

    public Instruction fetchInstruction() {
        if( dataRegion != null && dataRegion.isDataRegion(cpu.PC) ){
            return fetchData(currentComp.peek(cpu.PC++));
        }

        if(cpu != null)
            return fetchInstruction(currentComp.peek(cpu.PC++));
        else
            return fetchInstruction(currentComp.peek(PC++));
         }

    public Instruction fetchInstruction(byte opC) {
        this.PC = this.getPC();
        instruction.PC = this.PC - 1;
        instruction.opCodes[0] = opC;
        instruction.opCodeCounter = 1;
        super.fetch(opC);
        System.out.println(instruction);
        //instructions.add(instruction);
        Instruction i = instruction;
        instruction = null;
        instruction = new Instruction();
        cpu.PC = this.PC; //this.PC = this.getPC();
        return i;
    }

    public Instruction fetchData(byte opC) {
        this.PC = this.getPC();
        instruction.PC = this.PC - 1;
        instruction.opCodes[0] = opC;
        instruction.opCodeCounter = 1;
        //super.fetch(opC);
        String label = dataRegion.getLabel(instruction.PC);
        int end = dataRegion.getEnd();
        int size = end - instruction.PC + 1;
        String comment = dataRegion.getComment();
        fetchLabel(opC, label, size);
        instruction.comment = comment;
        instruction.isData = true;
        System.out.println(instruction);
        //instructions.add(instruction);
        Instruction i = instruction;
        instruction = null;
        instruction = new Instruction();
        cpu.PC = this.PC; //this.PC = this.getPC();
        return i;
    }

    public void setDataBlock(DataRegion dataRegion) {
        this.dataRegion = dataRegion;
    }

    void fetchLabel(byte opC, String label, int size) {
        switch(label) {
            case "DEFB":
            case "DEFS":
                label = "DEFB";
                fetchDEFB(opC, label, size);
                break;
            case "DEFW":
                fetchDEFW(opC, label, size);
                break;
            case "DEFM":
                fetchDEFM(opC, label, size);
                break;
            case "ASCII80":
                fetchASCII80(opC, label, size);
                break;
            case "ASCIIZ":
                fetchASCIIZ(opC, label, size);
                break;
        }
    }

    private void fetchDEFB(byte opC, String label, int size) {
        StringBuilder s = new StringBuilder();
        while (instruction.opCodeCounter < 4 && instruction.opCodeCounter < size) {
            opC = currentComp.peek(PC++);
            instruction.opCodes[instruction.opCodeCounter++] = opC;
        }
        for(int i = 0; i < instruction.opCodeCounter; i++) {
            opC = instruction.opCodes[i];
            if( i > 0 ) // i == 1 or i == 2
                s.append(", ");
            s.append("0x"+Integer.toHexString(Byte.toUnsignedInt(opC)).toUpperCase());
        }
        instruction.mnemonic = label + " " + s;
    }

    private void fetchDEFW(byte opC, String label, int size) {
        StringBuilder s = new StringBuilder();
        while (instruction.opCodeCounter < 4 && instruction.opCodeCounter < size*2) { // size => 1 word, 2 words, 3 words, etc
            opC = currentComp.peek(PC++);
            instruction.opCodes[instruction.opCodeCounter++] = opC;
        }
        for(int i = 0; i < instruction.opCodeCounter; i+=2) {
            Z = instruction.opCodes[i];
            W = instruction.opCodes[i+1];
            if( i == 2 )
                s.append(", ");
            s.append("0x"+getWord(W, Z));
        }
        instruction.mnemonic = label + " " + s;
    }

    private void fetchDEFM(byte opC, String label, int size) {
        StringBuilder s = new StringBuilder();
        while(instruction.opCodeCounter < 4 && instruction.opCodeCounter < size) {
            opC = currentComp.peek(PC++);
            instruction.opCodes[instruction.opCodeCounter++] = opC;
        }
        for(int i = 0; i < instruction.opCodeCounter; i++) {
            opC = instruction.opCodes[i];
            s.append(escapeCharForString((char)opC));
            if( i > 0 && i < instruction.opCodeCounter )
                s.append(", ");
        }
        instruction.mnemonic = "DEFM \"" + s + "\"";
    }

    private void fetchASCIIZ(byte opC, String label, int size) {
        StringBuilder s = new StringBuilder();
        boolean endString = (opC == 0x0);
        while(instruction.opCodeCounter < 4 && instruction.opCodeCounter < size && !endString) {
            opC = currentComp.peek(PC++);
            instruction.opCodes[instruction.opCodeCounter++] = opC;
            endString = (opC == 0x0);
        }
        for(int i = 0; i < instruction.opCodeCounter; i++) {
            opC = instruction.opCodes[i];
           // Sacar la logica de si es printable o no fuera de la funcion para poder
            //concatenar las , separadoras y los inicio y fin de ' '
            s.append(escapeCharForString((char)opC));
        }
        instruction.mnemonic = "DEFM \"" + s + "\"";
    }

    private void fetchASCII80(byte opC, String label, int size) {
        StringBuilder s = new StringBuilder();
        boolean endString = (opC & 0x080) == 0x080;
        while(instruction.opCodeCounter < 4 && instruction.opCodeCounter < size && !endString) {
            opC = currentComp.peek(PC++);
            instruction.opCodes[instruction.opCodeCounter++] = opC;
            endString = (opC & 0x080) == 0x080;
        }
        instruction.mnemonic = renderOpCodesAsASCII80();
    }

    private String renderOpCodesAsASCII80() {
        StringBuilder sb = new StringBuilder();
        sb.append("DEFM ");

        StringBuilder stringPart = new StringBuilder();
        boolean inString = false;

        for (int i = 0; i < instruction.opCodeCounter; i++) {
            int b = instruction.opCodes[i] & 0xFF;
            boolean bit7 = (b & 0x80) != 0;
            int charCode = b & 0x7F; // limpio bit 7 para imprimir

            char c = (char) charCode;
            boolean printable = (c >= 32 && c <= 126 && c != '"' && c != '\\')
                    || c == '\n' || c == '\r' || c == '\t';

            if (bit7) {
                // Cierra cualquier cadena abierta antes de este byte
                if (inString) {
                    sb.append('"').append(stringPart).append('"');
                    stringPart.setLength(0);
                    inString = false;
                    sb.append(',');
                } else if (i > 0) {
                    sb.append(',');
                }

                // Representación individual con bit 7
                sb.append(renderCharWithEscape(c)).append("+0x80");
            } else {
                if (printable) {
                    if (!inString) {
                        if (i > 0) sb.append(',');
                        inString = true;
                    }
                    stringPart.append(escapeCharForString(c));
                } else {
                    // Cierra cadena si estaba abierta
                    if (inString) {
                        sb.append('"').append(stringPart).append('"').append(',');
                        stringPart.setLength(0);
                        inString = false;
                    } else if (i > 0) {
                        sb.append(',');
                    }
                    sb.append("0x").append(Integer.toHexString(b).toUpperCase());
                }
            }
        }

        // Cierra cadena final si quedó abierta
        if (inString) {
            sb.append('"').append(stringPart).append('"');
        }

        return sb.toString();
    }

    /**
     * Escapa caracteres de control o especiales para que aparezcan correctamente
     * dentro de cadenas entre comillas.
     */
    private static String escapeCharForString(char c) {
        switch (c) {
            case '\n': return "\\n";
            case '\r': return "\\r";
            case '\t': return "\\t";
            case '\"': return "\\\"";
            case '\\': return "\\\\";
            case '\0': return "\\0";
            //default:   return String.valueOf(c);
            default:
                if (c >= 32 && c <= 126) {
                    return String.valueOf(c);
                } else {
                    return String.format("'0x%02X'", (int) c);
                }
        }
    }

    /**
     * Renderiza un carácter individual entre comillas simples,
     * escapando si es necesario (\n, \r, etc.).
     */
    private static String renderCharWithEscape(char c) {
        switch (c) {
            case '\n': return "'\\n'";
            case '\r': return "'\\r'";
            case '\t': return "'\\t'";
            case '\'': return "'\\''"; // comilla simple escapada
            case '\\': return "'\\\\'";
            default:
                if (c >= 32 && c <= 126) {
                    return "'" + c + "'";
                } else {
                    return String.format("0x%02X", (int) c);
                }
        }
    }

    @Override
    public void NOP() {
        instruction.mnemonic = "NOP";
    }
    public void NONI() {
        instruction.mnemonic = "NONI";
        instruction.comment = "; NOP + next instruct, No Interrupts";
    }

	@Override
    public void EX_AF_AF_() {
        instruction.mnemonic = "EX AF, AF'";
    }

	@Override
    public void DJNZ() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "DJNZ " + String.format("%04X", PC+d);
    }

	@Override
    public void JR() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "JR " + String.format("%04X", PC+d);
    }

	@Override
    public void JR_cc() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "JR " + cc[y-4] + ", " + String.format("%04X", PC+d);
    }

    @Override
    public void LD_rp_p_nn() {
        byte Z = currentComp.peek(PC++);
        byte W = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.opCodes[instruction.opCodeCounter++] = W;
        instruction.mnemonic = "LD "+rp[p]+", "+getWord(W, Z);
    }

    @Override
    public void ADD_HL_rp_p() {
        instruction.mnemonic = "ADD HL, "+rp[p];
    }

    @Override
    public void LD_BC_A() {
        instruction.mnemonic = "LD (BC), A";
    }
    
    @Override
    public void LD_DE_A() {
        instruction.mnemonic = "LD (DE), A";
    }

	@Override
    public void LD_mm_HL() {
        byte Z = currentComp.peek(PC++);
        byte W = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.opCodes[instruction.opCodeCounter++] = W;
        instruction.mnemonic = "LD ("+getWord(W, Z)+"), HL";
    }

	@Override
    public void LD_mm_A() {
        byte Z = currentComp.peek(PC++);
        byte W = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.opCodes[instruction.opCodeCounter++] = W;
        instruction.mnemonic = "LD ("+getWord(W, Z)+"), A";
    }

	@Override
    public void LD_A_BC() {
        instruction.mnemonic = "LD A, (BC)";
    }

	@Override
    public void LD_A_DE() {
        instruction.mnemonic = "LD A, (DE)";
    }

	@Override
    public void LD_HL_mm() {
        byte Z = currentComp.peek(PC++);
        byte W = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.opCodes[instruction.opCodeCounter++] = W;
        instruction.mnemonic = "LD HL, (" + getWord(W, Z) + ")";
    }

	@Override
    public void LD_A_mm() {
        byte Z = currentComp.peek(PC++);
        byte W = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.opCodes[instruction.opCodeCounter++] = W;
        instruction.mnemonic = "LD A, (" + getWord(W, Z) + ")";
    }

	@Override
    public void INC_rp_p() {
        instruction.mnemonic = "INC "+rp[p];
    }

	@Override
    public void DEC_rp_p() {
        instruction.mnemonic = "DEC " + rp[p];
    }
	@Override
    public void INC_r_y() {
        instruction.mnemonic = "INC "+r[y];
    }

	@Override
    public void DEC_r_y() {
        instruction.mnemonic = "DEC "+r[y];
    }

	@Override
    public void LD_r_y_n() {
        Z = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.mnemonic = "LD "+r[y]+", "+toByteHex(Z);
    }

    private String toByteHex(byte z) {
        return String.format("%02X", z);
    }

    @Override
    public void RLCA() {
        instruction.mnemonic = "RLCA";
    }
    
	@Override
    public void RRCA() {
        instruction.mnemonic = "RRCA";
    }

	@Override
    public void RLA() {
        instruction.mnemonic = "RLA";
    }

	@Override
    public void RRA() {
        instruction.mnemonic = "RRA";
    }

	@Override
    public void DAA() {
        instruction.mnemonic = "DAA";
    }

	@Override
    public void CPL() {
        instruction.mnemonic = "CPL";
    }

	@Override
    public void SCF() {
        instruction.mnemonic = "SCF";
    }

    @Override
    public void CCF() {
        instruction.mnemonic = "CCF";
    }

	@Override
    public void LD_r_y_r_z() {
        instruction.mnemonic = "LD "+r[y]+", "+r[z];
    }

	@Override
    public void HALT() {
        instruction.mnemonic = "HALT";
    }

	@Override
    public void ADD_A_r_z() {
        instruction.mnemonic = "ADD A, "+r[z];
    }

	@Override
    public void ADC_A_r_z() {
        instruction.mnemonic = "ADC A, "+r[z];
    }
	@Override
    public void SUB_r_z() {
        instruction.mnemonic = "SUB "+r[z];
    }
	@Override
    public void SBC_A_r_z() {
        instruction.mnemonic = "SBC A, "+r[z];
    }

	@Override
    public void AND_r_z() {
        instruction.mnemonic = "AND "+r[z];
    }

	@Override
    public void XOR_r_z() {
        instruction.mnemonic = "XOR "+r[z];
    }

	@Override
    public void OR_r_z() {
        instruction.mnemonic = "OR "+r[z];
    }

	@Override
    public void CP_r_z() {
        instruction.mnemonic = "CP "+r[z];
    }

	@Override    
    public void RET_cc_y() {
        instruction.mnemonic = "RET "+cc[y];
    }

	@Override        
    public void POP_rp2_p() {
        instruction.mnemonic = "POP "+rp2[p];
    }

	@Override    
    public void RET() {
        instruction.mnemonic = "RET";
    }

	@Override    
    public void EXX() {
        instruction.mnemonic = "EXX";
    }

	@Override    
    public void JP_HL() {
        instruction.mnemonic = "JP (HL)";
    }

    @Override
    public void LD_SP_HL() {
        instruction.mnemonic = "LD SP, HL";
    }

    @Override
    public void LD_SP_IX() {
        instruction.mnemonic = "LD SP, IX";
    }

    @Override
    public void LD_SP_IY() {
        instruction.mnemonic = "LD SP, IY";
    }

	@Override
    public void JP_cc_y_nn() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.opCodes[instruction.opCodeCounter++] = W;
        instruction.mnemonic = "JP "+cc[y]+", "+getWord(W, Z);
    }

	@Override
    public void JP_nn() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.opCodes[instruction.opCodeCounter++] = W;
        instruction.mnemonic = "JP "+getWord(W, Z);
    }

	@Override
    public void OUT_n_A() {
        Z = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.mnemonic = "OUT ("+toByteHex(Z)+"), A";
    }

	@Override
    public void IN_A_n() {
        Z = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.mnemonic = "IN A, ("+toByteHex(Z)+")";
    }

	@Override
    public void EX_SP_HL() {
        instruction.mnemonic = "EX (SP), HL";
    }

    @Override
    public void EX_SP_IX() {
        instruction.mnemonic = "EX (SP), IX";
    }

    @Override
    public void EX_SP_IY() {
        instruction.mnemonic = "EX (SP), IY";
    }

	@Override
    public void EX_DE_HL() {
        instruction.mnemonic = "EX DE, HL";
    }

	@Override

    public void DI() {
        instruction.mnemonic = "DI";
    }
    
    @Override
    public void EI() {
        instruction.mnemonic = "EI";
    }

	@Override
    public void CALL_cc_y_nn() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.opCodes[instruction.opCodeCounter++] = W;
        instruction.mnemonic = "CALL "+cc[y]+", "+getWord(W, Z);
    }

	@Override
    public void PUSH_rp2_p() {
        instruction.mnemonic = "PUSH "+rp2[p];
    }

	@Override
    public void CALL_nn() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.opCodes[instruction.opCodeCounter++] = W;
        instruction.mnemonic = "CALL "+getWord(W, Z);
    }

	@Override
    public void ADD_A_n() {
        Z = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.mnemonic = "ADD A, "+toByteHex(Z);
    }

	@Override
    public void ADC_A_n() {
        Z = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.mnemonic = "ADC A, "+toByteHex(Z);
    }

	@Override
    public void SUB_n() {
        Z = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.mnemonic = "SUB "+toByteHex(Z);
    }

	@Override
    public void SBC_A_n() {
        Z = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.mnemonic = "SBC A, "+toByteHex(Z);
    }
	@Override
    public void AND_n() {
        Z = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.mnemonic = "AND "+toByteHex(Z);
    }

	@Override
    public void XOR_n() {
        Z = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.mnemonic = "XOR "+toByteHex(Z);
    }

	@Override
    public void OR_n() {
        Z = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.mnemonic = "OR "+toByteHex(Z);
    }

	@Override
    public void CP_n() {
        Z = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.mnemonic = "CP "+toByteHex(Z);
    }

	@Override
    public void RST_y_8() {
        instruction.mnemonic = "RST "+String.format("%02x", y*8);
    }

    /* ED prefix */

	@Override
    public void SBC_HL_rp_p() {
        instruction.mnemonic = "SBC HL, "+rp[p];
    }

	@Override
    public void ADC_HL_rp_p() {
        instruction.mnemonic = "ADC HL, "+rp[p];
    }

	@Override
    public void LD_mm_rp_p() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.opCodes[instruction.opCodeCounter++] = W;
        instruction.mnemonic = "LD ("+getWord(W,Z)+"), "+rp[p];
    }

	@Override
    public void LD_rp_p_mm() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.opCodes[instruction.opCodeCounter++] = W;
        instruction.mnemonic = "LD "+rp[p]+", ("+getWord(W,Z)+")";
    }

	/* CB Prefix */

    public void RLC_r_z() { instruction.mnemonic = "RLC "+r[z]; }
    public void RRC_r_z() { instruction.mnemonic = "RLC "+r[z]; }
    public void RL_r_z() {
        instruction.mnemonic = "RL "+r[z];
    }
    public void RR_r_z() { instruction.mnemonic = "RR "+r[z]; }
    public void SLA_r_z() { instruction.mnemonic = "SLA "+r[z]; }
    public void SRA_r_z() { instruction.mnemonic = "SRA "+r[z]; }
    public void SLL_r_z() { instruction.mnemonic = "SLL "+r[z]; instruction.comment = "; undocumented"; }
    public void SRL_r_z() {
        instruction.mnemonic = "SRL "+r[z];
    }

	@Override
    public void BIT_y_r_z() {
        instruction.mnemonic = "BIT "+y+", "+r[z];
    }

	@Override
    public void RES_y_r_z() {
        instruction.mnemonic = "RES "+y+", "+r[z];
    }

	@Override
    public void SET_y_r_z() {
        instruction.mnemonic = "SET "+y+", "+r[z];
    }

	/* ED Prefix */

    public void OUT_C_r_y() {
        instruction.mnemonic = "OUT (C), "+r[y];
    }

	@Override
    public void IN_r_y_C() {
        instruction.mnemonic = "IN "+r[y]+", (C)";
    }

	@Override
    public void NEG() {
        instruction.mnemonic = "NEG";
    }

    @Override
    public void RETN() { instruction.mnemonic = "RETN"; }

    @Override
    public void RETI() { instruction.mnemonic = "RETI"; }

	@Override
    public void IM_im_y() {
        instruction.mnemonic = "IM "+im[y];
    }

	@Override
    public void LD_I_A() {
        instruction.mnemonic = "LD I, A";
    }

	@Override
    public void LD_R_A() {
        instruction.mnemonic = "LD R, A";
    }

    @Override
    public void LD_A_I() {
        instruction.mnemonic = "LD A, I";
    }
    
    @Override
    public void LD_A_R() {
        instruction.mnemonic = "LD A, R";
    }

	@Override
    public void RRD() {
        instruction.mnemonic = "RRD";
    }

    @Override
    public void RLD() {
        instruction.mnemonic = "RLD";
    }

	@Override
    public void LDI() { // TODO
        instruction.mnemonic = "LDI";
    }

	@Override
    public void LDD() { // TODO
        instruction.mnemonic = "LDD";
    }

	@Override
    public void LDIR() {
        instruction.mnemonic = "LDIR";
    }

	@Override
    public void LDDR() {
        instruction.mnemonic = "LDDR";
    }

	@Override
    public void CPI() { // TODO
        instruction.mnemonic = "CPI";
    }

	@Override
    public void CPD() { // TODO
        instruction.mnemonic = "CPD";
    }

	@Override
    public void CPIR() { // TODO
        instruction.mnemonic = "CPIR";
    }

	@Override
    public void CPDR() { // TODO
        instruction.mnemonic = "CPDR";
    }

	@Override
    public void INI() { // TODO
        instruction.mnemonic = "INI";
    }

	@Override
    public void IND() { // TODO
        instruction.mnemonic = "IND";
    }

	@Override
    public void INIR() { //TODO
        instruction.mnemonic = "INIR";
    }

	@Override
    public void INDR() { // TODO
        instruction.mnemonic = "INDR";
    }

	@Override
    public void OUTI() { // TODO
        instruction.mnemonic = "OUTI";
    }

	@Override
    public void OUTD() { // TODO
        instruction.mnemonic = "OUTD";
    }

	@Override
    public void OTIR() { // TODO
        instruction.mnemonic = "OTIR";
    }

	@Override
    public void OTDR() {
        // TODO
    }

    /* DD prefix */

    @Override
    public void INC_IX() {
        instruction.mnemonic = "INC IX";
    }

    @Override
    public void DEC_IX() {
        instruction.mnemonic = "DEC IX";
    }

    @Override
    public void INC_IX_d() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "INC (IX+"+String.format("%02X",d)+")";
    }

    @Override
    public void DEC_IX_d() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "DEC (IX+"+String.format("%02X",d)+")";
    }

    @Override
    public void LD_IXH_n() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "LD IXh, "+String.format("%02X",d);
        instruction.comment = "; undocumented";
    }

    @Override
    public void LD_IXL_n() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "LD IXl, "+String.format("%02X",d);
        instruction.comment = "; undocumented";
    }

    @Override
    public void LD_r_y_IX_d() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "LD "+r[y]+", (IX+"+String.format("%02X",d)+")";
    }

    @Override
    public void POP_IX() {
        instruction.mnemonic = "POP IX";
    }

    @Override
    public void JP_IX() {
        instruction.mnemonic = "JP (IX)";
    }

    @Override
    public void PUSH_IX() {
        instruction.mnemonic = "PUSH IX";
    }

    @Override
    public void LD_IX_nn() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);

        instruction.mnemonic = "LD IX, "+getWord(W, Z);
    }
    
    @Override
    public void ADD_IX_rp_p() {
        instruction.mnemonic = "ADD IX, "+rp[p];
	}

    @Override
    public void ADD_A_IX_d() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "ADD A, (IX+"+String.format("%02X",d)+")";
    }

    @Override
    public void ADC_A_IX_d() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "ADC A, (IX+"+String.format("%02X",d)+")";
    }

    @Override
    public void SUB_IX_d() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "SUB (IX+"+String.format("%02X",d)+")";
    }

    @Override
    public void SBC_A_IX_d() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "SBC A, (IX+"+String.format("%02X",d)+")";
    }

    @Override
    public void AND_IX_d() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "AND (IX+"+String.format("%02X",d)+")";
    }

    @Override
    public void XOR_IX_d() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "XOR (IX+"+String.format("%02X",d)+")";
    }

    @Override
    public void OR_IX_d() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "OR (IX+"+String.format("%02X",d)+")";
    }

    @Override
    public void CP_IX_d() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "CP (IX+"+String.format("%02X",d)+")";
    }

	/* FD Prefix */

	@Override
    public void LD_IY_nn() {
        Z = currentComp.peek(PC++);
        W = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.opCodes[instruction.opCodeCounter++] = W;
        instruction.mnemonic = "LD IY, "+getWord(W,Z);
    }

	@Override
    public void LD_IY_d_r_z() {
        d = currentComp.peek(PC++);
        instruction.mnemonic = "LD (IY+"+String.format("%02X",d)+"), "+r[z];
    }

	@Override
    public void LD_r_y_IY_d() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "LD "+r[y]+", (IY+"+String.format("%02X",d)+")";
    }

    @Override
    public void ADD_IY_rp_p()  { instruction.mnemonic = "ADD IY, "+rp[p]; }

    @Override
    public void INC_IY_d() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "INC (IY+"+String.format("%02X",d)+")";
    }

	@Override
    public void DEC_IY_d() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "DEC (IY+"+String.format("%02X",d)+")";
    }

    @Override
    public void INC_IY() {
        instruction.mnemonic = "INC IY";
    }

    @Override
    public void DEC_IY() {
        instruction.mnemonic = "DEC IY";
    }

	@Override
    public void LD_IY_d_n() {
        byte d = currentComp.peek(PC++);
        Z = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.mnemonic = "LD (IY+"+String.format("%02X",d)+"), "+Z;
    }

    /* DDCB prefix */

    @Override
    public void BIT_y_IX_d() {
        byte opC = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = opC;
        instruction.mnemonic = "BIT "+y+", (IX+"+String.format("%02X",d)+")";
    }

    @Override
    public void RES_y_IX_d() {
        byte opC = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = opC;
        instruction.mnemonic = "RES "+y+", (IX+"+String.format("%02X",d)+")";
    }

    @Override
    public void SET_y_IX_d() {
        byte opC = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = opC;
        instruction.mnemonic = "SET "+y+", (IX+"+String.format("%02X",d)+")";
    }

	/* FDCB prefix */

	@Override
    public void BIT_y_IY_d() {
        byte opC = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = opC;
        instruction.mnemonic = "BIT "+y+", (IY+"+String.format("%02X",d)+")";
    }

	@Override
    public void RES_y_IY_d() {
        byte opC = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = opC;
        instruction.mnemonic = "RES "+y+", (IY+"+String.format("%02X",d)+")";
    }

	@Override
    public void SET_y_IY_d() {
        byte opC = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = opC;
        instruction.mnemonic = "SET "+y+", (IY+"+String.format("%02X",d)+")";
    }

	@Override
    public void ADD_A_IY_d() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "ADD A, (IY+"+String.format("%02X",d)+")";
    }

    @Override
    public void ADC_A_IY_d() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "ADC A, (IY+"+String.format("%02X",d)+")";
    }

    @Override
    public void CP_IY_d() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "CP (IY+"+String.format("%02X",d)+")";
    }

    public void SUB_IY_d() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "SUB A, (IY+"+String.format("%02X",d)+")";
    }

    public void SBC_A_IY_d() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "SBC A, (IY+"+String.format("%02X",d)+")";
    }

    @Override
    public void AND_IY_d() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "AND (IY+"+String.format("%02X",d)+")";
    }

    @Override
    public void OR_IY_d() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "OR (IY+"+String.format("%02X",d)+")";
    }

    @Override
    public void XOR_IY_d() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "XOR (IY+"+String.format("%02X",d)+")";
    }

    @Override
    public void LD_IYH_n() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "LD IYh, "+String.format("%02X",d);
        instruction.comment = "; undocumented";
    }

    @Override
    public void LD_IYL_n() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "LD IYl, "+String.format("%02X",d);
        instruction.comment = "; undocumented";
    }

    @Override
    public void LD_IX_d_n() {
        d = currentComp.peek(PC++);
        Z = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.mnemonic = "LD (IX+"+String.format("%02X",d)+"), "+Z;
    }

    public void LD_IX_d_r_z() {
        d = currentComp.peek(PC++);
        instruction.mnemonic = "LD (IX+"+String.format("%02X",d)+"), "+r[z];
    }

    public void LD_mm_IX() {
        byte Z = currentComp.peek(PC++);
        byte W = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.opCodes[instruction.opCodeCounter++] = W;
        instruction.mnemonic = "LD ("+getWord(W, Z)+"), IX";
    }

    public void LD_IX_mm() {
        byte Z = currentComp.peek(PC++);
        byte W = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.opCodes[instruction.opCodeCounter++] = W;
        instruction.mnemonic = "LD IX, (" + getWord(W, Z) + ")";
    }

    public void POP_IY() { instruction.mnemonic = "POP IY"; }

    //@Override
    public void JP_IY() { instruction.mnemonic = "JP (IY)"; }

    @Override
    public void PUSH_IY() { instruction.mnemonic = "PUSH IY"; }
}
