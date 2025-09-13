package com.github.jagarsoft;

import java.util.LinkedList;

public class Z80Disassembler extends Z80 implements Z80OpCode {

    Instruction instruction = new Instruction();
    LinkedList<Instruction> instructions = new LinkedList<Instruction>();
    Z80 cpu;

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

    public int getPC() {
        if(cpu != null)
            return cpu.PC;
        else
            return PC;
    }

    public Instruction fetchInstruction() {
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
        this.PC = this.getPC();
        return i;
    }

	@Override
    public void NOP() {
        instruction.mnemonic = "NOP";
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
    public void LD_nn_HL() {
        byte Z = currentComp.peek(PC++);
        byte W = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.opCodes[instruction.opCodeCounter++] = W;
        instruction.mnemonic = "LD ("+getWord(W, Z)+"), HL";
    }

	@Override
    public void LD_nn_A() {
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
    public void LD_HL_nn() {
        byte Z = currentComp.peek(PC++);
        byte W = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.opCodes[instruction.opCodeCounter++] = W;
        instruction.mnemonic = "LD HL, (" + getWord(W, Z) + ")";
    }

	@Override
    public void LD_A_nn() {
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

    public void RL_r_z() {
        instruction.mnemonic = "RL "+r[z];
    }

/*      CBopCodes[0][0][3] = opC::RR_r_z;
        CBopCodes[0][0][4] = opC::SLA_r_z;
        CBopCodes[0][0][5] = opC::SRA_r_z;
        CBopCodes[0][0][6] = opC::SLL_r_z;*/
    public void RR_r_z() { instruction.mnemonic = "RR "+r[z]; System.out.println("RR_r_z ERROR"); }
    public void SRA_r_z() { instruction.mnemonic = "SRA "+r[z]; System.out.println("SRA_r_z ERROR"); }
    public void SLL_r_z() { instruction.mnemonic = "SRL "+r[z]; System.out.println("SLL_r_z ERROR"); }
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

/*	@Override
    public void RRD() { // TODO
        instruction.mnemonic = "RRD";
    }

    @Override
    public void RLD() { // TODO
        instruction.mnemonic = "RLD";
    }
*/
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
    public void DEC_IX() {
        instruction.mnemonic = "DEC IX";
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
    public void DEC_IY_d() {
        byte d = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.mnemonic = "DEC (IY+"+String.format("%02X",d)+")";
    }

	@Override
    public void LD_IY_d_n() {
        byte d = currentComp.peek(PC++);
        Z = currentComp.peek(PC++);
        instruction.opCodes[instruction.opCodeCounter++] = d;
        instruction.opCodes[instruction.opCodeCounter++] = Z;
        instruction.mnemonic = "LD (IY+"+String.format("%02X",d)+"), "+Z;
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

    public void POP_IY() { instruction.mnemonic = "POP IY"; }

    //@Override
    public void JP_IY() { instruction.mnemonic = "JP (IY)"; }

    @Override
    public void PUSH_IY() { instruction.mnemonic = "PUSH IY"; }
}
