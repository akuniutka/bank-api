package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.Transfer;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.IllegalAmountException;
import dev.akuniutka.bank.api.exception.NullUserIdException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.repository.TransferRepository;
import dev.akuniutka.bank.api.util.ErrorMessage;
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
        try {
            Date date = new Date();
            Operation outgoingTransfer = operationService.createOutgoingTransfer(payerId, amount, date);
            try {
                Operation incomingTransfer = operationService.createIncomingTransfer(payeeId, amount, date);
                Transfer transfer = new Transfer();
                transfer.setDebit(outgoingTransfer);
                transfer.setCredit(incomingTransfer);
                return repository.save(transfer);
            } catch (UserNotFoundException e) {
                throw new UserNotFoundException(ErrorMessage.RECEIVER_NOT_FOUND);
            } catch (NullUserIdException e) {
                throw new NullUserIdException(ErrorMessage.RECEIVER_ID_IS_NULL);
            }
        } catch (IllegalAmountException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}
