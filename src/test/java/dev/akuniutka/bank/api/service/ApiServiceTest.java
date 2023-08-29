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
        when(accountService.getUserBalance(null)).thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class, () -> service.getBalance(null));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(accountService).getUserBalance(null);
    }

    @Test
    void testGetBalanceWhenUserDoesNotExist() {
        when(accountService.getUserBalance(USER_ID)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundToGetBalanceException.class, () -> service.getBalance(USER_ID));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(accountService).getUserBalance(USER_ID);
    }

    @Test
    void testGetBalanceWhenUserExists() {
        when(accountService.getUserBalance(USER_ID)).thenReturn(FORMATTED_TEN);
        assertEquals(FORMATTED_TEN, service.getBalance(USER_ID));
        verify(accountService).getUserBalance(USER_ID);
    }

    @Test
    void testPutMoneyWhenUserIdIsNull() {
        when(operationService.createDeposit(null, TEN)).thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class, () -> service.putMoney(null, TEN));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(operationService).createDeposit(null, TEN);
    }

    @Test
    void testPutMoneyWhenUserDoesNotExist() {
        when(operationService.createDeposit(USER_ID, TEN)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class, () -> service.putMoney(USER_ID, TEN));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(operationService).createDeposit(USER_ID, TEN);
    }

    @Test
    void testPutMoneyWhenAmountIsNull() {
        when(operationService.createDeposit(USER_ID, null))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_NULL));
        Exception e = assertThrows(BadRequestException.class, () -> service.putMoney(USER_ID, null));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(operationService).createDeposit(USER_ID, null);
    }

    @Test
    void testPutMoneyWhenAmountIsNegative() {
        when(operationService.createDeposit(USER_ID, MINUS_TEN))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_NEGATIVE));
        Exception e = assertThrows(BadRequestException.class, () -> service.putMoney(USER_ID, MINUS_TEN));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(operationService).createDeposit(USER_ID, MINUS_TEN);
    }

    @Test
    void testPutMoneyWhenAmountIsZero() {
        when(operationService.createDeposit(USER_ID, ZERO))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_ZERO));
        Exception e = assertThrows(BadRequestException.class, () -> service.putMoney(USER_ID, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(operationService).createDeposit(USER_ID, ZERO);
    }

    @Test
    void testPutMoneyWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        when(operationService.createDeposit(USER_ID, ONE_THOUSANDTH))
                .thenThrow(new IllegalAmountException(WRONG_MINOR_UNITS));
        Exception e = assertThrows(BadRequestException.class, () -> service.putMoney(USER_ID, ONE_THOUSANDTH));
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verify(operationService).createDeposit(USER_ID, ONE_THOUSANDTH);
    }

    @Test
    void testPutMoneyWhenAmountIsPositive() {
        Operation operation = mock(Operation.class);
        when(operationService.createDeposit(USER_ID, TEN)).thenReturn(operation);
        assertDoesNotThrow(() -> service.putMoney(USER_ID, TEN));
        verify(operationService).createDeposit(USER_ID, TEN);
        verifyNoMoreInteractions(ignoreStubs(operation));
    }

    @Test
    void testPutMoneyWhenScaleIsGreaterThanTwoButWithZeros() {
        Operation operation = mock(Operation.class);
        when(operationService.createDeposit(USER_ID, TEN_THOUSANDTHS)).thenReturn(operation);
        assertDoesNotThrow(() -> service.putMoney(USER_ID, TEN_THOUSANDTHS));
        verify(operationService).createDeposit(USER_ID, TEN_THOUSANDTHS);
        verifyNoMoreInteractions(ignoreStubs(operation));
    }

    @Test
    void testTakeMoneyWhenUserIdIsNull() {
        when(operationService.createWithdrawal(null, ONE)).thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class, () -> service.takeMoney(null, ONE));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(operationService).createWithdrawal(null, ONE);
    }

    @Test
    void testTakeMoneyWhenUserDoesNotExist() {
        when(operationService.createWithdrawal(USER_ID, ONE)).thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class, () -> service.takeMoney(USER_ID, ONE));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(operationService).createWithdrawal(USER_ID, ONE);
    }

    @Test
    void testTakeMoneyWhenAmountIsNull() {
        when(operationService.createWithdrawal(USER_ID, null))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_NULL));
        Exception e = assertThrows(BadRequestException.class, () -> service.takeMoney(USER_ID, null));
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(operationService).createWithdrawal(USER_ID, null);
    }

    @Test
    void testTakeMoneyWhenAmountIsNegative() {
        when(operationService.createWithdrawal(USER_ID, MINUS_ONE))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_NEGATIVE));
        Exception e = assertThrows(BadRequestException.class, () -> service.takeMoney(USER_ID, MINUS_ONE));
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(operationService).createWithdrawal(USER_ID, MINUS_ONE);
    }

    @Test
    void testTakeMoneyWhenAmountIsZero() {
        when(operationService.createWithdrawal(USER_ID, ZERO))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_ZERO));
        Exception e = assertThrows(BadRequestException.class, () -> service.takeMoney(USER_ID, ZERO));
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(operationService).createWithdrawal(USER_ID, ZERO);
    }

    @Test
    void testTakeMoneyWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        when(operationService.createWithdrawal(USER_ID, ONE_THOUSANDTH))
                .thenThrow(new IllegalAmountException(WRONG_MINOR_UNITS));
        Exception e = assertThrows(BadRequestException.class, () -> service.takeMoney(USER_ID, ONE_THOUSANDTH));
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verify(operationService).createWithdrawal(USER_ID, ONE_THOUSANDTH);
    }

    @Test
    void testTakeMoneyWhenBalanceIsInsufficient() {
        when(operationService.createWithdrawal(USER_ID, ONE))
                .thenThrow(new IllegalAmountException(INSUFFICIENT_BALANCE));
        Exception e = assertThrows(BadRequestException.class, () -> service.takeMoney(USER_ID, ONE));
        assertEquals(INSUFFICIENT_BALANCE, e.getMessage());
        verify(operationService).createWithdrawal(USER_ID, ONE);
    }

    @Test
    void testTakeMoneyWhenBalanceIsSufficient() {
        Operation operation = mock(Operation.class);
        when(operationService.createWithdrawal(USER_ID, ONE)).thenReturn(operation);
        assertDoesNotThrow(() -> service.takeMoney(USER_ID, ONE));
        verify(operationService).createWithdrawal(USER_ID, ONE);
        verifyNoMoreInteractions(ignoreStubs(operation));
    }

    @Test
    void testTakeMoneyWhenScaleIsGreaterThanTwoButWithZeros() {
        Operation operation = mock(Operation.class);
        when(operationService.createWithdrawal(USER_ID, TEN_THOUSANDTHS)).thenReturn(operation);
        assertDoesNotThrow(() -> service.takeMoney(USER_ID, TEN_THOUSANDTHS));
        verify(operationService).createWithdrawal(USER_ID, TEN_THOUSANDTHS);
        verifyNoMoreInteractions(ignoreStubs(operation));
    }

    @Test
    void testGetOperationListWhenUserIdIsNull() {
        when(operationService.getUserOperations(null, dateFrom, dateTo))
                .thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class,
                () -> service.getOperationList(null, dateFrom, dateTo)
        );
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(operationService).getUserOperations(null, dateFrom, dateTo);
    }

    @Test
    void testGetOperationListWhenUserNotFound() {
        when(operationService.getUserOperations(USER_ID, dateFrom, dateTo))
                .thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class,
                () -> service.getOperationList(USER_ID, dateFrom, dateTo)
        );
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(operationService).getUserOperations(USER_ID, dateFrom, dateTo);
    }

    @Test
    void testGetOperationListWhenUserExists() {
        when(operationService.getUserOperations(USER_ID, dateFrom, dateTo)).thenReturn(operations);
        assertEquals(operations, service.getOperationList(USER_ID, dateFrom, dateTo));
        verify(operationService).getUserOperations(USER_ID, dateFrom, dateTo);
    }

    @Test
    void testTransferMoneyWhenUserIdIsNull() {
        when(operationService.createOutgoingTransfer(eq(null), eq(TEN), any(Date.class)))
                .thenThrow(new BadRequestException(USER_ID_IS_NULL));
        Exception e = assertThrows(BadRequestException.class,
                () -> service.transferMoney(null, RECEIVER_ID, TEN)
        );
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(operationService).createOutgoingTransfer(eq(null), eq(TEN), any(Date.class));
    }

    @Test
    void testTransferMoneyWhenReceiverIdIsNull() {
        Operation outgoing = mock(Operation.class);
        when(operationService.createOutgoingTransfer(eq(USER_ID), eq(TEN), any(Date.class))).thenAnswer(a -> {
            storeTransferDate(a.getArguments()[2]);
            return outgoing;
        });
        when(operationService.createIncomingTransfer(eq(null), eq(TEN), any(Date.class))).thenAnswer(a -> {
            assertEquals(transferDate, a.getArguments()[2]);
            throw new BadRequestException(USER_ID_IS_NULL);
        });
        Exception e = assertThrows(BadRequestException.class,
                () -> service.transferMoney(USER_ID, null, TEN)
        );
        assertEquals(RECEIVER_ID_IS_NULL, e.getMessage());
        verify(operationService).createOutgoingTransfer(eq(USER_ID), eq(TEN), any(Date.class));
        verify(operationService).createIncomingTransfer(eq(null), eq(TEN), any(Date.class));
        verifyNoMoreInteractions(ignoreStubs(outgoing));
    }

    @Test
    void testTransferMoneyWhenUserDoesNotExist() {
        when(operationService.createOutgoingTransfer(eq(USER_ID), eq(TEN), any(Date.class)))
                .thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class,
                () -> service.transferMoney(USER_ID, RECEIVER_ID, TEN)
        );
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(operationService).createOutgoingTransfer(eq(USER_ID), eq(TEN), any(Date.class));
    }

    @Test
    void testTransferMoneyWhenReceiverDoesNotExist() {
        Operation outgoing = mock(Operation.class);
        when(operationService.createOutgoingTransfer(eq(USER_ID), eq(TEN), any(Date.class))).thenAnswer(a -> {
            storeTransferDate(a.getArguments()[2]);
            return outgoing;
        });
        when(operationService.createIncomingTransfer(eq(RECEIVER_ID), eq(TEN), any(Date.class))).thenAnswer(a -> {
            assertEquals(transferDate, a.getArguments()[2]);
            throw new UserNotFoundException(USER_NOT_FOUND);
        });
        Exception e = assertThrows(UserNotFoundException.class,
                () -> service.transferMoney(USER_ID, RECEIVER_ID, TEN)
        );
        assertEquals(RECEIVER_NOT_FOUND, e.getMessage());
        verify(operationService).createOutgoingTransfer(eq(USER_ID), eq(TEN), any(Date.class));
        verify(operationService).createIncomingTransfer(eq(RECEIVER_ID), eq(TEN), any(Date.class));
        verifyNoMoreInteractions(ignoreStubs(outgoing));
    }

    @Test
    void testTransferMoneyWhenAmountIsNull() {
        when(operationService.createOutgoingTransfer(eq(USER_ID), eq(null), any(Date.class)))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_NULL));
        Exception e = assertThrows(BadRequestException.class,
                () -> service.transferMoney(USER_ID, RECEIVER_ID, null)
        );
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(operationService).createOutgoingTransfer(eq(USER_ID), eq(null), any(Date.class));
    }

    @Test
    void testTransferMoneyWhenAmountIsNegative() {
        BigDecimal amount = MINUS_TEN;
        when(operationService.createOutgoingTransfer(eq(USER_ID), eq(amount), any(Date.class)))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_NEGATIVE));
        Exception e = assertThrows(BadRequestException.class,
                    () -> service.transferMoney(USER_ID, RECEIVER_ID, amount)
        );
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(operationService).createOutgoingTransfer(eq(USER_ID), eq(amount), any(Date.class));
    }

    @Test
    void testTransferMoneyWhenAmountIsZero() {
        BigDecimal amount = ZERO;
        when(operationService.createOutgoingTransfer(eq(USER_ID), eq(amount), any(Date.class)))
                .thenThrow(new IllegalAmountException(AMOUNT_IS_ZERO));
        Exception e = assertThrows(BadRequestException.class,
                () -> service.transferMoney(USER_ID, RECEIVER_ID, amount)
        );
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(operationService).createOutgoingTransfer(eq(USER_ID), eq(amount), any(Date.class));
    }

    @Test
    void testTransferMoneyWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        BigDecimal amount = ONE_THOUSANDTH;
        when(operationService.createOutgoingTransfer(eq(USER_ID), eq(amount), any(Date.class)))
                .thenThrow(new IllegalAmountException(WRONG_MINOR_UNITS));
        Exception e = assertThrows(BadRequestException.class,
                () -> service.transferMoney(USER_ID, RECEIVER_ID, amount)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verify(operationService).createOutgoingTransfer(eq(USER_ID), eq(amount), any(Date.class));
    }

    @Test
    void testTransferMoneyWhenBalanceIsInsufficient() {
        BigDecimal amount = TEN;
        when(operationService.createOutgoingTransfer(eq(USER_ID), eq(amount), any(Date.class)))
                .thenThrow(new IllegalAmountException(INSUFFICIENT_BALANCE));
        Exception e = assertThrows(BadRequestException.class,
                () -> service.transferMoney(USER_ID, RECEIVER_ID, amount)
        );
        assertEquals(INSUFFICIENT_BALANCE, e.getMessage());
        verify(operationService).createOutgoingTransfer(eq(USER_ID), eq(amount), any(Date.class));
    }

    @Test
    void testTransferMoneyWhenBalanceIsSufficient() {
        BigDecimal amount = TEN;
        Operation outgoing = mock(Operation.class);
        Operation incoming = mock(Operation.class);
        Transfer transfer = mock(Transfer.class);
        when(operationService.createOutgoingTransfer(eq(USER_ID), eq(amount), any(Date.class))).thenAnswer(a -> {
            storeTransferDate(a.getArguments()[2]);
            return outgoing;
        });
        when(operationService.createIncomingTransfer(eq(RECEIVER_ID), eq(amount), any(Date.class))).thenAnswer(a -> {
            assertEquals(transferDate, a.getArguments()[2]);
            return incoming;

        });
        when(transferService.createTransfer(outgoing, incoming)).thenReturn(transfer);
        when(transferService.saveTransfer(transfer)).thenReturn(transfer);
        assertDoesNotThrow(() -> service.transferMoney(USER_ID, RECEIVER_ID, amount));
        verify(operationService).createOutgoingTransfer(eq(USER_ID), eq(amount), any(Date.class));
        verify(operationService).createIncomingTransfer(eq(RECEIVER_ID), eq(amount), any(Date.class));
        verify(transferService).createTransfer(outgoing, incoming);
        verify(transferService).saveTransfer(transfer);
        verifyNoMoreInteractions(ignoreStubs(outgoing));
        verifyNoMoreInteractions(ignoreStubs(incoming));
        verifyNoMoreInteractions(ignoreStubs(transfer));
    }

    @Test
    void testTransferMoneyWhenScaleIsGreaterThatTwoButWithZeros() {
        BigDecimal amount = TEN_THOUSANDTHS;
        Operation outgoing = mock(Operation.class);
        Operation incoming = mock(Operation.class);
        Transfer transfer = mock(Transfer.class);
        when(operationService.createOutgoingTransfer(eq(USER_ID), eq(amount), any(Date.class))).thenAnswer(a -> {
            storeTransferDate(a.getArguments()[2]);
            return outgoing;
        });
        when(operationService.createIncomingTransfer(eq(RECEIVER_ID), eq(amount), any(Date.class))).thenAnswer(a -> {
            assertEquals(transferDate, a.getArguments()[2]);
            return incoming;
        });
        when(transferService.createTransfer(outgoing, incoming)).thenReturn(transfer);
        when(transferService.saveTransfer(transfer)).thenReturn(transfer);
        assertDoesNotThrow(() -> service.transferMoney(USER_ID, RECEIVER_ID, amount));
        verify(operationService).createOutgoingTransfer(eq(USER_ID), eq(amount), any(Date.class));
        verify(operationService).createIncomingTransfer(eq(RECEIVER_ID), eq(amount), any(Date.class));
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