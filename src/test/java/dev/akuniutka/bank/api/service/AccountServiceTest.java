package dev.akuniutka.bank.api.service;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.repository.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static dev.akuniutka.bank.api.entity.ErrorMessage.*;

class AccountServiceTest {
    private static final int MAX_MOCK_CALLS = 1;
    private static final Long USER_ID = 1L;
    private AccountRepository repository;
    private AccountService service;

    @BeforeEach
    public void setUp() {
        repository = mock(AccountRepository.class);
        service = new AccountService(repository);
    }

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(ignoreStubs(repository));
    }

    @Test
    void testGetWhenUserIdIsNull() {
        Exception e = assertThrows(BadRequestException.class, () -> service.get(null));
        assertEquals(USER_ID_IS_NULL, e.getMessage());
    }

    @Test
    void testGetWhenUserDoesNotExist() {
        when(repository.findById(USER_ID)).thenReturn(Optional.empty());
        Exception e = assertThrows(UserNotFoundException.class, () -> service.get(USER_ID));
        assertEquals(USER_NOT_FOUND, e.getMessage());
        verify(repository, times(MAX_MOCK_CALLS)).findById(USER_ID);
    }

    @Test
    void testGetWhenUserExists() {
        Account account = new Account();
        when(repository.findById(USER_ID)).thenReturn(Optional.of(account));
        assertEquals(account, service.get(USER_ID));
        verify(repository, times(MAX_MOCK_CALLS)).findById(USER_ID);
    }

    @Test
    void testSave() {
        Account account = new Account();
        when(repository.save(account)).thenReturn(account);
        assertEquals(account, service.save(account));
        verify(repository, times(MAX_MOCK_CALLS)).save(account);
    }
}