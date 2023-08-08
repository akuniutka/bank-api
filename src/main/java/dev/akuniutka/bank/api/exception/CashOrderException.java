package dev.akuniutka.bank.api.exception;

public class CashOrderException extends RuntimeException {
    public CashOrderException(String errorMessage) {
        super(errorMessage);
    }
}
