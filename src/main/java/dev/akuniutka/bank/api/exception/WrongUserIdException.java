package dev.akuniutka.bank.api.exception;

public class WrongUserIdException extends GeneralApiException {
    public WrongUserIdException(String errorMessage) {
        super(errorMessage);
    }
}
