package dev.akuniutka.bank.api.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static dev.akuniutka.bank.api.util.ErrorMessage.USER_ID_IS_NULL;

class NullUserIdExceptionTest {
    @Test
    void testNullUserIdException() {
        Exception e = assertThrows(NullUserIdException.class, () -> {
            throw new NullUserIdException(USER_ID_IS_NULL);
        });
        assertEquals(USER_ID_IS_NULL, e.getMessage());
    }
}