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

    void ADD_A();
    void ADC_A();
    void SUB();
    void SBC_A();
    void AND();
    void XOR();
    void OR();
    void CP();

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
}
