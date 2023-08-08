package dev.akuniutka.bank.api.dto;

import java.math.BigDecimal;

public class ResponseDto {
    private final BigDecimal result;
    private String message = "";

    public ResponseDto(BigDecimal result) {
        if (result == null) {
            throw new IllegalArgumentException("result is null");
        }
        this.result = result;
    }

    public ResponseDto(BigDecimal result, String message) {
        this(result);
        if (message != null) {
            this.message = message;
        }
    }

    public BigDecimal getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }
}
