package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BalanceService {
    private final AccountService accountService;

    public BalanceService(AccountService accountService) {
        this.accountService = accountService;
    }

    public BigDecimal getUserBalance(Long userId) {
        return accountService.getAccount(userId).getBalance();
    }

    public Account increaseUserBalance(Long userId, BigDecimal amount) {
        Account account = accountService.getAccount(userId);
        account.increaseBalance(amount);
        return accountService.saveAccount(account);
    }

    public Account decreaseUserBalance(Long userId, BigDecimal amount) {
        Account account = accountService.getAccount(userId);
        account.decreaseBalance(amount);
        return accountService.saveAccount(account);
    }
}
