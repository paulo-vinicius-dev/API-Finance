package br.com.pauloviniciusdeveloper.finance.account.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.pauloviniciusdeveloper.finance.account.dto.AccountRequest;
import br.com.pauloviniciusdeveloper.finance.account.dto.AccountResponse;
import br.com.pauloviniciusdeveloper.finance.account.entity.Account;
import br.com.pauloviniciusdeveloper.finance.account.mapper.AccountMapper;
import br.com.pauloviniciusdeveloper.finance.account.repository.AccountRepository;
import br.com.pauloviniciusdeveloper.finance.common.exception.ResourceNotFoundException;
import br.com.pauloviniciusdeveloper.finance.recurring.repository.RecurringRepository;
import br.com.pauloviniciusdeveloper.finance.transaction.repository.TransactionRepository;
import br.com.pauloviniciusdeveloper.finance.user.entity.User;
import br.com.pauloviniciusdeveloper.finance.user.mapper.UserMapper;
import br.com.pauloviniciusdeveloper.finance.user.service.UserService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImp implements AccountService {

    private final AccountRepository accountRepository;
    private final RecurringRepository recurringRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    @Override
    public AccountResponse create(AccountRequest accountRequest, UUID userId) {
        User user = UserMapper.toEntity(userService.findById(userId));

        Account account = AccountMapper.toEntity(accountRequest);
        account.setUser(user);

        return AccountMapper.toResponse(accountRepository.save(account));
    }

    @Override
    public AccountResponse findByIdAndUserId(UUID id, UUID userId) {
        Account account = accountRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Account", id));
        return AccountMapper.toResponse(account);
    }

    @Override
    public List<AccountResponse> findByUserId(UUID userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);
        return accounts.stream()
            .map(AccountMapper::toResponse)
            .toList();
    }

    @Override
    public AccountResponse updateByIdAndUserId(UUID id, UUID userId, AccountRequest accountRequest) {
        Account account = accountRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Account", id));

        account.setName(accountRequest.name());

        return AccountMapper.toResponse(accountRepository.save(account));
    }

    @Override
    @Transactional
    public void deleteByIdAndUserId(UUID id, UUID userId) {
        accountRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Account", id));

        transactionRepository.deleteByAccountId(id);
        recurringRepository.deleteByAccountId(id);
        accountRepository.deleteById(id);
    }
}
