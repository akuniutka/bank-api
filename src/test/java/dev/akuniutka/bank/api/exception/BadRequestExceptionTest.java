package dev.akuniutka.bank.api.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BadRequestExceptionTest {
    @Test
    void testBadRequestException() {
        String expected = "wrong amount error message for bank API";
        Exception exception = assertThrows(BadRequestException.class, () -> {
            throw new BadRequestException(expected);
        });
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }
}