package dev.akuniutka.bank.api.entity;

import dev.akuniutka.bank.api.exception.InsufficientFundsException;
import dev.akuniutka.bank.api.exception.WrongAmountException;
import dev.akuniutka.bank.api.exception.WrongUserIdException;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
public class Account {
    private final static String USER_ID_IS_NULL = "user id is null";
    private final static String AMOUNT_IS_NULL = "amount is null";
    private final static String AMOUNT_IS_NOT_POSITIVE = "amount is not positive";
    private final static String INSUFFICIENT_BALANCE = "insufficient balance";
    @Id
    private Long id;
    private BigDecimal balance;

    public Account(Long userId) {
        this();
        if (userId == null) {
            throw new WrongUserIdException(USER_ID_IS_NULL);
        }
        this.id = userId;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void increaseBalance(BigDecimal amount) {
        if (amount == null) {
            throw new WrongAmountException(AMOUNT_IS_NULL);
        } else if (amount.signum() != 1) {
            throw new WrongAmountException(AMOUNT_IS_NOT_POSITIVE);
        }
        balance = balance.add(amount);
    }

    public void decreaseBalance(BigDecimal amount) {
        if (amount == null) {
            throw new WrongAmountException(AMOUNT_IS_NULL);
        } else if (amount.signum() != 1) {
            throw new WrongAmountException(AMOUNT_IS_NOT_POSITIVE);
        } else if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(INSUFFICIENT_BALANCE);
        }
        balance = balance.subtract(amount);
    }

    protected Account() {
        balance = BigDecimal.valueOf(0.0).setScale(2, RoundingMode.HALF_UP);
    }
}
