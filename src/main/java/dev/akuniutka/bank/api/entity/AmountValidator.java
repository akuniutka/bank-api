package dev.akuniutka.bank.api.entity;

import dev.akuniutka.bank.api.exception.BadRequestException;

import java.math.BigDecimal;
import java.math.RoundingMode;

class AmountValidator {
    static final boolean ZERO_IS_ALLOWED = true;
    static final boolean ZERO_IS_NOT_ALLOWED = false;

    static void assertBalance(BigDecimal amount) {
        assertAmount(amount, ZERO_IS_ALLOWED);
    }

    static void assertAmount(BigDecimal amount) {
        assertAmount(amount, ZERO_IS_NOT_ALLOWED);
    }

    private static void assertAmount(BigDecimal amount, boolean isZeroAllowed) {
        String message;
        if (amount == null) {
            message = ErrorMessage.AMOUNT_IS_NULL;
        } else if (!isZeroAllowed && amount.signum() == 0) {
            message = ErrorMessage.AMOUNT_IS_ZERO;
        } else if (amount.signum() < 0) {
            message = ErrorMessage.AMOUNT_IS_NEGATIVE;
        } else if (amount.setScale(2, RoundingMode.HALF_UP).compareTo(amount) != 0) {
            message = ErrorMessage.WRONG_MINOR_UNITS;
        } else {
            return;
        }
        if (isZeroAllowed) {
            throw new IllegalArgumentException(message);
        } else {
            throw new BadRequestException(message);
        }
    }
}
