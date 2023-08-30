package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.IllegalAmountException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.exception.UserNotFoundToGetBalanceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static dev.akuniutka.bank.api.util.ErrorMessage.*;
import static dev.akuniutka.bank.api.util.Amount.*;

@ExtendWith(MockitoExtension.class)
class ApiServiceTest {
    private static final Long USER_ID = 1L;
    private Date dateFrom;
    private Date dateTo;
    private AccountService accountService;
    private OperationService operationService;
    private ApiService service;

    @BeforeEach
    void setUp() {
        dateFrom = mock(Date.class);
        dateTo = mock(Date.class);
        accountService = mock(AccountService.class);
        operationService = mock(OperationService.class);
        service = new ApiService(accountService, operationService);
    }

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(ignoreStubs(dateFrom));
        verifyNoMoreInteractions(ignoreStubs(dateTo));
        verifyNoMoreInteractions(ignoreStubs(accountService));
        verifyNoMoreInteractions(ignoreStubs(operationService));
    }

    @Test
    void testGetBalanceWhenUserIdIsNull() {
        when(accountService.getUserBalance(null)).thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class, () -> service.getBalance(null));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(accountService).getUserBalance(null);
    }

    @Test
    void testGetBalanceWhenUserDoesNotExist() {
        when(accountService.getUserBalance(USER_ID)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundToGetBalanceException.class, () -> service.getBalance(USER_ID));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(accountService).getUserBalance(USER_ID);
    }

    @Test
    void testGetBalanceWhenUserExists() {
        when(accountService.getUserBalance(USER_ID)).thenReturn(FORMATTED_TEN);
        assertEquals(FORMATTED_TEN, service.getBalance(USER_ID));
        verify(accountService).getUserBalance(USER_ID);
    }

    @Test
    void testPutMoneyWhenUserIdIsNull() {
        when(operationService.createDeposit(null, TEN)).thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class, () -> service.putMoney(null, TEN));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(operationService).createDeposit(null, TEN);
    }

    @Test
    void testPutMoneyWhenUserDoesNotExist() {
        when(operationService.createDeposit(USER_ID, TEN)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class, () -> service.putMoney(USER_ID, TEN));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(operationService).createDeposit(USER_ID, TEN);
    }

    @Test
    void testPutMoneyWhenAmountIsNull() {
        when(operationService.createDeposit(USER_ID, null))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_NULL));
        Exception e = assertThrows(BadRequestException.class, () -> service.putMoney(USER_ID, null));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(operationService).createDeposit(USER_ID, null);
    }

    @Test
    void testPutMoneyWhenAmountIsNegative() {
        when(operationService.createDeposit(USER_ID, MINUS_TEN))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_NEGATIVE));
        Exception e = assertThrows(BadRequestException.class, () -> service.putMoney(USER_ID, MINUS_TEN));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(operationService).createDeposit(USER_ID, MINUS_TEN);
    }

    @Test
    void testPutMoneyWhenAmountIsZero() {
        when(operationService.createDeposit(USER_ID, ZERO))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_ZERO));
        Exception e = assertThrows(BadRequestException.class, () -> service.putMoney(USER_ID, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(operationService).createDeposit(USER_ID, ZERO);
    }

    @Test
    void testPutMoneyWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        when(operationService.createDeposit(USER_ID, ONE_THOUSANDTH))
                .thenThrow(new IllegalAmountException(WRONG_MINOR_UNITS));
        Exception e = assertThrows(BadRequestException.class, () -> service.putMoney(USER_ID, ONE_THOUSANDTH));
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verify(operationService).createDeposit(USER_ID, ONE_THOUSANDTH);
    }

    @Test
    void testPutMoneyWhenAmountIsPositive() {
        Operation operation = mock(Operation.class);
        when(operationService.createDeposit(USER_ID, TEN)).thenReturn(operation);
        assertDoesNotThrow(() -> service.putMoney(USER_ID, TEN));
        verify(operationService).createDeposit(USER_ID, TEN);
        verifyNoMoreInteractions(ignoreStubs(operation));
    }

    @Test
    void testPutMoneyWhenScaleIsGreaterThanTwoButWithZeros() {
        Operation operation = mock(Operation.class);
        when(operationService.createDeposit(USER_ID, TEN_THOUSANDTHS)).thenReturn(operation);
        assertDoesNotThrow(() -> service.putMoney(USER_ID, TEN_THOUSANDTHS));
        verify(operationService).createDeposit(USER_ID, TEN_THOUSANDTHS);
        verifyNoMoreInteractions(ignoreStubs(operation));
    }

    @Test
    void testTakeMoneyWhenUserIdIsNull() {
        when(operationService.createWithdrawal(null, ONE)).thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class, () -> service.takeMoney(null, ONE));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(operationService).createWithdrawal(null, ONE);
    }

    @Test
    void testTakeMoneyWhenUserDoesNotExist() {
        when(operationService.createWithdrawal(USER_ID, ONE)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class, () -> service.takeMoney(USER_ID, ONE));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(operationService).createWithdrawal(USER_ID, ONE);
    }

    @Test
    void testTakeMoneyWhenAmountIsNull() {
        when(operationService.createWithdrawal(USER_ID, null))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_NULL));
        Exception e = assertThrows(BadRequestException.class, () -> service.takeMoney(USER_ID, null));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(operationService).createWithdrawal(USER_ID, null);
    }

    @Test
    void testTakeMoneyWhenAmountIsNegative() {
        when(operationService.createWithdrawal(USER_ID, MINUS_ONE))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_NEGATIVE));
        Exception e = assertThrows(BadRequestException.class, () -> service.takeMoney(USER_ID, MINUS_ONE));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(operationService).createWithdrawal(USER_ID, MINUS_ONE);
    }

    @Test
    void testTakeMoneyWhenAmountIsZero() {
        when(operationService.createWithdrawal(USER_ID, ZERO))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_ZERO));
        Exception e = assertThrows(BadRequestException.class, () -> service.takeMoney(USER_ID, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(operationService).createWithdrawal(USER_ID, ZERO);
    }

    @Test
    void testTakeMoneyWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        when(operationService.createWithdrawal(USER_ID, ONE_THOUSANDTH))
                .thenThrow(new IllegalAmountException(WRONG_MINOR_UNITS));
        Exception e = assertThrows(BadRequestException.class, () -> service.takeMoney(USER_ID, ONE_THOUSANDTH));
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verify(operationService).createWithdrawal(USER_ID, ONE_THOUSANDTH);
    }

    @Test
    void testTakeMoneyWhenBalanceIsInsufficient() {
        when(operationService.createWithdrawal(USER_ID, ONE))
                .thenThrow(new IllegalAmountException(INSUFFICIENT_BALANCE));
        Exception e = assertThrows(BadRequestException.class, () -> service.takeMoney(USER_ID, ONE));
        assertEquals(INSUFFICIENT_BALANCE, e.getMessage());
        verify(operationService).createWithdrawal(USER_ID, ONE);
    }

    @Test
    void testTakeMoneyWhenBalanceIsSufficient() {
        Operation operation = mock(Operation.class);
        when(operationService.createWithdrawal(USER_ID, ONE)).thenReturn(operation);
        assertDoesNotThrow(() -> service.takeMoney(USER_ID, ONE));
        verify(operationService).createWithdrawal(USER_ID, ONE);
        verifyNoMoreInteractions(ignoreStubs(operation));
    }

    @Test
    void testTakeMoneyWhenScaleIsGreaterThanTwoButWithZeros() {
        Operation operation = mock(Operation.class);
        when(operationService.createWithdrawal(USER_ID, TEN_THOUSANDTHS)).thenReturn(operation);
        assertDoesNotThrow(() -> service.takeMoney(USER_ID, TEN_THOUSANDTHS));
        verify(operationService).createWithdrawal(USER_ID, TEN_THOUSANDTHS);
        verifyNoMoreInteractions(ignoreStubs(operation));
    }

    @Test
    void testGetOperationListWhenUserIdIsNull() {
        when(operationService.getUserOperations(null, dateFrom, dateTo))
                .thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class,
                () -> service.getOperationList(null, dateFrom, dateTo)
        );
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(operationService).getUserOperations(null, dateFrom, dateTo);
    }

    @Test
    void testGetOperationListWhenUserNotFound() {
        when(operationService.getUserOperations(USER_ID, dateFrom, dateTo))
                .thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class,
                () -> service.getOperationList(USER_ID, dateFrom, dateTo)
        );
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(operationService).getUserOperations(USER_ID, dateFrom, dateTo);
    }

    @Test
    void testGetOperationListWhenUserExists() {
        List<Operation> operations = spy(new ArrayList<>());
        when(operationService.getUserOperations(USER_ID, dateFrom, dateTo)).thenReturn(operations);
        assertEquals(operations, service.getOperationList(USER_ID, dateFrom, dateTo));
        verify(operationService).getUserOperations(USER_ID, dateFrom, dateTo);
        verifyNoMoreInteractions(ignoreStubs(operations));
    }
}