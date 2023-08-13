package dev.akuniutka.bank.api.repository;

import dev.akuniutka.bank.api.entity.OperationType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class OperationTypeConverter implements AttributeConverter<OperationType, String> {
    @Override
    public String convertToDatabaseColumn(OperationType operationType) {
        if (operationType == null) {
            return null;
        }
        return operationType.getCode();
    }

    @Override
    public OperationType convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }
        return Stream.of(OperationType.values())
                .filter(t -> t.getCode().equals(code))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
