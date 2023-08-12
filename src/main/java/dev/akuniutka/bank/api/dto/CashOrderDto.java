package dev.akuniutka.bank.api.dto;

import java.math.BigDecimal;

public class CashOrderDto {
    private Long userId;
    private BigDecimal amount;

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
