package dev.akuniutka.bank.api.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OperationTypeTest {
    @Test
    void testValueOf() {
        assertEquals(4, OperationType.values().length);
        assertDoesNotThrow(() -> OperationType.valueOf("DEPOSIT"));
        assertDoesNotThrow(() -> OperationType.valueOf("WITHDRAWAL"));
        assertDoesNotThrow(() -> OperationType.valueOf("OUTGOING_TRANSFER"));
        assertDoesNotThrow(() -> OperationType.valueOf("INCOMING_TRANSFER"));
    }

    @Test
    void testGetCode() {
        assertEquals("D", OperationType.valueOf("DEPOSIT").getCode());
        assertEquals("W", OperationType.valueOf("WITHDRAWAL").getCode());
        assertEquals("P", OperationType.valueOf("OUTGOING_TRANSFER").getCode());
        assertEquals("R", OperationType.valueOf("INCOMING_TRANSFER").getCode());
    }

    @Test
    void testGetDescription() {
        assertEquals("deposit", OperationType.valueOf("DEPOSIT").getDescription());
        assertEquals("withdrawal", OperationType.valueOf("WITHDRAWAL").getDescription());
        assertEquals("outgoing transfer", OperationType.valueOf("OUTGOING_TRANSFER").getDescription());
        assertEquals("incoming transfer", OperationType.valueOf("INCOMING_TRANSFER").getDescription());
    }
}