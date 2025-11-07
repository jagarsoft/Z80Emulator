package com.github.jagarsoft;

public class Logger {
    public static void info(String s) {
        System.out.println(s);
    }

    public static void reg(String reg, int w) {
        System.out.println(reg + ": "+String.format("%04X", w) );
    }

    public static void reg(String reg, byte r) {
        System.out.println(reg + ": "+String.format("%02X", r) );
    }

    public static void tick(String s) {
        System.out.println(s);
    }

    public static void halt() {
        System.out.println("Halt instruction executed");
    }

    public static void halted(int tstate) {
        System.out.println("Halted: NOP executed. Tstate: " + tstate);
    }

    public static void write(String s) { System.out.println("str:"+s); }

    public static void read(String s) {
        System.out.println(s);
    }

    public static void compMem(String s) {System.out.println(s); }

    public static void intvalue(int data) {System.out.println("intvalue:"+String.format("%04X", data)); }
}
