package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.util.ErrorMessage;
import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.OperationType;
import dev.akuniutka.bank.api.repository.OperationRepository;
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

    public void addDeposit(Account account, BigDecimal amount) {
        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setType(OperationType.DEPOSIT);
        operation.setAmount(amount);
        operation.setDate(new Date());
        repository.save(operation);
    }

    public void addWithdrawal(Account account, BigDecimal amount) {
        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setType(OperationType.WITHDRAWAL);
        operation.setAmount(amount);
        operation.setDate(new Date());
        repository.save(operation);
    }

    public Operation createDeposit(Account account, BigDecimal amount) {
        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setType(OperationType.DEPOSIT);
        operation.setAmount(amount);
        operation.setDate(new Date());
        return operation;
    }

    public Operation createWithdrawal(Account account, BigDecimal amount) {
        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setType(OperationType.WITHDRAWAL);
        operation.setAmount(amount);
        operation.setDate(new Date());
        return operation;
    }

    public List<Operation> getOperations(Account account, Date dateFrom, Date dateTo) {
        if (account == null) {
            throw new IllegalArgumentException(ErrorMessage.ACCOUNT_IS_NULL);
        }
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

    public Operation saveOperation(Operation operation) {
        return saveOperation(operation, false);
    }

    public Operation saveOperationWithAllRelated(Operation operation) {
        return saveOperation(operation, true);
    }

    private Operation saveOperation(Operation operation, boolean saveRelated) {
        if (operation == null) {
            throw new IllegalArgumentException(ErrorMessage.OPERATION_IS_NULL);
        }
        if (saveRelated) {
            accountService.saveAccount(operation.getAccount());
        }
        return repository.save(operation);
    }

    // TODO: do not forget to remove
    public List<Operation> getOperations0(Long userId, Date dateFrom, Date dateTo) {
        Account account = accountService.getAccount(userId);
        return getOperations(account, dateFrom, dateTo);
    }
}
