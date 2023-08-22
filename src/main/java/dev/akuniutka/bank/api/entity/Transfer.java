package dev.akuniutka.bank.api.entity;

import dev.akuniutka.bank.api.util.ErrorMessage;

import javax.persistence.*;

@Entity
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEBIT_ID", referencedColumnName = "id", nullable = false)
    private Operation debit;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREDIT_ID", referencedColumnName = "id", nullable = false)
    private Operation credit;

    public Long getId() {
        return id;
    }

    public void setDebit(Operation debit) {
        if (debit == null) {
            throw new IllegalArgumentException(ErrorMessage.TRANSFER_DEBIT_IS_NULL);
        }
        this.debit = debit;
    }

    public Operation getDebit() {
        return debit;
    }

    public void setCredit(Operation credit) {
        if (credit == null) {
            throw new IllegalArgumentException(ErrorMessage.TRANSFER_CREDIT_IS_NULL);
        }
        this.credit = credit;
    }

    public Operation getCredit() {
        return credit;
    }
}
