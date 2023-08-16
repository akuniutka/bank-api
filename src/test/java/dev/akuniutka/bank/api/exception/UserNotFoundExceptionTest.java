package dev.akuniutka.bank.api.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserNotFoundExceptionTest {
    @Test
    void testUserNotFoundException() {
        String expected = "wrong user id error message for bank API";
        Exception e = assertThrows(UserNotFoundException.class, () -> {
            throw new UserNotFoundException(expected);
        });
        assertEquals(expected, e.getMessage());
    }
}