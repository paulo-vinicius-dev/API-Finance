package br.com.pauloviniciusdeveloper.finance.transaction.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.pauloviniciusdeveloper.finance.account.entity.Account;
import br.com.pauloviniciusdeveloper.finance.account.repository.AccountRepository;
import br.com.pauloviniciusdeveloper.finance.transaction.dto.TransactionRequest;
import br.com.pauloviniciusdeveloper.finance.transaction.dto.TransactionResponse;
import br.com.pauloviniciusdeveloper.finance.transaction.entity.Transaction;
import br.com.pauloviniciusdeveloper.finance.transaction.entity.TransactionType;
import br.com.pauloviniciusdeveloper.finance.transaction.mapper.TransactionMapper;
import br.com.pauloviniciusdeveloper.finance.transaction.repository.TransactionRepository;
import br.com.pauloviniciusdeveloper.finance.category.entity.Category;
import br.com.pauloviniciusdeveloper.finance.category.repository.CategoryRepository;
import br.com.pauloviniciusdeveloper.finance.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public TransactionResponse create(TransactionRequest transactionRequest, UUID userId) {
        Account account = accountRepository.findByIdAndUserId(transactionRequest.accountId(), userId)
            .orElseThrow(() -> new ResourceNotFoundException("Account", transactionRequest.accountId()));

        Category category = categoryRepository.findByIdAndUserIdOrDefault(transactionRequest.categoryId(), userId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", transactionRequest.categoryId()));

        return TransactionMapper.toResponse(
            transactionRepository.save(TransactionMapper.toEntity(transactionRequest, account, category)));
    }

    @Override
    public TransactionResponse findByIdAndUserId(UUID id, UUID userId) {
        Transaction transaction = transactionRepository.findByIdAndAccountUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));

        return TransactionMapper.toResponse(transaction);
    }

    @Override
    public List<TransactionResponse> findByUserId(UUID userId) {
        return transactionRepository.findByAccountUserId(userId).stream()
            .map(TransactionMapper::toResponse)
            .toList();
    }

    @Override
    public List<TransactionResponse> findByUserIdWithFilters(UUID userId, LocalDate startDate, LocalDate endDate, UUID categoryId, TransactionType type, UUID accountId) {
        return transactionRepository.findByUserIdWithFilters(userId, startDate, endDate, categoryId, type, accountId).stream()
            .map(TransactionMapper::toResponse)
            .toList();
    }

    @Override
    public TransactionResponse updateByIdAndUserId(UUID id, UUID userId, TransactionRequest transactionRequest) {
        Transaction transaction = transactionRepository.findByIdAndAccountUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));

        Account account = accountRepository.findByIdAndUserId(transactionRequest.accountId(), userId)
            .orElseThrow(() -> new ResourceNotFoundException("Account", transactionRequest.accountId()));

        Category category = categoryRepository.findByIdAndUserIdOrDefault(transactionRequest.categoryId(), userId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", transactionRequest.categoryId()));

        transaction.setDescription(transactionRequest.description());
        transaction.setAmount(transactionRequest.amount());
        transaction.setDate(transactionRequest.date());
        transaction.setType(transactionRequest.type());
        transaction.setAccount(account);
        transaction.setCategory(category);

        return TransactionMapper.toResponse(transactionRepository.save(transaction));
    }

    @Override
    public void deleteByIdAndUserId(UUID id, UUID userId) {
        Transaction transaction = transactionRepository.findByIdAndAccountUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));

        transactionRepository.delete(transaction);
    }
}
