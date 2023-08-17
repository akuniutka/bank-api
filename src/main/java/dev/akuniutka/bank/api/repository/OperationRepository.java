package dev.akuniutka.bank.api.repository;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.entity.Operation;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface OperationRepository extends CrudRepository<Operation, Long> {
    List<Operation> findByAccountAndDateBetween(Account account, Date start, Date finish);
}
