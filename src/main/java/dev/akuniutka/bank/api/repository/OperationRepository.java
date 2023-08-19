package dev.akuniutka.bank.api.repository;

import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.entity.Operation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface OperationRepository extends CrudRepository<Operation, Long> {
    List<Operation> findByAccount(Account account);
    @Query("SELECT o FROM Operation o WHERE o.account = :account AND o.date < :finish")
    List<Operation> findByAccountAndDateBefore(@Param("account") Account account, @Param("finish") Date finish);
    @Query("SELECT o FROM Operation o WHERE o.account = :account AND o.date >= :start")
    List<Operation> findByAccountAndDateAfter(@Param("account") Account account, @Param("start") Date start);
    @Query("SELECT o FROM Operation o WHERE o.account = :account AND o.date >= :start AND o.date < :finish")
    List<Operation> findByAccountAndDateBetween(
            @Param("account") Account account, @Param("start") Date start, @Param("finish") Date finish
    );
}
