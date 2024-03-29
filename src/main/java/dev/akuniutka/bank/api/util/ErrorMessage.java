package dev.akuniutka.bank.api.util;

public class ErrorMessage {
    public final static String USER_ID_IS_NULL = "user id is null";
    public final static String USER_NOT_FOUND = "user not found";
    public static final String AMOUNT_IS_NULL = "amount is null";
    public static final String AMOUNT_IS_ZERO = "amount is zero";
    public static final String AMOUNT_IS_NEGATIVE = "amount is negative";
    public static final String WRONG_MINOR_UNITS = "wrong minor units";
    public static final String INSUFFICIENT_BALANCE = "insufficient balance";
    public static final String ACCOUNT_IS_NULL = "account for operation is null";
    public static final String OPERATION_TYPE_IS_NULL = "type of operation is null";
    public static final String DATE_IS_NULL = "date is null";
    public static final String RESULT_IS_NULL = "response result is null";
    public static final String OPERATION_IS_NULL = "operation is null";
    public static final String OPERATIONS_NOT_FOUND = "operations not found";
    public static final String TRANSFER_DEBIT_IS_NULL = "transfer debit is null";
    public static final String TRANSFER_CREDIT_IS_NULL = "transfer credit is null";
    public static final String RECEIVER_ID_IS_NULL = "receiver id is null";
    public static final String RECEIVER_NOT_FOUND = "receiver not found";
    public static final String WRONG_OPERATION_TYPE = "wrong operation type";
    public static final String WRONG_OPERATION_ACCOUNT = "payer and payee are the same";
    public static final String WRONG_OPERATION_AMOUNT = "transfer legs amounts differ";
    public static final String WRONG_OPERATION_DATE = "transfer legs dates differ";
}
