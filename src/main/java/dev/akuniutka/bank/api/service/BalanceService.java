package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.exception.IllegalAmountException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.UserNotFoundToGetBalanceException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class BalanceService {
    private final AccountService accountService;
    private final OperationService operationService;

    public BalanceService(AccountService accountService, OperationService operationService) {
        this.accountService = accountService;
        this.operationService = operationService;
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
}
