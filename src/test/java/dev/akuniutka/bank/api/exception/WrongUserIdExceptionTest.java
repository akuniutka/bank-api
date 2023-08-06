package dev.akuniutka.bank.api.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrongUserIdExceptionTest {
    @Test
    void testWrongUserIdException() {
        String expected = "wronf user id error message for bank API";
        Exception exception = assertThrows(WrongUserIdException.class, () -> {
            throw new WrongUserIdException(expected);
        });
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }
}