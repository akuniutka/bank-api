package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.Transfer;
import dev.akuniutka.bank.api.repository.TransferRepository;
import dev.akuniutka.bank.api.util.ErrorMessage;
import org.springframework.stereotype.Service;

@Service
public class TransferService {
    private final TransferRepository repository;

    public TransferService(TransferRepository repository) {
        this.repository = repository;
    }

    public Transfer createTransfer(Operation outgoing, Operation incoming) {
        if (outgoing == null) {
            throw new IllegalArgumentException(ErrorMessage.TRANSFER_DEBIT_IS_NULL);
        } else if (incoming == null) {
            throw new IllegalArgumentException(ErrorMessage.TRANSFER_CREDIT_IS_NULL);
        }
        Transfer transfer = new Transfer();
        transfer.setDebit(outgoing);
        transfer.setCredit(incoming);
        return transfer;
    }

    public Transfer saveTransfer(Transfer transfer) {
        if (transfer == null) {
            throw new IllegalArgumentException(ErrorMessage.TRANSFER_IS_NULL);
        }
        return repository.save(transfer);
    }
}
