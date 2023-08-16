package dev.akuniutka.bank.api.exception;

public class BackendErrorException extends RuntimeException {
    public BackendErrorException(String errorMessage) {
        super(errorMessage);
    }
}
