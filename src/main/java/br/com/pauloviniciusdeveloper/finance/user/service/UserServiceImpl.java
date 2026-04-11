package br.com.pauloviniciusdeveloper.finance.user.service;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.pauloviniciusdeveloper.finance.account.repository.AccountRepository;
import br.com.pauloviniciusdeveloper.finance.budget.repository.BudgetRepository;
import br.com.pauloviniciusdeveloper.finance.category.repository.CategoryRepository;
import br.com.pauloviniciusdeveloper.finance.common.exception.ResourceNotFoundException;
import br.com.pauloviniciusdeveloper.finance.recurring.repository.RecurringRepository;
import br.com.pauloviniciusdeveloper.finance.transaction.repository.TransactionRepository;
import br.com.pauloviniciusdeveloper.finance.user.dto.UserAdminUpdateRequest;
import br.com.pauloviniciusdeveloper.finance.user.dto.UserResponse;
import br.com.pauloviniciusdeveloper.finance.user.mapper.UserMapper;
import br.com.pauloviniciusdeveloper.finance.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final RecurringRepository recurringRepository;
    private final BudgetRepository budgetRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public UserResponse findByEmail(String email) {
        return userRepository.findByEmail(email)
            .map(UserMapper::toResponse)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    @Override
    public UserResponse findById(UUID id) {
        return userRepository.findById(id)
            .map(UserMapper::toResponse)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));
    }

    @Override
    public Page<UserResponse> findAll(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return userRepository.searchByNameOrEmail(search.trim(), pageable)
                .map(UserMapper::toResponse);
        }
        return userRepository.findAll(pageable).map(UserMapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse adminUpdate(UUID id, UserAdminUpdateRequest request) {
        var user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.setIsActive(request.isActive());
        user.setRoles(request.roles());

        return UserMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void selfDelete(UUID id) {
        deleteAllUserData(id);
    }

    @Override
    @Transactional
    public void adminDelete(UUID id) {
        deleteAllUserData(id);
    }

    private void deleteAllUserData(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        // Delete in dependency order to satisfy FK constraints:
        // 1. transactions (reference accounts and categories)
        transactionRepository.deleteByAccountUserId(userId);
        // 2. recurring_transactions (reference accounts, categories, and users)
        recurringRepository.deleteByUserId(userId);
        // 3. budgets (reference categories and users)
        budgetRepository.deleteByUserId(userId);
        // 4. accounts (reference users)
        accountRepository.deleteByUserId(userId);
        // 5. categories (reference users)
        categoryRepository.deleteByUserId(userId);
        // 6. user (user_roles collection table is handled by JPA cascade)
        userRepository.deleteById(userId);
    }
}
