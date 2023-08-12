package dev.akuniutka.bank.api.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserNotFoundExceptionTest {
    @Test
    void testUserNotFoundException() {
        String expected = "wrong user id error message for bank API";
        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            throw new UserNotFoundException(expected);
        });
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }
}