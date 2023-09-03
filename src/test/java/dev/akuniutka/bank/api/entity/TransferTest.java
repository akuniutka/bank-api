package dev.akuniutka.bank.api.entity;

import dev.akuniutka.bank.api.exception.BadRequestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static dev.akuniutka.bank.api.util.ErrorMessage.*;
import static dev.akuniutka.bank.api.util.Amount.*;

class TransferTest {
    private Account payer;
    private Account payee;
    private Operation outgoingTransfer;
    private Operation incomingTransfer;

    @BeforeEach
    public void setUp() {
        payer = mock(Account.class);
        payee = mock(Account.class);
        outgoingTransfer = mock(Operation.class);
        incomingTransfer = mock(Operation.class);
    }

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(ignoreStubs(payer));
        verifyNoMoreInteractions(ignoreStubs(payee));
        verifyNoMoreInteractions(ignoreStubs(outgoingTransfer));
        verifyNoMoreInteractions(ignoreStubs(incomingTransfer));
    }

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
    void testSetOutgoingTransferWhenWrongOperationType() {
        when(outgoingTransfer.getType()).thenReturn(OperationType.WITHDRAWAL);
        Transfer transfer = new Transfer();
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> transfer.setOutgoingTransfer(outgoingTransfer)
        );
        assertEquals(WRONG_OPERATION_TYPE, e.getMessage());
        verify(outgoingTransfer).getType();
    }

    @Test
    void testSetOutgoingTransferWhenFromTheSameAccount() {
        when(payee.getId()).thenReturn(1L);
        when(payer.getId()).thenReturn(1L);
        when(incomingTransfer.getType()).thenReturn(OperationType.INCOMING_TRANSFER);
        when(incomingTransfer.getAccount()).thenReturn(payee);
        when(outgoingTransfer.getType()).thenReturn(OperationType.OUTGOING_TRANSFER);
        when(outgoingTransfer.getAccount()).thenReturn(payer);
        Transfer transfer = new Transfer();
        transfer.setIncomingTransfer(incomingTransfer);
        Exception e = assertThrows(BadRequestException.class,
                () -> transfer.setOutgoingTransfer(outgoingTransfer)
        );
        assertEquals(WRONG_OPERATION_ACCOUNT, e.getMessage());
        verify(incomingTransfer).getType();
        verify(outgoingTransfer).getType();
        verify(incomingTransfer).getAccount();
        verify(outgoingTransfer).getAccount();
        verify(payee).getId();
        verify(payer).getId();
    }

    @Test
    void testSetOutgoingTransferWhenAmountsAreNotEqual() {
        when(payee.getId()).thenReturn(null);
        when(payer.getId()).thenReturn(null);
        when(incomingTransfer.getType()).thenReturn(OperationType.INCOMING_TRANSFER);
        when(incomingTransfer.getAccount()).thenReturn(payee);
        when(incomingTransfer.getAmount()).thenReturn(FORMATTED_TEN);
        when(outgoingTransfer.getType()).thenReturn(OperationType.OUTGOING_TRANSFER);
        when(outgoingTransfer.getAccount()).thenReturn(payer);
        when(outgoingTransfer.getAmount()).thenReturn(FORMATTED_ONE);
        Transfer transfer = new Transfer();
        transfer.setIncomingTransfer(incomingTransfer);
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> transfer.setOutgoingTransfer(outgoingTransfer)
        );
        assertEquals(WRONG_OPERATION_AMOUNT, e.getMessage());
        verify(incomingTransfer).getType();
        verify(outgoingTransfer).getType();
        verify(incomingTransfer).getAccount();
        verify(outgoingTransfer).getAccount();
        verify(payee).getId();
        verify(payer).getId();
        verify(incomingTransfer).getAmount();
        verify(outgoingTransfer).getAmount();
    }

    @Test
    void testSetOutgoingTransferWhenDatesAreNotEqual() {
        OffsetDateTime date = OffsetDateTime.now();
        when(payee.getId()).thenReturn(null);
        when(payer.getId()).thenReturn(null);
        when(incomingTransfer.getType()).thenReturn(OperationType.INCOMING_TRANSFER);
        when(incomingTransfer.getAccount()).thenReturn(payee);
        when(incomingTransfer.getAmount()).thenReturn(FORMATTED_TEN);
        when(incomingTransfer.getDate()).thenReturn(date);
        when(outgoingTransfer.getType()).thenReturn(OperationType.OUTGOING_TRANSFER);
        when(outgoingTransfer.getAccount()).thenReturn(payer);
        when(outgoingTransfer.getAmount()).thenReturn(FORMATTED_TEN);
        when(outgoingTransfer.getDate()).thenReturn(date.plusMinutes(1L));
        Transfer transfer = new Transfer();
        transfer.setIncomingTransfer(incomingTransfer);
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> transfer.setOutgoingTransfer(outgoingTransfer)
        );
        assertEquals(WRONG_OPERATION_DATE, e.getMessage());
        verify(incomingTransfer).getType();
        verify(outgoingTransfer).getType();
        verify(incomingTransfer).getAccount();
        verify(outgoingTransfer).getAccount();
        verify(payee).getId();
        verify(payer).getId();
        verify(incomingTransfer).getAmount();
        verify(outgoingTransfer).getAmount();
        verify(incomingTransfer).getDate();
        verify(outgoingTransfer).getDate();
    }

    @Test
    void testSetOutgoingTransferWhenBothAccountsAreNew() {
        OffsetDateTime date = OffsetDateTime.now();
        when(payee.getId()).thenReturn(null);
        when(payer.getId()).thenReturn(null);
        when(incomingTransfer.getType()).thenReturn(OperationType.INCOMING_TRANSFER);
        when(incomingTransfer.getAccount()).thenReturn(payee);
        when(incomingTransfer.getAmount()).thenReturn(FORMATTED_TEN);
        when(incomingTransfer.getDate()).thenReturn(date);
        when(outgoingTransfer.getType()).thenReturn(OperationType.OUTGOING_TRANSFER);
        when(outgoingTransfer.getAccount()).thenReturn(payer);
        when(outgoingTransfer.getAmount()).thenReturn(FORMATTED_TEN);
        when(outgoingTransfer.getDate()).thenReturn(date);
        Transfer transfer = new Transfer();
        transfer.setIncomingTransfer(incomingTransfer);
        assertDoesNotThrow(() -> transfer.setOutgoingTransfer(outgoingTransfer));
        assertEquals(outgoingTransfer, transfer.getOutgoingTransfer());
        verify(incomingTransfer).getType();
        verify(outgoingTransfer).getType();
        verify(incomingTransfer).getAccount();
        verify(outgoingTransfer).getAccount();
        verify(payee).getId();
        verify(payer).getId();
        verify(incomingTransfer).getAmount();
        verify(outgoingTransfer).getAmount();
        verify(incomingTransfer).getDate();
        verify(outgoingTransfer).getDate();
    }

    @Test
    void testSetOutgoingTransferWhenPayerIsNew() {
        OffsetDateTime date = OffsetDateTime.now();
        when(payee.getId()).thenReturn(1L);
        when(payer.getId()).thenReturn(null);
        when(incomingTransfer.getType()).thenReturn(OperationType.INCOMING_TRANSFER);
        when(incomingTransfer.getAccount()).thenReturn(payee);
        when(incomingTransfer.getAmount()).thenReturn(FORMATTED_TEN);
        when(incomingTransfer.getDate()).thenReturn(date);
        when(outgoingTransfer.getType()).thenReturn(OperationType.OUTGOING_TRANSFER);
        when(outgoingTransfer.getAccount()).thenReturn(payer);
        when(outgoingTransfer.getAmount()).thenReturn(FORMATTED_TEN);
        when(outgoingTransfer.getDate()).thenReturn(date);
        Transfer transfer = new Transfer();
        transfer.setIncomingTransfer(incomingTransfer);
        assertDoesNotThrow(() -> transfer.setOutgoingTransfer(outgoingTransfer));
        assertEquals(outgoingTransfer, transfer.getOutgoingTransfer());
        verify(incomingTransfer).getType();
        verify(outgoingTransfer).getType();
        verify(incomingTransfer).getAccount();
        verify(outgoingTransfer).getAccount();
        verify(payee).getId();
        verify(payer).getId();
        verify(incomingTransfer).getAmount();
        verify(outgoingTransfer).getAmount();
        verify(incomingTransfer).getDate();
        verify(outgoingTransfer).getDate();
    }

    @Test
    void testSetOutgoingTransferWhenPayeeIsNew() {
        OffsetDateTime date = OffsetDateTime.now();
        when(payee.getId()).thenReturn(null);
        when(payer.getId()).thenReturn(2L);
        when(incomingTransfer.getType()).thenReturn(OperationType.INCOMING_TRANSFER);
        when(incomingTransfer.getAccount()).thenReturn(payee);
        when(incomingTransfer.getAmount()).thenReturn(FORMATTED_TEN);
        when(incomingTransfer.getDate()).thenReturn(date);
        when(outgoingTransfer.getType()).thenReturn(OperationType.OUTGOING_TRANSFER);
        when(outgoingTransfer.getAccount()).thenReturn(payer);
        when(outgoingTransfer.getAmount()).thenReturn(FORMATTED_TEN);
        when(outgoingTransfer.getDate()).thenReturn(date);
        Transfer transfer = new Transfer();
        transfer.setIncomingTransfer(incomingTransfer);
        assertDoesNotThrow(() -> transfer.setOutgoingTransfer(outgoingTransfer));
        assertEquals(outgoingTransfer, transfer.getOutgoingTransfer());
        verify(incomingTransfer).getType();
        verify(outgoingTransfer).getType();
        verify(incomingTransfer).getAccount();
        verify(outgoingTransfer).getAccount();
        verify(payee).getId();
        verify(payer).getId();
        verify(incomingTransfer).getAmount();
        verify(outgoingTransfer).getAmount();
        verify(incomingTransfer).getDate();
        verify(outgoingTransfer).getDate();
    }

    @Test
    void testSetOutgoingTransferWhenPayerAndPayeeDiffer() {
        OffsetDateTime date = OffsetDateTime.now();
        when(payee.getId()).thenReturn(1L);
        when(payer.getId()).thenReturn(2L);
        when(incomingTransfer.getType()).thenReturn(OperationType.INCOMING_TRANSFER);
        when(incomingTransfer.getAccount()).thenReturn(payee);
        when(incomingTransfer.getAmount()).thenReturn(FORMATTED_TEN);
        when(incomingTransfer.getDate()).thenReturn(date);
        when(outgoingTransfer.getType()).thenReturn(OperationType.OUTGOING_TRANSFER);
        when(outgoingTransfer.getAccount()).thenReturn(payer);
        when(outgoingTransfer.getAmount()).thenReturn(FORMATTED_TEN);
        when(outgoingTransfer.getDate()).thenReturn(date);
        Transfer transfer = new Transfer();
        transfer.setIncomingTransfer(incomingTransfer);
        assertDoesNotThrow(() -> transfer.setOutgoingTransfer(outgoingTransfer));
        assertEquals(outgoingTransfer, transfer.getOutgoingTransfer());
        verify(incomingTransfer).getType();
        verify(outgoingTransfer).getType();
        verify(incomingTransfer).getAccount();
        verify(outgoingTransfer).getAccount();
        verify(payee).getId();
        verify(payer).getId();
        verify(incomingTransfer).getAmount();
        verify(outgoingTransfer).getAmount();
        verify(incomingTransfer).getDate();
        verify(outgoingTransfer).getDate();
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
    void testSetIncomingTransferWhenWrongOperationType() {
        when(incomingTransfer.getType()).thenReturn(OperationType.DEPOSIT);
        Transfer transfer = new Transfer();
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> transfer.setIncomingTransfer(incomingTransfer)
        );
        assertEquals(WRONG_OPERATION_TYPE, e.getMessage());
        verify(incomingTransfer).getType();
    }

    @Test
    void testSetIncomingTransferWhenFromTheSameAccount() {
        when(payee.getId()).thenReturn(1L);
        when(payer.getId()).thenReturn(1L);
        when(outgoingTransfer.getType()).thenReturn(OperationType.OUTGOING_TRANSFER);
        when(outgoingTransfer.getAccount()).thenReturn(payer);
        when(incomingTransfer.getType()).thenReturn(OperationType.INCOMING_TRANSFER);
        when(incomingTransfer.getAccount()).thenReturn(payee);
        Transfer transfer = new Transfer();
        transfer.setOutgoingTransfer(outgoingTransfer);
        Exception e = assertThrows(BadRequestException.class,
                () -> transfer.setIncomingTransfer(incomingTransfer)
        );
        assertEquals(WRONG_OPERATION_ACCOUNT, e.getMessage());
        verify(outgoingTransfer).getType();
        verify(incomingTransfer).getType();
        verify(outgoingTransfer).getAccount();
        verify(incomingTransfer).getAccount();
        verify(payer).getId();
        verify(payee).getId();
    }

    @Test
    void testSetIncomingTransferWhenAmountsAreNotEqual() {
        when(payee.getId()).thenReturn(null);
        when(payer.getId()).thenReturn(null);
        when(outgoingTransfer.getType()).thenReturn(OperationType.OUTGOING_TRANSFER);
        when(outgoingTransfer.getAccount()).thenReturn(payer);
        when(outgoingTransfer.getAmount()).thenReturn(FORMATTED_ONE);
        when(incomingTransfer.getType()).thenReturn(OperationType.INCOMING_TRANSFER);
        when(incomingTransfer.getAccount()).thenReturn(payee);
        when(incomingTransfer.getAmount()).thenReturn(FORMATTED_TEN);
        Transfer transfer = new Transfer();
        transfer.setOutgoingTransfer(outgoingTransfer);
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> transfer.setIncomingTransfer(incomingTransfer)
        );
        assertEquals(WRONG_OPERATION_AMOUNT, e.getMessage());
        verify(outgoingTransfer).getType();
        verify(incomingTransfer).getType();
        verify(outgoingTransfer).getAccount();
        verify(incomingTransfer).getAccount();
        verify(payer).getId();
        verify(payee).getId();
        verify(outgoingTransfer).getAmount();
        verify(incomingTransfer).getAmount();
    }

    @Test
    void testSetIncomingTransferWhenDatesAreNotEqual() {
        OffsetDateTime date = OffsetDateTime.now();
        when(payee.getId()).thenReturn(null);
        when(payer.getId()).thenReturn(null);
        when(outgoingTransfer.getType()).thenReturn(OperationType.OUTGOING_TRANSFER);
        when(outgoingTransfer.getAccount()).thenReturn(payer);
        when(outgoingTransfer.getAmount()).thenReturn(FORMATTED_TEN);
        when(outgoingTransfer.getDate()).thenReturn(date.plusMinutes(1L));
        when(incomingTransfer.getType()).thenReturn(OperationType.INCOMING_TRANSFER);
        when(incomingTransfer.getAccount()).thenReturn(payee);
        when(incomingTransfer.getAmount()).thenReturn(FORMATTED_TEN);
        when(incomingTransfer.getDate()).thenReturn(date);
        Transfer transfer = new Transfer();
        transfer.setOutgoingTransfer(outgoingTransfer);
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> transfer.setIncomingTransfer(incomingTransfer)
        );
        assertEquals(WRONG_OPERATION_DATE, e.getMessage());
        verify(outgoingTransfer).getType();
        verify(incomingTransfer).getType();
        verify(outgoingTransfer).getAccount();
        verify(incomingTransfer).getAccount();
        verify(payer).getId();
        verify(payee).getId();
        verify(outgoingTransfer).getAmount();
        verify(incomingTransfer).getAmount();
        verify(outgoingTransfer).getDate();
        verify(incomingTransfer).getDate();
    }

    @Test
    void testSetIncomingTransferWhenBothAccountsAreNew() {
        OffsetDateTime date = OffsetDateTime.now();
        when(payee.getId()).thenReturn(null);
        when(payer.getId()).thenReturn(null);
        when(outgoingTransfer.getType()).thenReturn(OperationType.OUTGOING_TRANSFER);
        when(outgoingTransfer.getAccount()).thenReturn(payer);
        when(outgoingTransfer.getAmount()).thenReturn(FORMATTED_TEN);
        when(outgoingTransfer.getDate()).thenReturn(date);
        when(incomingTransfer.getType()).thenReturn(OperationType.INCOMING_TRANSFER);
        when(incomingTransfer.getAccount()).thenReturn(payee);
        when(incomingTransfer.getAmount()).thenReturn(FORMATTED_TEN);
        when(incomingTransfer.getDate()).thenReturn(date);
        Transfer transfer = new Transfer();
        transfer.setOutgoingTransfer(outgoingTransfer);
        assertDoesNotThrow(() -> transfer.setIncomingTransfer(incomingTransfer));
        assertEquals(incomingTransfer, transfer.getIncomingTransfer());
        verify(outgoingTransfer).getType();
        verify(incomingTransfer).getType();
        verify(outgoingTransfer).getAccount();
        verify(incomingTransfer).getAccount();
        verify(payer).getId();
        verify(payee).getId();
        verify(outgoingTransfer).getAmount();
        verify(incomingTransfer).getAmount();
        verify(outgoingTransfer).getDate();
        verify(incomingTransfer).getDate();
    }

    @Test
    void testSetIncomingTransferWhenPayerIsNew() {
        OffsetDateTime date = OffsetDateTime.now();
        when(payee.getId()).thenReturn(1L);
        when(payer.getId()).thenReturn(null);
        when(outgoingTransfer.getType()).thenReturn(OperationType.OUTGOING_TRANSFER);
        when(outgoingTransfer.getAccount()).thenReturn(payer);
        when(outgoingTransfer.getAmount()).thenReturn(FORMATTED_TEN);
        when(outgoingTransfer.getDate()).thenReturn(date);
        when(incomingTransfer.getType()).thenReturn(OperationType.INCOMING_TRANSFER);
        when(incomingTransfer.getAccount()).thenReturn(payee);
        when(incomingTransfer.getAmount()).thenReturn(FORMATTED_TEN);
        when(incomingTransfer.getDate()).thenReturn(date);
        Transfer transfer = new Transfer();
        transfer.setOutgoingTransfer(outgoingTransfer);
        assertDoesNotThrow(() -> transfer.setIncomingTransfer(incomingTransfer));
        assertEquals(incomingTransfer, transfer.getIncomingTransfer());
        verify(outgoingTransfer).getType();
        verify(incomingTransfer).getType();
        verify(outgoingTransfer).getAccount();
        verify(incomingTransfer).getAccount();
        verify(payer).getId();
        verify(payee).getId();
        verify(outgoingTransfer).getAmount();
        verify(incomingTransfer).getAmount();
        verify(outgoingTransfer).getDate();
        verify(incomingTransfer).getDate();
    }

    @Test
    void testSetIncomingTransferWhenPayeeIsNew() {
        OffsetDateTime date = OffsetDateTime.now();
        when(payee.getId()).thenReturn(null);
        when(payer.getId()).thenReturn(2L);
        when(outgoingTransfer.getType()).thenReturn(OperationType.OUTGOING_TRANSFER);
        when(outgoingTransfer.getAccount()).thenReturn(payer);
        when(outgoingTransfer.getAmount()).thenReturn(FORMATTED_TEN);
        when(outgoingTransfer.getDate()).thenReturn(date);
        when(incomingTransfer.getType()).thenReturn(OperationType.INCOMING_TRANSFER);
        when(incomingTransfer.getAccount()).thenReturn(payee);
        when(incomingTransfer.getAmount()).thenReturn(FORMATTED_TEN);
        when(incomingTransfer.getDate()).thenReturn(date);
        Transfer transfer = new Transfer();
        transfer.setOutgoingTransfer(outgoingTransfer);
        assertDoesNotThrow(() -> transfer.setIncomingTransfer(incomingTransfer));
        assertEquals(incomingTransfer, transfer.getIncomingTransfer());
        verify(outgoingTransfer).getType();
        verify(incomingTransfer).getType();
        verify(outgoingTransfer).getAccount();
        verify(incomingTransfer).getAccount();
        verify(payer).getId();
        verify(payee).getId();
        verify(outgoingTransfer).getAmount();
        verify(incomingTransfer).getAmount();
        verify(outgoingTransfer).getDate();
        verify(incomingTransfer).getDate();
    }

    @Test
    void testSetIncomingTransferWhenPayerAndPayeeDiffer() {
        OffsetDateTime date = OffsetDateTime.now();
        when(payee.getId()).thenReturn(1L);
        when(payer.getId()).thenReturn(2L);
        when(outgoingTransfer.getType()).thenReturn(OperationType.OUTGOING_TRANSFER);
        when(outgoingTransfer.getAccount()).thenReturn(payer);
        when(outgoingTransfer.getAmount()).thenReturn(FORMATTED_TEN);
        when(outgoingTransfer.getDate()).thenReturn(date);
        when(incomingTransfer.getType()).thenReturn(OperationType.INCOMING_TRANSFER);
        when(incomingTransfer.getAccount()).thenReturn(payee);
        when(incomingTransfer.getAmount()).thenReturn(FORMATTED_TEN);
        when(incomingTransfer.getDate()).thenReturn(date);
        Transfer transfer = new Transfer();
        transfer.setOutgoingTransfer(outgoingTransfer);
        assertDoesNotThrow(() -> transfer.setIncomingTransfer(incomingTransfer));
        assertEquals(incomingTransfer, transfer.getIncomingTransfer());
        verify(outgoingTransfer).getType();
        verify(incomingTransfer).getType();
        verify(outgoingTransfer).getAccount();
        verify(incomingTransfer).getAccount();
        verify(payer).getId();
        verify(payee).getId();
        verify(outgoingTransfer).getAmount();
        verify(incomingTransfer).getAmount();
        verify(outgoingTransfer).getDate();
        verify(incomingTransfer).getDate();
    }
}