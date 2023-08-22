package dev.akuniutka.bank.api.repository;

import dev.akuniutka.bank.api.entity.Account;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static dev.akuniutka.bank.api.util.ErrorMessage.*;
import static dev.akuniutka.bank.api.Amount.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountRepositoryIT {
    @Autowired
    private AccountRepository repository;

    @Test
    void testFindById() {
        Long id = 1068L;
        Account account = repository.findById(id).orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        assertNotNull(account);
        assertEquals(id, account.getId());
        assertEquals(FORMATTED_TEN, account.getBalance());
    }

    @Test
    void testSave() {
        Account account = new Account();
        account.increaseBalance(TEN);
        account = repository.save(account);
        Long id = account.getId();
        assertNotNull(id);
        Account actual = repository.findById(id).orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        assertNotNull(actual);
        assertEquals(id, actual.getId());
        assertEquals(FORMATTED_TEN, actual.getBalance());
    }
}