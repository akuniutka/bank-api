package dev.akuniutka.bank.api.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BadRequestExceptionTest {
    @Test
    void testBadRequestException() {
        String expected = "wrong amount error message for bank API";
        Exception e = assertThrows(BadRequestException.class, () -> {
            throw new BadRequestException(expected);
        });
        assertEquals(expected, e.getMessage());
    }
}