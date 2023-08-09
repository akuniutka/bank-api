package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.exception.GetBalanceException;
import dev.akuniutka.bank.api.exception.CashOrderException;
import dev.akuniutka.bank.api.repository.AccountRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;

@Service
public class AccountService {
    private final static String USER_ID_IS_NULL = "user id is null";
    private final static String USER_NOT_FOUND = "user not found";
    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public BigDecimal getUserBalance(Long userId) {
        if (userId == null) {
            throw new GetBalanceException(USER_ID_IS_NULL);
        }
        Account account = repository.findById(userId).orElseThrow(
                () -> new GetBalanceException(USER_NOT_FOUND)
        );
        return account.getBalance();
    }

    @Transactional
    public void increaseUserBalance(Long userId, BigDecimal amount) {
        Account account = getAccountById(userId);
        account.increaseBalance(amount);
        repository.save(account);
    }

    @Transactional
    public void decreaseUserBalance(Long userId, BigDecimal amount) {
        Account account = getAccountById(userId);
        account.decreaseBalance(amount);
        repository.save(account);
    }

    private Account getAccountById(Long userId) {
        if (userId == null) {
            throw new CashOrderException(USER_ID_IS_NULL);
        }
        return repository.findById(userId).orElseThrow(() -> new CashOrderException(USER_NOT_FOUND));
    }
}
