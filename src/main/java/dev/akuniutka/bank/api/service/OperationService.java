package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.OperationType;
import dev.akuniutka.bank.api.exception.NullUserIdException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.repository.OperationRepository;
import dev.akuniutka.bank.api.util.ErrorMessage;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class OperationService {
    private final AccountService accountService;
    private final OperationRepository repository;

    public OperationService(OperationRepository repository, AccountService accountService) {
        this.repository = repository;
        this.accountService = accountService;
    }

    public Operation createDeposit(Long userId, BigDecimal amount) {
        Account account = accountService.increaseUserBalance(userId, amount);
        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setType(OperationType.DEPOSIT);
        operation.setAmount(amount);
        operation.setDate(new Date());
        return repository.save(operation);
    }

    public Operation createWithdrawal(Long userId, BigDecimal amount) {
        Account account = accountService.decreaseUserBalance(userId, amount);
        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setType(OperationType.WITHDRAWAL);
        operation.setAmount(amount);
        operation.setDate(new Date());
        return repository.save(operation);
    }

    public Operation createIncomingTransfer(Long userId, BigDecimal amount, Date date) {
        try {
            Account account = accountService.increaseUserBalance(userId, amount);
            Operation operation = new Operation();
            operation.setAccount(account);
            operation.setType(OperationType.INCOMING_TRANSFER);
            operation.setAmount(amount);
            operation.setDate(date);
            return repository.save(operation);
        } catch (NullUserIdException e) {
            throw new NullUserIdException(ErrorMessage.RECEIVER_ID_IS_NULL);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException(ErrorMessage.RECEIVER_NOT_FOUND);
        }
    }

    public Operation createOutgoingTransfer(Long userId, BigDecimal amount, Date date) {
        Account account = accountService.decreaseUserBalance(userId, amount);
        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setType(OperationType.OUTGOING_TRANSFER);
        operation.setAmount(amount);
        operation.setDate(date);
        return repository.save(operation);
    }

    public List<Operation> getUserOperations(Long userId, Date dateFrom, Date dateTo) {
        Account account = accountService.getAccount(userId);
        if (dateFrom == null && dateTo == null) {
            return repository.findByAccountOrderByDate(account);
        } else if (dateFrom == null) {
            return repository.findByAccountAndDateBeforeOrderByDate(account, dateTo);
        } else if (dateTo == null) {
            return repository.findByAccountAndDateAfterOrderByDate(account, dateFrom);
        } else {
            return repository.findByAccountAndDateBetweenOrderByDate(account, dateFrom, dateTo);
        }
    }
}
