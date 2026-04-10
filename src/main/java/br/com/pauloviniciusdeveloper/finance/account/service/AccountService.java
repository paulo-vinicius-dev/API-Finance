package br.com.pauloviniciusdeveloper.finance.account.service;

import java.util.List;
import java.util.UUID;

import br.com.pauloviniciusdeveloper.finance.account.dto.AccountRequest;
import br.com.pauloviniciusdeveloper.finance.account.dto.AccountResponse;

public interface AccountService {
    AccountResponse create(AccountRequest accountRequest, UUID userId);
    AccountResponse findByIdAndUserId(UUID id, UUID userId);
    List<AccountResponse> findByUserId(UUID userId);
    AccountResponse updateByIdAndUserId(UUID id, UUID userId, AccountRequest accountRequest);
    void deleteByIdAndUserId(UUID id, UUID userId);
}
