package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.Transfer;
import dev.akuniutka.bank.api.exception.IllegalAmountException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.UserNotFoundToGetBalanceException;
import dev.akuniutka.bank.api.util.ErrorMessage;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ApiService {
    private final AccountService accountService;
    private final OperationService operationService;
    private final TransferService transferService;

    public ApiService(
            AccountService accountService,
            OperationService operationService,
            TransferService transferService
    ) {
        this.accountService = accountService;
        this.operationService = operationService;
        this.transferService = transferService;
    }

    public BigDecimal getBalance(Long userid) {
        try {
            return accountService.getUserBalance(userid);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundToGetBalanceException(e.getMessage());
        }
    }

    public void putMoney(Long userId, BigDecimal amount) {
        try {
            operationService.createDeposit(userId, amount);
        } catch (IllegalAmountException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    public void takeMoney(Long userId, BigDecimal amount) {
        try {
            operationService.createWithdrawal(userId, amount);
        } catch (IllegalAmountException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    public List<Operation> getOperationList(Long userId, Date dateFrom, Date dateTo) {
        return operationService.getUserOperations(userId, dateFrom, dateTo);
    }

    public void transferMoney(Long payerId, Long payeeId, BigDecimal amount) {
        try {
            Date date = new Date();
            Operation outgoingTransfer = operationService.createOutgoingTransfer(payerId, amount, date);
            try {
                Operation incomingTransfer = operationService.createIncomingTransfer(payeeId, amount, date);
                Transfer transfer = transferService.createTransfer(outgoingTransfer, incomingTransfer);
                transferService.saveTransfer(transfer);

            } catch (UserNotFoundException e) {
                throw new UserNotFoundException(ErrorMessage.RECEIVER_NOT_FOUND);
            } catch (BadRequestException e) {
                if (ErrorMessage.USER_ID_IS_NULL.equals(e.getMessage())) {
                    throw new BadRequestException(ErrorMessage.RECEIVER_ID_IS_NULL);
                }
                throw e;
            }
        } catch (IllegalAmountException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}
