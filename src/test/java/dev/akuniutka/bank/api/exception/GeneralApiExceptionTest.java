package dev.akuniutka.bank.api.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeneralApiExceptionTest {
    @Test
    void testGeneralApiException() {
        String expected = "custom error message for bank API";
        Exception exception = assertThrows(GeneralApiException.class, () -> {
            throw new GeneralApiException(expected);
        });
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }
}