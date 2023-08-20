package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.exception.IllegalAmountException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.UserNotFoundToGetBalanceException;
import dev.akuniutka.bank.api.repository.AccountRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;

import dev.akuniutka.bank.api.entity.ErrorMessage;

@Service
public class BalanceService {
    private final AccountRepository repository;
    private final Operations operations;

    public BalanceService(AccountRepository repository, Operations operations) {
        this.repository = repository;
        this.operations = operations;
    }

    @Transactional
    public BigDecimal getUserBalance(Long userId) {
//        if (userId == null) {
//            throw new BadRequestException(ErrorMessage.USER_ID_IS_NULL);
//        }
//        Account account = repository.findById(userId).orElseThrow(
//                () -> new UserNotFoundToGetBalanceException(ErrorMessage.USER_NOT_FOUND)
//        );
        try {
            Account account = findById(userId);
            return account.getBalance();
        } catch (UserNotFoundException e) {
            throw new UserNotFoundToGetBalanceException(e.getMessage());
        }
    }

    @Transactional
    public void increaseUserBalance(Long userId, BigDecimal amount) {
//        if (userId == null) {
//            throw new BadRequestException(ErrorMessage.USER_ID_IS_NULL);
//        }
//        Account account = repository.findById(userId).orElseThrow(
//                () -> new UserNotFoundException(ErrorMessage.USER_NOT_FOUND)
//        );
        Account account = findById(userId);
        try {
            account.increaseBalance(amount);
        } catch (IllegalAmountException e) {
            throw new BadRequestException(e.getMessage());
        }
        account = repository.save(account);
        operations.addDeposit(account, amount);
    }

    @Transactional
    public void decreaseUserBalance(Long userId, BigDecimal amount) {
//        if (userId == null) {
//            throw new BadRequestException(ErrorMessage.USER_ID_IS_NULL);
//        }
//        Account account = repository.findById(userId).orElseThrow(
//                () -> new UserNotFoundException(ErrorMessage.USER_NOT_FOUND)
//        );
        Account account = findById(userId);
        try {
            account.decreaseBalance(amount);
        } catch (IllegalAmountException e) {
            throw new BadRequestException(e.getMessage());
        }
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        account = repository.save(account);
        operations.addWithdrawal(account, amount);
    }

    @Transactional
    public Account findById(Long userId) {
        if (userId == null) {
            throw new BadRequestException(ErrorMessage.USER_ID_IS_NULL);
        }
        return repository.findById(userId).orElseThrow(() -> new UserNotFoundException(ErrorMessage.USER_NOT_FOUND));
    }
}
