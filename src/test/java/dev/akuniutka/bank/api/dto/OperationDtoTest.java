package dev.akuniutka.bank.api.dto;

import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.OperationType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static dev.akuniutka.bank.api.util.ErrorMessage.*;
import static dev.akuniutka.bank.api.util.Amount.*;

class OperationDtoTest {
    private Operation operation;

    @BeforeEach
    public void setUp() {
        operation = mock(Operation.class);
    }

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(ignoreStubs(operation));
    }

    @Test
    void testOperationDtoWhenOperationIsNull() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> new OperationDto(null));
        assertEquals(OPERATION_IS_NULL, e.getMessage());
    }

    @Test
    void testOperationDtoWhenOperationIsNotNull() {
        OffsetDateTime date = OffsetDateTime.now();
        when(operation.getType()).thenReturn(OperationType.DEPOSIT);
        when(operation.getAmount()).thenReturn(FORMATTED_TEN);
        when(operation.getDate()).thenReturn(date);
        OperationDto dto = new OperationDto(operation);
        assertNotNull(dto);
        assertEquals(OperationType.DEPOSIT.getDescription(), dto.getType());
        assertEquals(FORMATTED_TEN, dto.getAmount());
        assertTrue(date.isEqual(dto.getDate()));
        verify(operation).getType();
        verify(operation).getAmount();
        verify(operation).getDate();
    }

    @Test
    void testGetDate() {
        OffsetDateTime date = OffsetDateTime.now();
        when(operation.getType()).thenReturn(OperationType.DEPOSIT);
        when(operation.getAmount()).thenReturn(FORMATTED_TEN);
        when(operation.getDate()).thenReturn(date);
        OperationDto dto = new OperationDto(operation);
        assertTrue(date.isEqual(dto.getDate()));
        verify(operation).getType();
        verify(operation).getAmount();
        verify(operation).getDate();
    }

    @Test
    void testGetType() {
        OffsetDateTime date = OffsetDateTime.now();
        when(operation.getType()).thenReturn(OperationType.DEPOSIT);
        when(operation.getAmount()).thenReturn(FORMATTED_TEN);
        when(operation.getDate()).thenReturn(date);
        OperationDto dto = new OperationDto(operation);
        assertEquals(OperationType.DEPOSIT.getDescription(), dto.getType());
        verify(operation).getType();
        verify(operation).getAmount();
        verify(operation).getDate();
    }

    @Test
    void testGetAmount() {
        OffsetDateTime date = OffsetDateTime.now();
        when(operation.getType()).thenReturn(OperationType.DEPOSIT);
        when(operation.getAmount()).thenReturn(FORMATTED_TEN);
        when(operation.getDate()).thenReturn(date);
        OperationDto dto = new OperationDto(operation);
        assertEquals(FORMATTED_TEN, dto.getAmount());
        verify(operation).getType();
        verify(operation).getAmount();
        verify(operation).getDate();
    }
}