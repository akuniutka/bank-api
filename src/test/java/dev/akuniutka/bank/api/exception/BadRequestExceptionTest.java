package dev.akuniutka.bank.api.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static dev.akuniutka.bank.api.util.ErrorMessage.AMOUNT_IS_NULL;

class BadRequestExceptionTest {
    @Test
    void testBadRequestException() {
        Exception e = assertThrows(BadRequestException.class, () -> {
            throw new BadRequestException(AMOUNT_IS_NULL);
        });
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
    }
}