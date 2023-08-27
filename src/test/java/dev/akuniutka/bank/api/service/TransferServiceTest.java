package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.Transfer;
import dev.akuniutka.bank.api.repository.TransferRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static dev.akuniutka.bank.api.util.ErrorMessage.*;

class TransferServiceTest {
    private TransferRepository repository;
    private TransferService service;

    @BeforeEach
    public void setUp() {
        repository = mock(TransferRepository.class);
        service = new TransferService(repository);
    }

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(ignoreStubs(repository));
    }

    @Test
    void testCreateTransferWhenOutgoingIsNull() {
        Operation incoming = mock(Operation.class);
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> service.createTransfer(null, incoming)
        );
        assertEquals(TRANSFER_DEBIT_IS_NULL, e.getMessage());
        verifyNoMoreInteractions(ignoreStubs(incoming));
    }

    @Test
    void testCreateTransferWhenIncomingIsNull() {
        Operation outgoing = mock(Operation.class);
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> service.createTransfer(outgoing, null)
        );
        assertEquals(TRANSFER_CREDIT_IS_NULL, e.getMessage());
        verifyNoMoreInteractions(ignoreStubs(outgoing));
    }

    @Test
    void testCreateTransferWhenOutgoingIsNotNullAndIncomingIsNotNull() {
        Operation outgoing = mock(Operation.class);
        Operation incoming = mock(Operation.class);
        Transfer transfer = service.createTransfer(outgoing, incoming);
        assertNotNull(transfer);
        assertEquals(outgoing, transfer.getDebit());
        assertEquals(incoming, transfer.getCredit());
        verifyNoMoreInteractions(ignoreStubs(outgoing));
        verifyNoMoreInteractions(ignoreStubs(incoming));
    }

    @Test
    void saveTransferWhenTransferIsNull() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> service.saveTransfer(null));
        assertEquals(TRANSFER_IS_NULL, e.getMessage());
    }

    @Test
    void saveTransferWhenTransferIsNotNull() {
        Transfer transfer = mock(Transfer.class);
        when(repository.save(transfer)).thenReturn(transfer);
        assertEquals(transfer, service.saveTransfer(transfer));
        verify(repository).save(transfer);
        verifyNoMoreInteractions(ignoreStubs(transfer));
    }
}