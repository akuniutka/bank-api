package dev.akuniutka.bank.api.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IllegalAmountExceptionTest {
    @Test
    void testIllegalAmountException() {
        String expected = "wrong amount error message for bank API";
        Exception e = assertThrows(IllegalAmountException.class, () -> {
            throw new IllegalAmountException(expected);
        });
        assertEquals(expected, e.getMessage());
    }
}