package dev.akuniutka.bank.api.exception;

public class IllegalAmountException extends RuntimeException {
    public IllegalAmountException(String errorMessage) {
        super(errorMessage);
    }
}
