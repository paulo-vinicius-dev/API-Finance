package br.com.pauloviniciusdeveloper.finance.report.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.pauloviniciusdeveloper.finance.report.dto.CategoryBreakdownResponse;
import br.com.pauloviniciusdeveloper.finance.report.dto.MonthlyEvolutionResponse;
import br.com.pauloviniciusdeveloper.finance.report.dto.SummaryResponse;
import br.com.pauloviniciusdeveloper.finance.transaction.entity.TransactionType;
import br.com.pauloviniciusdeveloper.finance.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final TransactionRepository transactionRepository;

    @Override
    public SummaryResponse getSummary(UUID userId, LocalDate startDate, LocalDate endDate) {
        BigDecimal totalIncome = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.INCOME, startDate, endDate);
        BigDecimal totalExpense = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.EXPENSE, startDate, endDate);
        BigDecimal balance = totalIncome.subtract(totalExpense);

        BigDecimal savingsRate = BigDecimal.ZERO;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = balance.divide(totalIncome, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        }

        return new SummaryResponse(totalIncome, totalExpense, balance, savingsRate);
    }

    @Override
    public List<CategoryBreakdownResponse> getExpensesByCategory(UUID userId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = transactionRepository.sumAmountByCategoryAndUserIdAndType(userId, TransactionType.EXPENSE, startDate, endDate);

        BigDecimal grandTotal = rows.stream()
            .map(r -> (BigDecimal) r[2])
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return rows.stream().map(r -> {
            UUID categoryId = (UUID) r[0];
            String categoryName = (String) r[1];
            BigDecimal total = (BigDecimal) r[2];
            BigDecimal percentage = grandTotal.compareTo(BigDecimal.ZERO) > 0
                ? total.divide(grandTotal, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            return new CategoryBreakdownResponse(categoryId, categoryName, total, percentage);
        }).toList();
    }

    @Override
    public List<MonthlyEvolutionResponse> getMonthlyEvolution(UUID userId, int year) {
        List<Object[]> incomeRows = transactionRepository.sumAmountByMonthAndUserIdAndType(userId, TransactionType.INCOME, year);
        List<Object[]> expenseRows = transactionRepository.sumAmountByMonthAndUserIdAndType(userId, TransactionType.EXPENSE, year);

        BigDecimal[] monthlyIncome = new BigDecimal[13];
        BigDecimal[] monthlyExpense = new BigDecimal[13];
        for (int i = 1; i <= 12; i++) {
            monthlyIncome[i] = BigDecimal.ZERO;
            monthlyExpense[i] = BigDecimal.ZERO;
        }

        for (Object[] row : incomeRows) {
            int month = ((Number) row[0]).intValue();
            monthlyIncome[month] = (BigDecimal) row[1];
        }
        for (Object[] row : expenseRows) {
            int month = ((Number) row[0]).intValue();
            monthlyExpense[month] = (BigDecimal) row[1];
        }

        List<MonthlyEvolutionResponse> result = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            BigDecimal income = monthlyIncome[m];
            BigDecimal expense = monthlyExpense[m];
            result.add(new MonthlyEvolutionResponse(
                m,
                Month.of(m).name(),
                income,
                expense,
                income.subtract(expense)
            ));
        }
        return result;
    }
}
