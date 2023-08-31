package dev.akuniutka.bank.api.exception;

public class OperationsNotFoundException extends RuntimeException {
    public OperationsNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
