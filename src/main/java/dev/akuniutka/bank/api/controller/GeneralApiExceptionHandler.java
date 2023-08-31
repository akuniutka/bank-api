package dev.akuniutka.bank.api.controller;

import dev.akuniutka.bank.api.dto.ResponseDto;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.OperationsNotFoundException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.exception.UserNotFoundToGetBalanceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.math.BigDecimal;

@RestControllerAdvice
public class GeneralApiExceptionHandler {
    @ExceptionHandler(UserNotFoundToGetBalanceException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDto catchUserNotFoundToGetBalanceException(UserNotFoundToGetBalanceException e) {
        return new ResponseDto(BigDecimal.ONE.negate(), e.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDto catchUserNotFoundException(UserNotFoundException e) {
        return new ResponseDto(BigDecimal.ZERO, e.getMessage());
    }

    @ExceptionHandler(OperationsNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDto catchOperationsNotFoundException(OperationsNotFoundException e) {
        return new ResponseDto(BigDecimal.ZERO, e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDto catchBadRequestException(BadRequestException e) {
        return new ResponseDto(BigDecimal.ZERO, e.getMessage());
    }
}
