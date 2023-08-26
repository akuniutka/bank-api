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
import org.mockito.Mock;
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
    private static final Date DATE_FROM = mock(Date.class);
    private static final Date DATE_TO = mock(Date.class);
    private static final Account ACCOUNT = mock(Account.class);
    @Mock
    private static List<Operation> operations;
    private OperationRepository repository;
    private AccountService accountService;
    private OperationService service;

    @BeforeEach
    public void setUp() {
        repository = mock(OperationRepository.class);
        accountService = mock(AccountService.class);
        service = new OperationService(repository, accountService);
    }

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(ignoreStubs(DATE_FROM));
        verifyNoMoreInteractions(ignoreStubs(DATE_TO));
        verifyNoMoreInteractions(ignoreStubs(ACCOUNT));
        verifyNoMoreInteractions(ignoreStubs(operations));
        verifyNoMoreInteractions(ignoreStubs(repository));
        verifyNoMoreInteractions(ignoreStubs(accountService));
    }

    @Test
    void testCreateDepositWhenAccountIsNull() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> service.createDeposit(null, TEN));
        assertEquals(ACCOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testCreateDepositWhenAmountIsNull() {
        Exception e = assertThrows(IllegalAmountException.class, () -> service.createDeposit(ACCOUNT, NULL));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testCreateDepositWhenAmountIsNegative() {
        Exception e = assertThrows(IllegalAmountException.class, () -> service.createDeposit(ACCOUNT, MINUS_TEN));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
    }

    @Test
    void testCreateDepositWhenAmountIsZero() {
        Exception e = assertThrows(IllegalAmountException.class, () -> service.createDeposit(ACCOUNT, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
    }

    @Test
    void testCreateDepositWhenScaleIsGreaterThanTwoWithNonZeros() {
        Exception e = assertThrows(IllegalAmountException.class, () -> service.createDeposit(ACCOUNT, ONE_THOUSANDTH));
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
    }

    @Test
    void testCreateDepositWhenScaleIsGreaterThanTwoButWithZeros() {
        Date start = new Date();
        Operation operation = service.createDeposit(ACCOUNT, TEN_THOUSANDTHS);
        Date finish = new Date();
        assertNotNull(operation);
        assertNull(operation.getId());
        assertEquals(ACCOUNT, operation.getAccount());
        assertEquals(OperationType.DEPOSIT, operation.getType());
        assertEquals(FORMATTED_TEN_THOUSANDTHS, operation.getAmount());
        assertTrue(isDateBetween(operation.getDate(), start, finish));
    }

    @Test
    void testCreateDepositWhenAmountIsPositive() {
        Date start = new Date();
        Operation operation = service.createDeposit(ACCOUNT, TEN);
        Date finish = new Date();
        assertNotNull(operation);
        assertNull(operation.getId());
        assertEquals(ACCOUNT, operation.getAccount());
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
        Exception e = assertThrows(IllegalAmountException.class, () -> service.createWithdrawal(ACCOUNT, NULL));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testCreateWithdrawalWhenAmountIsNegative() {
        Exception e = assertThrows(IllegalAmountException.class, () -> service.createWithdrawal(ACCOUNT, MINUS_ONE));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
    }

    @Test
    void testCreateWithdrawalWhenAmountIsZero() {
        Exception e = assertThrows(IllegalAmountException.class, () -> service.createWithdrawal(ACCOUNT, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
    }

    @Test
    void testCreateWithdrawalWhenScaleIsGreaterThanTwoWithNonZeros() {
        Exception e = assertThrows(IllegalAmountException.class,
                () -> service.createWithdrawal(ACCOUNT, ONE_THOUSANDTH)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
    }

    @Test
    void testCreateWithdrawalWhenScaleIsGreaterThanTwoButWithZeros() {
        Date start = new Date();
        Operation operation = service.createWithdrawal(ACCOUNT, TEN_THOUSANDTHS);
        Date finish = new Date();
        assertNotNull(operation);
        assertNull(operation.getId());
        assertEquals(ACCOUNT, operation.getAccount());
        assertEquals(OperationType.WITHDRAWAL, operation.getType());
        assertEquals(FORMATTED_TEN_THOUSANDTHS, operation.getAmount());
        assertTrue(isDateBetween(operation.getDate(), start, finish));
    }

    @Test
    void testCreateWithdrawalWhenAmountIsPositive() {
        Date start = new Date();
        Operation operation = service.createWithdrawal(ACCOUNT, ONE);
        Date finish = new Date();
        assertNotNull(operation);
        assertNull(operation.getId());
        assertEquals(ACCOUNT, operation.getAccount());
        assertEquals(OperationType.WITHDRAWAL, operation.getType());
        assertEquals(FORMATTED_ONE, operation.getAmount());
        assertTrue(isDateBetween(operation.getDate(), start, finish));
    }

    @Test
    void testGetOperationsWhenAccountIsNull() {
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> service.getOperations(null, DATE_FROM, DATE_TO)
        );
        assertEquals(ACCOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testGetOperationsWhenDateFromIsNullAndDateToIsNull() {
        when(repository.findByAccountOrderByDate(ACCOUNT)).thenReturn(operations);
        assertEquals(operations, service.getOperations(ACCOUNT, null, null));
        verify(repository, times(MAX_MOCK_CALLS)).findByAccountOrderByDate(ACCOUNT);
    }

    @Test
    void testGetOperationsWhenDateFromIsNotNullAndDateToIsNull() {
        when(repository.findByAccountAndDateAfterOrderByDate(ACCOUNT, DATE_FROM)).thenReturn(operations);
        assertEquals(operations, service.getOperations(ACCOUNT, DATE_FROM, null));
        verify(repository, times(MAX_MOCK_CALLS)).findByAccountAndDateAfterOrderByDate(ACCOUNT, DATE_FROM);
    }

    @Test
    void testGetOperationsWhenDateFromIsNullAndDateToIsNotNull() {
        when(repository.findByAccountAndDateBeforeOrderByDate(ACCOUNT, DATE_TO)).thenReturn(operations);
        assertEquals(operations, service.getOperations(ACCOUNT, null, DATE_TO));
        verify(repository, times(MAX_MOCK_CALLS)).findByAccountAndDateBeforeOrderByDate(ACCOUNT, DATE_TO);
    }

    @Test
    void testGetOperationsWhenDateFromIsNotNullAndDateToIsNotNull() {
        when(repository.findByAccountAndDateBetweenOrderByDate(ACCOUNT, DATE_FROM, DATE_TO)).thenReturn(operations);
        assertEquals(operations, service.getOperations(ACCOUNT, DATE_FROM, DATE_TO));
        verify(repository, times(MAX_MOCK_CALLS)).findByAccountAndDateBetweenOrderByDate(ACCOUNT, DATE_FROM, DATE_TO);
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

    @Test
    void testSaveOperationWithAllRelatedWhenOperationIsNull() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> service.saveOperationWithAllRelated(null));
        assertEquals(OPERATION_IS_NULL, e.getMessage());
    }

    @Test
    void testSaveOperationWithAllRelatedWhenOperationIsNotNull() {
        Operation operation = mock(Operation.class);
        when(operation.getAccount()).thenReturn(ACCOUNT);
        when(accountService.saveAccount(ACCOUNT)).thenReturn(ACCOUNT);
        when(repository.save(operation)).thenAnswer(a -> {
            verify(accountService, times(MAX_MOCK_CALLS)).saveAccount(ACCOUNT);
            return a.getArguments()[0];
        });
        assertEquals(operation, service.saveOperationWithAllRelated(operation));
        verify(repository, times(MAX_MOCK_CALLS)).save(operation);
        verify(operation, times(MAX_MOCK_CALLS)).getAccount();
        verifyNoMoreInteractions(ignoreStubs(operation));
    }
}