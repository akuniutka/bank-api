package dev.akuniutka.bank.api.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

@Entity
public class Operation {
    private static final String ACCOUNT_IS_NULL = "account for operation is null";
    private static final String OPERATION_TYPE_IS_NULL = "type of operation is null";
    private static final String DATE_IS_NULL = "date is null";
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_ID", nullable = false)
    private Account account;
    @Column(nullable = false)
    private OperationType type;
    @Column(nullable = false)
    private BigDecimal amount;
    @Column(nullable = false)
    private Date date;

    public Long getId() {
        return id;
    }

    public void setAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException(ACCOUNT_IS_NULL);
        }
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    public void setType(OperationType type) {
        if (type == null) {
            throw new IllegalArgumentException(OPERATION_TYPE_IS_NULL);
        }
        this.type = type;
    }

    public OperationType getType() {
        return type;
    }

    public void setAmount(BigDecimal amount) {
        AmountValidator.assertUserInput(amount);
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setDate(Date date) {
        if (date == null) {
            throw new IllegalArgumentException(DATE_IS_NULL);
        }
        this.date = date;
    }

    public Date getDate() {
        return date;
    }
}
