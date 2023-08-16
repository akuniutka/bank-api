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
import java.util.ArrayList;
import java.util.Date;
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
    private List<Operation> storedOperations;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private OperationRepository operationRepository;
    @InjectMocks
    private AccountService service;

    @BeforeEach
    void setUp() {
        account = new Account();
        storedAccounts = new ArrayList<>();
        storedOperations = new ArrayList<>();
        List<Account> accounts = new ArrayList<>();
        accounts.add(account);
        List<Operation> operations = new ArrayList<>();
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
        assertDoesNotThrow(() -> new AccountService(accountRepository, operationRepository));
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
        BigDecimal expected = FORMATTED_TEN;
        Date start = new Date();
        service.increaseUserBalance(EXISTING_USER_ID, TEN);
        Date finish = new Date();
        assertEquals(expected, account.getBalance());
        assertEquals(1, storedOperations.size());
        Operation operation = storedOperations.get(0);
        assertNull(operation.getId());
        assertEquals(account, operation.getAccount());
        assertEquals(OperationType.DEPOSIT, operation.getType());
        assertEquals(expected, operation.getAmount());
        assertTrue(isDateBetween(operation.getDate(), start, finish));
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndScaleIsGreaterThanTwoButWithZeros() {
        BigDecimal expected = FORMATTED_TEN_THOUSANDTHS;
        Date start = new Date();
        service.increaseUserBalance(EXISTING_USER_ID, TEN_THOUSANDTHS);
        Date finish = new Date();
        assertEquals(expected, account.getBalance());
        assertEquals(1, storedOperations.size());
        Operation operation = storedOperations.get(0);
        assertNull(operation.getId());
        assertEquals(account, operation.getAccount());
        assertEquals(OperationType.DEPOSIT, operation.getType());
        assertEquals(expected, operation.getAmount());
        assertTrue(isDateBetween(operation.getDate(), start, finish));
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndScaleIsGreaterThanTwoAndWithNonZeros() {
        Exception e = assertThrows(BadRequestException.class,
                () -> service.increaseUserBalance(EXISTING_USER_ID, ONE_THOUSANDTH)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        assertEquals(0, storedOperations.size());
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndAmountIsZero() {
        Exception e = assertThrows(BadRequestException.class,
                () -> service.increaseUserBalance(EXISTING_USER_ID, ZERO)
        );
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        assertEquals(0, storedOperations.size());
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndAmountIsNegative() {
        Exception e = assertThrows(BadRequestException.class,
                () -> service.increaseUserBalance(EXISTING_USER_ID, MINUS_TEN)
        );
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        assertEquals(0, storedOperations.size());
    }

    @Test
    void testIncreaseUserBalanceWhenUserExistsAndAmountIsNull() {
        Exception e = assertThrows(BadRequestException.class,
                () -> service.increaseUserBalance(EXISTING_USER_ID, NULL)
        );
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        assertEquals(0, storedOperations.size());
    }

    @Test
    void testIncreaseUserBalanceWhenUserDoesNotExist() {
        Exception e = assertThrows(UserNotFoundException.class,
                () -> service.increaseUserBalance(NON_EXISTING_USER_ID, TEN)
        );
        assertEquals(USER_NOT_FOUND, e.getMessage());
        assertEquals(0, storedOperations.size());
    }

    @Test
    void testIncreaseUserBalanceWhenUserIdIsNull() {
        Exception e = assertThrows(BadRequestException.class, () -> service.increaseUserBalance(null, TEN));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        assertEquals(0, storedOperations.size());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsLessThatBalance() {
        account.increaseBalance(TEN);
        Date start = new Date();
        service.decreaseUserBalance(EXISTING_USER_ID, ONE);
        Date finish = new Date();
        assertEquals(FORMATTED_NINE, account.getBalance());
        assertEquals(1, storedOperations.size());
        Operation operation = storedOperations.get(0);
        assertNull(operation.getId());
        assertEquals(account, operation.getAccount());
        assertEquals(OperationType.WITHDRAWAL, operation.getType());
        assertEquals(FORMATTED_ONE, operation.getAmount());
        assertTrue(isDateBetween(operation.getDate(), start, finish));
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsEqualToBalance() {
        account.increaseBalance(TEN);
        Date start = new Date();
        service.decreaseUserBalance(EXISTING_USER_ID, TEN);
        Date finish = new Date();
        assertEquals(FORMATTED_ZERO, account.getBalance());
        assertEquals(1, storedOperations.size());
        Operation operation = storedOperations.get(0);
        assertNull(operation.getId());
        assertEquals(account, operation.getAccount());
        assertEquals(OperationType.WITHDRAWAL, operation.getType());
        assertEquals(FORMATTED_TEN, operation.getAmount());
        assertTrue(isDateBetween(operation.getDate(), start, finish));
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndScaleIsGreaterThanTwoButWithZeros() {
        BigDecimal expected = FORMATTED_TEN.subtract(FORMATTED_TEN_THOUSANDTHS);
        account.increaseBalance(TEN);
        Date start = new Date();
        service.decreaseUserBalance(EXISTING_USER_ID, TEN_THOUSANDTHS);
        Date finish = new Date();
        assertEquals(expected, account.getBalance());
        assertEquals(1, storedOperations.size());
        Operation operation = storedOperations.get(0);
        assertNull(operation.getId());
        assertEquals(account, operation.getAccount());
        assertEquals(OperationType.WITHDRAWAL, operation.getType());
        assertEquals(FORMATTED_TEN_THOUSANDTHS, operation.getAmount());
        assertTrue(isDateBetween(operation.getDate(), start, finish));
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndScaleIsGreaterThanTwoAndWithNonZeros() {
        account.increaseBalance(TEN);
        Exception e = assertThrows(BadRequestException.class,
                () -> service.decreaseUserBalance(EXISTING_USER_ID, ONE_THOUSANDTH)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        assertEquals(0, storedOperations.size());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsGreaterThanBalance() {
        Exception e = assertThrows(BadRequestException.class, () -> service.decreaseUserBalance(EXISTING_USER_ID, ONE));
        assertEquals(INSUFFICIENT_BALANCE, e.getMessage());
        assertEquals(0, storedOperations.size());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsZero() {
        Exception e = assertThrows(BadRequestException.class,
                () -> service.decreaseUserBalance(EXISTING_USER_ID, ZERO)
        );
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        assertEquals(0, storedOperations.size());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsNegative() {
        account.increaseBalance(TEN);
        Exception e = assertThrows(BadRequestException.class,
                () -> service.decreaseUserBalance(EXISTING_USER_ID, MINUS_ONE)
        );
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        assertEquals(0, storedOperations.size());
    }

    @Test
    void testDecreaseUserBalanceWhenUserExistsAndAmountIsNull() {
        Exception e = assertThrows(BadRequestException.class,
                () -> service.decreaseUserBalance(EXISTING_USER_ID, NULL)
        );
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        assertEquals(0, storedOperations.size());
    }

    @Test
    void testDecreaseUserBalanceWhenUserDoesNotExist() {
        account.increaseBalance(TEN);
        Exception e = assertThrows(UserNotFoundException.class,
                () -> service.decreaseUserBalance(NON_EXISTING_USER_ID, ONE)
        );
        assertEquals(USER_NOT_FOUND, e.getMessage());
        assertEquals(0, storedOperations.size());
    }

    @Test
    void testDecreaseUserBalanceWhenUserIdIsNull() {
        account.increaseBalance(TEN);
        Exception e = assertThrows(BadRequestException.class, () -> service.decreaseUserBalance(null, ONE));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
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

    private boolean isDateBetween(Date date, Date start, Date finish) {
        if (date == null || start == null || finish == null) {
            return false;
        }
        return start.compareTo(date) * date.compareTo(finish) >= 0;
    }
}