package br.com.pauloviniciusdeveloper.finance.budget.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.pauloviniciusdeveloper.finance.budget.dto.BudgetRequest;
import br.com.pauloviniciusdeveloper.finance.budget.dto.BudgetResponse;
import br.com.pauloviniciusdeveloper.finance.budget.entity.Budget;
import br.com.pauloviniciusdeveloper.finance.budget.entity.BudgetStatus;
import br.com.pauloviniciusdeveloper.finance.budget.repository.BudgetRepository;
import br.com.pauloviniciusdeveloper.finance.category.dto.CategoryResponse;
import br.com.pauloviniciusdeveloper.finance.category.entity.Category;
import br.com.pauloviniciusdeveloper.finance.category.repository.CategoryRepository;
import br.com.pauloviniciusdeveloper.finance.common.exception.ConflictException;
import br.com.pauloviniciusdeveloper.finance.common.exception.ResourceNotFoundException;
import br.com.pauloviniciusdeveloper.finance.transaction.entity.TransactionType;
import br.com.pauloviniciusdeveloper.finance.transaction.repository.TransactionRepository;
import br.com.pauloviniciusdeveloper.finance.user.entity.User;
import br.com.pauloviniciusdeveloper.finance.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public BudgetResponse create(BudgetRequest request, UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Category category = categoryRepository.findByIdAndUserIdOrDefault(request.categoryId(), userId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));

        budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(userId, request.categoryId(), request.month(), request.year())
            .ifPresent(b -> { throw new ConflictException("Já existe um orçamento para esta categoria neste período"); });

        Budget budget = Budget.builder()
            .user(user)
            .category(category)
            .limitAmount(request.limitAmount())
            .month(request.month())
            .year(request.year())
            .build();

        return toResponse(budgetRepository.save(budget), userId);
    }

    @Override
    public List<BudgetResponse> findByUserId(UUID userId, Integer month, Integer year) {
        List<Budget> budgets;
        if (month != null && year != null) {
            budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);
        } else {
            budgets = budgetRepository.findByUserId(userId);
        }
        return budgets.stream().map(b -> toResponse(b, userId)).toList();
    }

    @Override
    public BudgetResponse findByIdAndUserId(UUID id, UUID userId) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
        return toResponse(budget, userId);
    }

    @Override
    public BudgetResponse update(UUID id, UUID userId, BudgetRequest request) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Budget", id));

        Category category = categoryRepository.findByIdAndUserIdOrDefault(request.categoryId(), userId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));

        // Check for duplicate only if category/month/year changed
        if (!budget.getCategory().getId().equals(request.categoryId())
                || budget.getMonth() != request.month()
                || budget.getYear() != request.year()) {
            budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(userId, request.categoryId(), request.month(), request.year())
                .ifPresent(b -> { throw new ConflictException("Já existe um orçamento para esta categoria neste período"); });
        }

        budget.setCategory(category);
        budget.setLimitAmount(request.limitAmount());
        budget.setMonth(request.month());
        budget.setYear(request.year());

        return toResponse(budgetRepository.save(budget), userId);
    }

    @Override
    public void delete(UUID id, UUID userId) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
        budgetRepository.delete(budget);
    }

    private BudgetResponse toResponse(Budget budget, UUID userId) {
        BigDecimal spent = transactionRepository.sumAmountByUserIdAndTypeAndCategoryAndYearAndMonth(
            userId, TransactionType.EXPENSE, budget.getCategory().getId(), budget.getYear(), budget.getMonth());
        if (spent == null) spent = BigDecimal.ZERO;

        BigDecimal usagePercentage = budget.getLimitAmount().compareTo(BigDecimal.ZERO) > 0
            ? spent.divide(budget.getLimitAmount(), 4, RoundingMode.HALF_UP)
                   .multiply(BigDecimal.valueOf(100))
                   .setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        BudgetStatus status;
        if (usagePercentage.compareTo(BigDecimal.valueOf(100)) >= 0) {
            status = BudgetStatus.EXCEEDED;
        } else if (usagePercentage.compareTo(BigDecimal.valueOf(80)) >= 0) {
            status = BudgetStatus.WARNING;
        } else {
            status = BudgetStatus.ON_TRACK;
        }

        CategoryResponse categoryResponse = new CategoryResponse(
            budget.getCategory().getId(),
            budget.getCategory().getName(),
            budget.getCategory().isDefault()
        );

        return new BudgetResponse(
            budget.getId(),
            categoryResponse,
            budget.getLimitAmount(),
            spent,
            usagePercentage,
            status,
            budget.getMonth(),
            budget.getYear()
        );
    }
}
