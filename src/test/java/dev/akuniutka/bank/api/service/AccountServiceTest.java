package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.OperationType;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.exception.UserNotFoundToGetBalanceException;
import dev.akuniutka.bank.api.repository.AccountRepository;
import dev.akuniutka.bank.api.repository.OperationRepository;
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
import java.util.Date;
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
    private AccountRepository accountRepository;
    @Mock
    private OperationRepository operationRepository;
    @InjectMocks
    private AccountService service;
    private final List<Account> storedAccounts = new ArrayList<>();
    private final List<Operation> storedOperations = new ArrayList<>();

    @BeforeEach
    void setUp() {
        account = new Account();
        List<Account> accounts = new ArrayList<>();
        List<Operation> operations = new ArrayList<>();
        accounts.add(account);
        storedAccounts.clear();
        storedOperations.clear();
        Mockito.lenient().when(accountRepository.findById(EXISTING_USER_ID)).thenReturn(Optional.of(account));
        Mockito.lenient().when(accountRepository.findById(NON_EXISTING_USER_ID)).thenReturn(Optional.empty());
        Mockito.lenient().when(accountRepository.existsById(EXISTING_USER_ID)).thenReturn(true);
        Mockito.lenient().when(accountRepository.existsById(NON_EXISTING_USER_ID)).thenReturn(false);
        Mockito.lenient().when(accountRepository.save(Mockito.any(Account.class))).thenAnswer(
                a -> storeAccount(a.getArguments()[0])
        );
        Mockito.lenient().when(accountRepository.findAll()).thenReturn(accounts);
        Mockito.lenient().when(accountRepository.findAllById(Mockito.anyIterable())).thenAnswer(
                a -> userIdsToAccounts(a.getArguments()[0])
        );
        Mockito.lenient().when(accountRepository.saveAll(Mockito.anyIterable())).thenAnswer(
                a -> storeAllAccounts(a.getArguments()[0])
        );
        Mockito.lenient().when(accountRepository.count()).thenReturn(1L);
        Mockito.lenient().when(operationRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.empty());
        Mockito.lenient().when(operationRepository.existsById(Mockito.any(Long.class))).thenReturn(false);
        Mockito.lenient().when(operationRepository.save(Mockito.any(Operation.class))).thenAnswer(
                a -> storeOperation(a.getArguments()[0])
        );
        Mockito.lenient().when(operationRepository.findAll()).thenReturn(operations);
        Mockito.lenient().when(operationRepository.findAllById(Mockito.anyIterable())).thenReturn(operations);
        Mockito.lenient().when(operationRepository.saveAll(Mockito.anyIterable())).thenAnswer(
                a -> storeAllOperations(a.getArguments()[0])
        );
        Mockito.lenient().when(operationRepository.count()).thenReturn(0L);
    }

    @Test
    void testAccountService() {
        assertDoesNotThrow(() -> new AccountService(null, null));
    }

    @Test
    void testGetUserBalanceWhenUserExists() {
        BigDecimal expected = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal actual = service.getUserBalance(EXISTING_USER_ID);
        assertEquals(expected, actual);
    }

    @Test
    void testGetUserBalanceWhenUserDoesNotExist() {
        Exception exception = assertThrows(UserNotFoundToGetBalanceException.class,
                () -> service.getUserBalance(NON_EXISTING_USER_ID)
        );
        assertEquals(USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testGetUserBalanceWhenUserIdIsNull() {
        Exception exception = assertThrows(BadRequestException.class,
                () -> service.getUserBalance(null)
        );
        assertEquals(USER_ID_IS_NULL, exception.getMessage());
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndAmountIsPositive() {
        BigDecimal amount = BigDecimal.TEN;
        BigDecimal expected = amount.setScale(2, RoundingMode.HALF_UP);
        Date startDate = new Date();
        service.increaseUserBalance(EXISTING_USER_ID, amount);
        Date finishDate = new Date();
        assertEquals(expected, account.getBalance());
        assertEquals(1, storedOperations.size());
        Operation operation = storedOperations.get(0);
        assertNull(operation.getId());
        assertEquals(account, operation.getAccount());
        assertEquals(OperationType.DEPOSIT, operation.getType());
        assertEquals(expected, operation.getAmount());
        assertTrue(startDate.compareTo(operation.getDate()) <= 0);
        assertTrue(finishDate.compareTo(operation.getDate()) >= 0);
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndScaleIsGreaterThanTwoButWithZeros() {
        BigDecimal amount = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        BigDecimal expected = amount.setScale(2, RoundingMode.HALF_UP);
        Date startDate = new Date();
        service.increaseUserBalance(EXISTING_USER_ID, amount);
        Date finishDate = new Date();
        assertEquals(expected, account.getBalance());
        assertEquals(1, storedOperations.size());
        Operation operation = storedOperations.get(0);
        assertNull(operation.getId());
        assertEquals(account, operation.getAccount());
        assertEquals(OperationType.DEPOSIT, operation.getType());
        assertEquals(expected, operation.getAmount());
        assertTrue(startDate.compareTo(operation.getDate()) <= 0);
        assertTrue(finishDate.compareTo(operation.getDate()) >= 0);
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndScaleIsGreaterThanTwoAndWithNonZeros() {
        BigDecimal amount = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        Exception exception = assertThrows(BadRequestException.class,
                () -> service.increaseUserBalance(EXISTING_USER_ID, amount)
        );
        assertEquals(WRONG_MINOR_UNITS, exception.getMessage());
        assertEquals(0, storedOperations.size());
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndAmountIsZero() {
        BigDecimal amount = BigDecimal.ZERO;
        Exception exception = assertThrows(BadRequestException.class,
                () -> service.increaseUserBalance(EXISTING_USER_ID, amount)
        );
        assertEquals(AMOUNT_IS_ZERO, exception.getMessage());
        assertEquals(0, storedOperations.size());
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndAmountIsNegative() {
        BigDecimal amount = BigDecimal.TEN.negate();
        Exception exception = assertThrows(BadRequestException.class,
                () -> service.increaseUserBalance(EXISTING_USER_ID, amount)
        );
        assertEquals(AMOUNT_IS_NEGATIVE, exception.getMessage());
        assertEquals(0, storedOperations.size());
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndAmountIsNull() {
        Exception exception = assertThrows(BadRequestException.class,
                () -> service.increaseUserBalance(EXISTING_USER_ID, null)
        );
        assertEquals(AMOUNT_IS_NULL, exception.getMessage());
        assertEquals(0, storedOperations.size());
    }

    @Test
    void testIncreaseUserBalanceWhenUserDoesNotExist() {
        BigDecimal amount = BigDecimal.TEN;
        Exception exception = assertThrows(UserNotFoundException.class,
                () -> service.increaseUserBalance(NON_EXISTING_USER_ID, amount)
        );
        assertEquals(USER_NOT_FOUND, exception.getMessage());
        assertEquals(0, storedOperations.size());
    }

    @Test
    void testIncreaseUserBalanceWhenUserIdIsNull() {
        BigDecimal amount = BigDecimal.TEN;
        Exception exception = assertThrows(BadRequestException.class,
                () -> service.increaseUserBalance(null, amount)
        );
        assertEquals(USER_ID_IS_NULL, exception.getMessage());
        assertEquals(0, storedOperations.size());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsLessThatBalance() {
        BigDecimal initialBalance = BigDecimal.TEN;
        BigDecimal amountWithdrawn = BigDecimal.ONE;
        BigDecimal expected = initialBalance.subtract(amountWithdrawn).setScale(2, RoundingMode.HALF_UP);
        account.increaseBalance(initialBalance);
        Date startDate = new Date();
        service.decreaseUserBalance(EXISTING_USER_ID, amountWithdrawn);
        Date finishDate = new Date();
        assertEquals(expected, account.getBalance());
        expected = amountWithdrawn.setScale(2, RoundingMode.HALF_UP);
        assertEquals(1, storedOperations.size());
        Operation operation = storedOperations.get(0);
        assertNull(operation.getId());
        assertEquals(account, operation.getAccount());
        assertEquals(OperationType.WITHDRAWAL, operation.getType());
        assertEquals(expected, operation.getAmount());
        assertTrue(startDate.compareTo(operation.getDate()) <= 0);
        assertTrue(finishDate.compareTo(operation.getDate()) >= 0);
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsEqualToBalance() {
        BigDecimal initialBalance = BigDecimal.TEN;
        BigDecimal amountWithdrawn = BigDecimal.TEN;
        BigDecimal expected = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        account.increaseBalance(initialBalance);
        Date startDate = new Date();
        service.decreaseUserBalance(EXISTING_USER_ID, amountWithdrawn);
        Date finishDate = new Date();
        assertEquals(expected, account.getBalance());
        expected = amountWithdrawn.setScale(2, RoundingMode.HALF_UP);
        assertEquals(1, storedOperations.size());
        Operation operation = storedOperations.get(0);
        assertNull(operation.getId());
        assertEquals(account, operation.getAccount());
        assertEquals(OperationType.WITHDRAWAL, operation.getType());
        assertEquals(expected, operation.getAmount());
        assertTrue(startDate.compareTo(operation.getDate()) <= 0);
        assertTrue(finishDate.compareTo(operation.getDate()) >= 0);
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
        Date startDate = new Date();
        service.decreaseUserBalance(EXISTING_USER_ID, amountWithdrawn);
        Date finishDate = new Date();
        assertEquals(expected, account.getBalance());
        expected = amountWithdrawn.setScale(2, RoundingMode.HALF_UP);
        assertEquals(1, storedOperations.size());
        Operation operation = storedOperations.get(0);
        assertNull(operation.getId());
        assertEquals(account, operation.getAccount());
        assertEquals(OperationType.WITHDRAWAL, operation.getType());
        assertEquals(expected, operation.getAmount());
        assertTrue(startDate.compareTo(operation.getDate()) <= 0);
        assertTrue(finishDate.compareTo(operation.getDate()) >= 0);
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
        Exception exception = assertThrows(BadRequestException.class,
                () -> service.decreaseUserBalance(EXISTING_USER_ID, amountWithdrawn)
        );
        assertEquals(WRONG_MINOR_UNITS, exception.getMessage());
        assertEquals(0, storedOperations.size());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsGreaterThanBalance() {
        BigDecimal amountWithdrawn = BigDecimal.ONE;
        Exception exception = assertThrows(BadRequestException.class,
                () -> service.decreaseUserBalance(EXISTING_USER_ID, amountWithdrawn)
        );
        assertEquals(INSUFFICIENT_BALANCE, exception.getMessage());
        assertEquals(0, storedOperations.size());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsZero() {
        BigDecimal amountWithdrawn = BigDecimal.ZERO;
        Exception exception = assertThrows(BadRequestException.class,
                () -> service.decreaseUserBalance(EXISTING_USER_ID, amountWithdrawn)
        );
        assertEquals(AMOUNT_IS_ZERO, exception.getMessage());
        assertEquals(0, storedOperations.size());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsNegative() {
        BigDecimal initialBalance = BigDecimal.TEN;
        BigDecimal amountWithdrawn = BigDecimal.ONE.negate();
        account.increaseBalance(initialBalance);
        Exception exception = assertThrows(BadRequestException.class,
                () -> service.decreaseUserBalance(EXISTING_USER_ID, amountWithdrawn)
        );
        assertEquals(AMOUNT_IS_NEGATIVE, exception.getMessage());
        assertEquals(0, storedOperations.size());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsNull() {
        Exception exception = assertThrows(BadRequestException.class,
                () -> service.decreaseUserBalance(EXISTING_USER_ID, null)
        );
        assertEquals(AMOUNT_IS_NULL, exception.getMessage());
        assertEquals(0, storedOperations.size());
    }

    @Test
    void testDecreaseUserBalanceWhenUserDoesNotExist() {
        BigDecimal initialBalance = BigDecimal.TEN;
        BigDecimal amountWithdrawn = BigDecimal.ONE;
        account.increaseBalance(initialBalance);
        Exception exception = assertThrows(UserNotFoundException.class,
                () -> service.decreaseUserBalance(NON_EXISTING_USER_ID, amountWithdrawn)
        );
        assertEquals(USER_NOT_FOUND, exception.getMessage());
        assertEquals(0, storedOperations.size());
    }

    @Test
    void testDecreaseUserBalanceWhenUserIdIsNull() {
        BigDecimal initialBalance = BigDecimal.TEN;
        BigDecimal amountWithdrawn = BigDecimal.ONE;
        account.increaseBalance(initialBalance);
        Exception exception = assertThrows(BadRequestException.class,
                () -> service.decreaseUserBalance(null, amountWithdrawn)
        );
        assertEquals(USER_ID_IS_NULL, exception.getMessage());
        assertEquals(0, storedOperations.size());
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

    private Operation storeOperation(Object o) {
        try {
            Operation op = (Operation) o;
            storedOperations.add(op);
            return op;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("argument iis not of Operation type");
        }
    }

    private List<Operation> storeAllOperations(Object o) {
        try {
            @SuppressWarnings("unchecked") Iterable<Operation> operations = (Iterable<Operation>) o;
            for (Operation op : operations) {
                storedOperations.add(op);
            }
            return storedOperations;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("argument is not of Iterable<Operation> type");
        }
    }
}