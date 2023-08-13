package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.UserNotFoundToGetBalanceException;
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
            throw new BadRequestException(USER_ID_IS_NULL);
        }
        Account account = repository.findById(userId).orElseThrow(
                () -> new UserNotFoundToGetBalanceException(USER_NOT_FOUND)
        );
        return account.getBalance();
    }

    @Transactional
    public void increaseUserBalance(Long userId, BigDecimal amount) {
        if (userId == null) {
            throw new BadRequestException(USER_ID_IS_NULL);
        }
        Account account = repository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(USER_NOT_FOUND)
        );
        account.increaseBalance(amount);
        repository.save(account);
    }

    @Transactional
    public void decreaseUserBalance(Long userId, BigDecimal amount) {
        if (userId == null) {
            throw new BadRequestException(USER_ID_IS_NULL);
        }
        Account account = repository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(USER_NOT_FOUND)
        );
        account.decreaseBalance(amount);
        repository.save(account);
    }
}
