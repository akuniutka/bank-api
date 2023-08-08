package dev.akuniutka.bank.api.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GetBalanceExceptionTest {
    @Test
    void testGetBalanceException() {
        String expected = "wrong user id error message for bank API";
        Exception exception = assertThrows(GetBalanceException.class, () -> {
            throw new GetBalanceException(expected);
        });
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }
}