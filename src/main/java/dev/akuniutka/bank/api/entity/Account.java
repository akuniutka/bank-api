package dev.akuniutka.bank.api.entity;

import dev.akuniutka.bank.api.exception.BadRequestException;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
public class Account {
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
        AmountValidator.assertBalance(balance);
        this.balance = balance.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void increaseBalance(BigDecimal amount) {
        AmountValidator.assertAmount(amount);
        balance = balance.add(amount.setScale(2, RoundingMode.HALF_UP));
    }

    public void decreaseBalance(BigDecimal amount) {
        AmountValidator.assertAmount(amount);
        if (balance.compareTo(amount) < 0) {
            throw new BadRequestException(ErrorMessage.INSUFFICIENT_BALANCE);
        }
        balance = balance.subtract(amount.setScale(2, RoundingMode.HALF_UP));
    }
}
