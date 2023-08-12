package dev.akuniutka.bank.api.exception;

public class UserNotFoundToGetBalanceException extends RuntimeException {
    public UserNotFoundToGetBalanceException(String errorMessage) {
        super(errorMessage);
    }
}
