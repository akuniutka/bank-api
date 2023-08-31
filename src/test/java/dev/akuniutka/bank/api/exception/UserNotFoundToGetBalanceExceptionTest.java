package dev.akuniutka.bank.api.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static dev.akuniutka.bank.api.util.ErrorMessage.USER_NOT_FOUND;

class UserNotFoundToGetBalanceExceptionTest {
    @Test
    void testUserNotFoundToGetBalanceException() {
        Exception e = assertThrows(UserNotFoundToGetBalanceException.class, () -> {
            throw new UserNotFoundToGetBalanceException(USER_NOT_FOUND);
        });
        assertEquals(USER_NOT_FOUND, e.getMessage());
    }
}