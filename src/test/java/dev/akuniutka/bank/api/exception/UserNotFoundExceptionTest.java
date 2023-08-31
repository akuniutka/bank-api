package dev.akuniutka.bank.api.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static dev.akuniutka.bank.api.util.ErrorMessage.USER_NOT_FOUND;

class UserNotFoundExceptionTest {
    @Test
    void testUserNotFoundException() {
        Exception e = assertThrows(UserNotFoundException.class, () -> {
            throw new UserNotFoundException(USER_NOT_FOUND);
        });
        assertEquals(USER_NOT_FOUND, e.getMessage());
    }
}