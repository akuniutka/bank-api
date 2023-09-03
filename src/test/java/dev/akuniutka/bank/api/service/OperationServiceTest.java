package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.OperationType;
import dev.akuniutka.bank.api.exception.WrongAmountException;
import dev.akuniutka.bank.api.exception.NullUserIdException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.repository.OperationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static dev.akuniutka.bank.api.util.ErrorMessage.*;
import static dev.akuniutka.bank.api.util.Amount.*;
import static dev.akuniutka.bank.api.util.DateChecker.isDateBetween;

@ExtendWith(MockitoExtension.class)
class OperationServiceTest {
    private static final Long USER_ID = 1L;
    private static final OffsetDateTime DATE_FROM = OffsetDateTime.now();
    private static final OffsetDateTime DATE_TO = OffsetDateTime.now().plusDays(1L);
    private Account account;
    private Operation operation;
    private Operation storedOperation;
    private List<Operation> presetOperations;
    private List<Operation> operations;
    private AccountService accountService;
    private OperationRepository repository;
    private OperationService service;

    @BeforeEach
    public void setUp() {
        account = mock(Account.class);
        operation = mock(Operation.class);
        storedOperation = null;
        presetOperations = new ArrayList<>();
        presetOperations.add(new Operation(account, OperationType.DEPOSIT, TEN, OffsetDateTime.now()));
        presetOperations.add(new Operation(account, OperationType.WITHDRAWAL, ONE, OffsetDateTime.now().minusDays(1L)));
        operations = spy(new ArrayList<>(presetOperations));
        accountService = mock(AccountService.class);
        repository = mock(OperationRepository.class);
        service = new OperationService(repository, accountService);
    }

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(ignoreStubs(account));
        verifyNoMoreInteractions(ignoreStubs(operation));
        verifyNoMoreInteractions(ignoreStubs(operations));
        verifyNoMoreInteractions(ignoreStubs(accountService));
        verifyNoMoreInteractions(ignoreStubs(repository));
    }

    @Test
    void testCreateDepositWhenUserIdIsNull() {
        when(accountService.increaseUserBalance(null, TEN)).thenThrow(new NullUserIdException(USER_ID_IS_NULL));
        Exception e = assertThrows(NullUserIdException.class, () -> service.createDeposit(null, TEN));
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
                .thenThrow(new WrongAmountException(AMOUNT_IS_NULL));
        Exception e = assertThrows(WrongAmountException.class, () -> service.createDeposit(USER_ID, null));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(accountService).increaseUserBalance(USER_ID, null);
    }

    @Test
    void testCreateDepositWhenAmountIsNegative() {
        when(accountService.increaseUserBalance(USER_ID, MINUS_TEN))
                .thenThrow(new WrongAmountException(AMOUNT_IS_NEGATIVE));
        Exception e = assertThrows(WrongAmountException.class, () -> service.createDeposit(USER_ID, MINUS_TEN));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(accountService).increaseUserBalance(USER_ID, MINUS_TEN);
    }

    @Test
    void testCreateDepositWhenAmountIsZero() {
        when(accountService.increaseUserBalance(USER_ID, ZERO)).thenThrow(new WrongAmountException(AMOUNT_IS_ZERO));
        Exception e = assertThrows(WrongAmountException.class, () -> service.createDeposit(USER_ID, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(accountService).increaseUserBalance(USER_ID, ZERO);
    }

    @Test
    void testCreateDepositWhenScaleIsGreaterThanTwoWithNonZeros() {
        when(accountService.increaseUserBalance(USER_ID, ONE_THOUSANDTH))
                .thenThrow(new WrongAmountException(WRONG_MINOR_UNITS));
        Exception e = assertThrows(WrongAmountException.class, () -> service.createDeposit(USER_ID, ONE_THOUSANDTH));
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verify(accountService).increaseUserBalance(USER_ID, ONE_THOUSANDTH);
    }

    @Test
    void testCreateDepositWhenScaleIsGreaterThanTwoButWithZeros() {
        when(accountService.increaseUserBalance(USER_ID, TEN_THOUSANDTHS)).thenReturn(account);
        when(repository.save(any(Operation.class))).thenAnswer(a -> storeOperation(a.getArguments()[0]));
        OffsetDateTime start = OffsetDateTime.now();
        assertDoesNotThrow(() -> service.createDeposit(USER_ID, TEN_THOUSANDTHS));
        OffsetDateTime finish = OffsetDateTime.now();
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
        OffsetDateTime start = OffsetDateTime.now();
        assertDoesNotThrow(() -> service.createDeposit(USER_ID, TEN));
        OffsetDateTime finish = OffsetDateTime.now();
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
        when(accountService.decreaseUserBalance(null, ONE)).thenThrow(new NullUserIdException(USER_ID_IS_NULL));
        Exception e = assertThrows(NullUserIdException.class, () -> service.createWithdrawal(null, ONE));
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
                .thenThrow(new WrongAmountException(AMOUNT_IS_NULL));
        Exception e = assertThrows(WrongAmountException.class, () -> service.createWithdrawal(USER_ID, null));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(accountService).decreaseUserBalance(USER_ID, null);
    }

    @Test
    void testCreateWithdrawalWhenAmountIsNegative() {
        when(accountService.decreaseUserBalance(USER_ID, MINUS_ONE))
                .thenThrow(new WrongAmountException(AMOUNT_IS_NEGATIVE));
        Exception e = assertThrows(WrongAmountException.class, () -> service.createWithdrawal(USER_ID, MINUS_ONE));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(accountService).decreaseUserBalance(USER_ID, MINUS_ONE);
    }

    @Test
    void testCreateWithdrawalWhenAmountIsZero() {
        when(accountService.decreaseUserBalance(USER_ID, ZERO))
                .thenThrow(new WrongAmountException(AMOUNT_IS_ZERO));
        Exception e = assertThrows(WrongAmountException.class, () -> service.createWithdrawal(USER_ID, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(accountService).decreaseUserBalance(USER_ID, ZERO);
    }

    @Test
    void testCreateWithdrawalWhenScaleIsGreaterThanTwoWithNonZeros() {
        when(accountService.decreaseUserBalance(USER_ID, ONE_THOUSANDTH))
                .thenThrow(new WrongAmountException(WRONG_MINOR_UNITS));
        Exception e = assertThrows(WrongAmountException.class,
                () -> service.createWithdrawal(USER_ID, ONE_THOUSANDTH)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verify(accountService).decreaseUserBalance(USER_ID, ONE_THOUSANDTH);
    }

    @Test
    void testCreateWithdrawalWhenBalanceIsInsufficient() {
        when(accountService.decreaseUserBalance(USER_ID, ONE))
                .thenThrow(new WrongAmountException(INSUFFICIENT_BALANCE));
        Exception e = assertThrows(WrongAmountException.class, () -> service.createWithdrawal(USER_ID, ONE));
        assertEquals(INSUFFICIENT_BALANCE, e.getMessage());
        verify(accountService).decreaseUserBalance(USER_ID, ONE);
    }

    @Test
    void testCreateWithdrawalWhenScaleIsGreaterThanTwoButWithZeros() {
        when(accountService.decreaseUserBalance(USER_ID, TEN_THOUSANDTHS)).thenReturn(account);
        when(repository.save(any(Operation.class))).thenAnswer(a -> storeOperation(a.getArguments()[0]));
        OffsetDateTime start = OffsetDateTime.now();
        assertDoesNotThrow(() -> service.createWithdrawal(USER_ID, TEN_THOUSANDTHS));
        OffsetDateTime finish = OffsetDateTime.now();
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
        OffsetDateTime start = OffsetDateTime.now();
        assertDoesNotThrow(() -> service.createWithdrawal(USER_ID, ONE));
        OffsetDateTime finish = OffsetDateTime.now();
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
        when(accountService.increaseUserBalance(null, TEN))
                .thenThrow(new NullUserIdException(RECEIVER_ID_IS_NULL));
        Exception e = assertThrows(NullUserIdException.class,
                () -> service.createIncomingTransfer(null, TEN, DATE_TO)
        );
        assertEquals(RECEIVER_ID_IS_NULL, e.getMessage());
        verify(accountService).increaseUserBalance(null, TEN);
    }

    @Test
    void testCreateIncomingTransferWhenUserDoesNotExist() {
        when(accountService.increaseUserBalance(USER_ID, TEN)).thenThrow(new UserNotFoundException(RECEIVER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class,
                () -> service.createIncomingTransfer(USER_ID, TEN, DATE_TO)
        );
        assertEquals(RECEIVER_NOT_FOUND, e.getMessage());
        verify(accountService).increaseUserBalance(USER_ID, TEN);
    }

    @Test
    void testCreateIncomingTransferWhenAmountIsNull() {
        when(accountService.increaseUserBalance(USER_ID, null))
                .thenThrow(new WrongAmountException(AMOUNT_IS_NULL));
        Exception e = assertThrows(WrongAmountException.class,
                () -> service.createIncomingTransfer(USER_ID, null, DATE_TO));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(accountService).increaseUserBalance(USER_ID, null);
    }

    @Test
    void testCreateIncomingTransferWhenAmountIsNegative() {
        when(accountService.increaseUserBalance(USER_ID, MINUS_TEN))
                .thenThrow(new WrongAmountException(AMOUNT_IS_NEGATIVE));
        Exception e = assertThrows(WrongAmountException.class,
                () -> service.createIncomingTransfer(USER_ID, MINUS_TEN, DATE_TO)
        );
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(accountService).increaseUserBalance(USER_ID, MINUS_TEN);
    }

    @Test
    void testCreateIncomingTransferWhenAmountIsZero() {
        when(accountService.increaseUserBalance(USER_ID, ZERO)).thenThrow(new WrongAmountException(AMOUNT_IS_ZERO));
        Exception e = assertThrows(WrongAmountException.class,
                () -> service.createIncomingTransfer(USER_ID, ZERO, DATE_TO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(accountService).increaseUserBalance(USER_ID, ZERO);
    }

    @Test
    void testCreateIncomingTransferWhenScaleIsGreaterThanTwoWithNonZeros() {
        when(accountService.increaseUserBalance(USER_ID, ONE_THOUSANDTH))
                .thenThrow(new WrongAmountException(WRONG_MINOR_UNITS));
        Exception e = assertThrows(WrongAmountException.class,
                () -> service.createIncomingTransfer(USER_ID, ONE_THOUSANDTH, DATE_TO)
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
        Operation actual = service.createIncomingTransfer(USER_ID, TEN_THOUSANDTHS, DATE_TO);
        assertEquals(operation, actual);
        assertNotNull(storedOperation);
        assertNull(storedOperation.getId());
        assertEquals(account, storedOperation.getAccount());
        assertEquals(OperationType.INCOMING_TRANSFER, storedOperation.getType());
        assertEquals(FORMATTED_TEN_THOUSANDTHS, storedOperation.getAmount());
        assertEquals(DATE_TO, storedOperation.getDate());
        verify(accountService).increaseUserBalance(USER_ID, TEN_THOUSANDTHS);
        verify(repository).save(any(Operation.class));
    }

    @Test
    void testCreateIncomingTransferWhenAmountIsPositive() {
        when(accountService.increaseUserBalance(USER_ID, TEN)).thenReturn(account);
        when(repository.save(any(Operation.class))).thenAnswer(a -> storeOperation(a.getArguments()[0]));
        Operation actual = service.createIncomingTransfer(USER_ID, TEN, DATE_TO);
        assertEquals(operation, actual);
        assertNotNull(storedOperation);
        assertNull(storedOperation.getId());
        assertEquals(account, storedOperation.getAccount());
        assertEquals(OperationType.INCOMING_TRANSFER, storedOperation.getType());
        assertEquals(FORMATTED_TEN, storedOperation.getAmount());
        assertEquals(DATE_TO, storedOperation.getDate());
        verify(accountService).increaseUserBalance(USER_ID, TEN);
        verify(repository).save(any(Operation.class));
    }

    @Test
    void testCreateOutgoingTransferWhenUserIdIsNull() {
        when(accountService.decreaseUserBalance(null, ONE)).thenThrow(new NullUserIdException(USER_ID_IS_NULL));
        Exception e = assertThrows(NullUserIdException.class,
                () -> service.createOutgoingTransfer(null, ONE, DATE_FROM)
        );
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(accountService).decreaseUserBalance(null, ONE);
    }

    @Test
    void testCreateOutgoingTransferWhenUserDoesNotExist() {
        when(accountService.decreaseUserBalance(USER_ID, ONE)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class,
                () -> service.createOutgoingTransfer(USER_ID, ONE, DATE_FROM)
        );
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(accountService).decreaseUserBalance(USER_ID, ONE);
    }

    @Test
    void testCreateOutgoingTransferWhenAmountIsNull() {
        when(accountService.decreaseUserBalance(USER_ID, null))
                .thenThrow(new WrongAmountException(AMOUNT_IS_NULL));
        Exception e = assertThrows(WrongAmountException.class,
                () -> service.createOutgoingTransfer(USER_ID, null, DATE_FROM));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(accountService).decreaseUserBalance(USER_ID, null);
    }

    @Test
    void testCreateOutgoingTransferWhenAmountIsNegative() {
        when(accountService.decreaseUserBalance(USER_ID, MINUS_ONE))
                .thenThrow(new WrongAmountException(AMOUNT_IS_NEGATIVE));
        Exception e = assertThrows(WrongAmountException.class,
                () -> service.createOutgoingTransfer(USER_ID, MINUS_ONE, DATE_FROM)
        );
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(accountService).decreaseUserBalance(USER_ID, MINUS_ONE);
    }

    @Test
    void testCreateOutgoingTransferWhenAmountIsZero() {
        when(accountService.decreaseUserBalance(USER_ID, ZERO))
                .thenThrow(new WrongAmountException(AMOUNT_IS_ZERO));
        Exception e = assertThrows(WrongAmountException.class,
                () -> service.createOutgoingTransfer(USER_ID, ZERO, DATE_FROM));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(accountService).decreaseUserBalance(USER_ID, ZERO);
    }

    @Test
    void testCreateOutgoingTransferWhenScaleIsGreaterThanTwoWithNonZeros() {
        when(accountService.decreaseUserBalance(USER_ID, ONE_THOUSANDTH))
                .thenThrow(new WrongAmountException(WRONG_MINOR_UNITS));
        Exception e = assertThrows(WrongAmountException.class,
                () -> service.createOutgoingTransfer(USER_ID, ONE_THOUSANDTH, DATE_FROM)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verify(accountService).decreaseUserBalance(USER_ID, ONE_THOUSANDTH);
    }

    @Test
    void testCreateOutgoingTransferWhenBalanceIsInsufficient() {
        when(accountService.decreaseUserBalance(USER_ID, ONE))
                .thenThrow(new WrongAmountException(INSUFFICIENT_BALANCE));
        Exception e = assertThrows(WrongAmountException.class,
                () -> service.createOutgoingTransfer(USER_ID, ONE, DATE_FROM)
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
        Operation actual = service.createOutgoingTransfer(USER_ID, TEN_THOUSANDTHS, DATE_FROM);
        assertEquals(operation, actual);
        assertNotNull(storedOperation);
        assertNull(storedOperation.getId());
        assertEquals(account, storedOperation.getAccount());
        assertEquals(OperationType.OUTGOING_TRANSFER, storedOperation.getType());
        assertEquals(FORMATTED_TEN_THOUSANDTHS, storedOperation.getAmount());
        assertEquals(DATE_FROM, storedOperation.getDate());
        verify(accountService).decreaseUserBalance(USER_ID, TEN_THOUSANDTHS);
        verify(repository).save(any(Operation.class));
    }

    @Test
    void testCreateOutgoingTransferWhenBalanceIsSufficient() {
        when(accountService.decreaseUserBalance(USER_ID, ONE)).thenReturn(account);
        when(repository.save(any(Operation.class))).thenAnswer(a -> storeOperation(a.getArguments()[0]));
        Operation actual = service.createOutgoingTransfer(USER_ID, ONE, DATE_FROM);
        assertEquals(operation, actual);
        assertNotNull(storedOperation);
        assertNull(storedOperation.getId());
        assertEquals(account, storedOperation.getAccount());
        assertEquals(OperationType.OUTGOING_TRANSFER, storedOperation.getType());
        assertEquals(FORMATTED_ONE, storedOperation.getAmount());
        assertEquals(DATE_FROM, storedOperation.getDate());
        verify(accountService).decreaseUserBalance(USER_ID, ONE);
        verify(repository).save(any(Operation.class));
    }

    @Test
    void testGetUserOperationsWhenUserIdIsNull() {
        when(accountService.getAccount(null)).thenThrow(new NullUserIdException(USER_ID_IS_NULL));
        Exception e = assertThrows(NullUserIdException.class,
                () -> service.getUserOperations(null, DATE_FROM, DATE_TO)
        );
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(accountService).getAccount(null);
    }

    @Test
    void testGetUserOperationsWhenUserDoesNotExist() {
        when(accountService.getAccount(USER_ID)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class,
                () -> service.getUserOperations(USER_ID, DATE_FROM, DATE_TO)
        );
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(accountService).getAccount(USER_ID);
    }

    @Test
    void testGetUserOperationsWhenDateFromIsNullAndDateToIsNull() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(repository.findByAccount(account)).thenReturn(operations);
        assertEquals(operations, service.getUserOperations(USER_ID, null, null));
        assertSame(operations.get(0), presetOperations.get(1));
        assertSame(operations.get(1), presetOperations.get(0));
        verify(accountService).getAccount(USER_ID);
        verify(repository).findByAccount(account);
        verify(operations).sort(ArgumentMatchers.<Comparator<Operation>>any());
        verify(operations).get(0);
        verify(operations).get(1);
    }

    @Test
    void testGetUserOperationsWhenDateFromIsNotNullAndDateToIsNull() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(repository.findByAccountAndDateAfter(account, DATE_FROM)).thenReturn(operations);
        assertEquals(operations, service.getUserOperations(USER_ID, DATE_FROM, null));
        assertSame(operations.get(0), presetOperations.get(1));
        assertSame(operations.get(1), presetOperations.get(0));
        verify(accountService).getAccount(USER_ID);
        verify(repository).findByAccountAndDateAfter(account, DATE_FROM);
        verify(operations).sort(ArgumentMatchers.<Comparator<Operation>>any());
        verify(operations).get(0);
        verify(operations).get(1);
    }

    @Test
    void testGetUserOperationsWhenDateFromIsNullAndDateToIsNotNull() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(repository.findByAccountAndDateBefore(account, DATE_TO)).thenReturn(operations);
        assertEquals(operations, service.getUserOperations(USER_ID, null, DATE_TO));
        assertSame(operations.get(0), presetOperations.get(1));
        assertSame(operations.get(1), presetOperations.get(0));
        verify(accountService).getAccount(USER_ID);
        verify(repository).findByAccountAndDateBefore(account, DATE_TO);
        verify(operations).sort(ArgumentMatchers.<Comparator<Operation>>any());
        verify(operations).get(0);
        verify(operations).get(1);
    }

    @Test
    void testGetUserOperationsWhenDateFromIsNotNullAndDateToIsNotNull() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(repository.findByAccountAndDateBetween(account, DATE_FROM, DATE_TO)).thenReturn(operations);
        assertEquals(operations, service.getUserOperations(USER_ID, DATE_FROM, DATE_TO));
        assertSame(operations.get(0), presetOperations.get(1));
        assertSame(operations.get(1), presetOperations.get(0));
        verify(accountService).getAccount(USER_ID);
        verify(repository).findByAccountAndDateBetween(account, DATE_FROM, DATE_TO);
        verify(operations).sort(ArgumentMatchers.<Comparator<Operation>>any());
        verify(operations).get(0);
        verify(operations).get(1);
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