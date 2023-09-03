package dev.akuniutka.bank.api.entity;

import dev.akuniutka.bank.api.exception.WrongAmountException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static dev.akuniutka.bank.api.util.ErrorMessage.*;
import static dev.akuniutka.bank.api.util.Amount.*;

class OperationTest {
    @Test
    void testOperationWhenNoArgs() {
        assertDoesNotThrow(() -> new Operation());
    }

    @Test
    void testOperationWhenAccountIsNull() {
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> new Operation(null, OperationType.DEPOSIT, TEN, OffsetDateTime.now())
        );
        assertEquals(ACCOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testOperationWhenOperationTypeIsNull() {
        Account account = mock(Account.class);
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> new Operation(account, null, TEN, OffsetDateTime.now())
        );
        assertEquals(OPERATION_TYPE_IS_NULL, e.getMessage());
        verifyNoMoreInteractions(ignoreStubs(account));
    }

    @Test
    void testOperationWhenAmountIsNull() {
        Account account = mock(Account.class);
        Exception e = assertThrows(WrongAmountException.class,
                () -> new Operation(account, OperationType.DEPOSIT, null, OffsetDateTime.now())
        );
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verifyNoMoreInteractions(ignoreStubs(account));
    }

    @Test
    void testOperationWhenAmountIsNegative() {
        Account account = mock(Account.class);
        Exception e = assertThrows(WrongAmountException.class,
                () -> new Operation(account, OperationType.DEPOSIT, MINUS_TEN, OffsetDateTime.now())
        );
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verifyNoMoreInteractions(ignoreStubs(account));
    }

    @Test
    void testOperationWhenAmountIsZero() {
        Account account = mock(Account.class);
        Exception e = assertThrows(WrongAmountException.class,
                () -> new Operation(account, OperationType.DEPOSIT, ZERO, OffsetDateTime.now())
        );
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verifyNoMoreInteractions(ignoreStubs(account));
    }

    @Test
    void testOperationWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        Account account = mock(Account.class);
        Exception e = assertThrows(WrongAmountException.class,
                () -> new Operation(account, OperationType.DEPOSIT, ONE_THOUSANDTH, OffsetDateTime.now())
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verifyNoMoreInteractions(ignoreStubs(account));
    }

    @Test
    void testOperationWhenDateIsNull() {
        Account account = mock(Account.class);
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> new Operation(account, OperationType.DEPOSIT, TEN, null)
        );
        assertEquals(DATE_IS_NULL, e.getMessage());
        verifyNoMoreInteractions(ignoreStubs(account));
    }

    @Test
    void testOperationWhenAmountIsPositive() {
        OffsetDateTime date = OffsetDateTime.now();
        Account account = mock(Account.class);
        Operation operation = new Operation(account, OperationType.DEPOSIT, TEN, date);
        assertNotNull(operation);
        assertEquals(account, operation.getAccount());
        assertEquals(OperationType.DEPOSIT, operation.getType());
        assertEquals(FORMATTED_TEN, operation.getAmount());
        assertTrue(date.isEqual(operation.getDate()));
        verifyNoMoreInteractions(ignoreStubs(account));
    }

    @Test
    void testOperationWhenScaleIsGreaterThanTwoButWithZeros() {
        OffsetDateTime date = OffsetDateTime.now();
        Account account = mock(Account.class);
        Operation operation = new Operation(account, OperationType.DEPOSIT, TEN_THOUSANDTHS, date);
        assertNotNull(operation);
        assertEquals(account, operation.getAccount());
        assertEquals(OperationType.DEPOSIT, operation.getType());
        assertEquals(FORMATTED_TEN_THOUSANDTHS, operation.getAmount());
        assertTrue(date.isEqual(operation.getDate()));
        verifyNoMoreInteractions(ignoreStubs(account));
    }

    @Test
    void testGetId() {
        Operation operation = new Operation();
        assertNull(operation.getId());
    }

    @Test
    void testGetAccount() {
        Operation operation = new Operation();
        assertNull(operation.getAccount());
    }

    @Test
    void testSetAccountWhenAccountIsNotNull() {
        Account expected = new Account();
        Operation operation = new Operation();
        operation.setAccount(expected);
        assertEquals(expected, operation.getAccount());
    }

    @Test
    void testSetAccountWhenAccountIsNull() {
        Operation operation = new Operation();
        Exception e = assertThrows(IllegalArgumentException.class, () -> operation.setAccount(null));
        assertEquals(ACCOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testGetType() {
        Operation operation = new Operation();
        assertNull(operation.getType());
    }

    @Test
    void testSetTypeWhenOperationTypeIsNotNull() {
        Operation operation = new Operation();
        operation.setType(OperationType.DEPOSIT);
        assertEquals(OperationType.DEPOSIT, operation.getType());
    }

    @Test
    void testSetTypeWhenOperationTypeIsNull() {
        Operation operation = new Operation();
        Exception e = assertThrows(IllegalArgumentException.class, () -> operation.setType(null));
        assertEquals(OPERATION_TYPE_IS_NULL, e.getMessage());
    }

    @Test
    void testGetAmount() {
        Operation operation = new Operation();
        assertNull(operation.getAmount());
    }

    @Test
    void testSetAmountWhenAmountIsPositive() {
        Operation operation = new Operation();
        operation.setAmount(TEN);
        assertEquals(FORMATTED_TEN, operation.getAmount());
    }

    @Test
    void testSetAmountWhenScaleIsGreaterThanTwoButWithZeros() {
        Operation operation = new Operation();
        operation.setAmount(TEN_THOUSANDTHS);
        assertEquals(FORMATTED_TEN_THOUSANDTHS, operation.getAmount());
    }

    @Test
    void testSetAmountWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        Operation operation = new Operation();
        Exception e = assertThrows(WrongAmountException.class, () -> operation.setAmount(ONE_THOUSANDTH));
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
    }

    @Test
    void testSetAmountWhenAmountIsZero() {
        Operation operation = new Operation();
        Exception e = assertThrows(WrongAmountException.class, () -> operation.setAmount(ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
    }

    @Test
    void testSetAmountWhenAmountIsNegative() {
        Operation operation = new Operation();
        Exception e = assertThrows(WrongAmountException.class, () -> operation.setAmount(MINUS_TEN));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
    }

    @Test
    void testSetAmountWhenAmountIsNull() {
        Operation operation = new Operation();
        Exception e = assertThrows(WrongAmountException.class, () -> operation.setAmount(null));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testGetDate() {
        Operation operation = new Operation();
        assertNull(operation.getDate());
    }

    @Test
    void testSetDateWhenDateIsNotNull() {
        OffsetDateTime expected = OffsetDateTime.now();
        Operation operation = new Operation();
        operation.setDate(expected);
        assertEquals(expected, operation.getDate());
    }

    @Test
    void testSetDateWhenDateIsNull() {
        Operation operation = new Operation();
        Exception e = assertThrows(IllegalArgumentException.class, () -> operation.setDate(null));
        assertEquals(DATE_IS_NULL, e.getMessage());
    }
}