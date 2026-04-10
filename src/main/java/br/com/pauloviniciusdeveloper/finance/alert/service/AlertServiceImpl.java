package br.com.pauloviniciusdeveloper.finance.alert.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.pauloviniciusdeveloper.finance.alert.dto.AlertResponse;
import br.com.pauloviniciusdeveloper.finance.alert.dto.AlertResponse.AlertSeverity;
import br.com.pauloviniciusdeveloper.finance.alert.dto.AlertResponse.AlertType;
import br.com.pauloviniciusdeveloper.finance.budget.entity.Budget;
import br.com.pauloviniciusdeveloper.finance.budget.repository.BudgetRepository;
import br.com.pauloviniciusdeveloper.finance.transaction.entity.TransactionType;
import br.com.pauloviniciusdeveloper.finance.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public List<AlertResponse> getAlerts(UUID userId, int month, int year) {
        List<AlertResponse> alerts = new ArrayList<>();

        int prevMonth = month == 1 ? 12 : month - 1;
        int prevYear = month == 1 ? year - 1 : year;

        // 1. Budget-based alerts
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);
        for (Budget budget : budgets) {
            BigDecimal spent = orZero(transactionRepository.sumAmountByUserIdAndTypeAndCategoryAndYearAndMonth(
                userId, TransactionType.EXPENSE, budget.getCategory().getId(), year, month));

            if (budget.getLimitAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal pct = spent.divide(budget.getLimitAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP);

                if (pct.compareTo(BigDecimal.valueOf(100)) >= 0) {
                    alerts.add(new AlertResponse(
                        AlertType.BUDGET_EXCEEDED,
                        AlertSeverity.DANGER,
                        "Orçamento de " + budget.getCategory().getName() + " ultrapassado. Gasto: R$ " + spent.setScale(2, RoundingMode.HALF_UP) + " / Limite: R$ " + budget.getLimitAmount().setScale(2, RoundingMode.HALF_UP),
                        budget.getCategory().getId(),
                        budget.getCategory().getName()
                    ));
                } else if (pct.compareTo(BigDecimal.valueOf(80)) >= 0) {
                    alerts.add(new AlertResponse(
                        AlertType.BUDGET_WARNING,
                        AlertSeverity.WARNING,
                        "Orçamento de " + budget.getCategory().getName() + " próximo do limite (" + pct + "% utilizado)",
                        budget.getCategory().getId(),
                        budget.getCategory().getName()
                    ));
                }
            }
        }

        // 2. Category spending > 30% increase vs last month
        List<Object[]> currentRows = transactionRepository.sumAmountByCategoryAndUserIdAndType(
            userId, TransactionType.EXPENSE,
            java.time.LocalDate.of(year, month, 1),
            java.time.LocalDate.of(year, month, java.time.LocalDate.of(year, month, 1).lengthOfMonth()));

        for (Object[] row : currentRows) {
            UUID categoryId = (UUID) row[0];
            String categoryName = (String) row[1];
            BigDecimal currentSpent = (BigDecimal) row[2];

            BigDecimal prevSpent = orZero(transactionRepository.sumAmountByUserIdAndTypeAndCategoryAndYearAndMonth(
                userId, TransactionType.EXPENSE, categoryId, prevYear, prevMonth));

            if (prevSpent.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal increase = currentSpent.subtract(prevSpent)
                    .divide(prevSpent, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP);

                if (increase.compareTo(BigDecimal.valueOf(30)) > 0) {
                    alerts.add(new AlertResponse(
                        AlertType.SIGNIFICANT_EXPENSE_INCREASE,
                        AlertSeverity.WARNING,
                        "Gastos com " + categoryName + " aumentaram " + increase + "% em relação ao mês anterior",
                        categoryId,
                        categoryName
                    ));
                }
            }
        }

        return alerts;
    }

    private BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
