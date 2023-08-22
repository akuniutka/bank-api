package dev.akuniutka.bank.api.entity;

import dev.akuniutka.bank.api.exception.IllegalAmountException;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static dev.akuniutka.bank.api.util.ErrorMessage.*;
import static dev.akuniutka.bank.api.util.Amount.*;

class OperationTest {
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
    void testSetTypeWhenTypeIsNotNull() {
        Operation operation = new Operation();
        operation.setType(OperationType.DEPOSIT);
        assertEquals(OperationType.DEPOSIT, operation.getType());
    }

    @Test
    void testSetTypeWhenTypeIsNull() {
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
        Exception e = assertThrows(IllegalAmountException.class, () -> operation.setAmount(ONE_THOUSANDTH));
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
    }

    @Test
    void testSetAmountWhenAmountIsZero() {
        Operation operation = new Operation();
        Exception e = assertThrows(IllegalAmountException.class, () -> operation.setAmount(ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
    }

    @Test
    void testSetAmountWhenAmountIsNegative() {
        Operation operation = new Operation();
        Exception e = assertThrows(IllegalAmountException.class, () -> operation.setAmount(MINUS_TEN));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
    }

    @Test
    void testSetAmountWhenAmountIsNull() {
        Operation operation = new Operation();
        Exception e = assertThrows(IllegalAmountException.class, () -> operation.setAmount(NULL));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testGetDate() {
        Operation operation = new Operation();
        assertNull(operation.getDate());
    }

    @Test
    void testSetDateWhenDateIsNotNull() {
        Date expected = new Date();
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