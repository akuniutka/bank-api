package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.dto.OperationDto;
import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.entity.ErrorMessage;
import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.OperationType;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.repository.AccountRepository;
import dev.akuniutka.bank.api.repository.OperationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class Operations {
    private final AccountRepository accounts;
    private final OperationRepository repository;

    public Operations(OperationRepository repository, AccountRepository accounts) {
        this.repository = repository;
        this.accounts = accounts;
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

    public List<OperationDto> getList(Long userId, Date start, Date finish) {
        List<Operation> operations;
        if (userId == null) {
            throw new BadRequestException(ErrorMessage.USER_ID_IS_NULL);
        }
        Account account = accounts.findById(userId).orElseThrow(
                () -> new UserNotFoundException(ErrorMessage.USER_NOT_FOUND)
        );
        if (start == null && finish == null) {
            operations = repository.findByAccount(account);
        } else if (finish == null) {
            operations = repository.findByAccountAndDateAfter(account, start);
        } else if (start == null) {
            operations = repository.findByAccountAndDateBefore(account, finish);
        } else {
            operations = repository.findByAccountAndDateBetween(account, start, finish);
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
