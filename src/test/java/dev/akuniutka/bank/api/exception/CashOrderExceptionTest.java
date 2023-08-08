package dev.akuniutka.bank.api.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CashOrderExceptionTest {
    @Test
    void testCashOrderException() {
        String expected = "wrong amount error message for bank API";
        Exception exception = assertThrows(CashOrderException.class, () -> {
            throw new CashOrderException(expected);
        });
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }
}