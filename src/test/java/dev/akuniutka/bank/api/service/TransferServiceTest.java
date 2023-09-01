package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.Transfer;
import dev.akuniutka.bank.api.exception.WrongAmountException;
import dev.akuniutka.bank.api.exception.NullUserIdException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.repository.TransferRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

import static dev.akuniutka.bank.api.util.Amount.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static dev.akuniutka.bank.api.util.ErrorMessage.*;

class TransferServiceTest {
    private static final Long USER_ID = 1L;
    private static final Long RECEIVER_ID = 2L;
    private Operation outgoingTransfer;
    private Operation incomingTransfer;
    private Transfer transfer;
    private Transfer storedTransfer;
    private Date transferDate;
    private TransferRepository repository;
    private OperationService operationService;
    private TransferService service;

    @BeforeEach
    public void setUp() {
        outgoingTransfer = mock(Operation.class);
        incomingTransfer = mock(Operation.class);
        transfer = mock(Transfer.class);
        storedTransfer = null;
        transferDate = null;
        repository = mock(TransferRepository.class);
        operationService = mock(OperationService.class);
        service = new TransferService(repository, operationService);
    }

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(ignoreStubs(outgoingTransfer));
        verifyNoMoreInteractions(ignoreStubs(incomingTransfer));
        verifyNoMoreInteractions(ignoreStubs(transfer));
        verifyNoMoreInteractions(ignoreStubs(repository));
        verifyNoMoreInteractions(ignoreStubs(operationService));
    }

    @Test
    void testCreateTransferWhenUserIdIsNull() {
        when(operationService.createOutgoingTransfer(eq(null), eq(TEN), any(Date.class)))
                .thenThrow(new NullUserIdException(USER_ID_IS_NULL));
        Exception e = assertThrows(NullUserIdException.class,
                () -> service.createTransfer(null, RECEIVER_ID, TEN)
        );
        assertEquals(USER_ID_IS_NULL, e.getMessage());
        verify(operationService).createOutgoingTransfer(eq(null), eq(TEN), any(Date.class));
    }

    @Test
    void testCreateTransferWhenReceiverIdIsNull() {
        when(operationService.createOutgoingTransfer(eq(USER_ID), eq(TEN), any(Date.class))).thenAnswer(a -> {
            storeTransferDate(a.getArguments()[2]);
            return outgoingTransfer;
        });
        when(operationService.createIncomingTransfer(eq(null), eq(TEN), any(Date.class))).thenAnswer(a -> {
            assertEquals(transferDate, a.getArguments()[2]);
            throw new NullUserIdException(RECEIVER_ID_IS_NULL);
        });
        Exception e = assertThrows(NullUserIdException.class,
                () -> service.createTransfer(USER_ID, null, TEN)
        );
        assertEquals(RECEIVER_ID_IS_NULL, e.getMessage());
        verify(operationService).createOutgoingTransfer(eq(USER_ID), eq(TEN), any(Date.class));
        verify(operationService).createIncomingTransfer(eq(null), eq(TEN), any(Date.class));
    }

    @Test
    void testCreateTransferWhenUserDoesNotExist() {
        when(operationService.createOutgoingTransfer(eq(USER_ID), eq(TEN), any(Date.class)))
                .thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        Exception e = assertThrows(UserNotFoundException.class,
                () -> service.createTransfer(USER_ID, RECEIVER_ID, TEN)
        );
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(operationService).createOutgoingTransfer(eq(USER_ID), eq(TEN), any(Date.class));
    }

    @Test
    void testCreateTransferWhenReceiverDoesNotExist() {
        when(operationService.createOutgoingTransfer(eq(USER_ID), eq(TEN), any(Date.class))).thenAnswer(a -> {
            storeTransferDate(a.getArguments()[2]);
            return outgoingTransfer;
        });
        when(operationService.createIncomingTransfer(eq(RECEIVER_ID), eq(TEN), any(Date.class))).thenAnswer(a -> {
            assertEquals(transferDate, a.getArguments()[2]);
            throw new UserNotFoundException(RECEIVER_NOT_FOUND);
        });
        Exception e = assertThrows(UserNotFoundException.class,
                () -> service.createTransfer(USER_ID, RECEIVER_ID, TEN)
        );
        assertEquals(RECEIVER_NOT_FOUND, e.getMessage());
        verify(operationService).createOutgoingTransfer(eq(USER_ID), eq(TEN), any(Date.class));
        verify(operationService).createIncomingTransfer(eq(RECEIVER_ID), eq(TEN), any(Date.class));
    }

    @Test
    void testCreateTransferWhenAmountIsNull() {
        when(operationService.createOutgoingTransfer(eq(USER_ID), eq(null), any(Date.class)))
                .thenThrow(new WrongAmountException(AMOUNT_IS_NULL));
        Exception e = assertThrows(WrongAmountException.class,
                () -> service.createTransfer(USER_ID, RECEIVER_ID, null)
        );
        assertEquals(AMOUNT_IS_NULL, e.getMessage());
        verify(operationService).createOutgoingTransfer(eq(USER_ID), eq(null), any(Date.class));
    }

    @Test
    void testCreateTransferWhenAmountIsNegative() {
        BigDecimal amount = MINUS_TEN;
        when(operationService.createOutgoingTransfer(eq(USER_ID), eq(amount), any(Date.class)))
                .thenThrow(new WrongAmountException(AMOUNT_IS_NEGATIVE));
        Exception e = assertThrows(WrongAmountException.class,
                () -> service.createTransfer(USER_ID, RECEIVER_ID, amount)
        );
        assertEquals(AMOUNT_IS_NEGATIVE, e.getMessage());
        verify(operationService).createOutgoingTransfer(eq(USER_ID), eq(amount), any(Date.class));
    }

    @Test
    void testCreateTransferWhenAmountIsZero() {
        BigDecimal amount = ZERO;
        when(operationService.createOutgoingTransfer(eq(USER_ID), eq(amount), any(Date.class)))
                .thenThrow(new WrongAmountException(AMOUNT_IS_ZERO));
        Exception e = assertThrows(WrongAmountException.class,
                () -> service.createTransfer(USER_ID, RECEIVER_ID, amount)
        );
        assertEquals(AMOUNT_IS_ZERO, e.getMessage());
        verify(operationService).createOutgoingTransfer(eq(USER_ID), eq(amount), any(Date.class));
    }

    @Test
    void testCreateTransferWhenScaleIsGreaterThanTwoAndWithNonZeros() {
        BigDecimal amount = ONE_THOUSANDTH;
        when(operationService.createOutgoingTransfer(eq(USER_ID), eq(amount), any(Date.class)))
                .thenThrow(new WrongAmountException(WRONG_MINOR_UNITS));
        Exception e = assertThrows(WrongAmountException.class,
                () -> service.createTransfer(USER_ID, RECEIVER_ID, amount)
        );
        assertEquals(WRONG_MINOR_UNITS, e.getMessage());
        verify(operationService).createOutgoingTransfer(eq(USER_ID), eq(amount), any(Date.class));
    }

    @Test
    void testCreateTransferWhenBalanceIsInsufficient() {
        BigDecimal amount = TEN;
        when(operationService.createOutgoingTransfer(eq(USER_ID), eq(amount), any(Date.class)))
                .thenThrow(new WrongAmountException(INSUFFICIENT_BALANCE));
        Exception e = assertThrows(WrongAmountException.class,
                () -> service.createTransfer(USER_ID, RECEIVER_ID, amount)
        );
        assertEquals(INSUFFICIENT_BALANCE, e.getMessage());
        verify(operationService).createOutgoingTransfer(eq(USER_ID), eq(amount), any(Date.class));
    }

    @Test
    void testTransferMoneyWhenBalanceIsSufficient() {
        BigDecimal amount = TEN;
        when(operationService.createOutgoingTransfer(eq(USER_ID), eq(amount), any(Date.class))).thenAnswer(a -> {
            storeTransferDate(a.getArguments()[2]);
            return outgoingTransfer;
        });
        when(operationService.createIncomingTransfer(eq(RECEIVER_ID), eq(amount), any(Date.class))).thenAnswer(a -> {
            assertEquals(transferDate, a.getArguments()[2]);
            return incomingTransfer;
        });
        when(repository.save(any(Transfer.class))).thenAnswer(a -> storeTransfer(a.getArguments()[0]));
        Transfer actual = service.createTransfer(USER_ID, RECEIVER_ID, amount);
        assertEquals(transfer, actual);
        assertNotNull(storedTransfer);
        assertEquals(outgoingTransfer, storedTransfer.getOutgoingTransfer());
        assertEquals(incomingTransfer, storedTransfer.getIncomingTransfer());
        verify(operationService).createOutgoingTransfer(eq(USER_ID), eq(amount), any(Date.class));
        verify(operationService).createIncomingTransfer(eq(RECEIVER_ID), eq(amount), any(Date.class));
        verify(repository).save(any(Transfer.class));
    }

    @Test
    void testTransferMoneyWhenScaleIsGreaterThatTwoButWithZeros() {
        BigDecimal amount = TEN_THOUSANDTHS;
        when(operationService.createOutgoingTransfer(eq(USER_ID), eq(amount), any(Date.class))).thenAnswer(a -> {
            storeTransferDate(a.getArguments()[2]);
            return outgoingTransfer;
        });
        when(operationService.createIncomingTransfer(eq(RECEIVER_ID), eq(amount), any(Date.class))).thenAnswer(a -> {
            assertEquals(transferDate, a.getArguments()[2]);
            return incomingTransfer;
        });
        when(repository.save(any(Transfer.class))).thenAnswer(a -> storeTransfer(a.getArguments()[0]));
        Transfer actual = service.createTransfer(USER_ID, RECEIVER_ID, amount);
        assertEquals(transfer, actual);
        assertNotNull(storedTransfer);
        assertEquals(outgoingTransfer, storedTransfer.getOutgoingTransfer());
        assertEquals(incomingTransfer, storedTransfer.getIncomingTransfer());
        verify(operationService).createOutgoingTransfer(eq(USER_ID), eq(amount), any(Date.class));
        verify(operationService).createIncomingTransfer(eq(RECEIVER_ID), eq(amount), any(Date.class));
        verify(repository).save(any(Transfer.class));
    }

    private void storeTransferDate(Object o) {
        if (o instanceof Date) {
            if (transferDate == null) {
                transferDate = (Date) o;
            } else {
                throw  new RuntimeException("transfer date already stored");
            }
        } else {
            throw new IllegalArgumentException("argument is not of Date type");
        }
    }

    private Transfer storeTransfer(Object obj) {
        if (obj instanceof Transfer) {
            if (storedTransfer == null) {
                storedTransfer = (Transfer) obj;
                return transfer;
            } else {
                throw new RuntimeException("transfer already stored");
            }
        } else {
            throw new IllegalArgumentException("argument is not of Transfer type");
        }
    }
}