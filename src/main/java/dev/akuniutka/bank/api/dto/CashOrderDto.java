package dev.akuniutka.bank.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class CashOrderDto {
    @JsonProperty("userId")
    private final Long userId;
    @JsonProperty("amount")
    private final BigDecimal amount;

    public CashOrderDto(Long userId, BigDecimal amount) {
        this.userId = userId;
        this.amount = amount;
    }

    public Long getUserId() {
        return userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
