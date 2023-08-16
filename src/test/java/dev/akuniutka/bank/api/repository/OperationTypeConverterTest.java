package dev.akuniutka.bank.api.repository;

import dev.akuniutka.bank.api.entity.OperationType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OperationTypeConverterTest {
    @Test
    void testConvertToDatabaseColumnWhenExistingValue() {
        OperationTypeConverter converter = new OperationTypeConverter();
        assertEquals("D", converter.convertToDatabaseColumn(OperationType.valueOf("DEPOSIT")));
    }

    @Test
    void testConvertToDatabaseColumnWhenNull() {
        OperationTypeConverter converter = new OperationTypeConverter();
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void testConvertToEntityAttributeWhenExistingValue() {
        OperationTypeConverter converter = new OperationTypeConverter();
        assertSame(OperationType.valueOf("DEPOSIT"), converter.convertToEntityAttribute("D"));
    }

    @Test
    void testConvertToEntityAttributeWhenNull() {
        OperationTypeConverter converter = new OperationTypeConverter();
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void testConvertToEntityAttributeWhenNonExistingValue() {
        OperationTypeConverter converter = new OperationTypeConverter();
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute(""));
    }
}