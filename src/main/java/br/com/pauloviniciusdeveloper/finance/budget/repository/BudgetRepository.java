package br.com.pauloviniciusdeveloper.finance.budget.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.pauloviniciusdeveloper.finance.budget.entity.Budget;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    List<Budget> findByUserIdAndMonthAndYear(UUID userId, int month, int year);

    List<Budget> findByUserId(UUID userId);

    Optional<Budget> findByIdAndUserId(UUID id, UUID userId);

    Optional<Budget> findByUserIdAndCategoryIdAndMonthAndYear(UUID userId, UUID categoryId, int month, int year);
}
