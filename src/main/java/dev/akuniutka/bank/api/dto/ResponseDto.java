package dev.akuniutka.bank.api.dto;

import java.math.BigDecimal;

import dev.akuniutka.bank.api.util.ErrorMessage;

public class ResponseDto {
    private final BigDecimal result;
    private final String message;

    public ResponseDto(BigDecimal result, String message) {
        if (result == null) {
            throw new IllegalArgumentException(ErrorMessage.RESULT_IS_NULL);
        }
        this.result = result;
        this.message = message == null ? "" : message;
    }

    public ResponseDto(BigDecimal result) {
        this(result, "");
    }

    public BigDecimal getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }
}
