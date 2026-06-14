package com.github.jagarsoft;

public interface Z80OpCode {
    final String[] r = new String[] {
            "B", "C", "D", "E", "H", "L", "(HL)", "A"
    };
    
    final String[] rp = new String[] {
            "BC", "DE", "HL", "SP"
    };


    final String[] rp2 = new String[] {
            "BC", "DE", "HL", "AF"
    };

    final String[] cc = new String[] {
            "NZ", "Z", "NC", "C", "PO", "PE", "P", "M"
    };

    final String[] im = new String[] {
            "0", "?", "1", "2", "?", "?", "?", "?"
    };

    void CB_prefix();
    void DD_prefix();
    void ED_prefix();
    void FD_prefix();
    void DDCB_prefix();
    void FDCB_prefix();

    void NOP();
    void NONI(); // NOP + next instruct no interrupts
    void EX_AF_AF_();
    void DJNZ();
    void JR();
    void JR_cc();

    void LD_rp_p_nn();
    void ADD_HL_rp_p();
    
    void LD_BC_A();
    void LD_DE_A();
    void LD_mm_HL();
    void LD_mm_A();

    void LD_A_BC();
    void LD_A_DE();
    void LD_HL_mm();
    void LD_A_mm();
    
    void INC_rp_p();
    void DEC_rp_p();

    void INC_r_y();
    void DEC_r_y();

    void LD_r_y_n();

    void RLCA();
    void RRCA();
    void RLA();
    void RRA();
    void DAA();
    void CPL();
    void SCF();
    void CCF();

    void LD_r_y_r_z();
    void HALT();

    void ADD_A_r_z();
    void ADC_A_r_z();
    void SUB_r_z();
    void SBC_A_r_z();
    void AND_r_z();
    void XOR_r_z();
    void OR_r_z();
    void CP_r_z();

    void RET_cc_y();
    void POP_rp2_p();
    void RET();
    void EXX();
    void JP_HL();
    void LD_SP_HL();
    void LD_SP_IX();
    void LD_SP_IY();
    void JP_cc_y_nn();

    void JP_nn();
    void OUT_n_A();
    void IN_A_n();
    void EX_SP_HL();
    void EX_SP_IX();
    void EX_SP_IY();
    void EX_DE_HL();
    void DI();
    void EI();

    void CALL_cc_y_nn();

    void PUSH_rp2_p();
    void CALL_nn();

    void ADD_A_n();
    void ADC_A_n();
    void SUB_n();
    void SBC_A_n();
    void AND_n();
    void XOR_n();
    void OR_n();
    void CP_n();

    void RST_y_8();

    /* CB Prefix */

    void RLC_r_z();
    void RRC_r_z();
    void RL_r_z();
    void RR_r_z();
    void SLA_r_z();
    void SRA_r_z();
    void SLL_r_z(); // undocumented
    void SRL_r_z();

    void BIT_y_r_z();
    void RES_y_r_z();
    void SET_y_r_z();

    /* ED prefix */

    void IN_r_y_C();
    void OUT_C_r_y();

    void SBC_HL_rp_p();
    void ADC_HL_rp_p();

    void LD_mm_rp_p();
    void LD_rp_p_mm();

    void NEG();

    void RETN();
    void RETI();

    void IM_im_y();

    void LD_I_A();
    void LD_R_A();
    void LD_A_I();
    void LD_A_R();
    void RRD();
    void RLD();

    void LDI();
    void LDD();
    void LDIR();
    void LDDR();
    void CPI();
    void CPD();
    void CPIR();
    void CPDR();
    void INI();
    void IND();
    void INIR();
    void INDR();
    void OUTI();
    void OUTD();
    void OTIR();
    void OTDR();

    /* DD prefix */

    void LD_IX_nn();
    void INC_IX();
    void DEC_IX();
    void INC_IX_d();
    void DEC_IX_d();
    void LD_r_y_IXH(); // undocumented
    void LD_r_y_IXL(); // undocumented
    void LD_IXH_r_z(); // undocumented
    void LD_IXL_r_z(); // undocumented
    void LD_IXL_IXL(); // undocumented
    void LD_IXH_IXH(); // undocumented
    void LD_IXH_IXL(); // undocumented
    void LD_IXL_IXH(); // undocumented
    void LD_IXH_n(); // undocumented
    void LD_IXL_n(); // undocumented
    void LD_IX_d_r_z();
    void LD_IX_d_n();
    void LD_r_y_IX_d();
    void ADD_IX_rp_p();
    void ADC_A_IX_d();
    void SUB_IX_d();
    void SBC_A_IX_d();
    void AND_IX_d();
    void XOR_IX_d();
    void OR_IX_d();
    void CP_IX_d();
    void POP_IX();
    void LD_mm_IX();
    void LD_IX_mm();
    void JP_IX();
    void PUSH_IX();
    void ADD_A_IX_d();

    void INC_IXH();
    void INC_IXL();
    void DEC_IXH();
    void DEC_IXL();

    void ADD_A_IXH();
    void ADD_A_IXL();
    void ADC_A_IXH();
    void ADC_A_IXL();
    void SUB_IXH();
    void SUB_IXL();
    void SBC_IXH();
    void SBC_IXL();
    void AND_IXH();
    void AND_IXL();
    void XOR_IXH();
    void XOR_IXL();
    void OR_IXH();
    void OR_IXL();
    void CP_IXH();
    void CP_IXL();

    /* DDCB prefix */
    void RLC_IX_d();
    void RRC_IX_d();
    void RL_IX_d();
    void RR_IX_d();
    void SLA_IX_d();
    void SRA_IX_d();
    void SLL_IX_d(); // undocumented
    void SRL_IX_d();

    void BIT_y_IX_d();
    void RES_y_IX_d();
    void SET_y_IX_d();

    /* FD prefix */

    void LD_IY_nn();
    void LD_IY_d_r_z();
    void INC_IY_d();
    void DEC_IY_d();
    void LD_r_y_IYH(); // undocumented
    void LD_r_y_IYL(); // undocumented
    void LD_IYH_r_z(); // undocumented
    void LD_IYL_r_z(); // undocumented
    void LD_IYL_IYL(); // undocumented
    void LD_IYH_IYH(); // undocumented
    void LD_IYH_IYL(); // undocumented
    void LD_IYL_IYH(); // undocumented
    void LD_IYH_n(); // undocumented
    void LD_IYL_n(); // undocumented
    void LD_IY_d_n();
    void LD_r_y_IY_d();
    void ADD_IY_rp_p();
    void ADC_A_IY_d();
    void SBC_A_IY_d();
    void AND_IY_d();
    void XOR_IY_d();
    void OR_IY_d();
    void BIT_y_IY_d();
    void RES_y_IY_d();
    void ADD_A_IY_d();
    void SET_y_IY_d();
    void INC_IY();
    void DEC_IY();
    void CP_IY_d();
    void SUB_IY_d();

    void INC_IYH();
    void INC_IYL();
    void DEC_IYH();
    void DEC_IYL();

    void ADD_A_IYH();
    void ADD_A_IYL();
    void ADC_A_IYH();
    void ADC_A_IYL();
    void SUB_IYH();
    void SUB_IYL();
    void SBC_IYH();
    void SBC_IYL();
    void AND_IYH();
    void AND_IYL();
    void XOR_IYH();
    void XOR_IYL();
    void OR_IYH();
    void OR_IYL();
    void CP_IYH();
    void CP_IYL();

    void POP_IY();
    void LD_mm_IY();
    void LD_IY_mm();
    void JP_IY();
    void PUSH_IY();

    void RLC_IY_d();
    void RRC_IY_d();
    void RL_IY_d();
    void RR_IY_d();
    void SLA_IY_d();
    void SRA_IY_d();
    void SLL_IY_d(); // undocumented
    void SRL_IY_d();

}
