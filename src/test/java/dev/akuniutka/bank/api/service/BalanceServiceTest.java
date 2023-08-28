package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.IllegalAmountException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static dev.akuniutka.bank.api.util.ErrorMessage.*;
import static dev.akuniutka.bank.api.util.Amount.*;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {
    private static final Long USER_ID = 1L;
    private Account account;
    private AccountService accountService;
    private BalanceService service;

    @BeforeEach
    public void setUp() {
        account = mock(Account.class);
        accountService = mock(AccountService.class);
        service = new BalanceService(accountService);
    }

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(ignoreStubs(account));
        verifyNoMoreInteractions(ignoreStubs(accountService));
    }

    @Test
    void testGetUserBalanceWhenUserIdIsNull() {
        when(accountService.getAccount(null)).thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class, () -> service.getUserBalance(null));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(accountService).getAccount(null);
    }

    @Test
    void testGetUserBalanceWhenUserDoesNotExist() {
        when(accountService.getAccount(USER_ID)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class, () -> service.getUserBalance(USER_ID));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(accountService).getAccount(USER_ID);
    }

    @Test
    void testGetUserBalanceWhenUserExists() {
        when(account.getBalance()).thenReturn(FORMATTED_TEN);
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        assertEquals(FORMATTED_TEN, service.getUserBalance(USER_ID));
        verify(accountService).getAccount(USER_ID);
        verify(account).getBalance();
    }

    @Test
    void testIncreaseUserBalanceWhenUserIdIsNull() {
        when(accountService.getAccount(null)).thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class, () -> service.increaseUserBalance(null, TEN));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(accountService).getAccount(null);
    }

    @Test
    void testIncreaseUserBalanceWhenUserDoesNotExist() {
        when(accountService.getAccount(USER_ID)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class, () -> service.increaseUserBalance(USER_ID, TEN));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(accountService).getAccount(USER_ID);
    }

    @Test
    void testIncreaseUserBalanceWhenAmountIsNull() {
        doThrow(new IllegalAmountException(AMOUNT_IS_NULL)).when(account).increaseBalance(null);
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        Exception e = assertThrows(IllegalAmountException.class,
                () -> service.increaseUserBalance(USER_ID, null)
        );
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(accountService).getAccount(USER_ID);
        verify(account).increaseBalance(null);
    }

    @Test
    void testIncreaseUserBalanceWhenAmountIsNegative() {
        doThrow(new IllegalAmountException(AMOUNT_IS_NEGATIVE)).when(account).increaseBalance(MINUS_TEN);
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        Exception e = assertThrows(IllegalAmountException.class, () -> service.increaseUserBalance(USER_ID, MINUS_TEN));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(accountService).getAccount(USER_ID);
        verify(account).increaseBalance(MINUS_TEN);
    }

    @Test
    void testIncreaseUserBalanceWhenAmountIsZero() {
        doThrow(new IllegalAmountException(AMOUNT_IS_ZERO)).when(account).increaseBalance(ZERO);
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        Exception e = assertThrows(IllegalAmountException.class, () -> service.increaseUserBalance(USER_ID, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(accountService).getAccount(USER_ID);
        verify(account).increaseBalance(ZERO);
    }

    @Test
    void testIncreaseUserBalanceWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        doThrow(new IllegalAmountException(WRONG_MINOR_UNITS)).when(account).increaseBalance(ONE_THOUSANDTH);
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        Exception e = assertThrows(IllegalAmountException.class,
                () -> service.increaseUserBalance(USER_ID, ONE_THOUSANDTH)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verify(accountService).getAccount(USER_ID);
        verify(account).increaseBalance(ONE_THOUSANDTH);
    }

    @Test
    void testIncreaseUserBalanceWhenAmountIsPositive() {
        doNothing().when(account).increaseBalance(TEN);
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(accountService.saveAccount(account)).thenReturn(account);
        assertEquals(account, service.increaseUserBalance(USER_ID, TEN));
        InOrder inOrder = inOrder(account, accountService);
        inOrder.verify(accountService).getAccount(USER_ID);
        inOrder.verify(account).increaseBalance(TEN);
        inOrder.verify(accountService).saveAccount(account);
    }

    @Test
    void testIncreaseUserBalanceWhenScaleIsGreaterThanTwoButWithZeros() {
        doNothing().when(account).increaseBalance(TEN_THOUSANDTHS);
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(accountService.saveAccount(account)).thenReturn(account);
        assertEquals(account, service.increaseUserBalance(USER_ID, TEN_THOUSANDTHS));
        InOrder inOrder = inOrder(account, accountService);
        inOrder.verify(accountService).getAccount(USER_ID);
        inOrder.verify(account).increaseBalance(TEN_THOUSANDTHS);
        inOrder.verify(accountService).saveAccount(account);
    }
    @Test
    void testDecreaseUserBalanceWhenUserIdIsNull() {
        when(accountService.getAccount(null)).thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class, () -> service.decreaseUserBalance(null, ONE));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(accountService).getAccount(null);
    }

    @Test
    void testDecreaseUserBalanceWhenUserDoesNotExist() {
        when(accountService.getAccount(USER_ID)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class, () -> service.decreaseUserBalance(USER_ID, ONE));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(accountService).getAccount(USER_ID);
    }

    @Test
    void testDecreaseUserBalanceWhenAmountIsNull() {
        doThrow(new IllegalAmountException(AMOUNT_IS_NULL)).when(account).decreaseBalance(null);
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        Exception e = assertThrows(IllegalAmountException.class,
                () -> service.decreaseUserBalance(USER_ID, null)
        );
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(accountService).getAccount(USER_ID);
        verify(account).decreaseBalance(null);
    }

    @Test
    void testDecreaseUserBalanceWhenAmountIsNegative() {
        doThrow(new IllegalAmountException(AMOUNT_IS_NEGATIVE)).when(account).decreaseBalance(MINUS_ONE);
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        Exception e = assertThrows(IllegalAmountException.class, () -> service.decreaseUserBalance(USER_ID, MINUS_ONE));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(accountService).getAccount(USER_ID);
        verify(account).decreaseBalance(MINUS_ONE);
    }

    @Test
    void testDecreaseUserBalanceWhenAmountIsZero() {
        doThrow(new IllegalAmountException(AMOUNT_IS_ZERO)).when(account).decreaseBalance(ZERO);
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        Exception e = assertThrows(IllegalAmountException.class, () -> service.decreaseUserBalance(USER_ID, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(accountService).getAccount(USER_ID);
        verify(account).decreaseBalance(ZERO);
    }

    @Test
    void testDecreaseUserBalanceWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        doThrow(new IllegalAmountException(WRONG_MINOR_UNITS)).when(account).decreaseBalance(ONE_THOUSANDTH);
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        Exception e = assertThrows(IllegalAmountException.class,
                () -> service.decreaseUserBalance(USER_ID, ONE_THOUSANDTH)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verify(accountService).getAccount(USER_ID);
        verify(account).decreaseBalance(ONE_THOUSANDTH);
    }

    @Test
    void testDecreaseUserBalanceWhenAmountIsGreaterThanBalance() {
        doThrow(new IllegalAmountException(INSUFFICIENT_BALANCE)).when(account).decreaseBalance(ONE);
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        Exception e = assertThrows(IllegalAmountException.class, () -> service.decreaseUserBalance(USER_ID, ONE));
        assertEquals(INSUFFICIENT_BALANCE, e.getMessage());
        verify(accountService).getAccount(USER_ID);
        verify(account).decreaseBalance(ONE);
    }

    @Test
    void testDecreaseUserBalanceWhenAmountIsNotGreaterThanBalance() {
        doNothing().when(account).decreaseBalance(ONE);
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(accountService.saveAccount(account)).thenReturn(account);
        assertEquals(account, service.decreaseUserBalance(USER_ID, ONE));
        InOrder inOrder = inOrder(account, accountService);
        inOrder.verify(accountService).getAccount(USER_ID);
        inOrder.verify(account).decreaseBalance(ONE);
        inOrder.verify(accountService).saveAccount(account);
    }

    @Test
    void testDecreaseUserBalanceWhenScaleIsGreaterThanTwoButWithZeros() {
        doNothing().when(account).decreaseBalance(TEN_THOUSANDTHS);
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(accountService.saveAccount(account)).thenReturn(account);
        assertEquals(account, service.decreaseUserBalance(USER_ID, TEN_THOUSANDTHS));
        InOrder inOrder = inOrder(account, accountService);
        inOrder.verify(accountService).getAccount(USER_ID);
        inOrder.verify(account).decreaseBalance(TEN_THOUSANDTHS);
        inOrder.verify(accountService).saveAccount(account);
    }
}