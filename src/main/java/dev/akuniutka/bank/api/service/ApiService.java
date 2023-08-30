package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.exception.IllegalAmountException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.UserNotFoundToGetBalanceException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ApiService {
    private final AccountService accountService;
    private final OperationService operationService;

    public ApiService(AccountService accountService, OperationService operationService) {
        this.accountService = accountService;
        this.operationService = operationService;
    }

    public BigDecimal getBalance(Long userid) {
        try {
            return accountService.getUserBalance(userid);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundToGetBalanceException(e.getMessage());
        }
    }

    public void putMoney(Long userId, BigDecimal amount) {
        try {
            operationService.createDeposit(userId, amount);
        } catch (IllegalAmountException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    public void takeMoney(Long userId, BigDecimal amount) {
        try {
            operationService.createWithdrawal(userId, amount);
        } catch (IllegalAmountException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    public List<Operation> getOperationList(Long userId, Date dateFrom, Date dateTo) {
        return operationService.getUserOperations(userId, dateFrom, dateTo);
    }
}
