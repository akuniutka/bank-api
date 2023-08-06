package dev.akuniutka.bank.api.entity;

import dev.akuniutka.bank.api.exception.InsufficientFundsException;
import dev.akuniutka.bank.api.exception.WrongAmountException;
import dev.akuniutka.bank.api.exception.WrongUserIdException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {
    private static final Long USER_ID = 123456789L;
    private static final String AMOUNT_IS_NULL = "amount is null";
    private static final String AMOUNT_IS_NOT_POSITIVE = "amount is not positive";
    private static final String INSUFFICIENT_BALANCE = "insufficient balance";
    private static final String USER_ID_IS_NULL = "user id is null";

    @Test
    void testAccount$WhenNoArgs() {
        assertDoesNotThrow(() -> new Account());
    }

    @Test
    void testAccount$WhenNotNullUserId() {
        assertDoesNotThrow(() -> new Account(USER_ID));
    }

    @Test
    void testAccount$WhenNullUserId() {
        Exception exception = assertThrows(WrongUserIdException.class, () -> new Account(null));
        assertEquals(USER_ID_IS_NULL, exception.getMessage());
    }

    @Test
    void testGetId() {
        Account account = new Account(USER_ID);
        assertEquals(USER_ID, account.getId());
    }

    @Test
    void testGetBalance() {
        BigDecimal expected = BigDecimal.valueOf(0.0).setScale(2, RoundingMode.HALF_UP);
        Account account = new Account(USER_ID);
        assertEquals(expected, account.getBalance());
    }

    @Test
    void testIncreaseBalance$WhenNotNullAmount() {
        BigDecimal expected = BigDecimal.valueOf(999.99).setScale(2, RoundingMode.HALF_UP);
        Account account = new Account(USER_ID);
        account.increaseBalance(expected);
        assertEquals(expected, account.getBalance());
    }

    @Test
    void testIncreaseBalance$WhenZeroAmount() {
        BigDecimal amount = BigDecimal.valueOf(0.0).setScale(2, RoundingMode.HALF_UP);
        Account account = new Account(USER_ID);
        Exception exception = assertThrows(WrongAmountException.class, () -> account.increaseBalance(amount));
        assertEquals(AMOUNT_IS_NOT_POSITIVE, exception.getMessage());
    }

    @Test
    void testIncreaseBalance$WhenNegativeAmount() {
        BigDecimal amount = BigDecimal.valueOf(-999.99).setScale(2, RoundingMode.HALF_UP);
        Account account = new Account(USER_ID);
        Exception exception = assertThrows(WrongAmountException.class, () -> account.increaseBalance(amount));
        assertEquals(AMOUNT_IS_NOT_POSITIVE, exception.getMessage());
    }

    @Test
    void testIncreaseBalance$WhenNullAmount() {
        Account account = new Account(USER_ID);
        Exception exception = assertThrows(WrongAmountException.class, () -> account.increaseBalance(null));
        assertEquals(AMOUNT_IS_NULL, exception.getMessage());
    }

    @Test
    void testDecreaseBalance$WhenSufficientBalance() {
        BigDecimal initialBalance = BigDecimal.valueOf(999.99).setScale(2, RoundingMode.HALF_UP);
        BigDecimal amountWithdrawn = BigDecimal.valueOf(555.55).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expected = BigDecimal.valueOf(444.44).setScale(2, RoundingMode.HALF_UP);
        Account account = new Account(USER_ID);
        account.increaseBalance(initialBalance);
        account.decreaseBalance(amountWithdrawn);
        assertEquals(expected, account.getBalance());
    }

    @Test
    void testDecreaseBalance$WhenInsufficientBalance() {
        Account account = new Account(USER_ID);
        BigDecimal amountWithdrawn = BigDecimal.valueOf(555.55).setScale(2, RoundingMode.HALF_UP);
        Exception exception = assertThrows(InsufficientFundsException.class, () -> account.decreaseBalance(amountWithdrawn));
        assertEquals(INSUFFICIENT_BALANCE, exception.getMessage());
    }

    @Test
    void testDecreaseBalance$WhenZeroAmount() {
        BigDecimal amount = BigDecimal.valueOf(0.0).setScale(2, RoundingMode.HALF_UP);
        Account account = new Account(USER_ID);
        Exception exception = assertThrows(WrongAmountException.class, () -> account.decreaseBalance(amount));
        assertEquals(AMOUNT_IS_NOT_POSITIVE, exception.getMessage());
    }

    @Test
    void testDecreaseBalance$WhenNegativeAmount() {
        BigDecimal initialBalance = BigDecimal.valueOf(999.99).setScale(2, RoundingMode.HALF_UP);
        BigDecimal amount = BigDecimal.valueOf(-999.99).setScale(2, RoundingMode.HALF_UP);
        Account account = new Account(USER_ID);
        account.increaseBalance(initialBalance);
        Exception exception = assertThrows(WrongAmountException.class, () -> account.decreaseBalance(amount));
        assertEquals(AMOUNT_IS_NOT_POSITIVE, exception.getMessage());
    }

    @Test
    void testDecreaseBalance$WhenNullAmount() {
        Account account = new Account(USER_ID);
        Exception exception = assertThrows(WrongAmountException.class, () -> account.decreaseBalance(null));
        assertEquals(AMOUNT_IS_NULL, exception.getMessage());
    }
}