package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.Transfer;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.IllegalAmountException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.exception.UserNotFoundToGetBalanceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
    private static final Long RECEIVER_ID = 2L;
    private Date dateFrom;
    private Date dateTo;
    private Account account;
    private Account receiverAccount;
    private List<Operation> operations;
    private AccountService accountService;
    private OperationService operationService;
    private TransferService transferService;
    private ApiService service;
    private Date transferDate;

    @BeforeEach
    void setUp() {
        dateFrom = mock(Date.class);
        dateTo = mock(Date.class);
        account = mock(Account.class);
        receiverAccount = mock(Account.class);
        operations = spy(new ArrayList<>());
        accountService = mock(AccountService.class);
        operationService = mock(OperationService.class);
        transferService = mock(TransferService.class);
        service = new ApiService(accountService, operationService, transferService);
        transferDate = null;
    }

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(ignoreStubs(dateFrom));
        verifyNoMoreInteractions(ignoreStubs(dateTo));
        verifyNoMoreInteractions(ignoreStubs(account));
        verifyNoMoreInteractions(ignoreStubs(receiverAccount));
        verifyNoMoreInteractions(ignoreStubs(operations));
        verifyNoMoreInteractions(ignoreStubs(accountService));
        verifyNoMoreInteractions(ignoreStubs(operationService));
        verifyNoMoreInteractions(ignoreStubs(transferService));
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

    @Test
    void testTransferMoneyWhenUserIdIsNull() {
        Exception e = assertThrows(BadRequestException.class,
                () -> service.transferMoney(null, RECEIVER_ID, TEN)
        );
        assertEquals(USER_ID_IS_NULL, e.getMessage());
    }

    @Test
    void testTransferMoneyWhenReceiverIdIsNull() {
        Exception e = assertThrows(BadRequestException.class,
                () -> service.transferMoney(USER_ID, null, TEN)
        );
        assertEquals(RECEIVER_ID_IS_NULL, e.getMessage());
    }

    @Test
    void testTransferMoneyWhenUserDoesNotExist() {
        when(accountService.getAccount(USER_ID)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class,
                () -> service.transferMoney(USER_ID, RECEIVER_ID, TEN)
        );
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(accountService).getAccount(USER_ID);
    }

    @Test
    void testTransferMoneyWhenReceiverDoesNotExist() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(accountService.getAccount(RECEIVER_ID)).thenThrow(new UserNotFoundException(RECEIVER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class,
                () -> service.transferMoney(USER_ID, RECEIVER_ID, TEN)
        );
        assertEquals(RECEIVER_NOT_FOUND, e.getMessage());
        verify(accountService).getAccount(USER_ID);
        verify(accountService).getAccount(RECEIVER_ID);
    }

    @Test
    void testTransferMoneyWhenAmountIsNull() {
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(accountService.getAccount(RECEIVER_ID)).thenReturn(receiverAccount);
        doThrow(new IllegalAmountException(AMOUNT_IS_NULL)).when(account).decreaseBalance(null);
        Exception e = assertThrows(BadRequestException.class,
                () -> service.transferMoney(USER_ID, RECEIVER_ID, null)
        );
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(accountService).getAccount(USER_ID);
        verify(accountService).getAccount(RECEIVER_ID);
        verify(account).decreaseBalance(null);
    }

    @Test
    void testTransferMoneyWhenAmountIsNegative() {
        BigDecimal amount = MINUS_TEN;
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(accountService.getAccount(RECEIVER_ID)).thenReturn(receiverAccount);
        doThrow(new IllegalAmountException(AMOUNT_IS_NEGATIVE)).when(account).decreaseBalance(amount);
        Exception e = assertThrows(BadRequestException.class,
                    () -> service.transferMoney(USER_ID, RECEIVER_ID, amount)
        );
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(accountService).getAccount(USER_ID);
        verify(accountService).getAccount(RECEIVER_ID);
        verify(account).decreaseBalance(amount);
    }

    @Test
    void testTransferMoneyWhenAmountIsZero() {
        BigDecimal amount = ZERO;
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(accountService.getAccount(RECEIVER_ID)).thenReturn(receiverAccount);
        doThrow(new IllegalAmountException(AMOUNT_IS_ZERO)).when(account).decreaseBalance(amount);
        Exception e = assertThrows(BadRequestException.class,
                () -> service.transferMoney(USER_ID, RECEIVER_ID, amount)
        );
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(accountService).getAccount(USER_ID);
        verify(accountService).getAccount(RECEIVER_ID);
        verify(account).decreaseBalance(amount);
    }

    @Test
    void testTransferMoneyWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        BigDecimal amount = ONE_THOUSANDTH;
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(accountService.getAccount(RECEIVER_ID)).thenReturn(receiverAccount);
        doThrow(new IllegalAmountException(WRONG_MINOR_UNITS)).when(account).decreaseBalance(amount);
        Exception e = assertThrows(BadRequestException.class,
                () -> service.transferMoney(USER_ID, RECEIVER_ID, amount)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verify(accountService).getAccount(USER_ID);
        verify(accountService).getAccount(RECEIVER_ID);
        verify(account).decreaseBalance(amount);
    }

    @Test
    void testTransferMoneyWhenBalanceIsInsufficient() {
        BigDecimal amount = TEN;
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(accountService.getAccount(RECEIVER_ID)).thenReturn(receiverAccount);
        doThrow(new IllegalAmountException(INSUFFICIENT_BALANCE)).when(account).decreaseBalance(amount);
        Exception e = assertThrows(BadRequestException.class,
                () -> service.transferMoney(USER_ID, RECEIVER_ID, amount)
        );
        assertEquals(INSUFFICIENT_BALANCE, e.getMessage());
        verify(accountService).getAccount(USER_ID);
        verify(accountService).getAccount(RECEIVER_ID);
        verify(account).decreaseBalance(amount);
    }

    @Test
    void testTransferMoneyWhenBalanceIsSufficient() {
        BigDecimal amount = TEN;
        Operation outgoing = mock(Operation.class);
        Operation incoming = mock(Operation.class);
        Transfer transfer = mock(Transfer.class);
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(accountService.getAccount(RECEIVER_ID)).thenReturn(receiverAccount);
        doNothing().when(account).decreaseBalance(amount);
        doNothing().when(receiverAccount).increaseBalance(amount);
        when(accountService.saveAccount(account)).thenReturn(account);
        when(accountService.saveAccount(receiverAccount)).thenReturn(receiverAccount);
        when(operationService.createOutgoingTransfer(eq(account), eq(amount), any(Date.class))).thenAnswer(a -> {
            storeTransferDate(a.getArguments()[2]);
            return outgoing;
        });
        when(operationService.createIncomingTransfer(eq(receiverAccount), eq(amount), any(Date.class))).thenAnswer(
                a -> {
                    assertEquals(transferDate, a.getArguments()[2]);
                    return incoming;
                }
        );
        when(operationService.saveOperation(outgoing)).thenAnswer(a -> {
            verify(accountService).saveAccount(account);
            return a.getArguments()[0];
        });
        when(operationService.saveOperation(incoming)).thenAnswer(a -> {
            verify(accountService).saveAccount(receiverAccount);
            return a.getArguments()[0];
        });
        when(transferService.createTransfer(outgoing, incoming)).thenReturn(transfer);
        when(transferService.saveTransfer(transfer)).thenReturn(transfer);
        assertDoesNotThrow(() -> service.transferMoney(USER_ID, RECEIVER_ID, amount));
        verify(accountService).getAccount(USER_ID);
        verify(accountService).getAccount(RECEIVER_ID);
        verify(account).decreaseBalance(amount);
        verify(receiverAccount).increaseBalance(amount);
        verify(accountService).saveAccount(account);
        verify(accountService).saveAccount(receiverAccount);
        verify(operationService).createOutgoingTransfer(eq(account), eq(amount), any(Date.class));
        verify(operationService).createIncomingTransfer(eq(receiverAccount), eq(amount), any(Date.class));
        verify(operationService).saveOperation(outgoing);
        verify(operationService).saveOperation(incoming);
        verify(transferService).createTransfer(outgoing, incoming);
        verify(transferService).saveTransfer(transfer);
    }

    @Test
    void testTransferMoneyWhenScaleIsGreaterThatTwoButWithZeros() {
        BigDecimal amount = TEN_THOUSANDTHS;
        Operation outgoing = mock(Operation.class);
        Operation incoming = mock(Operation.class);
        Transfer transfer = mock(Transfer.class);
        when(accountService.getAccount(USER_ID)).thenReturn(account);
        when(accountService.getAccount(RECEIVER_ID)).thenReturn(receiverAccount);
        doNothing().when(account).decreaseBalance(amount);
        doNothing().when(receiverAccount).increaseBalance(amount);
        when(accountService.saveAccount(account)).thenReturn(account);
        when(accountService.saveAccount(receiverAccount)).thenReturn(receiverAccount);
        when(operationService.createOutgoingTransfer(eq(account), eq(amount), any(Date.class))).thenAnswer(a -> {
            storeTransferDate(a.getArguments()[2]);
            return outgoing;
        });
        when(operationService.createIncomingTransfer(eq(receiverAccount), eq(amount), any(Date.class))).thenAnswer(
                a -> {
                    assertEquals(transferDate, a.getArguments()[2]);
                    return incoming;
                }
        );
        when(operationService.saveOperation(outgoing)).thenAnswer(a -> {
            verify(accountService).saveAccount(account);
            return a.getArguments()[0];
        });
        when(operationService.saveOperation(incoming)).thenAnswer(a -> {
            verify(accountService).saveAccount(receiverAccount);
            return a.getArguments()[0];
        });
        when(transferService.createTransfer(outgoing, incoming)).thenReturn(transfer);
        when(transferService.saveTransfer(transfer)).thenReturn(transfer);
        assertDoesNotThrow(() -> service.transferMoney(USER_ID, RECEIVER_ID, amount));
        verify(accountService).getAccount(USER_ID);
        verify(accountService).getAccount(RECEIVER_ID);
        verify(account).decreaseBalance(amount);
        verify(receiverAccount).increaseBalance(amount);
        verify(accountService).saveAccount(account);
        verify(accountService).saveAccount(receiverAccount);
        verify(operationService).createOutgoingTransfer(eq(account), eq(amount), any(Date.class));
        verify(operationService).createIncomingTransfer(eq(receiverAccount), eq(amount), any(Date.class));
        verify(operationService).saveOperation(outgoing);
        verify(operationService).saveOperation(incoming);
        verify(transferService).createTransfer(outgoing, incoming);
        verify(transferService).saveTransfer(transfer);
    }

    private void storeTransferDate(Object o) {
        if (o instanceof Date) {
            transferDate = (Date) o;
        } else {
            throw new IllegalArgumentException("argument is not of Date type");
        }
    }

}