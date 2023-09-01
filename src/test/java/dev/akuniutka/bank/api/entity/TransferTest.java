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
    void testGetOutgoingTransfer() {
        Transfer transfer = new Transfer();
        assertNull(transfer.getOutgoingTransfer());
    }

    @Test
    void testSetOutgoingTransferWhenOperationIsNull() {
        Transfer transfer = new Transfer();
        Exception e = assertThrows(IllegalArgumentException.class, () -> transfer.setOutgoingTransfer(null));
        assertEquals(TRANSFER_DEBIT_IS_NULL, e.getMessage());
    }

    @Test
    void testSetOutgoingTransferWhenOperationIsNotNull() {
        Operation operation = new Operation();
        Transfer transfer = new Transfer();
        transfer.setOutgoingTransfer(operation);
        assertEquals(operation, transfer.getOutgoingTransfer());
    }

    @Test
    void testGetIncomingTransfer() {
        Transfer transfer = new Transfer();
        assertNull(transfer.getIncomingTransfer());
    }

    @Test
    void testSetIncomingTransferWhenOperationIsNull() {
        Transfer transfer = new Transfer();
        Exception e = assertThrows(IllegalArgumentException.class, () -> transfer.setIncomingTransfer(null));
        assertEquals(TRANSFER_CREDIT_IS_NULL, e.getMessage());
    }

    @Test
    void testSetIncomingTransferWhenOperationIsNotNull() {
        Operation operation = new Operation();
        Transfer transfer = new Transfer();
        transfer.setIncomingTransfer(operation);
        assertEquals(operation, transfer.getIncomingTransfer());
    }
}