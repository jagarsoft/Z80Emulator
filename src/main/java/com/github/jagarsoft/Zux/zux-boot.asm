;java -Dfile.encoding=UTF-8 -classpath C:\Users\fjgarrido\Documents\Private\Repositorio\Github\jagasoft\Z80Emulator\target\classes com.github.jagarsoft.Disassembler boot.bin

; z88dk-z80asm -D__SDCC_IY -IC:/Users/fjgarrido/Documents/Private/z88dk/lib/config\..\..\\libsrc\_DEVELOPMENT\target\z80 -IC:\Users\FJGARR~1AppData\Local\Temp\zcc000032CC352C3 -D__SDCC -s -v -mz80 -IC:/Users/fjgarrido/Documents/Private/z88dk/lib/config\..\..\/lib -IC:\Users\fjgarrido\Documents\Private\Repositorio\Github\jagasoft\Z80Emulator\src\main\java\com\github\jagarsoft\Zux C:\Users\FJGARR~1\AppData\Local\Temp\zcc000032CC352C2.asm

; z88dk-z80asm -s -v -mz80 zux-boot.asm            
            
            
            
            ORG 0
            DI
            LD A, 'Z'
            RST $08
            HALT

            ORG $08
            OUT ($AA), A
            RET