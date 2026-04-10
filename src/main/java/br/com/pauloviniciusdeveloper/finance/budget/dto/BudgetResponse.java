package br.com.pauloviniciusdeveloper.finance.budget.dto;

import java.math.BigDecimal;
import java.util.UUID;

import br.com.pauloviniciusdeveloper.finance.budget.entity.BudgetStatus;
import br.com.pauloviniciusdeveloper.finance.category.dto.CategoryResponse;

public record BudgetResponse(
    UUID id,
    CategoryResponse category,
    BigDecimal limitAmount,
    BigDecimal spentAmount,
    BigDecimal usagePercentage,
    BudgetStatus status,
    int month,
    int year
) {}
