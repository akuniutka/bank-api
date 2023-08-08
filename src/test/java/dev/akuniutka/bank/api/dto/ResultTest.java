package dev.akuniutka.bank.api.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {
    private static final BigDecimal CODE = BigDecimal.ONE;
    private static final String MESSAGE = "General Error Message";
    private static final String CODE_IS_NULL = "code is null";

    @Test
    void testResultWhenCodeIsNotNullAndMessageIsNotNull() {
        assertDoesNotThrow(() -> new Result(CODE, MESSAGE));
    }

    @Test
    void testResultWhenCodeIsNotNullAndMessageIsNull() {
        assertDoesNotThrow(() -> new Result(CODE, null));
    }

    @Test
    void testResultWhenCodeIsNullAndMessageIsNotNull() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new Result(null, MESSAGE));
        assertEquals(CODE_IS_NULL, exception.getMessage());
    }

    @Test
    void testResultWhenCodeIsNullAndMessageIsNull() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new Result(null, null));
        assertEquals(CODE_IS_NULL, exception.getMessage());
    }

    @Test
    void testGetCode() {
        Result result = new Result(CODE, MESSAGE);
        assertEquals(CODE, result.getCode());
    }

    @Test
    void testGetMessage() {
        Result result = new Result(CODE, MESSAGE);
        assertEquals(MESSAGE, result.getMessage());
    }

    @Test
    void testEquals() {
        Result resultA = new Result(CODE, MESSAGE);
        Result resultB = new Result(CODE, MESSAGE);
        assertEquals(resultA, resultB);
    }

    @Test
    void testHashCode() {
        Result resultA = new Result(CODE, MESSAGE);
        Result resultB = new Result(CODE, MESSAGE);
        assertEquals(resultA.hashCode(), resultB.hashCode());
    }
}