package dev.akuniutka.bank.api.exception;

public class InsufficientFundsException extends GeneralApiException {
    public InsufficientFundsException(String errorMessage) {
        super(errorMessage);
    }
}
