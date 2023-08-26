package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
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
            Account account = accountService.getAccount(userid);
            return account.getBalance();
        } catch (UserNotFoundException e) {
            throw new UserNotFoundToGetBalanceException(e.getMessage());
        }
    }

    public void putMoney(Long userId, BigDecimal amount) {
        Account account = accountService.getAccount(userId);
        try {
            account.increaseBalance(amount);
        } catch (IllegalAmountException e) {
            throw new BadRequestException(e.getMessage());
        }
        account = accountService.saveAccount(account);
        Operation operation = operationService.createDeposit(account, amount);
        operationService.saveOperation(operation);
    }

    public void takeMoney(Long userId, BigDecimal amount) {
        Account account = accountService.getAccount(userId);
        try {
            account.decreaseBalance(amount);
        } catch (IllegalAmountException e) {
            throw new BadRequestException(e.getMessage());
        }
        account = accountService.saveAccount(account);
        Operation operation = operationService.createWithdrawal(account, amount);
        operationService.saveOperation(operation);
    }

    public List<Operation> getOperationList(Long userId, Date dateFrom, Date dateTo) {
        Account account = accountService.getAccount(userId);
        return operationService.getOperations(account, dateFrom, dateTo);
    }

    public void transferMoney(Long payerId, Long payeeId, BigDecimal amount) {
        if (payerId == null) {
            throw new BadRequestException(ErrorMessage.USER_ID_IS_NULL);
        } else if (payeeId == null) {
            throw new BadRequestException(ErrorMessage.RECEIVER_ID_IS_NULL);
        }
        Account payer = accountService.getAccount(payerId);
        Account payee;
        try {
            payee = accountService.getAccount(payeeId);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException(ErrorMessage.RECEIVER_NOT_FOUND);
        }
        Date date = new Date();
        try {
            payer.decreaseBalance(amount);
        } catch (IllegalAmountException e) {
            throw new BadRequestException(e.getMessage());
        }
        payer = accountService.saveAccount(payer);
        payee.increaseBalance(amount);
        payee = accountService.saveAccount(payee);
        Operation outgoingTransfer = operationService.createOutgoingTransfer(payer, amount, date);
        outgoingTransfer = operationService.saveOperation(outgoingTransfer);
        Operation incomingTransfer = operationService.createIncomingTransfer(payee, amount, date);
        incomingTransfer = operationService.saveOperation(incomingTransfer);
        Transfer transfer = transferService.createTransfer(outgoingTransfer, incomingTransfer);
        transferService.saveTransfer(transfer);
    }
}
