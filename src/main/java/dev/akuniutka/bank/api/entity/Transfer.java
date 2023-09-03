package dev.akuniutka.bank.api.entity;

import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.util.ErrorMessage;

import javax.persistence.*;

@Entity
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "OUTGOING_TRANSFER_ID", referencedColumnName = "id", nullable = false)
//    @Access(AccessType.PROPERTY)
    private Operation outgoingTransfer;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "INCOMING_TRANSFER_ID", referencedColumnName = "id", nullable = false)
//    @Access(AccessType.PROPERTY)
    private Operation incomingTransfer;

    protected Transfer() {}

    public Transfer(Operation outgoingTransfer, Operation incomingTransfer) {
        id = null;
        setOutgoingTransfer(outgoingTransfer);
        setIncomingTransfer(incomingTransfer);
    }

    public Long getId() {
        return id;
    }

    protected void setOutgoingTransfer(Operation outgoingTransfer) {
        if (outgoingTransfer == null) {
            throw new IllegalArgumentException(ErrorMessage.TRANSFER_DEBIT_IS_NULL);
        } else if (!OperationType.OUTGOING_TRANSFER.equals(outgoingTransfer.getType())) {
            throw new IllegalArgumentException(ErrorMessage.WRONG_OPERATION_TYPE);
        } else if (incomingTransfer != null) {
           checkForConsistency(outgoingTransfer, incomingTransfer);
        }
        this.outgoingTransfer = outgoingTransfer;
    }

    public Operation getOutgoingTransfer() {
        return outgoingTransfer;
    }

    protected void setIncomingTransfer(Operation incomingTransfer) {
        if (incomingTransfer == null) {
            throw new IllegalArgumentException(ErrorMessage.TRANSFER_CREDIT_IS_NULL);
        } else if (!OperationType.INCOMING_TRANSFER.equals(incomingTransfer.getType())) {
            throw new IllegalArgumentException(ErrorMessage.WRONG_OPERATION_TYPE);
        } else if (outgoingTransfer != null) {
           checkForConsistency(outgoingTransfer, incomingTransfer);
        }
        this.incomingTransfer = incomingTransfer;
    }

    public Operation getIncomingTransfer() {
        return incomingTransfer;
    }

    private void checkForConsistency(Operation outgoingTransfer, Operation incomingTransfer) {
        Long payerId = outgoingTransfer.getAccount().getId();
        Long payeeId = incomingTransfer.getAccount().getId();
        if (payerId != null && payerId.equals(payeeId)) {
            throw new BadRequestException(ErrorMessage.WRONG_OPERATION_ACCOUNT);
        } else if (!outgoingTransfer.getAmount().equals(incomingTransfer.getAmount())) {
            throw new IllegalArgumentException(ErrorMessage.WRONG_OPERATION_AMOUNT);
        } else if (!outgoingTransfer.getDate().isEqual(incomingTransfer.getDate())) {
            throw new IllegalArgumentException(ErrorMessage.WRONG_OPERATION_DATE);
        }
    }
}
