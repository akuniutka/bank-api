package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.dto.OperationDto;
import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.OperationType;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.IllegalAmountException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.repository.AccountRepository;
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
import static dev.akuniutka.bank.api.entity.ErrorMessage.*;
import static dev.akuniutka.bank.api.Amount.*;

@ExtendWith(MockitoExtension.class)
class OperationServiceTest {
    private static final int MAX_MOCK_CALLS = 1;
    private static final Long USER_ID = 1L;
    private static final Account ACCOUNT = new Account();
    private AccountRepository accounts;
    private Operation operation;
    private OperationRepository repository;
    private OperationService operationService;
    private static final List<Operation> operationList = new ArrayList<>();
    private static Date start;
    private static Date finish;

    @BeforeAll
    static void init() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2023, Calendar.FEBRUARY, 1);
        start = calendar.getTime();
        calendar.set(Calendar.MONTH, Calendar.APRIL);
        finish = calendar.getTime();
        operationList.add(new Operation());
        operationList.get(0).setDate(finish);
        operationList.get(0).setType(OperationType.WITHDRAWAL);
        operationList.get(0).setAmount(ONE);
        operationList.add(new Operation());
        operationList.get(1).setDate(start);
        operationList.get(1).setType(OperationType.DEPOSIT);
        operationList.get(1).setAmount(TEN);
    }

    @BeforeEach
    public void setUp() {
        accounts = mock(AccountRepository.class);
        operation = null;
        repository = mock(OperationRepository.class);
        operationService = new OperationService(repository, accounts);
    }

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(ignoreStubs(repository));
        verifyNoMoreInteractions(ignoreStubs(accounts));
    }

    @Test
    void testAddDepositWhenAccountIsNullAndAmountIsPositive() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> operationService.addDeposit(null, TEN));
        assertEquals(ACCOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testAddDepositWhenAccountIsNotNullAndAmountIsNull() {
        Exception e = assertThrows(IllegalAmountException.class, () -> operationService.addDeposit(ACCOUNT, NULL));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testAddDepositWhenAccountIsNotNullAndAmountIsNegative() {
        Exception e = assertThrows(IllegalAmountException.class, () -> operationService.addDeposit(ACCOUNT, MINUS_TEN));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
    }

    @Test
    void testAddDepositWhenAccountIsNotNullAndAmountIsZero() {
        Exception e = assertThrows(IllegalAmountException.class, () -> operationService.addDeposit(ACCOUNT, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
    }

    @Test
    void testAddDepositWhenAccountIsNotNullAndScaleIsGreaterThanTwoAndWithNonZeros() {
        Exception e = assertThrows(IllegalAmountException.class,
                () -> operationService.addDeposit(ACCOUNT, ONE_THOUSANDTH)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
    }

    @Test
    void testAddDepositWhenAccountIsNotNullAndAmountIsPositive() {
        when(repository.save(any(Operation.class))).thenAnswer(a -> storeOperation(a.getArguments()[0]));
        Date start = new Date();
        operationService.addDeposit(ACCOUNT, TEN);
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
        when(repository.save(any(Operation.class))).thenAnswer(a -> storeOperation(a.getArguments()[0]));
        Date start = new Date();
        operationService.addDeposit(ACCOUNT, TEN_THOUSANDTHS);
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
        Exception e = assertThrows(IllegalArgumentException.class, () -> operationService.addWithdrawal(null, ONE));
        assertEquals(ACCOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testAddWithdrawalWhenAccountIsNotNullAndAmountIsNull() {
        Exception e = assertThrows(IllegalAmountException.class, () -> operationService.addWithdrawal(ACCOUNT, NULL));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testAddWithdrawalWhenAccountIsNotNullAndAmountIsNegative() {
        Exception e = assertThrows(IllegalAmountException.class, () -> operationService.addWithdrawal(ACCOUNT, MINUS_ONE));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
    }

    @Test
    void testAddWithdrawalWhenAccountIsNotNullAndAmountIsZero() {
        Exception e = assertThrows(IllegalAmountException.class, () -> operationService.addWithdrawal(ACCOUNT, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
    }

    @Test
    void testAddWithdrawalWhenAccountIsNotNullAndScaleIsGreaterThanTwoAndWithNonZeros() {
        Exception e = assertThrows(IllegalAmountException.class,
                () -> operationService.addWithdrawal(ACCOUNT, ONE_THOUSANDTH)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
    }

    @Test
    void testAddWithdrawalWhenAccountIsNotNullAndAmountIsPositive() {
        when(repository.save(any(Operation.class))).thenAnswer(a -> storeOperation(a.getArguments()[0]));
        Date start = new Date();
        operationService.addWithdrawal(ACCOUNT, TEN);
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
        when(repository.save(any(Operation.class))).thenAnswer(a -> storeOperation(a.getArguments()[0]));
        Date start = new Date();
        operationService.addWithdrawal(ACCOUNT, TEN_THOUSANDTHS);
        Date finish = new Date();
        assertNotNull(operation);
        assertNull(operation.getId());
        assertEquals(ACCOUNT, operation.getAccount());
        assertEquals(OperationType.WITHDRAWAL, operation.getType());
        assertEquals(FORMATTED_TEN_THOUSANDTHS, operation.getAmount());
        assertTrue(isDateBetween(operation.getDate(), start, finish));
    }

    @Test
    void testGetListWhenUserIdIsNull() {
        Exception e = assertThrows(BadRequestException.class, () -> operationService.getList(null, start, finish));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
    }

    @Test
    void testGetListWhenUserNotFound() {
        when(accounts.findById(USER_ID)).thenReturn(Optional.empty());
        Exception e = assertThrows(UserNotFoundException.class, () -> operationService.getList(USER_ID, start, finish));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(accounts, times(MAX_MOCK_CALLS)).findById(USER_ID);
    }

    @Test
    void testGetListWhenOperationsNotFound() {
        when(accounts.findById(USER_ID)).thenReturn(Optional.of(ACCOUNT));
        when(repository.findByAccountAndDateBetween(ACCOUNT, start, finish)).thenReturn(new ArrayList<>());
        Exception e = assertThrows(UserNotFoundException.class, () -> operationService.getList(USER_ID, start, finish));
        assertEquals(OPERATIONS_NOT_FOUND, e.getMessage());
        verify(accounts, times(MAX_MOCK_CALLS)).findById(USER_ID);
        verify(repository, times(MAX_MOCK_CALLS)).findByAccountAndDateBetween(ACCOUNT, start, finish);
    }

    @Test
    void testGetListWhenUserExistsAndStartIsNullAndFinishIsNull() {
        when(accounts.findById(USER_ID)).thenReturn(Optional.of(ACCOUNT));
        when(repository.findByAccount(ACCOUNT)).thenReturn(new ArrayList<>(operationList));
        List<OperationDto> dtoList = operationService.getList(USER_ID, null, null);
        assertEquals(2, dtoList.size());
        OperationDto dto = dtoList.get(0);
        assertEquals(operationList.get(1).getDate(), dto.getDate());
        assertEquals(operationList.get(1).getType().getDescription(), dto.getType());
        assertEquals(operationList.get(1).getAmount(), dto.getAmount());
        dto = dtoList.get(1);
        assertEquals(operationList.get(0).getDate(), dto.getDate());
        assertEquals(operationList.get(0).getType().getDescription(), dto.getType());
        assertEquals(operationList.get(0).getAmount(), dto.getAmount());
        verify(accounts, times(MAX_MOCK_CALLS)).findById(USER_ID);
        verify(repository, times(MAX_MOCK_CALLS)).findByAccount(ACCOUNT);
    }

    @Test
    void testGetListWhenUserExistsAndStartIsNotNullAndFinishIsNull() {
        when(accounts.findById(USER_ID)).thenReturn(Optional.of(ACCOUNT));
        when(repository.findByAccountAndDateAfter(ACCOUNT, start)).thenReturn(new ArrayList<>(operationList));
        List<OperationDto> dtoList = operationService.getList(USER_ID, start, null);
        assertEquals(2, dtoList.size());
        OperationDto dto = dtoList.get(0);
        assertEquals(operationList.get(1).getDate(), dto.getDate());
        assertEquals(operationList.get(1).getType().getDescription(), dto.getType());
        assertEquals(operationList.get(1).getAmount(), dto.getAmount());
        dto = dtoList.get(1);
        assertEquals(operationList.get(0).getDate(), dto.getDate());
        assertEquals(operationList.get(0).getType().getDescription(), dto.getType());
        assertEquals(operationList.get(0).getAmount(), dto.getAmount());
        verify(accounts, times(MAX_MOCK_CALLS)).findById(USER_ID);
        verify(repository, times(MAX_MOCK_CALLS)).findByAccountAndDateAfter(ACCOUNT, start);
    }

    @Test
    void testGetListWhenUserExistsAndStartIsNullAndFinishIsNotNull() {
        when(accounts.findById(USER_ID)).thenReturn(Optional.of(ACCOUNT));
        when(repository.findByAccountAndDateBefore(ACCOUNT, finish)).thenReturn(new ArrayList<>(operationList));
        List<OperationDto> dtoList = operationService.getList(USER_ID, null, finish);
        assertEquals(2, dtoList.size());
        OperationDto dto = dtoList.get(0);
        assertEquals(operationList.get(1).getDate(), dto.getDate());
        assertEquals(operationList.get(1).getType().getDescription(), dto.getType());
        assertEquals(operationList.get(1).getAmount(), dto.getAmount());
        dto = dtoList.get(1);
        assertEquals(operationList.get(0).getDate(), dto.getDate());
        assertEquals(operationList.get(0).getType().getDescription(), dto.getType());
        assertEquals(operationList.get(0).getAmount(), dto.getAmount());
        verify(accounts, times(MAX_MOCK_CALLS)).findById(USER_ID);
        verify(repository, times(MAX_MOCK_CALLS)).findByAccountAndDateBefore(ACCOUNT, finish);
    }

    @Test
    void testGetListWhenUserExistsAndStartIsNotNullAndFinishIsNotNull() {
        when(accounts.findById(USER_ID)).thenReturn(Optional.of(ACCOUNT));
        when(repository.findByAccountAndDateBetween(ACCOUNT, start, finish)).thenReturn(new ArrayList<>(operationList));
        List<OperationDto> dtoList = operationService.getList(USER_ID, start, finish);
        assertEquals(2, dtoList.size());
        OperationDto dto = dtoList.get(0);
        assertEquals(operationList.get(1).getDate(), dto.getDate());
        assertEquals(operationList.get(1).getType().getDescription(), dto.getType());
        assertEquals(operationList.get(1).getAmount(), dto.getAmount());
        dto = dtoList.get(1);
        assertEquals(operationList.get(0).getDate(), dto.getDate());
        assertEquals(operationList.get(0).getType().getDescription(), dto.getType());
        assertEquals(operationList.get(0).getAmount(), dto.getAmount());
        verify(accounts, times(MAX_MOCK_CALLS)).findById(USER_ID);
        verify(repository, times(MAX_MOCK_CALLS)).findByAccountAndDateBetween(ACCOUNT, start, finish);
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