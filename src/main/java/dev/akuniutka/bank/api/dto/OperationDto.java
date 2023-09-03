package dev.akuniutka.bank.api.dto;

import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.util.ErrorMessage;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class OperationDto {
    private final OffsetDateTime date;
    private final String type;
    private final BigDecimal amount;

    public OperationDto(Operation operation) {
        if (operation == null) {
            throw new IllegalArgumentException(ErrorMessage.OPERATION_IS_NULL);
        }
        date = operation.getDate();
        type = operation.getType().getDescription();
        amount = operation.getAmount();
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
