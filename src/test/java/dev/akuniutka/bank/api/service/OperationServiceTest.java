package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.OperationType;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.IllegalAmountException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
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
    private static final Long USER_ID = 1L;
    private Date dateFrom;
    private Date dateTo;
    private Account account;
    private Operation operation;
    private Operation storedOperation;
    private List<Operation> operations;
    private AccountService accountService;
    private OperationRepository repository;
    private OperationService service;

    @BeforeEach
    public void setUp() {
        dateFrom = mock(Date.class);
        dateTo = mock(Date.class);
        account = mock(Account.class);
        operation = mock(Operation.class);
        storedOperation = null;
        operations = spy(new ArrayList<>());
        accountService = mock(AccountService.class);
        repository = mock(OperationRepository.class);
        service = new OperationService(repository, accountService);
    }

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(ignoreStubs(dateFrom));
        verifyNoMoreInteractions(ignoreStubs(dateTo));
        verifyNoMoreInteractions(ignoreStubs(account));
        verifyNoMoreInteractions(ignoreStubs(operation));
        verifyNoMoreInteractions(ignoreStubs(operations));
        verifyNoMoreInteractions(ignoreStubs(accountService));
        verifyNoMoreInteractions(ignoreStubs(repository));
    }

    @Test
    void testCreateDepositWhenUserIdIsNull() {
        when(accountService.increaseUserBalance(null, TEN)).thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class, () -> service.createDeposit(null, TEN));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(accountService).increaseUserBalance(null, TEN);
    }

    @Test
    void testCreateDepositWhenUserDoesNotExist() {
        when(accountService.increaseUserBalance(USER_ID, TEN)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class, () -> service.createDeposit(USER_ID, TEN));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(accountService).increaseUserBalance(USER_ID, TEN);
    }

    @Test
    void testCreateDepositWhenAmountIsNull() {
        when(accountService.increaseUserBalance(USER_ID, null))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_NULL));
        Exception e = assertThrows(IllegalAmountException.class, () -> service.createDeposit(USER_ID, null));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(accountService).increaseUserBalance(USER_ID, null);
    }

    @Test
    void testCreateDepositWhenAmountIsNegative() {
        when(accountService.increaseUserBalance(USER_ID, MINUS_TEN))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_NEGATIVE));
        Exception e = assertThrows(IllegalAmountException.class, () -> service.createDeposit(USER_ID, MINUS_TEN));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(accountService).increaseUserBalance(USER_ID, MINUS_TEN);
    }

    @Test
    void testCreateDepositWhenAmountIsZero() {
        when(accountService.increaseUserBalance(USER_ID, ZERO)).thenThrow(new IllegalAmountException(AMOUNT_IS_ZERO));
        Exception e = assertThrows(IllegalAmountException.class, () -> service.createDeposit(USER_ID, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(accountService).increaseUserBalance(USER_ID, ZERO);
    }

    @Test
    void testCreateDepositWhenScaleIsGreaterThanTwoWithNonZeros() {
        when(accountService.increaseUserBalance(USER_ID, ONE_THOUSANDTH))
                .thenThrow(new IllegalAmountException(WRONG_MINOR_UNITS));
        Exception e = assertThrows(IllegalAmountException.class, () -> service.createDeposit(USER_ID, ONE_THOUSANDTH));
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verify(accountService).increaseUserBalance(USER_ID, ONE_THOUSANDTH);
    }

    @Test
    void testCreateDepositWhenScaleIsGreaterThanTwoButWithZeros() {
        when(accountService.increaseUserBalance(USER_ID, TEN_THOUSANDTHS)).thenReturn(account);
        when(repository.save(any(Operation.class))).thenAnswer(a -> storeOperation(a.getArguments()[0]));
        Date start = new Date();
        Operation actual = service.createDeposit(USER_ID, TEN_THOUSANDTHS);
        Date finish = new Date();
        assertEquals(operation, actual);
        assertNotNull(storedOperation);
        assertNull(storedOperation.getId());
        assertEquals(account, storedOperation.getAccount());
        assertEquals(OperationType.DEPOSIT, storedOperation.getType());
        assertEquals(FORMATTED_TEN_THOUSANDTHS, storedOperation.getAmount());
        assertTrue(isDateBetween(storedOperation.getDate(), start, finish));
        verify(accountService).increaseUserBalance(USER_ID, TEN_THOUSANDTHS);
        verify(repository).save(any(Operation.class));
    }

    @Test
    void testCreateDepositWhenAmountIsPositive() {
        when(accountService.increaseUserBalance(USER_ID, TEN)).thenReturn(account);
        when(repository.save(any(Operation.class))).thenAnswer(a -> storeOperation(a.getArguments()[0]));
        Date start = new Date();
        Operation actual = service.createDeposit(USER_ID, TEN);
        Date finish = new Date();
        assertEquals(operation, actual);
        assertNotNull(storedOperation);
        assertNull(storedOperation.getId());
        assertEquals(account, storedOperation.getAccount());
        assertEquals(OperationType.DEPOSIT, storedOperation.getType());
        assertEquals(FORMATTED_TEN, storedOperation.getAmount());
        assertTrue(isDateBetween(storedOperation.getDate(), start, finish));
        verify(accountService).increaseUserBalance(USER_ID, TEN);
        verify(repository).save(any(Operation.class));
    }

    @Test
    void testCreateWithdrawalWhenUserIdIsNull() {
        when(accountService.decreaseUserBalance(null, ONE)).thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class, () -> service.createWithdrawal(null, ONE));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(accountService).decreaseUserBalance(null, ONE);
    }

    @Test
    void testCreateWithdrawalWhenUserDoesNotExist() {
        when(accountService.decreaseUserBalance(USER_ID, ONE)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class, () -> service.createWithdrawal(USER_ID, ONE));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(accountService).decreaseUserBalance(USER_ID, ONE);
    }

    @Test
    void testCreateWithdrawalWhenAmountIsNull() {
        when(accountService.decreaseUserBalance(USER_ID, null))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_NULL));
        Exception e = assertThrows(IllegalAmountException.class, () -> service.createWithdrawal(USER_ID, null));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(accountService).decreaseUserBalance(USER_ID, null);
    }

    @Test
    void testCreateWithdrawalWhenAmountIsNegative() {
        when(accountService.decreaseUserBalance(USER_ID, MINUS_ONE))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_NEGATIVE));
        Exception e = assertThrows(IllegalAmountException.class, () -> service.createWithdrawal(USER_ID, MINUS_ONE));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(accountService).decreaseUserBalance(USER_ID, MINUS_ONE);
    }

    @Test
    void testCreateWithdrawalWhenAmountIsZero() {
        when(accountService.decreaseUserBalance(USER_ID, ZERO))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_ZERO));
        Exception e = assertThrows(IllegalAmountException.class, () -> service.createWithdrawal(USER_ID, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(accountService).decreaseUserBalance(USER_ID, ZERO);
    }

    @Test
    void testCreateWithdrawalWhenScaleIsGreaterThanTwoWithNonZeros() {
        when(accountService.decreaseUserBalance(USER_ID, ONE_THOUSANDTH))
                .thenThrow(new IllegalAmountException(WRONG_MINOR_UNITS));
        Exception e = assertThrows(IllegalAmountException.class,
                () -> service.createWithdrawal(USER_ID, ONE_THOUSANDTH)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verify(accountService).decreaseUserBalance(USER_ID, ONE_THOUSANDTH);
    }

    @Test
    void testCreateWithdrawalWhenBalanceIsInsufficient() {
        when(accountService.decreaseUserBalance(USER_ID, ONE))
                .thenThrow(new IllegalAmountException(INSUFFICIENT_BALANCE));
        Exception e = assertThrows(IllegalAmountException.class, () -> service.createWithdrawal(USER_ID, ONE));
        assertEquals(INSUFFICIENT_BALANCE, e.getMessage());
        verify(accountService).decreaseUserBalance(USER_ID, ONE);
    }

    @Test
    void testCreateWithdrawalWhenScaleIsGreaterThanTwoButWithZeros() {
        when(accountService.decreaseUserBalance(USER_ID, TEN_THOUSANDTHS)).thenReturn(account);
        when(repository.save(any(Operation.class))).thenAnswer(a -> storeOperation(a.getArguments()[0]));
        Date start = new Date();
        Operation actual = service.createWithdrawal(USER_ID, TEN_THOUSANDTHS);
        Date finish = new Date();
        assertEquals(operation, actual);
        assertNotNull(storedOperation);
        assertNull(storedOperation.getId());
        assertEquals(account, storedOperation.getAccount());
        assertEquals(OperationType.WITHDRAWAL, storedOperation.getType());
        assertEquals(FORMATTED_TEN_THOUSANDTHS, storedOperation.getAmount());
        assertTrue(isDateBetween(storedOperation.getDate(), start, finish));
        verify(accountService).decreaseUserBalance(USER_ID, TEN_THOUSANDTHS);
        verify(repository).save(any(Operation.class));
    }

    @Test
    void testCreateWithdrawalWhenBalanceIsSufficient() {
        when(accountService.decreaseUserBalance(USER_ID, ONE)).thenReturn(account);
        when(repository.save(any(Operation.class))).thenAnswer(a -> storeOperation(a.getArguments()[0]));
        Date start = new Date();
        Operation actual = service.createWithdrawal(USER_ID, ONE);
        Date finish = new Date();
        assertEquals(operation, actual);
        assertNotNull(storedOperation);
        assertNull(storedOperation.getId());
        assertEquals(account, storedOperation.getAccount());
        assertEquals(OperationType.WITHDRAWAL, storedOperation.getType());
        assertEquals(FORMATTED_ONE, storedOperation.getAmount());
        assertTrue(isDateBetween(storedOperation.getDate(), start, finish));
        verify(accountService).decreaseUserBalance(USER_ID, ONE);
        verify(repository).save(any(Operation.class));
    }

    @Test
    void testCreateIncomingTransferWhenUserIdIsNull() {
        when(accountService.increaseUserBalance(null, TEN)).thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class,
                () -> service.createIncomingTransfer(null, TEN, dateTo)
        );
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(accountService).increaseUserBalance(null, TEN);
    }

    @Test
    void testCreateIncomingTransferWhenUserDoesNotExist() {
        when(accountService.increaseUserBalance(USER_ID, TEN)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class,
                () -> service.createIncomingTransfer(USER_ID, TEN, dateTo)
        );
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(accountService).increaseUserBalance(USER_ID, TEN);
    }

    @Test
    void testCreateIncomingTransferWhenAmountIsNull() {
        when(accountService.increaseUserBalance(USER_ID, null))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_NULL));
        Exception e = assertThrows(IllegalAmountException.class,
                () -> service.createIncomingTransfer(USER_ID, null, dateTo));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(accountService).increaseUserBalance(USER_ID, null);
    }

    @Test
    void testCreateIncomingTransferWhenAmountIsNegative() {
        when(accountService.increaseUserBalance(USER_ID, MINUS_TEN))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_NEGATIVE));
        Exception e = assertThrows(IllegalAmountException.class,
                () -> service.createIncomingTransfer(USER_ID, MINUS_TEN, dateTo)
        );
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(accountService).increaseUserBalance(USER_ID, MINUS_TEN);
    }

    @Test
    void testCreateIncomingTransferWhenAmountIsZero() {
        when(accountService.increaseUserBalance(USER_ID, ZERO)).thenThrow(new IllegalAmountException(AMOUNT_IS_ZERO));
        Exception e = assertThrows(IllegalAmountException.class,
                () -> service.createIncomingTransfer(USER_ID, ZERO, dateTo));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(accountService).increaseUserBalance(USER_ID, ZERO);
    }

    @Test
    void testCreateIncomingTransferWhenScaleIsGreaterThanTwoWithNonZeros() {
        when(accountService.increaseUserBalance(USER_ID, ONE_THOUSANDTH))
                .thenThrow(new IllegalAmountException(WRONG_MINOR_UNITS));
        Exception e = assertThrows(IllegalAmountException.class,
                () -> service.createIncomingTransfer(USER_ID, ONE_THOUSANDTH, dateTo)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verify(accountService).increaseUserBalance(USER_ID, ONE_THOUSANDTH);
    }

    @Test
    void testCreateIncomingTransferWhenDateIsNull() {
        when(accountService.increaseUserBalance(USER_ID, TEN)).thenReturn(account);
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> service.createIncomingTransfer(USER_ID, TEN, null)
        );
        assertEquals(DATE_IS_NULL, e.getMessage());
        verify(accountService).increaseUserBalance(USER_ID, TEN);
    }

    @Test
    void testCreateIncomingTransferWhenScaleIsGreaterThanTwoButWithZeros() {
        when(accountService.increaseUserBalance(USER_ID, TEN_THOUSANDTHS)).thenReturn(account);
        when(repository.save(any(Operation.class))).thenAnswer(a -> storeOperation(a.getArguments()[0]));
        lenient().when(dateTo.clone()).thenReturn(dateTo);
        Operation actual = service.createIncomingTransfer(USER_ID, TEN_THOUSANDTHS, dateTo);
        assertEquals(operation, actual);
        assertNotNull(storedOperation);
        assertNull(storedOperation.getId());
        assertEquals(account, storedOperation.getAccount());
        assertEquals(OperationType.INCOMING_TRANSFER, storedOperation.getType());
        assertEquals(FORMATTED_TEN_THOUSANDTHS, storedOperation.getAmount());
        assertEquals(dateTo, storedOperation.getDate());
        verify(accountService).increaseUserBalance(USER_ID, TEN_THOUSANDTHS);
        verify(repository).save(any(Operation.class));
    }

    @Test
    void testCreateIncomingTransferWhenAmountIsPositive() {
        when(accountService.increaseUserBalance(USER_ID, TEN)).thenReturn(account);
        when(repository.save(any(Operation.class))).thenAnswer(a -> storeOperation(a.getArguments()[0]));
        lenient().when(dateTo.clone()).thenReturn(dateTo);
        Operation actual = service.createIncomingTransfer(USER_ID, TEN, dateTo);
        assertEquals(operation, actual);
        assertNotNull(storedOperation);
        assertNull(storedOperation.getId());
        assertEquals(account, storedOperation.getAccount());
        assertEquals(OperationType.INCOMING_TRANSFER, storedOperation.getType());
        assertEquals(FORMATTED_TEN, storedOperation.getAmount());
        assertEquals(dateTo, storedOperation.getDate());
        verify(accountService).increaseUserBalance(USER_ID, TEN);
        verify(repository).save(any(Operation.class));
    }

    @Test
    void testCreateOutgoingTransferWhenUserIdIsNull() {
        when(accountService.decreaseUserBalance(null, ONE)).thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class,
                () -> service.createOutgoingTransfer(null, ONE, dateFrom)
        );
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(accountService).decreaseUserBalance(null, ONE);
    }

    @Test
    void testCreateOutgoingTransferWhenUserDoesNotExist() {
        when(accountService.decreaseUserBalance(USER_ID, ONE)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class,
                () -> service.createOutgoingTransfer(USER_ID, ONE, dateFrom)
        );
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(accountService).decreaseUserBalance(USER_ID, ONE);
    }


    @Test
    void testCreateOutgoingTransferWhenAmountIsNull() {
        when(accountService.decreaseUserBalance(USER_ID, null))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_NULL));
        Exception e = assertThrows(IllegalAmountException.class,
                () -> service.createOutgoingTransfer(USER_ID, null, dateFrom));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(accountService).decreaseUserBalance(USER_ID, null);
    }

    @Test
    void testCreateOutgoingTransferWhenAmountIsNegative() {
        when(accountService.decreaseUserBalance(USER_ID, MINUS_ONE))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_NEGATIVE));
        Exception e = assertThrows(IllegalAmountException.class,
                () -> service.createOutgoingTransfer(USER_ID, MINUS_ONE, dateFrom)
        );
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(accountService).decreaseUserBalance(USER_ID, MINUS_ONE);
    }

    @Test
    void testCreateOutgoingTransferWhenAmountIsZero() {
        when(accountService.decreaseUserBalance(USER_ID, ZERO))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_ZERO));
        Exception e = assertThrows(IllegalAmountException.class,
                () -> service.createOutgoingTransfer(USER_ID, ZERO, dateFrom));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(accountService).decreaseUserBalance(USER_ID, ZERO);
    }

    @Test
    void testCreateOutgoingTransferWhenScaleIsGreaterThanTwoWithNonZeros() {
        when(accountService.decreaseUserBalance(USER_ID, ONE_THOUSANDTH))
                .thenThrow(new IllegalAmountException(WRONG_MINOR_UNITS));
        Exception e = assertThrows(IllegalAmountException.class,
                () -> service.createOutgoingTransfer(USER_ID, ONE_THOUSANDTH, dateFrom)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verify(accountService).decreaseUserBalance(USER_ID, ONE_THOUSANDTH);
    }

    @Test
    void testCreateOutgoingTransferWhenBalanceIsInsufficient() {
        when(accountService.decreaseUserBalance(USER_ID, ONE))
                .thenThrow(new IllegalAmountException(INSUFFICIENT_BALANCE));
        Exception e = assertThrows(IllegalAmountException.class,
                () -> service.createOutgoingTransfer(USER_ID, ONE, dateFrom)
        );
        assertEquals(INSUFFICIENT_BALANCE, e.getMessage());
        verify(accountService).decreaseUserBalance(USER_ID, ONE);
    }

    @Test
    void testCreateOutgoingTransferWhenDateIsNull() {
        when(accountService.decreaseUserBalance(USER_ID, ONE)).thenReturn(account);
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> service.createOutgoingTransfer(USER_ID, ONE, null)
        );
        assertEquals(DATE_IS_NULL, e.getMessage());
        verify(accountService).decreaseUserBalance(USER_ID, ONE);
    }

    @Test
    void testCreateOutgoingTransferWhenScaleIsGreaterThanTwoButWithZeros() {
        when(accountService.decreaseUserBalance(USER_ID, TEN_THOUSANDTHS)).thenReturn(account);
        when(repository.save(any(Operation.class))).thenAnswer(a -> storeOperation(a.getArguments()[0]));
        lenient().when(dateFrom.clone()).thenReturn(dateFrom);
        Operation actual = service.createOutgoingTransfer(USER_ID, TEN_THOUSANDTHS, dateFrom);
        assertEquals(operation, actual);
        assertNotNull(storedOperation);
        assertNull(storedOperation.getId());
        assertEquals(account, storedOperation.getAccount());
        assertEquals(OperationType.OUTGOING_TRANSFER, storedOperation.getType());
        assertEquals(FORMATTED_TEN_THOUSANDTHS, storedOperation.getAmount());
        assertEquals(dateFrom, storedOperation.getDate());
        verify(accountService).decreaseUserBalance(USER_ID, TEN_THOUSANDTHS);
        verify(repository).save(any(Operation.class));
    }

    @Test
    void testCreateOutgoingTransferWhenBalanceIsSufficient() {
        when(accountService.decreaseUserBalance(USER_ID, ONE)).thenReturn(account);
        when(repository.save(any(Operation.class))).thenAnswer(a -> storeOperation(a.getArguments()[0]));
        lenient().when(dateFrom.clone()).thenReturn(dateFrom);
        Operation actual = service.createOutgoingTransfer(USER_ID, ONE, dateFrom);
        assertEquals(operation, actual);
        assertNotNull(storedOperation);
        assertNull(storedOperation.getId());
        assertEquals(account, storedOperation.getAccount());
        assertEquals(OperationType.OUTGOING_TRANSFER, storedOperation.getType());
        assertEquals(FORMATTED_ONE, storedOperation.getAmount());
        assertEquals(dateFrom, storedOperation.getDate());
        verify(accountService).decreaseUserBalance(USER_ID, ONE);
        verify(repository).save(any(Operation.class));
    }

    @Test
    void testGetUserOperationsWhenUserIdIsNull() {
        when(accountService.getAccount(null)).thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class,
                () -> service.getUserOperations(null, dateFrom, dateTo)
        );
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(accountService).getAccount(null);
    }

    @Test
    void testGetUserOperationsWhenUserDoesNotExist() {
        when(accountService.getAccount(USER_ID)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class,
                () -> service.getUserOperations(USER_ID, dateFrom, dateTo)
        );
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(accountService).getAccount(USER_ID);
    }

    @Test
    void testGetUserOperationsWhenDateFromIsNullAndDateToIsNull() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(repository.findByAccountOrderByDate(account)).thenReturn(operations);
        assertEquals(operations, service.getUserOperations(USER_ID, null, null));
        verify(accountService).getAccount(USER_ID);
        verify(repository).findByAccountOrderByDate(account);
    }

    @Test
    void testGetUserOperationsWhenDateFromIsNotNullAndDateToIsNull() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(repository.findByAccountAndDateAfterOrderByDate(account, dateFrom)).thenReturn(operations);
        assertEquals(operations, service.getUserOperations(USER_ID, dateFrom, null));
        verify(accountService).getAccount(USER_ID);
        verify(repository).findByAccountAndDateAfterOrderByDate(account, dateFrom);
    }

    @Test
    void testGetUserOperationsWhenDateFromIsNullAndDateToIsNotNull() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(repository.findByAccountAndDateBeforeOrderByDate(account, dateTo)).thenReturn(operations);
        assertEquals(operations, service.getUserOperations(USER_ID, null, dateTo));
        verify(accountService).getAccount(USER_ID);
        verify(repository).findByAccountAndDateBeforeOrderByDate(account, dateTo);
    }

    @Test
    void testGetUserOperationsWhenDateFromIsNotNullAndDateToIsNotNull() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(repository.findByAccountAndDateBetweenOrderByDate(account, dateFrom, dateTo)).thenReturn(operations);
        assertEquals(operations, service.getUserOperations(USER_ID, dateFrom, dateTo));
        verify(accountService).getAccount(USER_ID);
        verify(repository).findByAccountAndDateBetweenOrderByDate(account, dateFrom, dateTo);
    }

    private Operation storeOperation(Object obj) {
        if (obj instanceof Operation) {
            if (storedOperation == null) {
                storedOperation = (Operation) obj;
                return operation;
            } else {
                throw new RuntimeException("operation already stored");
            }
        } else {
            throw new IllegalArgumentException("argument is not of Operation type");
        }
    }
}