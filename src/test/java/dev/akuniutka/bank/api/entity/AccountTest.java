package dev.akuniutka.bank.api.entity;

import dev.akuniutka.bank.api.exception.CashOrderException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {
    private static final String AMOUNT_IS_NULL = "amount is null";
    private static final String AMOUNT_IS_NOT_POSITIVE = "amount is not positive";
    private static final String WRONG_MINOR_UNITS = "wrong minor units";
    private static final String INSUFFICIENT_BALANCE = "insufficient balance";

    @Test
    void testAccount() {
        assertDoesNotThrow(Account::new);
    }

    @Test
    void testGetId() {
        Account account = new Account();
        assertNull(account.getId());
    }

    @Test
    void testGetBalance() {
        BigDecimal expected = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        Account account = new Account();
        assertEquals(expected, account.getBalance());
    }

    @Test
    void testIncreaseBalanceWhenAmountIsPositive() {
        BigDecimal amount = BigDecimal.TEN;
        BigDecimal expected = amount.setScale(2, RoundingMode.HALF_UP);
        Account account = new Account();
        account.increaseBalance(amount);
        assertEquals(expected, account.getBalance());
    }

    @Test
    void testIncreaseBalanceWhenScaleIsGreaterThanTwoButWithZeros() {
        BigDecimal amount = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        BigDecimal expected = amount.setScale(2, RoundingMode.HALF_UP);
        Account account = new Account();
        account.increaseBalance(amount);
        assertEquals(expected, account.getBalance());
    }

    @Test
    void testIncreaseBalanceWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        BigDecimal amount = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        Account account = new Account();
        Exception exception = assertThrows(CashOrderException.class, () -> account.increaseBalance(amount));
        assertEquals(WRONG_MINOR_UNITS, exception.getMessage());
    }

    @Test
    void testIncreaseBalanceWhenAmountIsZero() {
        BigDecimal amount = BigDecimal.ZERO;
        Account account = new Account();
        Exception exception = assertThrows(CashOrderException.class, () -> account.increaseBalance(amount));
        assertEquals(AMOUNT_IS_NOT_POSITIVE, exception.getMessage());
    }

    @Test
    void testIncreaseBalanceWhenAmountIsNegative() {
        BigDecimal amount = BigDecimal.TEN.negate();
        Account account = new Account();
        Exception exception = assertThrows(CashOrderException.class, () -> account.increaseBalance(amount));
        assertEquals(AMOUNT_IS_NOT_POSITIVE, exception.getMessage());
    }

    @Test
    void testIncreaseBalanceWhenAmountIsNull() {
        Account account = new Account();
        Exception exception = assertThrows(CashOrderException.class, () -> account.increaseBalance(null));
        assertEquals(AMOUNT_IS_NULL, exception.getMessage());
    }

    @Test
    void testDecreaseBalanceWhenAmountIsLessThanBalance() {
        BigDecimal initialBalance = BigDecimal.TEN;
        BigDecimal amountWithdrawn = BigDecimal.ONE;
        BigDecimal expected = initialBalance.subtract(amountWithdrawn).setScale(2, RoundingMode.HALF_UP);
        Account account = new Account();
        account.increaseBalance(initialBalance);
        account.decreaseBalance(amountWithdrawn);
        assertEquals(expected, account.getBalance());
    }

    @Test
    void testDecreaseBalanceWhenAmountIsEqualToBalance() {
        BigDecimal initialBalance = BigDecimal.TEN;
        BigDecimal amountWithdrawn = BigDecimal.TEN;
        BigDecimal expected = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        Account account = new Account();
        account.increaseBalance(initialBalance);
        account.decreaseBalance(amountWithdrawn);
        assertEquals(expected, account.getBalance());
    }

    @Test
    void testDecreaseBalanceWhenScaleIsGreaterThanTwoButWithZeros() {
        BigDecimal initialBalance = BigDecimal.TEN;
        BigDecimal amountWithdrawn = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        BigDecimal expected = initialBalance.subtract(amountWithdrawn).setScale(2, RoundingMode.HALF_UP);
        Account account = new Account();
        account.increaseBalance(initialBalance);
        account.decreaseBalance(amountWithdrawn);
        assertEquals(expected, account.getBalance());
    }

    @Test
    void testDecreaseBalanceWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        BigDecimal initialBalance = BigDecimal.TEN;
        BigDecimal amountWithdrawn = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        Account account = new Account();
        account.increaseBalance(initialBalance);
        Exception exception = assertThrows(CashOrderException.class, () -> account.decreaseBalance(amountWithdrawn));
        assertEquals(WRONG_MINOR_UNITS, exception.getMessage());
    }

    @Test
    void testDecreaseBalanceWhenAmountIsGreaterThanBalance() {
        BigDecimal amountWithdrawn = BigDecimal.ONE;
        Account account = new Account();
        Exception exception = assertThrows(CashOrderException.class, () -> account.decreaseBalance(amountWithdrawn));
        assertEquals(INSUFFICIENT_BALANCE, exception.getMessage());
    }

    @Test
    void testDecreaseBalanceWhenAmountIsZero() {
        BigDecimal amountWithdrawn = BigDecimal.ZERO;
        Account account = new Account();
        Exception exception = assertThrows(CashOrderException.class, () -> account.decreaseBalance(amountWithdrawn));
        assertEquals(AMOUNT_IS_NOT_POSITIVE, exception.getMessage());
    }

    @Test
    void testDecreaseBalanceWhenAmountIsNegative() {
        BigDecimal initialBalance = BigDecimal.TEN;
        BigDecimal amountWithdrawn = BigDecimal.ONE.negate();
        Account account = new Account();
        account.increaseBalance(initialBalance);
        Exception exception = assertThrows(CashOrderException.class, () -> account.decreaseBalance(amountWithdrawn));
        assertEquals(AMOUNT_IS_NOT_POSITIVE, exception.getMessage());
    }

    @Test
    void testDecreaseBalanceWhenAmountIsNull() {
        Account account = new Account();
        Exception exception = assertThrows(CashOrderException.class, () -> account.decreaseBalance(null));
        assertEquals(AMOUNT_IS_NULL, exception.getMessage());
    }
}