package dev.akuniutka.bank.api.entity;

import dev.akuniutka.bank.api.util.ErrorMessage;

import javax.persistence.*;

@Entity
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "OUTGOING_TRANSFER_ID", referencedColumnName = "id", nullable = false)
    private Operation outgoingTransfer;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "INCOMING_TRANSFER_ID", referencedColumnName = "id", nullable = false)
    private Operation incomingTransfer;

    public Long getId() {
        return id;
    }

    public void setOutgoingTransfer(Operation outgoingTransfer) {
        if (outgoingTransfer == null) {
            throw new IllegalArgumentException(ErrorMessage.TRANSFER_DEBIT_IS_NULL);
        }
        this.outgoingTransfer = outgoingTransfer;
    }

    public Operation getOutgoingTransfer() {
        return outgoingTransfer;
    }

    public void setIncomingTransfer(Operation incomingTransfer) {
        if (incomingTransfer == null) {
            throw new IllegalArgumentException(ErrorMessage.TRANSFER_CREDIT_IS_NULL);
        }
        this.incomingTransfer = incomingTransfer;
    }

    public Operation getIncomingTransfer() {
        return incomingTransfer;
    }
}
