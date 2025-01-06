; cd C:\Users\Usuario\GitHub\jagarsoft\Z80Emulator\src\main\resources
; pasmo kitt2.asm kitt2.hex symbols.kitt2.txt
; pasmo --tapbas kitt2.asm kitt2.tap symbols.kitt2.txt

    ORG $0
KEY EQU 0
    
L0: LD HL, $4000
L2: LD A, $80
L1: LD (HL), A

IF KEY        
K0: CALL KEYPRESS
    JR Z, K0
K1: CALL KEYPRESS
    JR NZ, K1
;K2: CALL KEYPRESS
;    JR Z, K2
ENDIF

    AND A
	RRA
    JR NZ, L1
    INC L
    LD A, L
	CP $20
    JR NZ, L2
	DEC L

L4: LD A, 1
L3: LD (HL), A

IF KEY
K0B: CALL KEYPRESS
    JR Z, K0B
K1B: CALL KEYPRESS
    JR NZ, K1B
;K2B: CALL KEYPRESS
;    JR Z, K2B
ENDIF

    AND A
	RLA
    JR NZ, L3
    DEC L
	LD A, L
	CP $FF
    JR NZ, L4
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