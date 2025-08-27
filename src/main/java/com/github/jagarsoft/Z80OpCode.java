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
    void FDCB_prefix();

    void NOP();
    void EX_AF_AF_();
    void DJNZ();
    void JR();
    void JR_cc();

    void LD_rp_p_nn();
    void ADD_HL_rp_p();
    
    void LD_BC_A();
    void LD_DE_A();
    void LD_nn_HL();
    void LD_nn_A();

    void LD_A_BC();
    void LD_A_DE();
    void LD_HL_nn();
    void LD_A_nn();
    
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

    void JP_cc_y_nn();

    void JP_nn();
    void OUT_n_A();
    void IN_A_n();
    void EX_SP_HL();
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

    void RL_r_z();
/*      CBopCodes[0][0][3] = opC::RR_r_z;
        CBopCodes[0][0][4] = opC::SLA_r_z;
        CBopCodes[0][0][5] = opC::SRA_r_z;
        CBopCodes[0][0][6] = opC::SLL_r_z;*/

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
    //void RRD();
    //void RLD();

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
    void DEC_IX();
    void LD_r_y_IX_d();
    void ADD_IX_rp_p();
    void POP_IX();
    void JP_IX();
    void PUSH_IX();

    /* FD prefix */

    void LD_IY_nn();
    void LD_IY_d_r_z();
    void DEC_IY_d();
    void LD_IY_d_n();
    void LD_r_y_IY_d();
    void BIT_y_IY_d();
    void RES_y_IY_d();
    void ADD_A_IY_d();
    void SET_y_IY_d();

    void CP_IY_d();
    void SUB_IY_d();

    void POP_IY();
    void JP_IY();
    void PUSH_IY();
}
