package br.com.pauloviniciusdeveloper.finance.transaction.dto;

import java.math.BigDecimal;
import java.util.UUID;
import java.time.LocalDate;

import br.com.pauloviniciusdeveloper.finance.account.dto.AccountResponse;
import br.com.pauloviniciusdeveloper.finance.category.dto.CategoryResponse;
import br.com.pauloviniciusdeveloper.finance.transaction.entity.TransactionType;
import lombok.Builder;

@Builder
public record TransactionResponse(
    UUID id, 
    CategoryResponse category,
    AccountResponse account,
    TransactionType type,
    BigDecimal amount,
    LocalDate date,
    String description
) {}
