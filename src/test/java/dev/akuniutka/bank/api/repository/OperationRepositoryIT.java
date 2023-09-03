package dev.akuniutka.bank.api.repository;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.OperationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static dev.akuniutka.bank.api.util.ErrorMessage.*;
import static dev.akuniutka.bank.api.util.Amount.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OperationRepositoryIT {
    private static final ZoneOffset OFFSET = ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now());
    private Account account;
    @Autowired
    private AccountRepository accounts;
    @Autowired
    private OperationRepository repository;

    @BeforeEach
    public void setUp() {
        account = accounts.findById(1069L).orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
    }

    @Test
    void testFindByAccount() {
        OffsetDateTime date = OffsetDateTime.of(LocalDate.parse("2023-01-01"), LocalTime.MIDNIGHT, OFFSET);
        List<Operation> operations = repository.findByAccount(account);
        assertNotNull(operations);
        assertEquals(12, operations.size());
        operations.sort(Comparator.comparing(Operation::getId));
        for (int i = 0; i < 12; i++) {
            Operation operation = operations.get(i);
            assertNotNull(operation);
            assertEquals(i + 1, operation.getId());
            assertEquals(account.getId(), operation.getAccount().getId());
            if (i < 2) {
                assertEquals(OperationType.DEPOSIT, operation.getType());
                assertEquals(FORMATTED_TEN, operation.getAmount());
            } else {
                assertEquals(OperationType.WITHDRAWAL, operation.getType());
                assertEquals(FORMATTED_ONE, operation.getAmount());
            }
            assertTrue(date.isEqual(operation.getDate()));
            date = date.plusMonths(1L);
        }
    }

    @Test
    void testFindByAccountAndDateBefore() {
        OffsetDateTime date = OffsetDateTime.of(LocalDate.parse("2023-07-01"), LocalTime.MIDNIGHT, OFFSET);
        List<Operation> operations = repository.findByAccountAndDateBefore(account, date);
        date = date.withMonth(1);
        assertNotNull(operations);
        assertEquals(6, operations.size());
        operations.sort(Comparator.comparing(Operation::getId));
        for (int i = 0; i < 6; i++) {
            Operation operation = operations.get(i);
            assertNotNull(operation);
            assertEquals(i + 1, operation.getId());
            assertEquals(account.getId(), operation.getAccount().getId());
            if (i < 2) {
                assertEquals(OperationType.DEPOSIT, operation.getType());
                assertEquals(FORMATTED_TEN, operation.getAmount());
            } else {
                assertEquals(OperationType.WITHDRAWAL, operation.getType());
                assertEquals(FORMATTED_ONE, operation.getAmount());
            }
            assertTrue(date.isEqual(operation.getDate()));
            date = date.plusMonths(1L);
        }
    }

    @Test
    void testFindByAccountAndDateAfter() {
        OffsetDateTime date = OffsetDateTime.of(LocalDate.parse("2023-02-01"), LocalTime.MIDNIGHT, OFFSET);
        List<Operation> operations = repository.findByAccountAndDateAfter(account, date);
        assertNotNull(operations);
        assertEquals(11, operations.size());
        operations.sort(Comparator.comparing(Operation::getId));
        for (int i = 0; i < 11; i++) {
            Operation operation = operations.get(i);
            assertNotNull(operation);
            assertEquals(i + 2, operation.getId());
            assertEquals(account.getId(), operation.getAccount().getId());
            if (i < 1) {
                assertEquals(OperationType.DEPOSIT, operation.getType());
                assertEquals(FORMATTED_TEN, operation.getAmount());
            } else {
                assertEquals(OperationType.WITHDRAWAL, operation.getType());
                assertEquals(FORMATTED_ONE, operation.getAmount());
            }
            assertTrue(date.isEqual(operation.getDate()));
            date = date.plusMonths(1L);
        }
    }

    @Test
    void testFindByAccountAndDateBetween() {
        OffsetDateTime start = OffsetDateTime.of(LocalDate.parse("2023-02-01"), LocalTime.MIDNIGHT, OFFSET);
        OffsetDateTime finish = OffsetDateTime.of(LocalDate.parse("2023-04-01"), LocalTime.MIDNIGHT, OFFSET);
        OffsetDateTime date = start;
        List<Operation> operations = repository.findByAccountAndDateBetween(account, start, finish);
        assertNotNull(operations);
        assertEquals(2, operations.size());
        operations.sort(Comparator.comparing(Operation::getId));
        for (int i = 0; i < 2; i++) {
            Operation operation = operations.get(i);
            assertNotNull(operation);
            assertEquals(i + 2, operation.getId());
            assertEquals(account.getId(), operation.getAccount().getId());
            if (i < 1) {
                assertEquals(OperationType.DEPOSIT, operation.getType());
                assertEquals(FORMATTED_TEN, operation.getAmount());
            } else {
                assertEquals(OperationType.WITHDRAWAL, operation.getType());
                assertEquals(FORMATTED_ONE, operation.getAmount());
            }
            assertTrue(date.isEqual(operation.getDate()));
            date = date.plusMonths(1L);
        }
    }

    @Test
    void testSave() {
        Account testAccount = accounts.findById(1093L).orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        OffsetDateTime date = OffsetDateTime.now().plusYears(100L);
        Operation operation = new Operation();
        operation.setAccount(testAccount);
        operation.setType(OperationType.DEPOSIT);
        operation.setAmount(TEN);
        operation.setDate(date);
        operation = repository.save(operation);
        Long id = operation.getId();
        assertNotNull(id);
        Operation actual = repository.findById(id).orElseThrow(() -> new RuntimeException(OPERATIONS_NOT_FOUND));
        assertNotNull(actual);
        assertEquals(id, actual.getId());
        assertEquals(testAccount.getId(), actual.getAccount().getId());
        assertEquals(OperationType.DEPOSIT, actual.getType());
        assertEquals(FORMATTED_TEN, actual.getAmount());
        assertTrue(date.isEqual(actual.getDate()));
    }
}