package dev.akuniutka.bank.api.repository;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.OperationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static dev.akuniutka.bank.api.entity.ErrorMessage.*;
import static dev.akuniutka.bank.api.Amount.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OperationRepositoryIT {
    private Account account;
    @Autowired
    private AccountRepository accounts;
    @Autowired
    private OperationRepository repository;

    @BeforeEach
    public void setUp() {
        account = accounts.findById(1019L).orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
    }

    @Test
    void testFindByAccount() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2023, Calendar.JANUARY, 1);
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
            assertEquals(calendar.getTime(), operation.getDate());
            calendar.add(Calendar.MONTH, 1);
        }
    }

    @Test
    void testFindByAccountAndDateBefore() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2023, Calendar.JULY, 1);
        List<Operation> operations = repository.findByAccountAndDateBefore(account, calendar.getTime());
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
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
            assertEquals(calendar.getTime(), operation.getDate());
            calendar.add(Calendar.MONTH, 1);
        }
    }

    @Test
    void testFindByAccountAndDateAfter() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2023, Calendar.FEBRUARY, 1);
        List<Operation> operations = repository.findByAccountAndDateAfter(account, calendar.getTime());
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
            assertEquals(calendar.getTime(), operation.getDate());
            calendar.add(Calendar.MONTH, 1);
        }
    }

    @Test
    void testFindByAccountAndDateBetween() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2023, Calendar.APRIL, 1);
        Date finish = calendar.getTime();
        calendar.set(Calendar.MONTH, Calendar.FEBRUARY);
        Date start = calendar.getTime();
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
            assertEquals(calendar.getTime(), operation.getDate());
            calendar.add(Calendar.MONTH, 1);
        }
    }
}