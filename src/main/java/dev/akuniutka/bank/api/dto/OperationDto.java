package dev.akuniutka.bank.api.dto;

import dev.akuniutka.bank.api.entity.Operation;

import java.math.BigDecimal;
import java.util.Date;

import static dev.akuniutka.bank.api.entity.ErrorMessage.*;

public class OperationDto {
    private final Date date;
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
        date = (Date) operation.getDate().clone();
        type = operation.getType().getDescription();
        amount = operation.getAmount();
    }

    public Date getDate() {
        return (Date) date.clone();
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
