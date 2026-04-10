package br.com.pauloviniciusdeveloper.finance.budget.service;

import java.util.List;
import java.util.UUID;

import br.com.pauloviniciusdeveloper.finance.budget.dto.BudgetRequest;
import br.com.pauloviniciusdeveloper.finance.budget.dto.BudgetResponse;

public interface BudgetService {
    BudgetResponse create(BudgetRequest request, UUID userId);
    List<BudgetResponse> findByUserId(UUID userId, Integer month, Integer year);
    BudgetResponse findByIdAndUserId(UUID id, UUID userId);
    BudgetResponse update(UUID id, UUID userId, BudgetRequest request);
    void delete(UUID id, UUID userId);
}
