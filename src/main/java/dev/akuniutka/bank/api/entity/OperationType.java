package dev.akuniutka.bank.api.entity;

public enum OperationType {
    DEPOSIT("D"),
    WITHDRAWAL("W");

    private final String code;

    OperationType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
