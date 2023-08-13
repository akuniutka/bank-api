package dev.akuniutka.bank.api.repository;

import dev.akuniutka.bank.api.entity.Operation;
import org.springframework.data.repository.CrudRepository;

public interface OperationRepository extends CrudRepository<Operation, Long> {
}
