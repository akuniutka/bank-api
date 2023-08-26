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
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static dev.akuniutka.bank.api.util.ErrorMessage.*;
import static dev.akuniutka.bank.api.util.Amount.*;
import static dev.akuniutka.bank.api.util.DateChecker.isDateBetween;

@ExtendWith(MockitoExtension.class)
class OperationServiceTest {
    private static final int MAX_MOCK_CALLS = 1;
    private Date dateFrom;
    private Date dateTo;
    private Account account;
    private List<Operation> operations;
    private OperationRepository repository;
    private OperationService service;

    @BeforeEach
    public void setUp() {
        dateFrom = mock(Date.class);
        dateTo = mock(Date.class);
        account = mock(Account.class);
        operations = spy(new ArrayList<>());
        repository = mock(OperationRepository.class);
        service = new OperationService(repository);
    }

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(ignoreStubs(dateFrom));
        verifyNoMoreInteractions(ignoreStubs(dateTo));
        verifyNoMoreInteractions(ignoreStubs(account));
        verifyNoMoreInteractions(ignoreStubs(operations));
        verifyNoMoreInteractions(ignoreStubs(repository));
    }

    @Test
    void testCreateDepositWhenAccountIsNull() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> service.createDeposit(null, TEN));
        assertEquals(ACCOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testCreateDepositWhenAmountIsNull() {
        Exception e = assertThrows(IllegalAmountException.class, () -> service.createDeposit(account, NULL));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testCreateDepositWhenAmountIsNegative() {
        Exception e = assertThrows(IllegalAmountException.class, () -> service.createDeposit(account, MINUS_TEN));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
    }

    @Test
    void testCreateDepositWhenAmountIsZero() {
        Exception e = assertThrows(IllegalAmountException.class, () -> service.createDeposit(account, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
    }

    @Test
    void testCreateDepositWhenScaleIsGreaterThanTwoWithNonZeros() {
        Exception e = assertThrows(IllegalAmountException.class, () -> service.createDeposit(account, ONE_THOUSANDTH));
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
    }

    @Test
    void testCreateDepositWhenScaleIsGreaterThanTwoButWithZeros() {
        Date start = new Date();
        Operation operation = service.createDeposit(account, TEN_THOUSANDTHS);
        Date finish = new Date();
        assertNotNull(operation);
        assertNull(operation.getId());
        assertEquals(account, operation.getAccount());
        assertEquals(OperationType.DEPOSIT, operation.getType());
        assertEquals(FORMATTED_TEN_THOUSANDTHS, operation.getAmount());
        assertTrue(isDateBetween(operation.getDate(), start, finish));
    }

    @Test
    void testCreateDepositWhenAmountIsPositive() {
        Date start = new Date();
        Operation operation = service.createDeposit(account, TEN);
        Date finish = new Date();
        assertNotNull(operation);
        assertNull(operation.getId());
        assertEquals(account, operation.getAccount());
        assertEquals(OperationType.DEPOSIT, operation.getType());
        assertEquals(FORMATTED_TEN, operation.getAmount());
        assertTrue(isDateBetween(operation.getDate(), start, finish));
    }

    @Test
    void testCreateWithdrawalWhenAccountIsNull() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> service.createWithdrawal(null, ONE));
        assertEquals(ACCOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testCreateWithdrawalWhenAmountIsNull() {
        Exception e = assertThrows(IllegalAmountException.class, () -> service.createWithdrawal(account, NULL));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testCreateWithdrawalWhenAmountIsNegative() {
        Exception e = assertThrows(IllegalAmountException.class, () -> service.createWithdrawal(account, MINUS_ONE));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
    }

    @Test
    void testCreateWithdrawalWhenAmountIsZero() {
        Exception e = assertThrows(IllegalAmountException.class, () -> service.createWithdrawal(account, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
    }

    @Test
    void testCreateWithdrawalWhenScaleIsGreaterThanTwoWithNonZeros() {
        Exception e = assertThrows(IllegalAmountException.class,
                () -> service.createWithdrawal(account, ONE_THOUSANDTH)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
    }

    @Test
    void testCreateWithdrawalWhenScaleIsGreaterThanTwoButWithZeros() {
        Date start = new Date();
        Operation operation = service.createWithdrawal(account, TEN_THOUSANDTHS);
        Date finish = new Date();
        assertNotNull(operation);
        assertNull(operation.getId());
        assertEquals(account, operation.getAccount());
        assertEquals(OperationType.WITHDRAWAL, operation.getType());
        assertEquals(FORMATTED_TEN_THOUSANDTHS, operation.getAmount());
        assertTrue(isDateBetween(operation.getDate(), start, finish));
    }

    @Test
    void testCreateWithdrawalWhenAmountIsPositive() {
        Date start = new Date();
        Operation operation = service.createWithdrawal(account, ONE);
        Date finish = new Date();
        assertNotNull(operation);
        assertNull(operation.getId());
        assertEquals(account, operation.getAccount());
        assertEquals(OperationType.WITHDRAWAL, operation.getType());
        assertEquals(FORMATTED_ONE, operation.getAmount());
        assertTrue(isDateBetween(operation.getDate(), start, finish));
    }

    @Test
    void testGetOperationsWhenAccountIsNull() {
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> service.getOperations(null, dateFrom, dateTo)
        );
        assertEquals(ACCOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testGetOperationsWhenDateFromIsNullAndDateToIsNull() {
        when(repository.findByAccountOrderByDate(account)).thenReturn(operations);
        assertEquals(operations, service.getOperations(account, null, null));
        verify(repository, times(MAX_MOCK_CALLS)).findByAccountOrderByDate(account);
    }

    @Test
    void testGetOperationsWhenDateFromIsNotNullAndDateToIsNull() {
        when(repository.findByAccountAndDateAfterOrderByDate(account, dateFrom)).thenReturn(operations);
        assertEquals(operations, service.getOperations(account, dateFrom, null));
        verify(repository, times(MAX_MOCK_CALLS)).findByAccountAndDateAfterOrderByDate(account, dateFrom);
    }

    @Test
    void testGetOperationsWhenDateFromIsNullAndDateToIsNotNull() {
        when(repository.findByAccountAndDateBeforeOrderByDate(account, dateTo)).thenReturn(operations);
        assertEquals(operations, service.getOperations(account, null, dateTo));
        verify(repository, times(MAX_MOCK_CALLS)).findByAccountAndDateBeforeOrderByDate(account, dateTo);
    }

    @Test
    void testGetOperationsWhenDateFromIsNotNullAndDateToIsNotNull() {
        when(repository.findByAccountAndDateBetweenOrderByDate(account, dateFrom, dateTo)).thenReturn(operations);
        assertEquals(operations, service.getOperations(account, dateFrom, dateTo));
        verify(repository, times(MAX_MOCK_CALLS)).findByAccountAndDateBetweenOrderByDate(account, dateFrom, dateTo);
    }

    @Test
    void testSaveOperationWhenOperationIsNull() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> service.saveOperation(null));
        assertEquals(OPERATION_IS_NULL, e.getMessage());
    }

    @Test
    void testSaveOperationWhenOperationIsNotNull() {
        Operation operation = mock(Operation.class);
        when(repository.save(operation)).thenReturn(operation);
        assertEquals(operation, service.saveOperation(operation));
        verify(repository, times(MAX_MOCK_CALLS)).save(operation);
        verifyNoMoreInteractions(ignoreStubs(operation));
    }
}