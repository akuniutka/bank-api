package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.util.ErrorMessage;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountService {
    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    public Account getAccount(Long userId) {
        if (userId == null) {
            throw new BadRequestException(ErrorMessage.USER_ID_IS_NULL);
        }
        return repository.findById(userId).orElseThrow(() -> new UserNotFoundException(ErrorMessage.USER_NOT_FOUND));
    }

    public BigDecimal getUserBalance(Long userId) {
        return getAccount(userId).getBalance();
    }

    public Account increaseUserBalance(Long userId, BigDecimal amount) {
        Account account = getAccount(userId);
        account.increaseBalance(amount);
        return repository.save(account);
    }

    public Account decreaseUserBalance(Long userId, BigDecimal amount) {
        Account account = getAccount(userId);
        account.decreaseBalance(amount);
        return repository.save(account);
    }
}
