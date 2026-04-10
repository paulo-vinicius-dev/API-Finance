package br.com.pauloviniciusdeveloper.finance.transaction.mapper;

import br.com.pauloviniciusdeveloper.finance.account.entity.Account;
import br.com.pauloviniciusdeveloper.finance.account.mapper.AccountMapper;
import br.com.pauloviniciusdeveloper.finance.category.entity.Category;
import br.com.pauloviniciusdeveloper.finance.category.mapper.CategoryMapper;
import br.com.pauloviniciusdeveloper.finance.transaction.dto.TransactionRequest;
import br.com.pauloviniciusdeveloper.finance.transaction.dto.TransactionResponse;
import br.com.pauloviniciusdeveloper.finance.transaction.entity.Transaction;

public class TransactionMapper {
    
    public static Transaction toEntity(TransactionRequest transactionRequest, Account account, Category category) {
        return Transaction.builder()
            .description(transactionRequest.description())
            .amount(transactionRequest.amount())
            .date(transactionRequest.date())
            .account(account)
            .category(category)
            .type(transactionRequest.type())
            .build();
    }

    public static TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
            .id(transaction.getId())
            .description(transaction.getDescription())
            .amount(transaction.getAmount())
            .date(transaction.getDate())
            .account(AccountMapper.toResponse(transaction.getAccount()))
            .category(CategoryMapper.toResponse(transaction.getCategory()))
            .type(transaction.getType())
            .build();
    }
}
