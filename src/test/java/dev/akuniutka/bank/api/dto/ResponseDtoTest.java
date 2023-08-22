package dev.akuniutka.bank.api.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static dev.akuniutka.bank.api.util.ErrorMessage.*;
import static dev.akuniutka.bank.api.util.Amount.*;

class ResponseDtoTest {
    private static final String MESSAGE = "General Error Message";

    @Test
    void testResponseDtoWhenResultIsNotNull() {
        assertDoesNotThrow(() -> new ResponseDto(BigDecimal.ONE));
    }

    @Test
    void testResponseDtoWhenResultIsNull() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> new ResponseDto(null));
        assertEquals(RESULT_IS_NULL, e.getMessage());
    }

    @Test
    void testResponseDtoWhenResultIsNotNullAndMessageIsNotNull() {
        assertDoesNotThrow(() -> new ResponseDto(ONE, MESSAGE));
    }

    @Test
    void testResponseDtoWhenResultIsNotNullAndMessageIsNull() {
        assertDoesNotThrow(() -> new ResponseDto(ONE, null));
    }

    @Test
    void testResponseDtoWhenResultIsNullAndMessageIsNotNull() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> new ResponseDto(NULL, MESSAGE));
        assertEquals(RESULT_IS_NULL, e.getMessage());
    }

    @Test
    void testResponseDtoWhenResultIsNullAndMessageIsNull() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> new ResponseDto(NULL, null));
        assertEquals(RESULT_IS_NULL, e.getMessage());
    }

    @Test
    void testGetResult() {
        BigDecimal expected = ONE;
        ResponseDto response = new ResponseDto(expected, MESSAGE);
        assertEquals(expected, response.getResult());
    }

    @Test
    void testGetMessageWhenMessageIsNotNull() {
        String expected = MESSAGE;
        ResponseDto response = new ResponseDto(ONE, expected);
        assertEquals(expected, response.getMessage());
    }

    @Test
    void testGetMessageWhenMessageIsNull() {
        String expected = "";
        ResponseDto response = new ResponseDto(ONE, null);
        assertEquals(expected, response.getMessage());
    }
}