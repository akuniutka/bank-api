package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.exception.IllegalAmountException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.UserNotFoundToGetBalanceException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;

@Service
@Transactional
public class BalanceService {
    private final AccountService accountService;
    private final OperationService operationService;

    public BalanceService(AccountService accountService, OperationService operationService) {
        this.accountService = accountService;
        this.operationService = operationService;
    }

    public BigDecimal getUserBalance(Long userId) {
        try {
            Account account = accountService.getAccount(userId);
            return account.getBalance();
        } catch (UserNotFoundException e) {
            throw new UserNotFoundToGetBalanceException(e.getMessage());
        }
    }

    public void increaseUserBalance(Long userId, BigDecimal amount) {
        Account account = accountService.getAccount(userId);
        try {
            account.increaseBalance(amount);
        } catch (IllegalAmountException e) {
            throw new BadRequestException(e.getMessage());
        }
        account = accountService.saveAccount(account);
        operationService.addDeposit(account, amount);
    }

    public void decreaseUserBalance(Long userId, BigDecimal amount) {
        Account account = accountService.getAccount(userId);
        try {
            account.decreaseBalance(amount);
        } catch (IllegalAmountException e) {
            throw new BadRequestException(e.getMessage());
        }
        account = accountService.saveAccount(account);
        operationService.addWithdrawal(account, amount);
    }
}
