package dev.akuniutka.bank.api.entity;

import dev.akuniutka.bank.api.util.AmountValidator;
import dev.akuniutka.bank.api.util.ErrorMessage;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

@Entity
public class Operation {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ACCOUNT_ID", nullable = false)
    @Access(AccessType.PROPERTY)
    private Account account;
    @Column(nullable = false)
    @Access(AccessType.PROPERTY)
    private OperationType type;
    @Column(nullable = false)
    @Access(AccessType.PROPERTY)
    private BigDecimal amount;
    @Column(nullable = false)
    @Access(AccessType.PROPERTY)
    private OffsetDateTime date;

    protected Operation() {}

    public Operation(Account account, OperationType type, BigDecimal amount, OffsetDateTime date) {
        id = null;
        setAccount(account);
        setType(type);
        setAmount(amount);
        setDate(date);
    }

    public Long getId() {
        return id;
    }

    protected void setAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException(ErrorMessage.ACCOUNT_IS_NULL);
        }
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    protected void setType(OperationType type) {
        if (type == null) {
            throw new IllegalArgumentException(ErrorMessage.OPERATION_TYPE_IS_NULL);
        }
        this.type = type;
    }

    public OperationType getType() {
        return type;
    }

    protected void setAmount(BigDecimal amount) {
        AmountValidator.assertAmount(amount);
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    protected void setDate(OffsetDateTime date) {
        if (date == null) {
            throw new IllegalArgumentException(ErrorMessage.DATE_IS_NULL);
        }
        this.date = date;
    }

    public OffsetDateTime getDate() {
        return date;
    }
}
