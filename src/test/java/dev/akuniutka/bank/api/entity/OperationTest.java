package dev.akuniutka.bank.api.entity;

import dev.akuniutka.bank.api.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class OperationTest {
    private static final String ACCOUNT_IS_NULL = "account for operation is null";
    private static final String OPERATION_TYPE_IS_NULL = "type of operation is null";
    private static final String AMOUNT_IS_NULL = "amount is null";
    private static final String AMOUNT_IS_ZERO = "amount is zero";
    private static final String AMOUNT_IS_NEGATIVE = "amount is negative";
    private static final String WRONG_MINOR_UNITS = "wrong minor units";
    private static final String DATE_IS_NULL = "date is null";

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
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> operation.setAccount(null)
        );
        String actual = exception.getMessage();
        assertEquals(ACCOUNT_IS_NULL, actual);
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
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> operation.setType(null)
        );
        String actual = exception.getMessage();
        assertEquals(OPERATION_TYPE_IS_NULL, actual);
    }

    @Test
    void testGetAmount() {
        Operation operation = new Operation();
        assertNull(operation.getAmount());
    }

    @Test
    void testSetAmountWhenAmountIsPositive() {
        BigDecimal expected = BigDecimal.TEN;
        Operation operation = new Operation();
        operation.setAmount(expected);
        expected = expected.setScale(2, RoundingMode.HALF_UP);
        assertEquals(expected, operation.getAmount());
    }

    @Test
    void testSetAmountWhenScaleIsGreaterThanTwoButWithZeros() {
        BigDecimal expected = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        Operation operation = new Operation();
        operation.setAmount(expected);
        expected = expected.setScale(2, RoundingMode.HALF_UP);
        assertEquals(expected, operation.getAmount());
    }

    @Test
    void testSetAmountWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        BigDecimal expected = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        Operation operation = new Operation();
        Exception exception = assertThrows(BadRequestException.class,
                () -> operation.setAmount(expected)
        );
        String actual = exception.getMessage();
        assertEquals(WRONG_MINOR_UNITS, actual);
    }

    @Test
    void testSetAmountWhenAmountIsZero() {
        BigDecimal expected = BigDecimal.ZERO;
        Operation operation = new Operation();
        Exception exception = assertThrows(BadRequestException.class,
                () -> operation.setAmount(expected)
        );
        String actual = exception.getMessage();
        assertEquals(AMOUNT_IS_ZERO, actual);
    }

    @Test
    void testSetAmountWhenAmountIsNegative() {
        BigDecimal expected = BigDecimal.TEN.negate();
        Operation operation = new Operation();
        Exception exception = assertThrows(BadRequestException.class,
                () -> operation.setAmount(expected)
        );
        String actual = exception.getMessage();
        assertEquals(AMOUNT_IS_NEGATIVE, actual);
    }

    @Test
    void testSetAmountWhenAmountIsNull() {
        Operation operation = new Operation();
        Exception exception = assertThrows(BadRequestException.class,
                () -> operation.setAmount(null)
        );
        String actual = exception.getMessage();
        assertEquals(AMOUNT_IS_NULL, actual);
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
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> operation.setDate(null)
        );
        String actual = exception.getMessage();
        assertEquals(DATE_IS_NULL, actual);
    }
}