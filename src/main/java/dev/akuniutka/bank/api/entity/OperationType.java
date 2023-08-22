package dev.akuniutka.bank.api.entity;

public enum OperationType {
    DEPOSIT("D", "deposit"),
    WITHDRAWAL("W", "withdrawal"),
    OUTGOING_TRANSFER("P", "outgoing transfer"),
    INCOMING_TRANSFER("R", "incoming transfer");

    private final String code;
    private final String description;

    OperationType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
