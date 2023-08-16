package dev.akuniutka.bank.api.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BackendErrorExceptionTest {
    @Test
    void testBackendErrorException() {
        String expected = "wrong amount error message for bank API";
        Exception e = assertThrows(BackendErrorException.class, () -> {
            throw new BackendErrorException(expected);
        });
        assertEquals(expected, e.getMessage());
    }
}