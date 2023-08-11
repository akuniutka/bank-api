package dev.akuniutka.bank.api.dto;

import java.math.BigDecimal;

public class ResponseDto {
    private final BigDecimal result;
    private final String message;

    public ResponseDto(BigDecimal result, String message) {
        if (result == null) {
            throw new IllegalArgumentException("response result is null");
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
