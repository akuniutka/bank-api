package dev.akuniutka.bank.api.dto;

import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.OperationType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;
import static dev.akuniutka.bank.api.entity.ErrorMessage.*;
import static dev.akuniutka.bank.api.Amount.*;

class OperationDtoTest {
    private static final Calendar CALENDAR = Calendar.getInstance();
    private static final Operation OPERATION = new Operation();

    @BeforeAll
    static void init() {
        CALENDAR.clear();
        CALENDAR.set(2023, Calendar.JANUARY, 1);
        OPERATION.setType(OperationType.DEPOSIT);
        OPERATION.setAmount(TEN);
        OPERATION.setDate(CALENDAR.getTime());
    }

    @Test
    void testOperationDtoWhenOperationIsNull() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> new OperationDto(null));
        assertEquals(OPERATION_IS_NULL, e.getMessage());
    }

    @Test
    void testOperationDtoWhenDateIsNull() {
        Operation operation = new Operation();
        operation.setType(OperationType.DEPOSIT);
        operation.setAmount(TEN);
        Exception e = assertThrows(IllegalArgumentException.class, () -> new OperationDto(operation));
        assertEquals(DATE_IS_NULL, e.getMessage());
    }

    @Test
    void testOperationDtoWhenTypeIsNull() {
        Operation operation = new Operation();
        operation.setDate(CALENDAR.getTime());
        operation.setAmount(TEN);
        Exception e = assertThrows(IllegalArgumentException.class, () -> new OperationDto(operation));
        assertEquals(OPERATION_TYPE_IS_NULL, e.getMessage());
    }

    @Test
    void testOperationDtoWhenAmountIsNull() {
        Operation operation = new Operation();
        operation.setDate(CALENDAR.getTime());
        operation.setType(OperationType.DEPOSIT);
        Exception e = assertThrows(IllegalArgumentException.class, () -> new OperationDto(operation));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testOperationDtoWhenOperationNotNullAndDateNotNullAndTypeNotNullAndAmountNotNull() {
        assertDoesNotThrow(() -> new OperationDto(OPERATION));
    }

    @Test
    void testGetDate() {
        OperationDto dto = new OperationDto(OPERATION);
        assertEquals(OPERATION.getDate(), dto.getDate());
    }

    @Test
    void testGetType() {
        OperationDto dto = new OperationDto(OPERATION);
        assertEquals(OperationType.DEPOSIT.getDescription(), dto.getType());
    }

    @Test
    void testGetAmount() {
        OperationDto dto = new OperationDto(OPERATION);
        assertEquals(FORMATTED_TEN, dto.getAmount());
    }
}