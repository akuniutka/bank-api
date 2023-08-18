package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.OperationType;
import dev.akuniutka.bank.api.exception.IllegalAmountException;
import dev.akuniutka.bank.api.repository.OperationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static dev.akuniutka.bank.api.entity.ErrorMessage.*;
import static dev.akuniutka.bank.api.Amount.*;

@ExtendWith(MockitoExtension.class)
class OperationsTest {
    private static final Account ACCOUNT = new Account();
    private Operation operation;
    private OperationRepository repository;
    private Operations operations;

    @BeforeEach
    public void setUp() {
        operation = null;
        repository = Mockito.mock(OperationRepository.class);
        operations = new Operations(repository);
    }

    @AfterEach
    public void tearDown() {
        Mockito.verifyNoMoreInteractions(Mockito.ignoreStubs(repository));
    }

    @Test
    void testAddDepositWhenAccountIsNullAndAmountIsPositive() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> operations.addDeposit(null, TEN));
        assertEquals(ACCOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testAddDepositWhenAccountIsNotNullAndAmountIsNull() {
        Exception e = assertThrows(IllegalAmountException.class, () -> operations.addDeposit(ACCOUNT, NULL));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testAddDepositWhenAccountIsNotNullAndAmountIsNegative() {
        Exception e = assertThrows(IllegalAmountException.class, () -> operations.addDeposit(ACCOUNT, MINUS_TEN));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
    }

    @Test
    void testAddDepositWhenAccountIsNotNullAndAmountIsZero() {
        Exception e = assertThrows(IllegalAmountException.class, () -> operations.addDeposit(ACCOUNT, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
    }

    @Test
    void testAddDepositWhenAccountIsNotNullAndScaleIsGreaterThanTwoAndWithNonZeros() {
        Exception e = assertThrows(IllegalAmountException.class,
                () -> operations.addDeposit(ACCOUNT, ONE_THOUSANDTH)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
    }

    @Test
    void testAddDepositWhenAccountIsNotNullAndAmountIsPositive() {
        Mockito.when(repository.save(Mockito.any(Operation.class))).thenAnswer(
                a -> storeOperation(a.getArguments()[0])
        );
        Date start = new Date();
        operations.addDeposit(ACCOUNT, TEN);
        Date finish = new Date();
        assertNotNull(operation);
        assertNull(operation.getId());
        assertEquals(ACCOUNT, operation.getAccount());
        assertEquals(OperationType.DEPOSIT, operation.getType());
        assertEquals(FORMATTED_TEN, operation.getAmount());
        assertTrue(isDateBetween(operation.getDate(), start, finish));
    }

    @Test
    void testAddDepositWhenAccountIsNotNullAndScaleIsGreaterThanTwoButWithZeros() {
        Mockito.when(repository.save(Mockito.any(Operation.class))).thenAnswer(
                a -> storeOperation(a.getArguments()[0])
        );
        Date start = new Date();
        operations.addDeposit(ACCOUNT, TEN_THOUSANDTHS);
        Date finish = new Date();
        assertNotNull(operation);
        assertNull(operation.getId());
        assertEquals(ACCOUNT, operation.getAccount());
        assertEquals(OperationType.DEPOSIT, operation.getType());
        assertEquals(FORMATTED_TEN_THOUSANDTHS, operation.getAmount());
        assertTrue(isDateBetween(operation.getDate(), start, finish));
    }

    @Test
    void testAddWithdrawalWhenAccountIsNullAndAmountIsPositive() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> operations.addWithdrawal(null, ONE));
        assertEquals(ACCOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testAddWithdrawalWhenAccountIsNotNullAndAmountIsNull() {
        Exception e = assertThrows(IllegalAmountException.class, () -> operations.addWithdrawal(ACCOUNT, NULL));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testAddWithdrawalWhenAccountIsNotNullAndAmountIsNegative() {
        Exception e = assertThrows(IllegalAmountException.class, () -> operations.addWithdrawal(ACCOUNT, MINUS_ONE));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
    }

    @Test
    void testAddWithdrawalWhenAccountIsNotNullAndAmountIsZero() {
        Exception e = assertThrows(IllegalAmountException.class, () -> operations.addWithdrawal(ACCOUNT, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
    }

    @Test
    void testAddWithdrawalWhenAccountIsNotNullAndScaleIsGreaterThanTwoAndWithNonZeros() {
        Exception e = assertThrows(IllegalAmountException.class,
                () -> operations.addWithdrawal(ACCOUNT, ONE_THOUSANDTH)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
    }

    @Test
    void testAddWithdrawalWhenAccountIsNotNullAndAmountIsPositive() {
        Mockito.when(repository.save(Mockito.any(Operation.class))).thenAnswer(
                a -> storeOperation(a.getArguments()[0])
        );
        Date start = new Date();
        operations.addWithdrawal(ACCOUNT, TEN);
        Date finish = new Date();
        assertNotNull(operation);
        assertNull(operation.getId());
        assertEquals(ACCOUNT, operation.getAccount());
        assertEquals(OperationType.WITHDRAWAL, operation.getType());
        assertEquals(FORMATTED_TEN, operation.getAmount());
        assertTrue(isDateBetween(operation.getDate(), start, finish));
    }

    @Test
    void testAddWithdrawalWhenAccountIsNotNullAndScaleIsGreaterThanTwoButWithZeros() {
        Mockito.when(repository.save(Mockito.any(Operation.class))).thenAnswer(
                a -> storeOperation(a.getArguments()[0])
        );
        Date start = new Date();
        operations.addWithdrawal(ACCOUNT, TEN_THOUSANDTHS);
        Date finish = new Date();
        assertNotNull(operation);
        assertNull(operation.getId());
        assertEquals(ACCOUNT, operation.getAccount());
        assertEquals(OperationType.WITHDRAWAL, operation.getType());
        assertEquals(FORMATTED_TEN_THOUSANDTHS, operation.getAmount());
        assertTrue(isDateBetween(operation.getDate(), start, finish));
    }

    private Operation storeOperation(Object o) {
        if (operation != null) {
            throw new RuntimeException("operation already has value");
        }
        try {
            operation = (Operation) o;
            return operation;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("argument is not of Operation type");
        }
    }

    private boolean isDateBetween(Date date, Date start, Date finish) {
        if (date == null || start == null || finish == null) {
            return false;
        }
        return start.compareTo(date) * date.compareTo(finish) >= 0;
    }
}