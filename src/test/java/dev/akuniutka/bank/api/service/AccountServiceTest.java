package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.exception.UserNotFoundToGetBalanceException;
import dev.akuniutka.bank.api.repository.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static dev.akuniutka.bank.api.entity.ErrorMessage.*;
import static dev.akuniutka.bank.api.Amount.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    private static final int MAX_MOCK_CALLS = 1;
    private static final Long USER_ID = 1L;
    private Account account;
    private AccountRepository repository;
    private Operations operations;
    private AccountService service;

    @BeforeEach
    void setUp() {
        repository = mock(AccountRepository.class);
        operations = mock(Operations.class);
        service = new AccountService(repository, operations);
        account = new Account();
    }

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(ignoreStubs(repository));
        verifyNoMoreInteractions(ignoreStubs(operations));
    }

    @Test
    void testGetUserBalanceWhenUserIdIsNull() {
        Exception e = assertThrows(BadRequestException.class, () -> service.getUserBalance(null));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
    }

    @Test
    void testGetUserBalanceWhenUserDoesNotExist() {
        when(repository.findById(USER_ID)).thenReturn(Optional.empty());
        Exception e = assertThrows(UserNotFoundToGetBalanceException.class, () -> service.getUserBalance(USER_ID));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(repository, times(MAX_MOCK_CALLS)).findById(USER_ID);
    }

    @Test
    void testGetUserBalanceWhenUserExists() {
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        assertEquals(FORMATTED_ZERO, service.getUserBalance(USER_ID));
        verify(repository, times(MAX_MOCK_CALLS)).findById(USER_ID);
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
        verify(repository, times(MAX_MOCK_CALLS)).findById(USER_ID);
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndAmountIsNull() {
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        Exception e = assertThrows(BadRequestException.class, () -> service.increaseUserBalance(USER_ID, NULL));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(repository, times(MAX_MOCK_CALLS)).findById(USER_ID);
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndAmountIsNegative() {
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        Exception e = assertThrows(BadRequestException.class, () -> service.increaseUserBalance(USER_ID, MINUS_TEN));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(repository, times(MAX_MOCK_CALLS)).findById(USER_ID);
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndAmountIsZero() {
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        Exception e = assertThrows(BadRequestException.class, () -> service.increaseUserBalance(USER_ID, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(repository, times(MAX_MOCK_CALLS)).findById(USER_ID);
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndScaleIsGreaterThanTwoAndWithNonZeros() {
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        Exception e = assertThrows(BadRequestException.class,
                () -> service.increaseUserBalance(USER_ID, ONE_THOUSANDTH)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verify(repository, times(MAX_MOCK_CALLS)).findById(USER_ID);
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndAmountIsPositive() {
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        when(repository.save(account)).thenReturn(account);
        doNothing().when(operations).addDeposit(account, TEN);
        service.increaseUserBalance(USER_ID, TEN);
        assertEquals(FORMATTED_TEN, account.getBalance());
        verify(repository, times(MAX_MOCK_CALLS)).findById(USER_ID);
        verify(repository, times(MAX_MOCK_CALLS)).save(account);
        verify(operations, times(MAX_MOCK_CALLS)).addDeposit(account, TEN);
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndScaleIsGreaterThanTwoButWithZeros() {
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        when(repository.save(account)).thenReturn(account);
        doNothing().when(operations).addDeposit(account, TEN_THOUSANDTHS);
        service.increaseUserBalance(USER_ID, TEN_THOUSANDTHS);
        assertEquals(FORMATTED_TEN_THOUSANDTHS, account.getBalance());
        verify(repository, times(MAX_MOCK_CALLS)).findById(USER_ID);
        verify(repository, times(MAX_MOCK_CALLS)).save(account);
        verify(operations, times(MAX_MOCK_CALLS)).addDeposit(account, TEN_THOUSANDTHS);
    }

    @Test
    void testDecreaseUserBalanceWhenUserIdIsNull() {
        account.increaseBalance(TEN);
        Exception e = assertThrows(BadRequestException.class, () -> service.decreaseUserBalance(null, ONE));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
    }

    @Test
    void testDecreaseUserBalanceWhenUserDoesNotExist() {
        account.increaseBalance(TEN);
        when(repository.findById(USER_ID)).thenReturn(Optional.empty());
        Exception e = assertThrows(UserNotFoundException.class, () -> service.decreaseUserBalance(USER_ID, ONE));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(repository, times(MAX_MOCK_CALLS)).findById(USER_ID);
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsNull() {
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        Exception e = assertThrows(BadRequestException.class, () -> service.decreaseUserBalance(USER_ID, NULL));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(repository, times(MAX_MOCK_CALLS)).findById(USER_ID);
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsNegative() {
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        Exception e = assertThrows(BadRequestException.class, () -> service.decreaseUserBalance(USER_ID, MINUS_ONE));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(repository, times(MAX_MOCK_CALLS)).findById(USER_ID);
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsZero() {
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        Exception e = assertThrows(BadRequestException.class, () -> service.decreaseUserBalance(USER_ID, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(repository, times(MAX_MOCK_CALLS)).findById(USER_ID);
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndScaleIsGreaterThanTwoAndWithNonZeros() {
        account.increaseBalance(TEN);
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        Exception e = assertThrows(BadRequestException.class,
                () -> service.decreaseUserBalance(USER_ID, ONE_THOUSANDTH)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verify(repository, times(MAX_MOCK_CALLS)).findById(USER_ID);
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsGreaterThanBalance() {
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        Exception e = assertThrows(BadRequestException.class, () -> service.decreaseUserBalance(USER_ID, ONE));
        assertEquals(INSUFFICIENT_BALANCE, e.getMessage());
        verify(repository, times(MAX_MOCK_CALLS)).findById(USER_ID);
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsLessThatBalance() {
        account.increaseBalance(TEN);
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        when(repository.save(account)).thenReturn(account);
        doNothing().when(operations).addWithdrawal(account, ONE);
        service.decreaseUserBalance(USER_ID, ONE);
        assertEquals(FORMATTED_NINE, account.getBalance());
        verify(repository, times(MAX_MOCK_CALLS)).findById(USER_ID);
        verify(repository, times(MAX_MOCK_CALLS)).save(account);
        verify(operations, times(MAX_MOCK_CALLS)).addWithdrawal(account, ONE);
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsEqualToBalance() {
        account.increaseBalance(TEN);
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        when(repository.save(account)).thenReturn(account);
        doNothing().when(operations).addWithdrawal(account, TEN);
        service.decreaseUserBalance(USER_ID, TEN);
        assertEquals(FORMATTED_ZERO, account.getBalance());
        verify(repository, times(MAX_MOCK_CALLS)).findById(USER_ID);
        verify(repository, times(MAX_MOCK_CALLS)).save(account);
        verify(operations, times(MAX_MOCK_CALLS)).addWithdrawal(account, TEN);
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndScaleIsGreaterThanTwoButWithZeros() {
        BigDecimal expected = FORMATTED_TEN.subtract(FORMATTED_TEN_THOUSANDTHS);
        account.increaseBalance(TEN);
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        when(repository.save(account)).thenReturn(account);
        doNothing().when(operations).addWithdrawal(account, TEN_THOUSANDTHS);
        service.decreaseUserBalance(USER_ID, TEN_THOUSANDTHS);
        assertEquals(expected, account.getBalance());
        verify(repository, times(MAX_MOCK_CALLS)).findById(USER_ID);
        verify(repository, times(MAX_MOCK_CALLS)).save(account);
        verify(operations, times(MAX_MOCK_CALLS)).addWithdrawal(account, TEN_THOUSANDTHS);
    }
}