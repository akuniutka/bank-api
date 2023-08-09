package dev.akuniutka.bank.api.controller;

import dev.akuniutka.bank.api.dto.CashOrderDto;
import dev.akuniutka.bank.api.dto.ResponseDto;
import dev.akuniutka.bank.api.service.AccountService;
//import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
public class AccountController {
    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @GetMapping("/getBalance/{userId}")
//    @Operation(summary = "Get the current balance for a selected user")
    public ResponseDto getBalance(@PathVariable Long userId) {
        return new ResponseDto(service.getUserBalance(userId));
    }

    @PutMapping("/putMoney")
//    @Operation(summary = "Put money to user's account")
    public ResponseDto putMoney(CashOrderDto order) {
        service.increaseUserBalance(order.getUserId(), order.getAmount());
        return new ResponseDto(BigDecimal.ONE);
    }

    @PutMapping("/takeMoney")
//    @Operation(summary = "Take money from user's account")
    public ResponseDto takeMoney(CashOrderDto order) {
        service.decreaseUserBalance(order.getUserId(), order.getAmount());
        return new ResponseDto(BigDecimal.ONE);
    }

}
