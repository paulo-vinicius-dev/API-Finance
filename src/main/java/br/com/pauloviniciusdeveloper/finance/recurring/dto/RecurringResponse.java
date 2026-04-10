package br.com.pauloviniciusdeveloper.finance.recurring.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import br.com.pauloviniciusdeveloper.finance.account.dto.AccountResponse;
import br.com.pauloviniciusdeveloper.finance.category.dto.CategoryResponse;
import br.com.pauloviniciusdeveloper.finance.recurring.entity.Frequency;
import br.com.pauloviniciusdeveloper.finance.transaction.entity.TransactionType;

public record RecurringResponse(
    UUID id,
    CategoryResponse category,
    AccountResponse account,
    TransactionType type,
    BigDecimal amount,
    String description,
    Frequency frequency,
    LocalDate startDate,
    LocalDate nextDueDate,
    boolean isActive
) {}
