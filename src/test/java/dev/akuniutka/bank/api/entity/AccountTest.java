package dev.akuniutka.bank.api.entity;

import dev.akuniutka.bank.api.exception.IllegalAmountException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static dev.akuniutka.bank.api.Amount.*;
import static dev.akuniutka.bank.api.entity.ErrorMessage.*;

class AccountTest {
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
    void testSetBalanceWhenAmountIsPositive() {
        Account account = new Account();
        account.setBalance(TEN);
        assertEquals(FORMATTED_TEN, account.getBalance());
    }

    @Test
    void testSetBalanceWhenAmountIsZero() {
        Account account = new Account();
        account.setBalance(TEN);
        account.setBalance(ZERO);
        assertEquals(FORMATTED_ZERO, account.getBalance());
    }

    @Test
    void testSetBalanceWhenScaleIsGreaterThanTwoButWithZeros() {
        Account account = new Account();
        account.setBalance(TEN_THOUSANDTHS);
        assertEquals(FORMATTED_TEN_THOUSANDTHS, account.getBalance());
    }

    @Test
    void testSetBalanceWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        Account account = new Account();
        Exception e = assertThrows(IllegalAmountException.class, () -> account.setBalance(ONE_THOUSANDTH));
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
    }

    @Test
    void testSetBalanceWhenAmountIsNegative() {
        Account account = new Account();
        Exception e = assertThrows(IllegalAmountException.class, () -> account.setBalance(MINUS_TEN));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
    }

    @Test
    void testSetBalanceWhenAmountIsNull() {
        Account account = new Account();
        Exception e = assertThrows(IllegalAmountException.class, () -> account.setBalance(NULL));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testGetBalance() {
        Account account = new Account();
        assertEquals(FORMATTED_ZERO, account.getBalance());
    }

    @Test
    void testIncreaseBalanceWhenAmountIsPositive() {
        Account account = new Account();
        account.increaseBalance(TEN);
        assertEquals(FORMATTED_TEN, account.getBalance());
    }

    @Test
    void testIncreaseBalanceWhenScaleIsGreaterThanTwoButWithZeros() {
        Account account = new Account();
        account.increaseBalance(TEN_THOUSANDTHS);
        assertEquals(FORMATTED_TEN_THOUSANDTHS, account.getBalance());
    }

    @Test
    void testIncreaseBalanceWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        Account account = new Account();
        Exception e = assertThrows(IllegalAmountException.class, () -> account.increaseBalance(ONE_THOUSANDTH));
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
    }

    @Test
    void testIncreaseBalanceWhenAmountIsZero() {
        Account account = new Account();
        Exception e = assertThrows(IllegalAmountException.class, () -> account.increaseBalance(ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
    }

    @Test
    void testIncreaseBalanceWhenAmountIsNegative() {
        Account account = new Account();
        Exception e = assertThrows(IllegalAmountException.class, () -> account.increaseBalance(MINUS_TEN));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
    }

    @Test
    void testIncreaseBalanceWhenAmountIsNull() {
        Account account = new Account();
        Exception e = assertThrows(IllegalAmountException.class, () -> account.increaseBalance(NULL));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testDecreaseBalanceWhenAmountIsLessThanBalance() {
        Account account = new Account();
        account.increaseBalance(TEN);
        account.decreaseBalance(ONE);
        assertEquals(FORMATTED_NINE, account.getBalance());
    }

    @Test
    void testDecreaseBalanceWhenAmountIsEqualToBalance() {
        Account account = new Account();
        account.increaseBalance(TEN);
        account.decreaseBalance(TEN);
        assertEquals(FORMATTED_ZERO, account.getBalance());
    }

    @Test
    void testDecreaseBalanceWhenScaleIsGreaterThanTwoButWithZeros() {
        BigDecimal expected = FORMATTED_TEN.subtract(FORMATTED_TEN_THOUSANDTHS);
        Account account = new Account();
        account.increaseBalance(TEN);
        account.decreaseBalance(TEN_THOUSANDTHS);
        assertEquals(expected, account.getBalance());
    }

    @Test
    void testDecreaseBalanceWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        Account account = new Account();
        account.increaseBalance(TEN);
        Exception e = assertThrows(IllegalAmountException.class, () -> account.decreaseBalance(ONE_THOUSANDTH));
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
    }

    @Test
    void testDecreaseBalanceWhenAmountIsGreaterThanBalance() {
        Account account = new Account();
        Exception e = assertThrows(IllegalAmountException.class, () -> account.decreaseBalance(ONE));
        assertEquals(INSUFFICIENT_BALANCE, e.getMessage());
    }

    @Test
    void testDecreaseBalanceWhenAmountIsZero() {
        Account account = new Account();
        Exception e = assertThrows(IllegalAmountException.class, () -> account.decreaseBalance(ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
    }

    @Test
    void testDecreaseBalanceWhenAmountIsNegative() {
        Account account = new Account();
        account.increaseBalance(TEN);
        Exception e = assertThrows(IllegalAmountException.class, () -> account.decreaseBalance(MINUS_ONE));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
    }

    @Test
    void testDecreaseBalanceWhenAmountIsNull() {
        Account account = new Account();
        Exception e = assertThrows(IllegalAmountException.class, () -> account.decreaseBalance(NULL));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
    }
}