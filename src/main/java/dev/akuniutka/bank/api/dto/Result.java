package dev.akuniutka.bank.api.dto;

import java.math.BigDecimal;
import java.util.Objects;

public class Result {
    private static final String CODE_IS_NULL = "code is null";
    private final BigDecimal code;
    private final String message;

    public Result(BigDecimal code, String message) {
        if (code == null) {
            throw new IllegalArgumentException(CODE_IS_NULL);
        }
        this.code = code;
        this.message = message;
    }

    public BigDecimal getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Result result = (Result) o;

        if (!code.equals(result.code)) return false;
        return Objects.equals(message, result.message);
    }

    @Override
    public int hashCode() {
        int result = code.hashCode();
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }
}
