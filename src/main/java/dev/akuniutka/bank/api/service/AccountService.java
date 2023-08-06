package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.exception.WrongUserIdException;
import dev.akuniutka.bank.api.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountService {
    private final static String USER_ID_IS_NULL = "user id is null";
    private final static String USER_DOES_NOT_EXIST = "user does not exist";
    private final static String USER_ALREADY_EXISTS = "user already exists";
    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    public void addNewUser(Long userId) {
        if (userId == null) {
            throw new WrongUserIdException(USER_ID_IS_NULL);
        }
        if (repository.existsById(userId)) {
            throw new WrongUserIdException(USER_ALREADY_EXISTS);
        }
        Account account = new Account(userId);
        repository.save(account);
    }

    public BigDecimal getUserBalance(Long userId) {
        if (userId == null) {
            throw new WrongUserIdException(USER_ID_IS_NULL);
        }
        Account account = repository.findById(userId).orElseThrow(() -> new WrongUserIdException(USER_DOES_NOT_EXIST));
        return account.getBalance();
    }

    public void increaseUserBalance(Long userId, BigDecimal amount) {
        if (userId == null) {
            throw new WrongUserIdException(USER_ID_IS_NULL);
        }
        Account account = repository.findById(userId).orElseThrow(() -> new WrongUserIdException(USER_DOES_NOT_EXIST));
        account.increaseBalance(amount);
        repository.save(account);
    }

    public void decreaseUserBalance(Long userId, BigDecimal amount) {
        if (userId == null) {
            throw new WrongUserIdException(USER_ID_IS_NULL);
        }
        Account account = repository.findById(userId).orElseThrow(() -> new WrongUserIdException(USER_DOES_NOT_EXIST));
        account.decreaseBalance(amount);
        repository.save(account);
    }
}
