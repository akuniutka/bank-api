package dev.akuniutka.bank.api.exception;

public class NullUserIdException extends BadRequestException {
    public NullUserIdException(String errorMessage) {
        super(errorMessage);
    }
}
