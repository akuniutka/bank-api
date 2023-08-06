package dev.akuniutka.bank.api.exception;

public class WrongAmountException extends GeneralApiException {
    public WrongAmountException(String errorMessage) {
        super(errorMessage);
    }
}
