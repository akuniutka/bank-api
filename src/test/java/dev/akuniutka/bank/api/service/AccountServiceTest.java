package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.exception.UserNotFoundToGetBalanceException;
import dev.akuniutka.bank.api.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static dev.akuniutka.bank.api.entity.ErrorMessage.*;
import static dev.akuniutka.bank.api.Amount.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    private static final Long EXISTING_USER_ID = 123456789L;
    private static final Long NON_EXISTING_USER_ID = 987654321L;
    private Account account;
    private List<Account> storedAccounts;
    @Mock
    private AccountRepository repository;
    @Mock
    private Operations operations;
    @InjectMocks
    private AccountService service;

    @BeforeEach
    void setUp() {
        account = new Account();
        storedAccounts = new ArrayList<>();
        List<Account> accounts = new ArrayList<>();
        accounts.add(account);
        Mockito.lenient().when(repository.findById(EXISTING_USER_ID)).thenReturn(Optional.of(account));
        Mockito.lenient().when(repository.findById(NON_EXISTING_USER_ID)).thenReturn(Optional.empty());
        Mockito.lenient().when(repository.existsById(EXISTING_USER_ID)).thenReturn(true);
        Mockito.lenient().when(repository.existsById(NON_EXISTING_USER_ID)).thenReturn(false);
        Mockito.lenient().when(repository.save(Mockito.any(Account.class))).thenAnswer(
                a -> storeAccount(a.getArguments()[0])
        );
        Mockito.lenient().when(repository.findAll()).thenReturn(accounts);
        Mockito.lenient().when(repository.findAllById(Mockito.anyIterable())).thenAnswer(
                a -> userIdsToAccounts(a.getArguments()[0])
        );
        Mockito.lenient().when(repository.saveAll(Mockito.anyIterable())).thenAnswer(
                a -> storeAllAccounts(a.getArguments()[0])
        );
        Mockito.lenient().when(repository.count()).thenReturn(1L);
    }

    @Test
    void testAccountService() {
        assertDoesNotThrow(() -> new AccountService(repository, operations));
    }

    @Test
    void testGetUserBalanceWhenUserExists() {
        assertEquals(FORMATTED_ZERO, service.getUserBalance(EXISTING_USER_ID));
    }

    @Test
    void testGetUserBalanceWhenUserDoesNotExist() {
        Exception e = assertThrows(UserNotFoundToGetBalanceException.class,
                () -> service.getUserBalance(NON_EXISTING_USER_ID)
        );
        assertEquals(USER_NOT_FOUND, e.getMessage());
    }

    @Test
    void testGetUserBalanceWhenUserIdIsNull() {
        Exception e = assertThrows(BadRequestException.class, () -> service.getUserBalance(null));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndAmountIsPositive() {
        service.increaseUserBalance(EXISTING_USER_ID, TEN);
        assertEquals(FORMATTED_TEN, account.getBalance());
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndScaleIsGreaterThanTwoButWithZeros() {
        service.increaseUserBalance(EXISTING_USER_ID, TEN_THOUSANDTHS);
        assertEquals(FORMATTED_TEN_THOUSANDTHS, account.getBalance());
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndScaleIsGreaterThanTwoAndWithNonZeros() {
        Exception e = assertThrows(BadRequestException.class,
                () -> service.increaseUserBalance(EXISTING_USER_ID, ONE_THOUSANDTH)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndAmountIsZero() {
        Exception e = assertThrows(BadRequestException.class,
                () -> service.increaseUserBalance(EXISTING_USER_ID, ZERO)
        );
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndAmountIsNegative() {
        Exception e = assertThrows(BadRequestException.class,
                () -> service.increaseUserBalance(EXISTING_USER_ID, MINUS_TEN)
        );
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndAmountIsNull() {
        Exception e = assertThrows(BadRequestException.class,
                () -> service.increaseUserBalance(EXISTING_USER_ID, NULL)
        );
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testIncreaseUserBalanceWhenUserDoesNotExist() {
        Exception e = assertThrows(UserNotFoundException.class,
                () -> service.increaseUserBalance(NON_EXISTING_USER_ID, TEN)
        );
        assertEquals(USER_NOT_FOUND, e.getMessage());
    }

    @Test
    void testIncreaseUserBalanceWhenUserIdIsNull() {
        Exception e = assertThrows(BadRequestException.class, () -> service.increaseUserBalance(null, TEN));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsLessThatBalance() {
        account.increaseBalance(TEN);
        service.decreaseUserBalance(EXISTING_USER_ID, ONE);
        assertEquals(FORMATTED_NINE, account.getBalance());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsEqualToBalance() {
        account.increaseBalance(TEN);
        service.decreaseUserBalance(EXISTING_USER_ID, TEN);
        assertEquals(FORMATTED_ZERO, account.getBalance());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndScaleIsGreaterThanTwoButWithZeros() {
        BigDecimal expected = FORMATTED_TEN.subtract(FORMATTED_TEN_THOUSANDTHS);
        account.increaseBalance(TEN);
        service.decreaseUserBalance(EXISTING_USER_ID, TEN_THOUSANDTHS);
        assertEquals(expected, account.getBalance());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndScaleIsGreaterThanTwoAndWithNonZeros() {
        account.increaseBalance(TEN);
        Exception e = assertThrows(BadRequestException.class,
                () -> service.decreaseUserBalance(EXISTING_USER_ID, ONE_THOUSANDTH)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsGreaterThanBalance() {
        Exception e = assertThrows(BadRequestException.class, () -> service.decreaseUserBalance(EXISTING_USER_ID, ONE));
        assertEquals(INSUFFICIENT_BALANCE, e.getMessage());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsZero() {
        Exception e = assertThrows(BadRequestException.class,
                () -> service.decreaseUserBalance(EXISTING_USER_ID, ZERO)
        );
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsNegative() {
        account.increaseBalance(TEN);
        Exception e = assertThrows(BadRequestException.class,
                () -> service.decreaseUserBalance(EXISTING_USER_ID, MINUS_ONE)
        );
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsNull() {
        Exception e = assertThrows(BadRequestException.class,
                () -> service.decreaseUserBalance(EXISTING_USER_ID, NULL)
        );
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
    }

    @Test
    void testDecreaseUserBalanceWhenUserDoesNotExist() {
        account.increaseBalance(TEN);
        Exception e = assertThrows(UserNotFoundException.class,
                () -> service.decreaseUserBalance(NON_EXISTING_USER_ID, ONE)
        );
        assertEquals(USER_NOT_FOUND, e.getMessage());
    }

    @Test
    void testDecreaseUserBalanceWhenUserIdIsNull() {
        account.increaseBalance(TEN);
        Exception e = assertThrows(BadRequestException.class, () -> service.decreaseUserBalance(null, ONE));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
    }

    private List<Account> userIdsToAccounts(Object o) {
        try {
            @SuppressWarnings("unchecked") Iterable<Long> userIds = (Iterable<Long>) o;
            List<Account> accounts = new ArrayList<>();
            for (Long currentId : userIds) {
                if (EXISTING_USER_ID.equals(currentId)) {
                    accounts.add(account);
                    return accounts;
                }
            }
            return accounts;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("argument is not of Iterable<Long> type");
        }
    }

    private Account storeAccount(Object o) {
        try {
            Account a = (Account) o;
            storedAccounts.add(a);
            return a;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("argument is not of Account type");
        }
    }

    private List<Account> storeAllAccounts(Object o) {
        try {
            @SuppressWarnings("unchecked") Iterable<Account> accounts = (Iterable<Account>) o;
            for (Account a : accounts) {
                storedAccounts.add(a);
            }
            return storedAccounts;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("argument is not of Iterable<Account> type");
        }
    }
}