package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.dto.OperationDto;
import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.entity.ErrorMessage;
import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.OperationType;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.repository.OperationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
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

    public List<OperationDto> getOperations(Long userId, Date start, Date finish) {
        Account account = accountService.getAccount(userId);
        List<Operation> operations;
        if (start == null && finish == null) {
            operations = repository.findByAccountOrderByDate(account);
        } else if (finish == null) {
            operations = repository.findByAccountAndDateAfterOrderByDate(account, start);
        } else if (start == null) {
            operations = repository.findByAccountAndDateBeforeOrderByDate(account, finish);
        } else {
            operations = repository.findByAccountAndDateBetweenOrderByDate(account, start, finish);
        }
        if (operations.isEmpty()) {
            throw new UserNotFoundException(ErrorMessage.OPERATIONS_NOT_FOUND);
        }
        List<OperationDto> dtoList = new ArrayList<>();
        for (Operation operation : operations) {
            dtoList.add(new OperationDto(operation));
        }
        return dtoList;
    }
}
