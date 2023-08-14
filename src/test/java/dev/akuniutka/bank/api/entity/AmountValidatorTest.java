package dev.akuniutka.bank.api.entity;

import dev.akuniutka.bank.api.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class AmountValidatorTest {
    private static final String AMOUNT_IS_NULL = "amount is null";
    private static final String AMOUNT_IS_ZERO = "amount is zero";
    private static final String AMOUNT_IS_NEGATIVE = "amount is negative";
    private static final String WRONG_MINOR_UNITS = "wrong minor units";

    @Test
    void testAssertAmountWhenAmountIsPositive() {
        assertDoesNotThrow(() -> AmountValidator.assertAmount(BigDecimal.ONE));
    }

    @Test
    void testAssertAmountWhenScaleIsGreaterThanTwoButWithZeros() {
        BigDecimal amount = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        assertDoesNotThrow(() -> AmountValidator.assertAmount(amount));
    }

    @Test
    void testAssertAmountWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        BigDecimal amount = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> AmountValidator.assertAmount(amount)
        );
        assertEquals(WRONG_MINOR_UNITS, exception.getMessage());
    }

    @Test
    void testAssertAmountWhenAmountIsZero() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> AmountValidator.assertAmount(BigDecimal.ZERO)
        );
        assertEquals(AMOUNT_IS_ZERO, exception.getMessage());
    }

    @Test
    void testAssertAmountWhenAmountIsNegative() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> AmountValidator.assertAmount(BigDecimal.ONE.negate())
        );
        assertEquals(AMOUNT_IS_NEGATIVE, exception.getMessage());
    }

    @Test
    void testAssertAmountWhenAmountIsNull() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> AmountValidator.assertAmount(null)
        );
        assertEquals(AMOUNT_IS_NULL, exception.getMessage());
    }

    @Test
    void testAssertAmountZeroAllowedWhenAmountIsPositive() {
        assertDoesNotThrow(
                () -> AmountValidator.assertAmountZeroAllowed(BigDecimal.ONE)
        );
    }

    @Test
    void testAssertAmountZeroAllowedWhenAmountIsZero() {
        assertDoesNotThrow(
                () -> AmountValidator.assertAmountZeroAllowed(BigDecimal.ZERO)
        );
    }

    @Test
    void testAssertAmountZeroAllowedWhenScaleIsGreaterThanTwoButWithZeros() {
        BigDecimal amount = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        assertDoesNotThrow(
                () -> AmountValidator.assertAmountZeroAllowed(amount)
        );
    }

    @Test
    void testAssertAmountZeroAllowedWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        BigDecimal amount = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> AmountValidator.assertAmountZeroAllowed(amount)
        );
        assertEquals(WRONG_MINOR_UNITS, exception.getMessage());
    }

    @Test
    void testAssertAmountZeroAllowedWhenAmountIsNegative() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> AmountValidator.assertAmountZeroAllowed(BigDecimal.ONE.negate())
        );
        assertEquals(AMOUNT_IS_NEGATIVE, exception.getMessage());
    }

    @Test
    void testAssertAmountZeroAllowedWhenAmountIsNull() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> AmountValidator.assertAmountZeroAllowed(null)
        );
        assertEquals(AMOUNT_IS_NULL, exception.getMessage());
    }

    @Test
    void testAssertUserInputWhenAmountIsPositive() {
        assertDoesNotThrow(() -> AmountValidator.assertUserInput(BigDecimal.ONE));
    }

    @Test
    void testAssertUserInputWhenScaleIsGreaterThanTwoButWithZeros() {
        BigDecimal amount = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        assertDoesNotThrow(() -> AmountValidator.assertUserInput(amount));
    }

    @Test
    void testAssertUserInputWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        BigDecimal amount = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        Exception exception = assertThrows(BadRequestException.class,
                () -> AmountValidator.assertUserInput(amount)
        );
        assertEquals(WRONG_MINOR_UNITS, exception.getMessage());
    }

    @Test
    void testAssertUserInputWhenAmountIsZero() {
        Exception exception = assertThrows(BadRequestException.class,
                () -> AmountValidator.assertUserInput(BigDecimal.ZERO)
        );
        assertEquals(AMOUNT_IS_ZERO, exception.getMessage());
    }

    @Test
    void testAssertUserInputWhenAmountIsNegative() {
        Exception exception = assertThrows(BadRequestException.class,
                () -> AmountValidator.assertUserInput(BigDecimal.ONE.negate())
        );
        assertEquals(AMOUNT_IS_NEGATIVE, exception.getMessage());
    }

    @Test
    void testAssertUserInputWhenAmountIsNull() {
        Exception exception = assertThrows(BadRequestException.class,
                () -> AmountValidator.assertUserInput(null)
        );
        assertEquals(AMOUNT_IS_NULL, exception.getMessage());
    }

}