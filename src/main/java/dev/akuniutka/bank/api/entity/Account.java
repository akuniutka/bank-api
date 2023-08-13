package dev.akuniutka.bank.api.entity;

import dev.akuniutka.bank.api.exception.BadRequestException;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
public class Account {
    private final static String AMOUNT_IS_NULL = "amount is null";
    private final static String AMOUNT_IS_ZERO = "amount is zero";
    private final static String AMOUNT_IS_NEGATIVE = "amount is negative";
    private final static String WRONG_MINOR_UNITS = "wrong minor units";
    private final static String INSUFFICIENT_BALANCE = "insufficient balance";
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(nullable = false)
    @Access(AccessType.PROPERTY)
    private BigDecimal balance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    public Long getId() {
        return id;
    }

    protected void setBalance(BigDecimal balance) {
        if (balance == null) {
            throw new IllegalArgumentException(AMOUNT_IS_NULL);
        } else if (balance.signum() < 0) {
            throw new IllegalArgumentException(AMOUNT_IS_NEGATIVE);
        } else if (balance.setScale(2, RoundingMode.HALF_UP).compareTo(balance) != 0) {
            throw new IllegalArgumentException(WRONG_MINOR_UNITS);
        }
        this.balance = balance.setScale(2, RoundingMode.HALF_UP);
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
            throw new BadRequestException(INSUFFICIENT_BALANCE);
        }
        balance = balance.subtract(amount.setScale(2, RoundingMode.HALF_UP));
    }

    private void assertAmount(BigDecimal amount) {
        if (amount == null) {
            throw new BadRequestException(AMOUNT_IS_NULL);
        } else if(amount.signum() == 0) {
            throw new BadRequestException(AMOUNT_IS_ZERO);
        } else if (amount.signum() < 0) {
            throw new BadRequestException(AMOUNT_IS_NEGATIVE);
        } else if (amount.setScale(2, RoundingMode.HALF_UP).compareTo(amount) != 0) {
            throw new BadRequestException(WRONG_MINOR_UNITS);
        }
    }
}
