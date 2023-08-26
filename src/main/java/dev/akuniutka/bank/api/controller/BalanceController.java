package dev.akuniutka.bank.api.controller;

import dev.akuniutka.bank.api.dto.CashOrderDto;
import dev.akuniutka.bank.api.dto.ResponseDto;
import dev.akuniutka.bank.api.service.BalanceService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
public class BalanceController {
    private static final ResponseDto OK = new ResponseDto(BigDecimal.ONE);
    private final BalanceService service;

    public BalanceController(BalanceService service) {
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
}
