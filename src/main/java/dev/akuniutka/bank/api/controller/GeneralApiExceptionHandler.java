package dev.akuniutka.bank.api.controller;

import dev.akuniutka.bank.api.dto.ResponseDto;
import dev.akuniutka.bank.api.exception.CashOrderException;
import dev.akuniutka.bank.api.exception.GetBalanceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.math.BigDecimal;

@RestControllerAdvice
public class GeneralApiExceptionHandler {
    @ExceptionHandler(GetBalanceException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDto catchGetBalanceException(GetBalanceException e) {
        return new ResponseDto(BigDecimal.ONE.negate(), e.getMessage());
    }

    @ExceptionHandler(CashOrderException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ResponseDto catchCashOrderException(CashOrderException e) {
        return new ResponseDto(BigDecimal.ZERO, e.getMessage());
    }
}
