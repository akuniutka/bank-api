package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.OperationType;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.IllegalAmountException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.repository.OperationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static dev.akuniutka.bank.api.util.ErrorMessage.*;
import static dev.akuniutka.bank.api.Amount.*;

@ExtendWith(MockitoExtension.class)
class OperationServiceTest {
    private static final int MAX_MOCK_CALLS = 1;
    private static final Long USER_ID = 1L;
    private static final Account ACCOUNT = new Account();
    private Operation operation;
    private static final List<Operation> OPERATIONS = new ArrayList<>();
    private static Date start;
    private static Date finish;
    private OperationRepository repository;
    private AccountService accountService;
    private OperationService service;

    @BeforeAll
    static void init() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2023, Calendar.FEBRUARY, 1);
        start = calendar.getTime();
        calendar.set(Calendar.MONTH, Calendar.APRIL);
        finish = calendar.getTime();
        OPERATIONS.add(new Operation());
        OPERATIONS.get(0).setDate(finish);
        OPERATIONS.get(0).setType(OperationType.WITHDRAWAL);
        OPERATIONS.get(0).setAmount(ONE);
        OPERATIONS.add(new Operation());
        OPERATIONS.get(1).setDate(start);
        OPERATIONS.get(1).setType(OperationType.DEPOSIT);
        OPERATIONS.get(1).setAmount(TEN);
    }

    @BeforeEach
    public void setUp() {
        operation = null;
        repository = mock(OperationRepository.class);
        accountService = mock(AccountService.class);
        service = new OperationService(repository, accountService);
    }

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(ignoreStubs(repository));
        verifyNoMoreInteractions(ignoreStubs(accountService));
    }

    @Test
    void testAddDepositWhenAccountIsNullAndAmountIsPositive() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> service.addDeposit(null, TEN));
        assertEquals(ACCOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testAddDepositWhenAccountIsNotNullAndAmountIsNull() {
        Exception e = assertThrows(IllegalAmountException.class, () -> service.addDeposit(ACCOUNT, NULL));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testAddDepositWhenAccountIsNotNullAndAmountIsNegative() {
        Exception e = assertThrows(IllegalAmountException.class, () -> service.addDeposit(ACCOUNT, MINUS_TEN));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
    }

    @Test
    void testAddDepositWhenAccountIsNotNullAndAmountIsZero() {
        Exception e = assertThrows(IllegalAmountException.class, () -> service.addDeposit(ACCOUNT, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
    }

    @Test
    void testAddDepositWhenAccountIsNotNullAndScaleIsGreaterThanTwoAndWithNonZeros() {
        Exception e = assertThrows(IllegalAmountException.class, () -> service.addDeposit(ACCOUNT, ONE_THOUSANDTH));
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
    }

    @Test
    void testAddDepositWhenAccountIsNotNullAndAmountIsPositive() {
        when(repository.save(any(Operation.class))).thenAnswer(a -> storeOperation(a.getArguments()[0]));
        Date start = new Date();
        service.addDeposit(ACCOUNT, TEN);
        Date finish = new Date();
        assertNotNull(operation);
        assertNull(operation.getId());
        assertEquals(ACCOUNT, operation.getAccount());
        assertEquals(OperationType.DEPOSIT, operation.getType());
        assertEquals(FORMATTED_TEN, operation.getAmount());
        assertTrue(isDateBetween(operation.getDate(), start, finish));
        verify(repository, times(MAX_MOCK_CALLS)).save(operation);
    }

    @Test
    void testAddDepositWhenAccountIsNotNullAndScaleIsGreaterThanTwoButWithZeros() {
        when(repository.save(any(Operation.class))).thenAnswer(a -> storeOperation(a.getArguments()[0]));
        Date start = new Date();
        service.addDeposit(ACCOUNT, TEN_THOUSANDTHS);
        Date finish = new Date();
        assertNotNull(operation);
        assertNull(operation.getId());
        assertEquals(ACCOUNT, operation.getAccount());
        assertEquals(OperationType.DEPOSIT, operation.getType());
        assertEquals(FORMATTED_TEN_THOUSANDTHS, operation.getAmount());
        assertTrue(isDateBetween(operation.getDate(), start, finish));
        verify(repository, times(MAX_MOCK_CALLS)).save(operation);
    }

    @Test
    void testAddWithdrawalWhenAccountIsNullAndAmountIsPositive() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> service.addWithdrawal(null, ONE));
        assertEquals(ACCOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testAddWithdrawalWhenAccountIsNotNullAndAmountIsNull() {
        Exception e = assertThrows(IllegalAmountException.class, () -> service.addWithdrawal(ACCOUNT, NULL));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testAddWithdrawalWhenAccountIsNotNullAndAmountIsNegative() {
        Exception e = assertThrows(IllegalAmountException.class, () -> service.addWithdrawal(ACCOUNT, MINUS_ONE));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
    }

    @Test
    void testAddWithdrawalWhenAccountIsNotNullAndAmountIsZero() {
        Exception e = assertThrows(IllegalAmountException.class, () -> service.addWithdrawal(ACCOUNT, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
    }

    @Test
    void testAddWithdrawalWhenAccountIsNotNullAndScaleIsGreaterThanTwoAndWithNonZeros() {
        Exception e = assertThrows(IllegalAmountException.class, () -> service.addWithdrawal(ACCOUNT, ONE_THOUSANDTH));
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
    }

    @Test
    void testAddWithdrawalWhenAccountIsNotNullAndAmountIsPositive() {
        when(repository.save(any(Operation.class))).thenAnswer(a -> storeOperation(a.getArguments()[0]));
        Date start = new Date();
        service.addWithdrawal(ACCOUNT, ONE);
        Date finish = new Date();
        assertNotNull(operation);
        assertNull(operation.getId());
        assertEquals(ACCOUNT, operation.getAccount());
        assertEquals(OperationType.WITHDRAWAL, operation.getType());
        assertEquals(FORMATTED_ONE, operation.getAmount());
        assertTrue(isDateBetween(operation.getDate(), start, finish));
        verify(repository, times((MAX_MOCK_CALLS))).save(operation);
    }

    @Test
    void testAddWithdrawalWhenAccountIsNotNullAndScaleIsGreaterThanTwoButWithZeros() {
        when(repository.save(any(Operation.class))).thenAnswer(a -> storeOperation(a.getArguments()[0]));
        Date start = new Date();
        service.addWithdrawal(ACCOUNT, TEN_THOUSANDTHS);
        Date finish = new Date();
        assertNotNull(operation);
        assertNull(operation.getId());
        assertEquals(ACCOUNT, operation.getAccount());
        assertEquals(OperationType.WITHDRAWAL, operation.getType());
        assertEquals(FORMATTED_TEN_THOUSANDTHS, operation.getAmount());
        assertTrue(isDateBetween(operation.getDate(), start, finish));
        verify(repository, times(MAX_MOCK_CALLS)).save(operation);
    }

    @Test
    void testGetOperationsWhenUserIdIsNull() {
        when(accountService.getAccount(null)).thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class, () -> service.getOperations(null, start, finish));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(null);
    }

    @Test
    void testGetOperationsWhenUserNotFound() {
        when(accountService.getAccount(USER_ID)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class, () -> service.getOperations(USER_ID, start, finish));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
    }

    @Test
    void testGetOperationsWhenOperationsNotFound() {
        when(accountService.getAccount(USER_ID)).thenReturn(ACCOUNT);
        when(repository.findByAccountAndDateBetweenOrderByDate(ACCOUNT, start, finish)).thenReturn(new ArrayList<>());
        Exception e = assertThrows(UserNotFoundException.class, () -> service.getOperations(USER_ID, start, finish));
        assertEquals(OPERATIONS_NOT_FOUND, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(repository, times(MAX_MOCK_CALLS)).findByAccountAndDateBetweenOrderByDate(ACCOUNT, start, finish);
    }

    @Test
    void testGetOperationsWhenUserExistsAndStartIsNullAndFinishIsNull() {
        when(accountService.getAccount(USER_ID)).thenReturn(ACCOUNT);
        when(repository.findByAccountOrderByDate(ACCOUNT)).thenReturn(new ArrayList<>(OPERATIONS));
        assertEquals(OPERATIONS, service.getOperations(USER_ID, null, null));
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(repository, times(MAX_MOCK_CALLS)).findByAccountOrderByDate(ACCOUNT);
    }

    @Test
    void testGetOperationsWhenUserExistsAndStartIsNotNullAndFinishIsNull() {
        when(accountService.getAccount(USER_ID)).thenReturn(ACCOUNT);
        when(repository.findByAccountAndDateAfterOrderByDate(ACCOUNT, start)).thenReturn(new ArrayList<>(OPERATIONS));
        assertEquals(OPERATIONS, service.getOperations(USER_ID, start, null));
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(repository, times(MAX_MOCK_CALLS)).findByAccountAndDateAfterOrderByDate(ACCOUNT, start);
    }

    @Test
    void testGetOperationsWhenUserExistsAndStartIsNullAndFinishIsNotNull() {
        when(accountService.getAccount(USER_ID)).thenReturn(ACCOUNT);
        when(repository.findByAccountAndDateBeforeOrderByDate(ACCOUNT, finish)).thenReturn(new ArrayList<>(OPERATIONS));
        assertEquals(OPERATIONS, service.getOperations(USER_ID, null, finish));
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(repository, times(MAX_MOCK_CALLS)).findByAccountAndDateBeforeOrderByDate(ACCOUNT, finish);
    }

    @Test
    void testGetOperationsWhenUserExistsAndStartIsNotNullAndFinishIsNotNull() {
        when(accountService.getAccount(USER_ID)).thenReturn(ACCOUNT);
        when(repository.findByAccountAndDateBetweenOrderByDate(ACCOUNT, start, finish))
                .thenReturn(new ArrayList<>(OPERATIONS));
        assertEquals(OPERATIONS, service.getOperations(USER_ID, start, finish));
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(repository, times(MAX_MOCK_CALLS)).findByAccountAndDateBetweenOrderByDate(ACCOUNT, start, finish);
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