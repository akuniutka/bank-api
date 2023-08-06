package dev.akuniutka.bank.api.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InsufficientFundsExceptionTest {
    @Test
    void testInsufficientFundsException() {
        String expected = "insufficient funds error message for bank API";
        Exception exception = assertThrows(InsufficientFundsException.class, () -> {
            throw new InsufficientFundsException(expected);
        });
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }
}