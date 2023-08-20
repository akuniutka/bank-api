package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.exception.UserNotFoundToGetBalanceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static dev.akuniutka.bank.api.entity.ErrorMessage.*;
import static dev.akuniutka.bank.api.Amount.*;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {
    private static final int MAX_MOCK_CALLS = 1;
    private static final Long USER_ID = 1L;
    private Account account;
    private AccountService accountService;
    private Operations operationService;
    private BalanceService service;

    @BeforeEach
    void setUp() {
        accountService = mock(AccountService.class);
        operationService = mock(Operations.class);
        service = new BalanceService(accountService, operationService);
        account = new Account();
    }

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(ignoreStubs(accountService));
        verifyNoMoreInteractions(ignoreStubs(operationService));
    }

    @Test
    void testGetUserBalanceWhenUserIdIsNull() {
        when(accountService.getAccount(null)).thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class, () -> service.getUserBalance(null));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(null);
    }

    @Test
    void testGetUserBalanceWhenUserDoesNotExist() {
        when(accountService.getAccount(USER_ID)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundToGetBalanceException.class, () -> service.getUserBalance(USER_ID));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
    }

    @Test
    void testGetUserBalanceWhenUserExists() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        assertEquals(FORMATTED_ZERO, service.getUserBalance(USER_ID));
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
    }

    @Test
    void testIncreaseUserBalanceWhenUserIdIsNull() {
        when(accountService.getAccount(null)).thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class, () -> service.increaseUserBalance(null, TEN));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(null);
    }

    @Test
    void testIncreaseUserBalanceWhenUserDoesNotExist() {
        when(accountService.getAccount(USER_ID)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class, () -> service.increaseUserBalance(USER_ID, TEN));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndAmountIsNull() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        Exception e = assertThrows(BadRequestException.class, () -> service.increaseUserBalance(USER_ID, NULL));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndAmountIsNegative() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        Exception e = assertThrows(BadRequestException.class, () -> service.increaseUserBalance(USER_ID, MINUS_TEN));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndAmountIsZero() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        Exception e = assertThrows(BadRequestException.class, () -> service.increaseUserBalance(USER_ID, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndScaleIsGreaterThanTwoAndWithNonZeros() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        Exception e = assertThrows(BadRequestException.class,
                () -> service.increaseUserBalance(USER_ID, ONE_THOUSANDTH)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndAmountIsPositive() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(accountService.saveAccount(account)).thenReturn(account);
        doNothing().when(operationService).addDeposit(account, TEN);
        service.increaseUserBalance(USER_ID, TEN);
        assertEquals(FORMATTED_TEN, account.getBalance());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(accountService, times(MAX_MOCK_CALLS)).saveAccount(account);
        verify(operationService, times(MAX_MOCK_CALLS)).addDeposit(account, TEN);
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndScaleIsGreaterThanTwoButWithZeros() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(accountService.saveAccount(account)).thenReturn(account);
        doNothing().when(operationService).addDeposit(account, TEN_THOUSANDTHS);
        service.increaseUserBalance(USER_ID, TEN_THOUSANDTHS);
        assertEquals(FORMATTED_TEN_THOUSANDTHS, account.getBalance());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(accountService, times(MAX_MOCK_CALLS)).saveAccount(account);
        verify(operationService, times(MAX_MOCK_CALLS)).addDeposit(account, TEN_THOUSANDTHS);
    }

    @Test
    void testDecreaseUserBalanceWhenUserIdIsNull() {
        when(accountService.getAccount(null)).thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class, () -> service.decreaseUserBalance(null, ONE));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(null);
    }

    @Test
    void testDecreaseUserBalanceWhenUserDoesNotExist() {
        when(accountService.getAccount(USER_ID)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class, () -> service.decreaseUserBalance(USER_ID, ONE));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsNull() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        Exception e = assertThrows(BadRequestException.class, () -> service.decreaseUserBalance(USER_ID, NULL));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsNegative() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        Exception e = assertThrows(BadRequestException.class, () -> service.decreaseUserBalance(USER_ID, MINUS_ONE));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsZero() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        Exception e = assertThrows(BadRequestException.class, () -> service.decreaseUserBalance(USER_ID, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndScaleIsGreaterThanTwoAndWithNonZeros() {
        account.increaseBalance(TEN);
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        Exception e = assertThrows(BadRequestException.class,
                () -> service.decreaseUserBalance(USER_ID, ONE_THOUSANDTH)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsGreaterThanBalance() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        Exception e = assertThrows(BadRequestException.class, () -> service.decreaseUserBalance(USER_ID, ONE));
        assertEquals(INSUFFICIENT_BALANCE, e.getMessage());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsLessThatBalance() {
        account.increaseBalance(TEN);
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(accountService.saveAccount(account)).thenReturn(account);
        doNothing().when(operationService).addWithdrawal(account, ONE);
        service.decreaseUserBalance(USER_ID, ONE);
        assertEquals(FORMATTED_NINE, account.getBalance());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(accountService, times(MAX_MOCK_CALLS)).saveAccount(account);
        verify(operationService, times(MAX_MOCK_CALLS)).addWithdrawal(account, ONE);
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsEqualToBalance() {
        account.increaseBalance(TEN);
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(accountService.saveAccount(account)).thenReturn(account);
        doNothing().when(operationService).addWithdrawal(account, TEN);
        service.decreaseUserBalance(USER_ID, TEN);
        assertEquals(FORMATTED_ZERO, account.getBalance());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(accountService, times(MAX_MOCK_CALLS)).saveAccount(account);
        verify(operationService, times(MAX_MOCK_CALLS)).addWithdrawal(account, TEN);
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndScaleIsGreaterThanTwoButWithZeros() {
        BigDecimal expected = FORMATTED_TEN.subtract(FORMATTED_TEN_THOUSANDTHS);
        account.increaseBalance(TEN);
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(accountService.saveAccount(account)).thenReturn(account);
        doNothing().when(operationService).addWithdrawal(account, TEN_THOUSANDTHS);
        service.decreaseUserBalance(USER_ID, TEN_THOUSANDTHS);
        assertEquals(expected, account.getBalance());
        verify(accountService, times(MAX_MOCK_CALLS)).getAccount(USER_ID);
        verify(accountService, times(MAX_MOCK_CALLS)).saveAccount(account);
        verify(operationService, times(MAX_MOCK_CALLS)).addWithdrawal(account, TEN_THOUSANDTHS);
    }
}