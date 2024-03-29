package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.Transfer;
import dev.akuniutka.bank.api.repository.TransferRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
public class TransferService {
    private final TransferRepository repository;
    private final OperationService operationService;

    public TransferService(TransferRepository repository, OperationService operationService) {
        this.repository = repository;
        this.operationService = operationService;
    }

    @Transactional
    public void createTransfer(Long payerId, Long payeeId, BigDecimal amount) {
        OffsetDateTime date = OffsetDateTime.now();
        Operation outgoingTransfer = operationService.createOutgoingTransfer(payerId, amount, date);
        Operation incomingTransfer = operationService.createIncomingTransfer(payeeId, amount, date);
        Transfer transfer = new Transfer(outgoingTransfer, incomingTransfer);
        repository.save(transfer);
    }
}
