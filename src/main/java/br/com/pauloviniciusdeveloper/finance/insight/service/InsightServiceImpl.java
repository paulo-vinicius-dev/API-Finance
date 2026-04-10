package br.com.pauloviniciusdeveloper.finance.insight.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.pauloviniciusdeveloper.finance.budget.entity.BudgetStatus;
import br.com.pauloviniciusdeveloper.finance.budget.repository.BudgetRepository;
import br.com.pauloviniciusdeveloper.finance.insight.dto.InsightResponse;
import br.com.pauloviniciusdeveloper.finance.insight.dto.InsightResponse.InsightType;
import br.com.pauloviniciusdeveloper.finance.transaction.entity.TransactionType;
import br.com.pauloviniciusdeveloper.finance.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InsightServiceImpl implements InsightService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    @Override
    public List<InsightResponse> getInsights(UUID userId, int month, int year) {
        List<InsightResponse> insights = new ArrayList<>();

        // Compute previous month/year
        int prevMonth = month == 1 ? 12 : month - 1;
        int prevYear = month == 1 ? year - 1 : year;

        BigDecimal currentExpense = orZero(transactionRepository.sumAmountByUserIdAndTypeAndYearAndMonth(
            userId, TransactionType.EXPENSE, year, month));
        BigDecimal prevExpense = orZero(transactionRepository.sumAmountByUserIdAndTypeAndYearAndMonth(
            userId, TransactionType.EXPENSE, prevYear, prevMonth));
        BigDecimal currentIncome = orZero(transactionRepository.sumAmountByUserIdAndTypeAndYearAndMonth(
            userId, TransactionType.INCOME, year, month));

        // 1. Top expense category
        List<Object[]> categoryRows = transactionRepository.sumAmountByCategoryAndUserIdAndType(
            userId, TransactionType.EXPENSE,
            java.time.LocalDate.of(year, month, 1),
            java.time.LocalDate.of(year, month, java.time.LocalDate.of(year, month, 1).lengthOfMonth()));

        if (!categoryRows.isEmpty()) {
            Object[] top = categoryRows.get(0);
            String categoryName = (String) top[1];
            BigDecimal total = (BigDecimal) top[2];
            insights.add(new InsightResponse(
                InsightType.TOP_EXPENSE_CATEGORY,
                "Categoria com maior gasto: " + categoryName,
                "R$ " + total.setScale(2, RoundingMode.HALF_UP)
            ));
        }

        // 2. Month-over-month comparison
        if (prevExpense.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal change = currentExpense.subtract(prevExpense)
                .divide(prevExpense, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);

            String direction = change.compareTo(BigDecimal.ZERO) >= 0 ? "aumentaram" : "reduziram";
            String absChange = change.abs().toPlainString();
            insights.add(new InsightResponse(
                change.compareTo(BigDecimal.ZERO) >= 0 ? InsightType.EXPENSE_INCREASE : InsightType.MONTHLY_COMPARISON,
                "Despesas " + direction + " " + absChange + "% em relação ao mês anterior",
                change.toPlainString() + "%"
            ));
        }

        // 3. Savings rate
        if (currentIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal balance = currentIncome.subtract(currentExpense);
            BigDecimal savingsRate = balance.divide(currentIncome, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
            insights.add(new InsightResponse(
                InsightType.SAVINGS_RATE,
                "Taxa de economia do mês: " + savingsRate.toPlainString() + "%",
                savingsRate.toPlainString() + "%"
            ));
        }

        // 4. Budget alerts from existing budgets
        budgetRepository.findByUserIdAndMonthAndYear(userId, month, year).forEach(budget -> {
            BigDecimal spent = orZero(transactionRepository.sumAmountByUserIdAndTypeAndCategoryAndYearAndMonth(
                userId, TransactionType.EXPENSE, budget.getCategory().getId(), year, month));

            if (budget.getLimitAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal pct = spent.divide(budget.getLimitAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP);

                BudgetStatus status = computeStatus(pct);
                if (status == BudgetStatus.EXCEEDED) {
                    insights.add(new InsightResponse(
                        InsightType.BUDGET_EXCEEDED,
                        "Orçamento de " + budget.getCategory().getName() + " ultrapassado (" + pct + "% utilizado)",
                        pct.toPlainString() + "%"
                    ));
                } else if (status == BudgetStatus.WARNING) {
                    insights.add(new InsightResponse(
                        InsightType.BUDGET_WARNING,
                        "Orçamento de " + budget.getCategory().getName() + " próximo do limite (" + pct + "% utilizado)",
                        pct.toPlainString() + "%"
                    ));
                }
            }
        });

        return insights;
    }

    private BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BudgetStatus computeStatus(BigDecimal pct) {
        if (pct.compareTo(BigDecimal.valueOf(100)) >= 0) return BudgetStatus.EXCEEDED;
        if (pct.compareTo(BigDecimal.valueOf(80)) >= 0) return BudgetStatus.WARNING;
        return BudgetStatus.ON_TRACK;
    }
}
