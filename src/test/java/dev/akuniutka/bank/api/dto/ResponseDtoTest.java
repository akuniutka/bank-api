package dev.akuniutka.bank.api.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ResponseDtoTest {
    private static final String RESULT_IS_NULL = "result is null";
    private static final String MESSAGE = "General Error Message";

    @Test
    void testResponseDtoWhenResultIsNotNull() {
        assertDoesNotThrow(() -> new ResponseDto(BigDecimal.ONE));
    }

    @Test
    void testResponseDtoWhenResultIsNull() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> new ResponseDto(null)
        );
        assertEquals(RESULT_IS_NULL, exception.getMessage());
    }

    @Test
    void testResponseDtoWhenResultIsNotNullAndMessageIsNotNull() {
        assertDoesNotThrow(() -> new ResponseDto(BigDecimal.ONE, MESSAGE));
    }

    @Test
    void testResponseDtoWhenResultIsNotNullAndMessageIsNull() {
        assertDoesNotThrow(() -> new ResponseDto(BigDecimal.ONE, null));
    }

    @Test
    void testResponseDtoWhenResultIsNullAndMessageIsNotNull() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> new ResponseDto(null, MESSAGE)
        );
        assertEquals(RESULT_IS_NULL, exception.getMessage());
    }

    @Test
    void testResponseDtoWhenResultIsNullAndMessageIsNull() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> new ResponseDto(null, null)
        );
        assertEquals(RESULT_IS_NULL, exception.getMessage());
    }

    @Test
    void testGetResult() {
        BigDecimal expected = BigDecimal.ONE;
        ResponseDto response = new ResponseDto(expected, MESSAGE);
        assertEquals(expected, response.getResult());
    }

    @Test
    void testGetMessageWhenMessageIsNotNull() {
        String expected = MESSAGE;
        ResponseDto response = new ResponseDto(BigDecimal.ONE, expected);
        assertEquals(expected, response.getMessage());
    }

    @Test
    void testGetMessageWhenMessageIsNull() {
        String expected = "";
        ResponseDto response = new ResponseDto(BigDecimal.ONE, null);
        assertEquals(expected, response.getMessage());
    }
}