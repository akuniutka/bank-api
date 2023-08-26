package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
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
    private static final int MAX_MOCK_CALLS = 1;
    private static final Long USER_ID = 1L;
    private Date dateFrom;
    private Date dateTo;
    private Account account;
    private List<Operation> operations;
    private AccountService accountService;
    private OperationService operationService;
    private ApiService service;

    @BeforeEach
    void setUp() {
        dateFrom = mock(Date.class);
        dateTo = mock(Date.class);
        account = mock(Account.class);
        operations = spy(new ArrayList<>());
        accountService = mock(AccountService.class);
        operationService = mock(OperationService.class);
        service = new ApiService(accountService, operationService);
    }

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(ignoreStubs(dateFrom));
        verifyNoMoreInteractions(ignoreStubs(dateTo));
        verifyNoMoreInteractions(ignoreStubs(account));
        verifyNoMoreInteractions(ignoreStubs(operations));
        verifyNoMoreInteractions(ignoreStubs(accountService));
        verifyNoMoreInteractions(ignoreStubs(operationService));
    }

    @Test
    void testGetBalanceWhenUserIdIsNull() {
        when(accountService.getAccount(null)).thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class, () -> service.getBalance(null));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(null);
    }

    @Test
    void testGetBalanceWhenUserDoesNotExist() {
        when(accountService.getAccount(USER_ID)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundToGetBalanceException.class, () -> service.getBalance(USER_ID));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
    }

    @Test
    void testGetBalanceWhenUserExists() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(account.getBalance()).thenReturn(FORMATTED_TEN);
        assertEquals(FORMATTED_TEN, service.getBalance(USER_ID));
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(account, times(MAX_MOCK_CALLS)).getBalance();
    }

    @Test
    void testPutMoneyWhenUserIdIsNull() {
        when(accountService.getAccount(null)).thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class, () -> service.putMoney(null, TEN));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(null);
    }

    @Test
    void testPutMoneyWhenUserDoesNotExist() {
        when(accountService.getAccount(USER_ID)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class, () -> service.putMoney(USER_ID, TEN));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
    }

    @Test
    void testPutMoneyWhenAmountIsNull() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        doThrow(new IllegalAmountException(AMOUNT_IS_NULL)).when(account).increaseBalance(NULL);
        Exception e = assertThrows(BadRequestException.class, () -> service.putMoney(USER_ID, NULL));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(account, times(MAX_MOCK_CALLS)).increaseBalance(NULL);
    }

    @Test
    void testPutMoneyWhenAmountIsNegative() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        doThrow(new IllegalAmountException(AMOUNT_IS_NEGATIVE)).when(account).increaseBalance(MINUS_TEN);
        Exception e = assertThrows(BadRequestException.class, () -> service.putMoney(USER_ID, MINUS_TEN));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(account, times(MAX_MOCK_CALLS)).increaseBalance(MINUS_TEN);
    }

    @Test
    void testPutMoneyWhenAmountIsZero() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        doThrow(new IllegalAmountException(AMOUNT_IS_ZERO)).when(account).increaseBalance(ZERO);
        Exception e = assertThrows(BadRequestException.class, () -> service.putMoney(USER_ID, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(account, times(MAX_MOCK_CALLS)).increaseBalance(ZERO);
    }

    @Test
    void testPutMoneyWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        doThrow(new IllegalAmountException(WRONG_MINOR_UNITS)).when(account).increaseBalance(ONE_THOUSANDTH);
        Exception e = assertThrows(BadRequestException.class, () -> service.putMoney(USER_ID, ONE_THOUSANDTH));
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(account, times(MAX_MOCK_CALLS)).increaseBalance(ONE_THOUSANDTH);
    }

    @Test
    void testPutMoneyWhenAmountIsPositive() {
        Operation operation = mock(Operation.class);
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        doNothing().when(account).increaseBalance(TEN);
        when(operationService.createDeposit(account, TEN)).thenReturn(operation);
        when(accountService.saveAccount(account)).thenAnswer(a -> {
            verify(account, times(MAX_MOCK_CALLS)).increaseBalance(TEN);
            return a.getArguments()[0];
        });
        when(operationService.saveOperation(operation)).thenAnswer(a -> {
            verify(accountService, times(MAX_MOCK_CALLS)).saveAccount(account);
            return a.getArguments()[0];
        });
        assertDoesNotThrow(() -> service.putMoney(USER_ID, TEN));
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(account, times(MAX_MOCK_CALLS)).increaseBalance(TEN);
        verify(operationService, times(MAX_MOCK_CALLS)).createDeposit(account, TEN);
        verify(accountService, times(MAX_MOCK_CALLS)).saveAccount(account);
        verify(operationService, times(MAX_MOCK_CALLS)).saveOperation(operation);
    }

    @Test
    void testPutMoneyWhenScaleIsGreaterThanTwoButWithZeros() {
        Operation operation = mock(Operation.class);
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        doNothing().when(account).increaseBalance(TEN_THOUSANDTHS);
        when(operationService.createDeposit(account, TEN_THOUSANDTHS)).thenReturn(operation);
        when(accountService.saveAccount(account)).thenAnswer(a -> {
            verify(account, times(MAX_MOCK_CALLS)).increaseBalance(TEN_THOUSANDTHS);
            return a.getArguments()[0];
        });
        when(operationService.saveOperation(operation)).thenAnswer(a -> {
            verify(accountService, times(MAX_MOCK_CALLS)).saveAccount(account);
            return a.getArguments()[0];
        });
        assertDoesNotThrow(() -> service.putMoney(USER_ID, TEN_THOUSANDTHS));
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(account, times(MAX_MOCK_CALLS)).increaseBalance(TEN_THOUSANDTHS);
        verify(operationService, times(MAX_MOCK_CALLS)).createDeposit(account, TEN_THOUSANDTHS);
        verify(accountService, times(MAX_MOCK_CALLS)).saveAccount(account);
        verify(operationService, times(MAX_MOCK_CALLS)).saveOperation(operation);
    }

    @Test
    void testTakeMoneyWhenUserIdIsNull() {
        when(accountService.getAccount(null)).thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class, () -> service.takeMoney(null, ONE));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(null);
    }

    @Test
    void testTakeMoneyWhenUserDoesNotExist() {
        when(accountService.getAccount(USER_ID)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class, () -> service.takeMoney(USER_ID, ONE));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
    }

    @Test
    void testTakeMoneyWhenAmountIsNull() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        doThrow(new IllegalAmountException(AMOUNT_IS_NULL)).when(account).decreaseBalance(NULL);
        Exception e = assertThrows(BadRequestException.class, () -> service.takeMoney(USER_ID, NULL));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(account, times(MAX_MOCK_CALLS)).decreaseBalance(NULL);
    }

    @Test
    void testTakeMoneyWhenAmountIsNegative() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        doThrow(new IllegalAmountException(AMOUNT_IS_NEGATIVE)).when(account).decreaseBalance(MINUS_ONE);
        Exception e = assertThrows(BadRequestException.class, () -> service.takeMoney(USER_ID, MINUS_ONE));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(account, times(MAX_MOCK_CALLS)).decreaseBalance(MINUS_ONE);
    }

    @Test
    void testTakeMoneyWhenAmountIsZero() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        doThrow(new IllegalAmountException(AMOUNT_IS_ZERO)).when(account).decreaseBalance(ZERO);
        Exception e = assertThrows(BadRequestException.class, () -> service.takeMoney(USER_ID, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(account, times(MAX_MOCK_CALLS)).decreaseBalance(ZERO);
    }

    @Test
    void testTakeMoneyWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        doThrow(new IllegalAmountException(WRONG_MINOR_UNITS)).when(account).decreaseBalance(ONE_THOUSANDTH);
        Exception e = assertThrows(BadRequestException.class, () -> service.takeMoney(USER_ID, ONE_THOUSANDTH));
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(account, times(MAX_MOCK_CALLS)).decreaseBalance(ONE_THOUSANDTH);
    }

    @Test
    void testTakeMoneyWhenBalanceIsInsufficient() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        doThrow(new IllegalAmountException(INSUFFICIENT_BALANCE)).when(account).decreaseBalance(ONE);
        Exception e = assertThrows(BadRequestException.class, () -> service.takeMoney(USER_ID, ONE));
        assertEquals(INSUFFICIENT_BALANCE, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(account, times(MAX_MOCK_CALLS)).decreaseBalance(ONE);
    }

    @Test
    void testTakeMoneyWhenBalanceIsSufficient() {
        Operation operation = mock(Operation.class);
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        doNothing().when(account).decreaseBalance(ONE);
        when(operationService.createWithdrawal(account, ONE)).thenReturn(operation);
        when(accountService.saveAccount(account)).thenAnswer(a -> {
            verify(account, times(MAX_MOCK_CALLS)).decreaseBalance(ONE);
            return a.getArguments()[0];
        });
        when(operationService.saveOperation(operation)).thenAnswer(a -> {
            verify(accountService, times(MAX_MOCK_CALLS)).saveAccount(account);
            return a.getArguments()[0];
        });
        assertDoesNotThrow(() -> service.takeMoney(USER_ID, ONE));
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(account, times(MAX_MOCK_CALLS)).decreaseBalance(ONE);
        verify(operationService, times(MAX_MOCK_CALLS)).createWithdrawal(account, ONE);
        verify(accountService, times(MAX_MOCK_CALLS)).saveAccount(account);
        verify(operationService, times(MAX_MOCK_CALLS)).saveOperation(operation);
    }

    @Test
    void testTakeMoneyWhenScaleIsGreaterThanTwoButWithZeros() {
        Operation operation = mock(Operation.class);
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        doNothing().when(account).decreaseBalance(TEN_THOUSANDTHS);
        when(operationService.createWithdrawal(account, TEN_THOUSANDTHS)).thenReturn(operation);
        when(accountService.saveAccount(account)).thenAnswer(a -> {
            verify(account, times(MAX_MOCK_CALLS)).decreaseBalance(TEN_THOUSANDTHS);
            return a.getArguments()[0];
        });
        when(operationService.saveOperation(operation)).thenAnswer(a -> {
            verify(accountService, times(MAX_MOCK_CALLS)).saveAccount(account);
            return a.getArguments()[0];
        });
        assertDoesNotThrow(() -> service.takeMoney(USER_ID, TEN_THOUSANDTHS));
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(account, times(MAX_MOCK_CALLS)).decreaseBalance(TEN_THOUSANDTHS);
        verify(operationService, times(MAX_MOCK_CALLS)).createWithdrawal(account, TEN_THOUSANDTHS);
        verify(accountService, times(MAX_MOCK_CALLS)).saveAccount(account);
        verify(operationService, times(MAX_MOCK_CALLS)).saveOperation(operation);
    }

    @Test
    void testGetOperationListWhenUserIdIsNull() {
        when(accountService.getAccount(null)).thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class,
                () -> service.getOperationList(null, dateFrom, dateTo)
        );
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(null);
    }

    @Test
    void testGetOperationListWhenUserNotFound() {
        when(accountService.getAccount(USER_ID)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class,
                () -> service.getOperationList(USER_ID, dateFrom, dateTo)
        );
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
    }

    @Test
    void testGetOperationListWhenUserExists() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(operationService.getOperations(account, dateFrom, dateTo)).thenReturn(operations);
        assertEquals(operations, service.getOperationList(USER_ID, dateFrom, dateTo));
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(operationService, times(MAX_MOCK_CALLS)).getOperations(account, dateFrom, dateTo);
    }
}