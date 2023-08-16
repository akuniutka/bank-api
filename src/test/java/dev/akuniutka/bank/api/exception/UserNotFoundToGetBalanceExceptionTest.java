package dev.akuniutka.bank.api.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserNotFoundToGetBalanceExceptionTest {
    @Test
    void testUserNotFoundToGetBalanceException() {
        String expected = "wrong user id error message for bank API";
        Exception e = assertThrows(UserNotFoundToGetBalanceException.class, () -> {
            throw new UserNotFoundToGetBalanceException(expected);
        });
        assertEquals(expected, e.getMessage());
    }
}