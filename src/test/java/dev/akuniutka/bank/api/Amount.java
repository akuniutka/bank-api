package dev.akuniutka.bank.api;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Amount {
    public static final BigDecimal TEN = BigDecimal.TEN;
    public static final BigDecimal FORMATTED_TEN = BigDecimal.TEN.setScale(2, RoundingMode.HALF_UP);
    public static final BigDecimal FORMATTED_NINE = FORMATTED_TEN.subtract(BigDecimal.ONE);
    public static final BigDecimal ONE = BigDecimal.ONE;
    public static final BigDecimal FORMATTED_ONE = BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);
    public static final BigDecimal ZERO = BigDecimal.ZERO;
    public static final BigDecimal FORMATTED_ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    public static final BigDecimal ONE_THOUSANDTH = BigDecimal.ONE
            .setScale(3, RoundingMode.HALF_UP)
            .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
            .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
            .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
    public static final BigDecimal TEN_THOUSANDTHS = BigDecimal.ONE
            .setScale(3, RoundingMode.HALF_UP)
            .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
            .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
    public static final BigDecimal FORMATTED_TEN_THOUSANDTHS = BigDecimal.ONE
            .setScale(2, RoundingMode.HALF_UP)
            .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
            .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
    public static final BigDecimal MINUS_ONE = BigDecimal.ONE.negate();
    public static final BigDecimal MINUS_TEN = BigDecimal.TEN.negate();
    public static final BigDecimal NULL = null;
}
