package dev.akuniutka.bank.api.repository;

import dev.akuniutka.bank.api.entity.Account;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {
}
