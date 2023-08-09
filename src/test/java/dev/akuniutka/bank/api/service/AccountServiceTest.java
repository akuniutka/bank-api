package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.exception.CashOrderException;
import dev.akuniutka.bank.api.exception.GetBalanceException;
import dev.akuniutka.bank.api.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    private static final Long EXISTING_USER_ID = 123456789L;
    private static final Long NON_EXISTING_USER_ID = 987654321L;
    private static final String USER_ID_IS_NULL = "user id is null";
    private static final String USER_NOT_FOUND = "user not found";
    private static final String AMOUNT_IS_NULL = "amount is null";
    private static final String AMOUNT_IS_ZERO = "amount is zero";
    private static final String AMOUNT_IS_NEGATIVE = "amount is negative";
    private static final String WRONG_MINOR_UNITS = "wrong minor units";
    private static final String INSUFFICIENT_BALANCE = "insufficient balance";
    private Account account;
    @Mock
    private AccountRepository repository;
    @InjectMocks
    private AccountService service;
    private final List<Account> storedAccounts = new ArrayList<>();

    @BeforeEach
    void setUp() {
        account = new Account();
        List<Account> accounts = new ArrayList<>();
        accounts.add(account);
        storedAccounts.clear();
        Mockito.lenient().when(repository.findById(EXISTING_USER_ID)).thenReturn(Optional.of(account));
        Mockito.lenient().when(repository.findById(NON_EXISTING_USER_ID)).thenReturn(Optional.empty());
        Mockito.lenient().when(repository.existsById(EXISTING_USER_ID)).thenReturn(true);
        Mockito.lenient().when(repository.existsById(NON_EXISTING_USER_ID)).thenReturn(false);
        Mockito.lenient().when(repository.save(Mockito.any(Account.class))).thenAnswer(a -> store(a.getArguments()[0]));
        Mockito.lenient().when(repository.findAll()).thenReturn(accounts);
        Mockito.lenient().when(repository.findAllById(Mockito.anyIterable())).thenAnswer(
                a -> userIdsToAccounts(a.getArguments()[0])
        );
        Mockito.lenient().when(repository.saveAll(Mockito.anyIterable())).thenAnswer(a -> storeAll(a.getArguments()[0]));
        Mockito.lenient().when(repository.count()).thenReturn(1L);
    }

    @Test
    void testAccountService() {
        assertDoesNotThrow(() -> new AccountService(null));
    }

    @Test
    void testGetUserBalanceWhenUserExists() {
        BigDecimal expected = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal actual = service.getUserBalance(EXISTING_USER_ID);
        assertEquals(expected, actual);
    }

    @Test
    void testGetUserBalanceWhenUserDoesNotExist() {
        Exception exception = assertThrows(GetBalanceException.class,
                () -> service.getUserBalance(NON_EXISTING_USER_ID)
        );
        assertEquals(USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testGetUserBalanceWhenUserIdIsNull() {
        Exception exception = assertThrows(GetBalanceException.class,
                () -> service.getUserBalance(null)
        );
        assertEquals(USER_ID_IS_NULL, exception.getMessage());
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndAmountIsPositive() {
        BigDecimal amount = BigDecimal.TEN;
        BigDecimal expected = amount.setScale(2, RoundingMode.HALF_UP);
        service.increaseUserBalance(EXISTING_USER_ID, amount);
        assertEquals(expected, account.getBalance());
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndScaleIsGreaterThanTwoButWithZeros() {
        BigDecimal amount = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        BigDecimal expected = amount.setScale(2, RoundingMode.HALF_UP);
        service.increaseUserBalance(EXISTING_USER_ID, amount);
        assertEquals(expected, account.getBalance());
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndScaleIsGreaterThanTwoAndWithNonZeros() {
        BigDecimal amount = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        Exception exception = assertThrows(CashOrderException.class,
                () -> service.increaseUserBalance(EXISTING_USER_ID, amount)
        );
        assertEquals(WRONG_MINOR_UNITS, exception.getMessage());
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndAmountIsZero() {
        BigDecimal amount = BigDecimal.ZERO;
        Exception exception = assertThrows(CashOrderException.class,
                () -> service.increaseUserBalance(EXISTING_USER_ID, amount)
        );
        assertEquals(AMOUNT_IS_ZERO, exception.getMessage());
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndAmountIsNegative() {
        BigDecimal amount = BigDecimal.TEN.negate();
        Exception exception = assertThrows(CashOrderException.class,
                () -> service.increaseUserBalance(EXISTING_USER_ID, amount)
        );
        assertEquals(AMOUNT_IS_NEGATIVE, exception.getMessage());
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndAmountIsNull() {
        Exception exception = assertThrows(CashOrderException.class,
                () -> service.increaseUserBalance(EXISTING_USER_ID, null)
        );
        assertEquals(AMOUNT_IS_NULL, exception.getMessage());
    }

    @Test
    void testIncreaseUserBalanceWhenUserDoesNotExist() {
        BigDecimal amount = BigDecimal.TEN;
        Exception exception = assertThrows(CashOrderException.class,
                () -> service.increaseUserBalance(NON_EXISTING_USER_ID, amount)
        );
        assertEquals(USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testIncreaseUserBalanceWhenUserIdIsNull() {
        BigDecimal amount = BigDecimal.TEN;
        Exception exception = assertThrows(CashOrderException.class,
                () -> service.increaseUserBalance(null, amount)
        );
        assertEquals(USER_ID_IS_NULL, exception.getMessage());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsLessThatBalance() {
        BigDecimal initialBalance = BigDecimal.TEN;
        BigDecimal amountWithdrawn = BigDecimal.ONE;
        BigDecimal expected = initialBalance.subtract(amountWithdrawn).setScale(2, RoundingMode.HALF_UP);
        account.increaseBalance(initialBalance);
        service.decreaseUserBalance(EXISTING_USER_ID, amountWithdrawn);
        assertEquals(expected, account.getBalance());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsEqualToBalance() {
        BigDecimal initialBalance = BigDecimal.TEN;
        BigDecimal amountWithdrawn = BigDecimal.TEN;
        BigDecimal expected = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        account.increaseBalance(initialBalance);
        service.decreaseUserBalance(EXISTING_USER_ID, amountWithdrawn);
        assertEquals(expected, account.getBalance());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndScaleIsGreaterThanTwoButWithZeros() {
        BigDecimal initialBalance = BigDecimal.TEN;
        BigDecimal amountWithdrawn = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        BigDecimal expected = initialBalance.subtract(amountWithdrawn).setScale(2, RoundingMode.HALF_UP);
        account.increaseBalance(initialBalance);
        service.decreaseUserBalance(EXISTING_USER_ID, amountWithdrawn);
        assertEquals(expected, account.getBalance());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndScaleIsGreaterThanTwoAndWithNonZeros() {
        BigDecimal initialBalance = BigDecimal.TEN;
        BigDecimal amountWithdrawn = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        account.increaseBalance(initialBalance);
        Exception exception = assertThrows(CashOrderException.class,
                () -> service.decreaseUserBalance(EXISTING_USER_ID, amountWithdrawn)
        );
        assertEquals(WRONG_MINOR_UNITS, exception.getMessage());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsGreaterThanBalance() {
        BigDecimal amountWithdrawn = BigDecimal.ONE;
        Exception exception = assertThrows(CashOrderException.class,
                () -> service.decreaseUserBalance(EXISTING_USER_ID, amountWithdrawn)
        );
        assertEquals(INSUFFICIENT_BALANCE, exception.getMessage());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsZero() {
        BigDecimal amountWithdrawn = BigDecimal.ZERO;
        Exception exception = assertThrows(CashOrderException.class,
                () -> service.decreaseUserBalance(EXISTING_USER_ID, amountWithdrawn)
        );
        assertEquals(AMOUNT_IS_ZERO, exception.getMessage());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsNegative() {
        BigDecimal initialBalance = BigDecimal.TEN;
        BigDecimal amountWithdrawn = BigDecimal.ONE.negate();
        account.increaseBalance(initialBalance);
        Exception exception = assertThrows(CashOrderException.class,
                () -> service.decreaseUserBalance(EXISTING_USER_ID, amountWithdrawn)
        );
        assertEquals(AMOUNT_IS_NEGATIVE, exception.getMessage());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsNull() {
        Exception exception = assertThrows(CashOrderException.class,
                () -> service.decreaseUserBalance(EXISTING_USER_ID, null)
        );
        assertEquals(AMOUNT_IS_NULL, exception.getMessage());
    }

    @Test
    void testDecreaseUserBalanceWhenUserDoesNotExist() {
        BigDecimal initialBalance = BigDecimal.TEN;
        BigDecimal amountWithdrawn = BigDecimal.ONE;
        account.increaseBalance(initialBalance);
        Exception exception = assertThrows(CashOrderException.class,
                () -> service.decreaseUserBalance(NON_EXISTING_USER_ID, amountWithdrawn)
        );
        assertEquals(USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testDecreaseUserBalanceWhenUserIdIsNull() {
        BigDecimal initialBalance = BigDecimal.TEN;
        BigDecimal amountWithdrawn = BigDecimal.ONE;
        account.increaseBalance(initialBalance);
        Exception exception = assertThrows(CashOrderException.class,
                () -> service.decreaseUserBalance(null, amountWithdrawn)
        );
        assertEquals(USER_ID_IS_NULL, exception.getMessage());
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

    private Account store(Object o) {
        try {
            Account a = (Account) o;
            storedAccounts.add(a);
            return a;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("argument is not of Account type");
        }
    }

    private List<Account> storeAll(Object o) {
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