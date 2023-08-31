package dev.akuniutka.bank.api.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static dev.akuniutka.bank.api.util.ErrorMessage.AMOUNT_IS_NEGATIVE;

class WrongAmountExceptionTest {
    @Test
    void testWrongAmountException() {
        Exception e = assertThrows(WrongAmountException.class, () -> {
            throw new WrongAmountException(AMOUNT_IS_NEGATIVE);
        });
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
    }
}