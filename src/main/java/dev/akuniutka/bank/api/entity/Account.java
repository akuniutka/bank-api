package dev.akuniutka.bank.api.entity;

import dev.akuniutka.bank.api.exception.CashOrderException;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
public class Account {
    private final static String AMOUNT_IS_NULL = "amount is null";
    private final static String AMOUNT_IS_NOT_POSITIVE = "amount is not positive";
    private final static String WRONG_MINOR_UNITS = "wrong minor units";
    private final static String INSUFFICIENT_BALANCE = "insufficient balance";
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private BigDecimal balance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    public Long getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void increaseBalance(BigDecimal amount) {
        assertAmount(amount);
        balance = balance.add(amount.setScale(2, RoundingMode.HALF_UP));
    }

    public void decreaseBalance(BigDecimal amount) {
        assertAmount(amount);
        if (balance.compareTo(amount) < 0) {
            throw new CashOrderException(INSUFFICIENT_BALANCE);
        }
        balance = balance.subtract(amount.setScale(2, RoundingMode.HALF_UP));
    }

    private void assertAmount(BigDecimal amount) {
        if (amount == null) {
            throw new CashOrderException(AMOUNT_IS_NULL);
        } else if (amount.signum() != 1) {
            throw new CashOrderException(AMOUNT_IS_NOT_POSITIVE);
        } else if (amount.setScale(2, RoundingMode.HALF_UP).compareTo(amount) != 0) {
            throw new CashOrderException(WRONG_MINOR_UNITS);
        }
    }
}
