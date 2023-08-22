package dev.akuniutka.bank.api.entity;

import dev.akuniutka.bank.api.exception.IllegalAmountException;
import dev.akuniutka.bank.api.util.AmountValidator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static dev.akuniutka.bank.api.util.ErrorMessage.*;
import static dev.akuniutka.bank.api.Amount.*;

class AmountValidatorTest {
    @Test
    void testAssertBalanceWhenAmountIsPositive() {
        assertDoesNotThrow(() -> AmountValidator.assertBalance(ONE));
    }

    @Test
    void testAssertBalanceWhenAmountIsZero() {
        assertDoesNotThrow(() -> AmountValidator.assertBalance(BigDecimal.ZERO));
    }

    @Test
    void testAssertBalanceWhenScaleIsGreaterThanTwoButWithZeros() {
        assertDoesNotThrow(() -> AmountValidator.assertBalance(TEN_THOUSANDTHS));
    }

    @Test
    void testAssertBalanceWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        Exception e = assertThrows(IllegalAmountException.class, () -> AmountValidator.assertBalance(ONE_THOUSANDTH));
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
    }

    @Test
    void testAssertBalanceWhenAmountIsNegative() {
        Exception e = assertThrows(IllegalAmountException.class, () -> AmountValidator.assertBalance(MINUS_ONE));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
    }

    @Test
    void testAssertBalanceWhenAmountIsNull() {
        Exception e = assertThrows(IllegalAmountException.class, () -> AmountValidator.assertBalance(NULL));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testAssertAmountWhenAmountIsPositive() {
        assertDoesNotThrow(() -> AmountValidator.assertAmount(ONE));
    }

    @Test
    void testAssertAmountWhenScaleIsGreaterThanTwoButWithZeros() {
        assertDoesNotThrow(() -> AmountValidator.assertAmount(TEN_THOUSANDTHS));
    }

    @Test
    void testAssertAmountWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        Exception e = assertThrows(IllegalAmountException.class, () -> AmountValidator.assertAmount(ONE_THOUSANDTH));
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
    }

    @Test
    void testAssertAmountWhenAmountIsZero() {
        Exception e = assertThrows(IllegalAmountException.class, () -> AmountValidator.assertAmount(ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
    }

    @Test
    void testAssertAmountWhenAmountIsNegative() {
        Exception e = assertThrows(IllegalAmountException.class, () -> AmountValidator.assertAmount(MINUS_ONE));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
    }

    @Test
    void testAssertAmountWhenAmountIsNull() {
        Exception e = assertThrows(IllegalAmountException.class, () -> AmountValidator.assertAmount(NULL));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
    }

}