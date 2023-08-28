package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.IllegalAmountException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.repository.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static dev.akuniutka.bank.api.util.ErrorMessage.*;
import static dev.akuniutka.bank.api.util.Amount.*;

class AccountServiceTest {
    private static final Long USER_ID = 1L;
    private Account account;
    private AccountRepository repository;
    private AccountService service;

    @BeforeEach
    public void setUp() {
        account = mock(Account.class);
        repository = mock(AccountRepository.class);
        service = new AccountService(repository);
    }

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(ignoreStubs(account));
        verifyNoMoreInteractions(ignoreStubs(repository));
    }

    @Test
    void testGetAccountWhenUserIdIsNull() {
        Exception e = assertThrows(BadRequestException.class, () -> service.getAccount(null));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
    }

    @Test
    void testGetAccountWhenUserDoesNotExist() {
        when(repository.findById(USER_ID)).thenReturn(Optional.empty());
        Exception e = assertThrows(UserNotFoundException.class, () -> service.getAccount(USER_ID));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(repository).findById(USER_ID);
    }

    @Test
    void testGetAccountWhenUserExists() {
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        assertEquals(account, service.getAccount(USER_ID));
        verify(repository).findById(USER_ID);
    }

    @Test
    void testGetUserBalanceWhenUserIdIsNull() {
        Exception e = assertThrows(BadRequestException.class, () -> service.getUserBalance(null));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
    }

    @Test
    void testGetUserBalanceWhenUserDoesNotExist() {
        when(repository.findById(USER_ID)).thenReturn(Optional.empty());
        Exception e = assertThrows(UserNotFoundException.class, () -> service.getUserBalance(USER_ID));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(repository).findById(USER_ID);
    }

    @Test
    void testGetUserBalanceWhenUserExists() {
        when(account.getBalance()).thenReturn(FORMATTED_TEN);
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        assertEquals(FORMATTED_TEN, service.getUserBalance(USER_ID));
        verify(repository).findById(USER_ID);
        verify(account).getBalance();
    }

    @Test
    void testIncreaseUserBalanceWhenUserIdIsNull() {
        Exception e = assertThrows(BadRequestException.class, () -> service.increaseUserBalance(null, TEN));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
    }

    @Test
    void testIncreaseUserBalanceWhenUserDoesNotExist() {
        when(repository.findById(USER_ID)).thenReturn(Optional.empty());
        Exception e = assertThrows(UserNotFoundException.class, () -> service.increaseUserBalance(USER_ID, TEN));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(repository).findById(USER_ID);
    }

    @Test
    void testIncreaseUserBalanceWhenAmountIsNull() {
        doThrow(new IllegalAmountException(AMOUNT_IS_NULL)).when(account).increaseBalance(null);
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        Exception e = assertThrows(IllegalAmountException.class,
                () -> service.increaseUserBalance(USER_ID, null)
        );
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(repository).findById(USER_ID);
        verify(account).increaseBalance(null);
    }

    @Test
    void testIncreaseUserBalanceWhenAmountIsNegative() {
        doThrow(new IllegalAmountException(AMOUNT_IS_NEGATIVE)).when(account).increaseBalance(MINUS_TEN);
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        Exception e = assertThrows(IllegalAmountException.class, () -> service.increaseUserBalance(USER_ID, MINUS_TEN));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(repository).findById(USER_ID);
        verify(account).increaseBalance(MINUS_TEN);
    }

    @Test
    void testIncreaseUserBalanceWhenAmountIsZero() {
        doThrow(new IllegalAmountException(AMOUNT_IS_ZERO)).when(account).increaseBalance(ZERO);
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        Exception e = assertThrows(IllegalAmountException.class, () -> service.increaseUserBalance(USER_ID, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(repository).findById(USER_ID);
        verify(account).increaseBalance(ZERO);
    }

    @Test
    void testIncreaseUserBalanceWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        doThrow(new IllegalAmountException(WRONG_MINOR_UNITS)).when(account).increaseBalance(ONE_THOUSANDTH);
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        Exception e = assertThrows(IllegalAmountException.class,
                () -> service.increaseUserBalance(USER_ID, ONE_THOUSANDTH)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verify(repository).findById(USER_ID);
        verify(account).increaseBalance(ONE_THOUSANDTH);
    }

    @Test
    void testIncreaseUserBalanceWhenAmountIsPositive() {
        doNothing().when(account).increaseBalance(TEN);
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        when(repository.save(account)).thenReturn(account);
        assertEquals(account, service.increaseUserBalance(USER_ID, TEN));
        InOrder inOrder = inOrder(account, repository);
        inOrder.verify(repository).findById(USER_ID);
        inOrder.verify(account).increaseBalance(TEN);
        inOrder.verify(repository).save(account);
    }

    @Test
    void testIncreaseUserBalanceWhenScaleIsGreaterThanTwoButWithZeros() {
        doNothing().when(account).increaseBalance(TEN_THOUSANDTHS);
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        when(repository.save(account)).thenReturn(account);
        assertEquals(account, service.increaseUserBalance(USER_ID, TEN_THOUSANDTHS));
        InOrder inOrder = inOrder(account, repository);
        inOrder.verify(repository).findById(USER_ID);
        inOrder.verify(account).increaseBalance(TEN_THOUSANDTHS);
        inOrder.verify(repository).save(account);
    }

    @Test
    void testDecreaseUserBalanceWhenUserIdIsNull() {
        Exception e = assertThrows(BadRequestException.class, () -> service.decreaseUserBalance(null, ONE));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
    }

    @Test
    void testDecreaseUserBalanceWhenUserDoesNotExist() {
        when(repository.findById(USER_ID)).thenReturn(Optional.empty());
        Exception e = assertThrows(UserNotFoundException.class, () -> service.decreaseUserBalance(USER_ID, ONE));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(repository).findById(USER_ID);
    }

    @Test
    void testDecreaseUserBalanceWhenAmountIsNull() {
        doThrow(new IllegalAmountException(AMOUNT_IS_NULL)).when(account).decreaseBalance(null);
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        Exception e = assertThrows(IllegalAmountException.class,
                () -> service.decreaseUserBalance(USER_ID, null)
        );
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(repository).findById(USER_ID);
        verify(account).decreaseBalance(null);
    }

    @Test
    void testDecreaseUserBalanceWhenAmountIsNegative() {
        doThrow(new IllegalAmountException(AMOUNT_IS_NEGATIVE)).when(account).decreaseBalance(MINUS_ONE);
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        Exception e = assertThrows(IllegalAmountException.class, () -> service.decreaseUserBalance(USER_ID, MINUS_ONE));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(repository).findById(USER_ID);
        verify(account).decreaseBalance(MINUS_ONE);
    }

    @Test
    void testDecreaseUserBalanceWhenAmountIsZero() {
        doThrow(new IllegalAmountException(AMOUNT_IS_ZERO)).when(account).decreaseBalance(ZERO);
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        Exception e = assertThrows(IllegalAmountException.class, () -> service.decreaseUserBalance(USER_ID, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(repository).findById(USER_ID);
        verify(account).decreaseBalance(ZERO);
    }

    @Test
    void testDecreaseUserBalanceWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        doThrow(new IllegalAmountException(WRONG_MINOR_UNITS)).when(account).decreaseBalance(ONE_THOUSANDTH);
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        Exception e = assertThrows(IllegalAmountException.class,
                () -> service.decreaseUserBalance(USER_ID, ONE_THOUSANDTH)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verify(repository).findById(USER_ID);
        verify(account).decreaseBalance(ONE_THOUSANDTH);
    }

    @Test
    void testDecreaseUserBalanceWhenBalanceIsInsufficient() {
        doThrow(new IllegalAmountException(INSUFFICIENT_BALANCE)).when(account).decreaseBalance(ONE);
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        Exception e = assertThrows(IllegalAmountException.class, () -> service.decreaseUserBalance(USER_ID, ONE));
        assertEquals(INSUFFICIENT_BALANCE, e.getMessage());
        verify(repository).findById(USER_ID);
        verify(account).decreaseBalance(ONE);
    }

    @Test
    void testDecreaseUserBalanceWhenBalanceIsSufficient() {
        doNothing().when(account).decreaseBalance(ONE);
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        when(repository.save(account)).thenReturn(account);
        assertEquals(account, service.decreaseUserBalance(USER_ID, ONE));
        InOrder inOrder = inOrder(account, repository);
        inOrder.verify(repository).findById(USER_ID);
        inOrder.verify(account).decreaseBalance(ONE);
        inOrder.verify(repository).save(account);
    }

    @Test
    void testDecreaseUserBalanceWhenScaleIsGreaterThanTwoButWithZeros() {
        doNothing().when(account).decreaseBalance(TEN_THOUSANDTHS);
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        when(repository.save(account)).thenReturn(account);
        assertEquals(account, service.decreaseUserBalance(USER_ID, TEN_THOUSANDTHS));
        InOrder inOrder = inOrder(account, repository);
        inOrder.verify(repository).findById(USER_ID);
        inOrder.verify(account).decreaseBalance(TEN_THOUSANDTHS);
        inOrder.verify(repository).save(account);
    }
}