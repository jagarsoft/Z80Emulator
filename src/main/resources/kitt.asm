; cd C:\Users\Usuario\GitHub\jagarsoft\Z80Emulator\src\main\resources
; pasmo kitt.asm kitt.hex symbols.kitt.txt
; pasmo --tapbas kitt.asm kitt.tap symbols.kitt.txt

    ORG $8000
KEY EQU 0
    
L0: LD HL, $4000
    LD A, $80
    LD B, $1F
L2: LD C, B
    LD B, 8
L1: LD (HL), A

IF KEY        
K0: CALL KEYPRESS
    JR Z, K0
K1: CALL KEYPRESS
    JR NZ, K1
;K2: CALL KEYPRESS
;    JR Z, K2
ENDIF

    RRA
    DJNZ L1
    INC L
    RRA
    LD B, C
    DJNZ L2
    
    LD B, L
    INC B
L4: LD C, B
    LD B, 8
L3: LD (HL), A

IF KEY
K0B: CALL KEYPRESS
    JR Z, K0B
K1B: CALL KEYPRESS
    JR NZ, K1B
;K2B: CALL KEYPRESS
;    JR Z, K2B
ENDIF

    RLA
    DJNZ L3
    RLA
    DEC L
    LD B, C
    DJNZ L4
    JP L0
    HALT

IF KEY
KEYPRESS:
    PUSH AF
    XOR A
    IN A, ($FE)    ; If key pressed, bit reset
    CPL
    AND $1F
    POP DE
    LD A, D
    RET
ENDIF

    END L0