package com.github.jagarsoft.Z80Emulator.test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppTest {

    @Test
    void testSuma() {
        int resultado = 2 + 3;
        assertEquals(5, resultado, "La suma debería ser 5");
    }
}
