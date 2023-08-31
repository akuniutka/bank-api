package dev.akuniutka.bank.api.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static dev.akuniutka.bank.api.util.ErrorMessage.OPERATIONS_NOT_FOUND;

class OperationsNotFoundExceptionTest {
    @Test
    void testOperationsNotFoundException() {
        Exception e = assertThrows(OperationsNotFoundException.class, () -> {
            throw new OperationsNotFoundException(OPERATIONS_NOT_FOUND);
        });
        assertEquals(OPERATIONS_NOT_FOUND, e.getMessage());
    }
}