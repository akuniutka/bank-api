package dev.akuniutka.bank.api.exception;

public class WrongAmountException extends BadRequestException {
    public WrongAmountException(String errorMessage) {
        super(errorMessage);
    }
}
