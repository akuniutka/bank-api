package dev.akuniutka.bank.api.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrongAmountExceptionTest {
    @Test
    void testWrongAmountException() {
        String expected = "wrong amount error message for bank API";
        Exception exception = assertThrows(WrongAmountException.class, () -> {
            throw new WrongAmountException(expected);
        });
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }
}