package br.com.pauloviniciusdeveloper.finance.recurring.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.pauloviniciusdeveloper.finance.recurring.entity.RecurringTransaction;

@Repository
public interface RecurringRepository extends JpaRepository<RecurringTransaction, UUID> {

    List<RecurringTransaction> findByUserId(UUID userId);

    Optional<RecurringTransaction> findByIdAndUserId(UUID id, UUID userId);

    List<RecurringTransaction> findByIsActiveTrueAndNextDueDateLessThanEqual(LocalDate date);

    boolean existsByAccountId(UUID accountId);
}
