package dev.akuniutka.bank.api.exception;

public class GetBalanceException extends RuntimeException {
    public GetBalanceException(String errorMessage) {
        super(errorMessage);
    }
}
