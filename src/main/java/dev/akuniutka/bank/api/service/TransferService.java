package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.Transfer;
import dev.akuniutka.bank.api.repository.TransferRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class TransferService {
    private final TransferRepository repository;
    private final OperationService operationService;

    public TransferService(TransferRepository repository, OperationService operationService) {
        this.repository = repository;
        this.operationService = operationService;
    }

    public Transfer createTransfer(Long payerId, Long payeeId, BigDecimal amount) {
        Date date = new Date();
        Operation outgoingTransfer = operationService.createOutgoingTransfer(payerId, amount, date);
        Operation incomingTransfer = operationService.createIncomingTransfer(payeeId, amount, date);
        Transfer transfer = new Transfer();
        transfer.setDebit(outgoingTransfer);
        transfer.setCredit(incomingTransfer);
        return repository.save(transfer);
    }
}
