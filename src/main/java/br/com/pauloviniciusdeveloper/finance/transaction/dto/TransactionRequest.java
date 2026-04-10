package br.com.pauloviniciusdeveloper.finance.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import br.com.pauloviniciusdeveloper.finance.transaction.entity.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record TransactionRequest(
    
    @NotNull(message = "Category ID is required")
    UUID categoryId,
    
    @NotNull(message = "Account ID is required")
    UUID accountId,
    
    @NotNull(message = "Transaction type is required")
    TransactionType type,
    
    @NotNull(message = "Amount is required")
    BigDecimal amount,

    @NotNull(message = "Date is required")
    LocalDate date,
    
    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must be at most 255 characters")
    String description
) {}
