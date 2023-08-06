package dev.akuniutka.bank.api.exception;

public class GeneralApiException extends RuntimeException {
    public GeneralApiException(String errorMessage) {
        super(errorMessage);
    }
}
