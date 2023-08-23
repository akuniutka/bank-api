package dev.akuniutka.bank.api.repository;

import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.Transfer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRepository extends CrudRepository<Transfer, Long> {
    List<Transfer> findByDebit(Operation debit);
    List<Transfer> findByCredit(Operation credit);
}
