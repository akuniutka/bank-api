package dev.akuniutka.bank.api.dto;

import dev.akuniutka.bank.api.entity.Operation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static dev.akuniutka.bank.api.util.ErrorMessage.*;

public class OperationDto {
    private final OffsetDateTime date;
    private final String type;
    private final BigDecimal amount;

    public OperationDto(Operation operation) {
        if (operation == null) {
            throw new IllegalArgumentException(OPERATION_IS_NULL);
        } else if (operation.getDate() == null) {
            throw new IllegalArgumentException(DATE_IS_NULL);
        } else if (operation.getType() == null) {
            throw new IllegalArgumentException(OPERATION_TYPE_IS_NULL);
        } else if (operation.getAmount() == null) {
            throw new IllegalArgumentException(AMOUNT_IS_NULL);
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
