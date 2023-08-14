package dev.akuniutka.bank.api.entity;

import dev.akuniutka.bank.api.exception.BadRequestException;

import java.math.BigDecimal;
import java.math.RoundingMode;

class AmountValidator {
    private final static String AMOUNT_IS_NULL = "amount is null";
    private final static String AMOUNT_IS_ZERO = "amount is zero";
    private final static String AMOUNT_IS_NEGATIVE = "amount is negative";
    private final static String WRONG_MINOR_UNITS = "wrong minor units";

    static void assertAmount(BigDecimal amount) {
        assertAmount(amount, false, false);
    }

    static void assertAmountZeroAllowed(BigDecimal amount) {
        assertAmount(amount, true, false);
    }

    static void assertUserInput(BigDecimal amount) {
        assertAmount(amount, false, true);
    }

    private static void assertAmount(BigDecimal amount, boolean isZeroAllowed, boolean isBusinessRule) {
        String message;
        if (amount == null) {
            message = AMOUNT_IS_NULL;
        } else if (!isZeroAllowed && amount.signum() == 0) {
            message = AMOUNT_IS_ZERO;
        } else if (amount.signum() < 0) {
            message = AMOUNT_IS_NEGATIVE;
        } else if (amount.setScale(2, RoundingMode.HALF_UP).compareTo(amount) != 0) {
            message = WRONG_MINOR_UNITS;
        } else {
            return;
        }
        if (isBusinessRule) {
            throw new BadRequestException(message);
        } else {
            throw new IllegalArgumentException(message);
        }
    }
}
