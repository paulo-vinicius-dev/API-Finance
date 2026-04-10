package br.com.pauloviniciusdeveloper.finance.transaction.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.com.pauloviniciusdeveloper.finance.transaction.dto.TransactionRequest;
import br.com.pauloviniciusdeveloper.finance.transaction.dto.TransactionResponse;
import br.com.pauloviniciusdeveloper.finance.transaction.entity.TransactionType;

public interface TransactionService {
    TransactionResponse create(TransactionRequest transactionRequest, UUID userId);
    TransactionResponse findByIdAndUserId(UUID id, UUID userId);
    List<TransactionResponse> findByUserId(UUID userId);
    List<TransactionResponse> findByUserIdWithFilters(UUID userId, LocalDate startDate, LocalDate endDate, UUID categoryId, TransactionType type, UUID accountId);
    TransactionResponse updateByIdAndUserId(UUID id, UUID userId, TransactionRequest transactionRequest);
    void deleteByIdAndUserId(UUID id, UUID userId);
}
