package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.OperationType;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.UserNotFoundToGetBalanceException;
import dev.akuniutka.bank.api.repository.AccountRepository;
import dev.akuniutka.bank.api.repository.OperationRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Date;

@Service
public class AccountService {
    private final static String USER_ID_IS_NULL = "user id is null";
    private final static String USER_NOT_FOUND = "user not found";
    private final AccountRepository accountRepository;
    private final OperationRepository operationRepository;

    public AccountService(AccountRepository accountRepository, OperationRepository operationRepository) {
        this.accountRepository = accountRepository;
        this.operationRepository = operationRepository;
    }

    @Transactional
    public BigDecimal getUserBalance(Long userId) {
        if (userId == null) {
            throw new BadRequestException(USER_ID_IS_NULL);
        }
        Account account = accountRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundToGetBalanceException(USER_NOT_FOUND)
        );
        return account.getBalance();
    }

    @Transactional
    public void increaseUserBalance(Long userId, BigDecimal amount) {
        if (userId == null) {
            throw new BadRequestException(USER_ID_IS_NULL);
        }
        Account account = accountRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(USER_NOT_FOUND)
        );
        account.increaseBalance(amount);
        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setType(OperationType.DEPOSIT);
        operation.setAmount(amount);
        operation.setDate(new Date());
        accountRepository.save(account);
        operationRepository.save(operation);
    }

    @Transactional
    public void decreaseUserBalance(Long userId, BigDecimal amount) {
        if (userId == null) {
            throw new BadRequestException(USER_ID_IS_NULL);
        }
        Account account = accountRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(USER_NOT_FOUND)
        );
        account.decreaseBalance(amount);
        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setType(OperationType.WITHDRAWAL);
        operation.setAmount(amount);
        operation.setDate(new Date());
        accountRepository.save(account);
        operationRepository.save(operation);
    }
}
