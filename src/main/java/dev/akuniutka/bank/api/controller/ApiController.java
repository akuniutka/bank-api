package dev.akuniutka.bank.api.controller;

import dev.akuniutka.bank.api.dto.CashOrderDto;
import dev.akuniutka.bank.api.dto.OperationDto;
import dev.akuniutka.bank.api.dto.PaymentOrderDto;
import dev.akuniutka.bank.api.dto.ResponseDto;
import dev.akuniutka.bank.api.exception.OperationsNotFoundException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.exception.UserNotFoundToGetBalanceException;
import dev.akuniutka.bank.api.service.AccountService;
import dev.akuniutka.bank.api.service.OperationService;
import dev.akuniutka.bank.api.service.TransferService;
import dev.akuniutka.bank.api.util.ErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ApiController {
    private static final ZoneOffset OFFSET = ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now());
    private static final ResponseDto OK = new ResponseDto(BigDecimal.ONE);
    private final AccountService accountService;
    private final OperationService operationService;
    private final TransferService transferService;

    public ApiController(AccountService accountService,
                         OperationService operationService,
                         TransferService transferService
    ) {
        this.accountService = accountService;
        this.operationService = operationService;
        this.transferService = transferService;
    }

    @GetMapping("/getBalance/{userId}")
    @Operation(summary = "Get the current balance for a selected user")
    public ResponseDto getBalance(@PathVariable Long userId) {
        try {
            return new ResponseDto(accountService.getUserBalance(userId));
        } catch (UserNotFoundException e) {
            throw new UserNotFoundToGetBalanceException(e.getMessage());
        }
    }

    @PutMapping("/putMoney")
    @Operation(summary = "Put money to user's account")
    public ResponseDto putMoney(@RequestBody CashOrderDto order) {
        operationService.createDeposit(order.getUserId(), order.getAmount());
        return OK;
    }

    @PutMapping("/takeMoney")
    @Operation(summary = "Take money from user's account")
    public ResponseDto takeMoney(@RequestBody CashOrderDto order) {
        operationService.createWithdrawal(order.getUserId(), order.getAmount());
        return OK;
    }

    @PutMapping("/transferMoney")
    @Operation(summary = "Transfer money from user's account to receiver's account")
    public ResponseDto transferMoney(@RequestBody PaymentOrderDto order) {
        transferService.createTransfer(order.getUserId(), order.getReceiverId(), order.getAmount());
        return OK;
    }

    @GetMapping("/getOperationList/{userId}")
    @Operation(summary = "Get the list of operations for a selected user (all or foe specified period)")
    public List<OperationDto> getOperationList(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo
    ) {
        List<dev.akuniutka.bank.api.entity.Operation> operations = operationService.getUserOperations(
                userId,
                dateFrom == null ? null : OffsetDateTime.of(dateFrom, LocalTime.MIDNIGHT, OFFSET),
                dateTo == null ? null : OffsetDateTime.of(dateTo, LocalTime.MIDNIGHT, OFFSET)
        );
        if (operations.isEmpty()) {
            throw new OperationsNotFoundException(ErrorMessage.OPERATIONS_NOT_FOUND);
        }
        List<OperationDto> dtoList = new ArrayList<>();
        for (dev.akuniutka.bank.api.entity.Operation operation : operations) {
            dtoList.add(new OperationDto(operation));
        }
        return dtoList;
    }
}
