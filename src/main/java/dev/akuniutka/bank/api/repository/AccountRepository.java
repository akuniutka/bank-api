package dev.akuniutka.bank.api.repository;

import dev.akuniutka.bank.api.entity.Account;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {
    @NonNull
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findById(@NonNull Long userId);
}
