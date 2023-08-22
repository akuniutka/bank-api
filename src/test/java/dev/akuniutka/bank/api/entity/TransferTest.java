package dev.akuniutka.bank.api.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static dev.akuniutka.bank.api.util.ErrorMessage.*;

class TransferTest {

    @Test
    void testGetId() {
        Transfer transfer = new Transfer();
        assertNull(transfer.getId());
    }

    @Test
    void testGetDebit() {
        Transfer transfer = new Transfer();
        assertNull(transfer.getDebit());
    }

    @Test
    void testSetDebitWhenOperationIsNull() {
        Transfer transfer = new Transfer();
        Exception e = assertThrows(IllegalArgumentException.class, () -> transfer.setDebit(null));
        assertEquals(TRANSFER_DEBIT_IS_NULL, e.getMessage());
    }

    @Test
    void testSetDebitWhenOperationIsNotNull() {
        Operation operation = new Operation();
        Transfer transfer = new Transfer();
        transfer.setDebit(operation);
        assertEquals(operation, transfer.getDebit());
    }

    @Test
    void testGetCredit() {
        Transfer transfer = new Transfer();
        assertNull(transfer.getCredit());
    }

    @Test
    void testSetCreditWhenOperationIsNull() {
        Transfer transfer = new Transfer();
        Exception e = assertThrows(IllegalArgumentException.class, () -> transfer.setCredit(null));
        assertEquals(TRANSFER_CREDIT_IS_NULL, e.getMessage());
    }

    @Test
    void testSetCreditWhenOperationIsNotNull() {
        Operation operation = new Operation();
        Transfer transfer = new Transfer();
        transfer.setCredit(operation);
        assertEquals(operation, transfer.getCredit());
    }
}