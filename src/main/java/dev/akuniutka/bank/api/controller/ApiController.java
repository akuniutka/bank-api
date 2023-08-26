package dev.akuniutka.bank.api.controller;

import dev.akuniutka.bank.api.dto.CashOrderDto;
import dev.akuniutka.bank.api.dto.OperationDto;
import dev.akuniutka.bank.api.dto.ResponseDto;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.service.ApiService;
import dev.akuniutka.bank.api.util.ErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class ApiController {
    private static final ResponseDto OK = new ResponseDto(BigDecimal.ONE);
    private final ApiService service;

    public ApiController(ApiService service) {
        this.service = service;
    }

    @GetMapping("/getBalance/{userId}")
    @Operation(summary = "Get the current balance for a selected user")
    public ResponseDto getBalance(@PathVariable Long userId) {
        return new ResponseDto(service.getBalance(userId));
    }

    @PutMapping("/putMoney")
    @Operation(summary = "Put money to user's account")
    public ResponseDto putMoney(@RequestBody CashOrderDto order) {
        service.putMoney(order.getUserId(), order.getAmount());
        return OK;
    }

    @PutMapping("/takeMoney")
    @Operation(summary = "Take money from user's account")
    public ResponseDto takeMoney(@RequestBody CashOrderDto order) {
        service.takeMoney(order.getUserId(), order.getAmount());
        return OK;
    }

    @GetMapping("/getOperationList/{userId}")
    @Operation(summary = "Get the list of operations for a selected user (all or foe specified period)")
    public List<OperationDto> getOperationList(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateTo
    ) {
        List<dev.akuniutka.bank.api.entity.Operation> operations = service.getOperationList(userId, dateFrom, dateTo);
        if (operations.isEmpty()) {
            throw new UserNotFoundException(ErrorMessage.OPERATIONS_NOT_FOUND);
        }
        List<OperationDto> dtoList = new ArrayList<>();
        for (dev.akuniutka.bank.api.entity.Operation operation : operations) {
            dtoList.add(new OperationDto(operation));
        }
        return dtoList;
    }
}
