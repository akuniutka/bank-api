package dev.akuniutka.bank.api.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OperationTypeTest {
    @Test
    void testValueOf() {
        assertEquals(2, OperationType.values().length);
        assertDoesNotThrow(() -> OperationType.valueOf("DEPOSIT"));
        assertDoesNotThrow(() -> OperationType.valueOf("WITHDRAWAL"));
    }

    @Test
    void testGetCode() {
        assertEquals("D", OperationType.valueOf("DEPOSIT").getCode());
        assertEquals("W", OperationType.valueOf("WITHDRAWAL").getCode());
    }
}