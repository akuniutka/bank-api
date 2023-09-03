package dev.akuniutka.bank.api.repository;

import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.Transfer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static dev.akuniutka.bank.api.util.ErrorMessage.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransferRepositoryIT {
    @Autowired
    private OperationRepository operations;
    @Autowired
    private TransferRepository repository;

    @Test
    void testFindByOutgoingTransfer() {
        Operation outgoingTransfer = operations.findById(26L).orElseThrow(
                () -> new RuntimeException("operation does not exist")
        );
        List<Transfer> transfers = repository.findByOutgoingTransfer(outgoingTransfer);
        assertNotNull(transfers);
        assertEquals(1, transfers.size());
        Transfer transfer = transfers.get(0);
        assertNotNull(transfer);
        assertEquals(501L, transfer.getId());
    }

    @Test
    void testFindByIncomingTransfer() {
        Operation incomingTransfer = operations.findById(29L).orElseThrow(
                () -> new RuntimeException("operation does not exist")
        );
        List<Transfer> transfers = repository.findByIncomingTransfer(incomingTransfer);
        assertNotNull(transfers);
        assertEquals(1, transfers.size());
        Transfer transfer = transfers.get(0);
        assertNotNull(transfer);
        assertEquals(502L, transfer.getId());
    }

    @Test
    void testSave() {
        Operation outgoingTransfer = operations.findById(30L).orElseThrow(
                () -> new RuntimeException(OPERATIONS_NOT_FOUND)
        );
        Operation incomingTransfer = operations.findById(31L).orElseThrow(
                () -> new RuntimeException(OPERATIONS_NOT_FOUND)
        );
        Transfer transfer = new Transfer();
        transfer.setOutgoingTransfer(outgoingTransfer);
        transfer.setIncomingTransfer(incomingTransfer);
        transfer = repository.save(transfer);
        Long id = transfer.getId();
        Transfer actual = repository.findById(id).orElseThrow(() -> new RuntimeException("transfer not found"));
        assertNotNull(actual);
        assertEquals(id, actual.getId());
        assertEquals(outgoingTransfer.getId(), actual.getOutgoingTransfer().getId());
        assertEquals(incomingTransfer.getId(), actual.getIncomingTransfer().getId());
    }
}