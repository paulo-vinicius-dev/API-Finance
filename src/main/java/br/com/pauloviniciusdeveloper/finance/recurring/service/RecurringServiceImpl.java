package br.com.pauloviniciusdeveloper.finance.recurring.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.pauloviniciusdeveloper.finance.account.dto.AccountResponse;
import br.com.pauloviniciusdeveloper.finance.account.entity.Account;
import br.com.pauloviniciusdeveloper.finance.account.repository.AccountRepository;
import br.com.pauloviniciusdeveloper.finance.category.dto.CategoryResponse;
import br.com.pauloviniciusdeveloper.finance.category.entity.Category;
import br.com.pauloviniciusdeveloper.finance.category.repository.CategoryRepository;
import br.com.pauloviniciusdeveloper.finance.common.exception.ResourceNotFoundException;
import br.com.pauloviniciusdeveloper.finance.recurring.dto.RecurringRequest;
import br.com.pauloviniciusdeveloper.finance.recurring.dto.RecurringResponse;
import br.com.pauloviniciusdeveloper.finance.recurring.entity.Frequency;
import br.com.pauloviniciusdeveloper.finance.recurring.entity.RecurringTransaction;
import br.com.pauloviniciusdeveloper.finance.recurring.repository.RecurringRepository;
import br.com.pauloviniciusdeveloper.finance.user.entity.User;
import br.com.pauloviniciusdeveloper.finance.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecurringServiceImpl implements RecurringService {

    private final RecurringRepository recurringRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    public RecurringResponse create(RecurringRequest request, UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Account account = accountRepository.findByIdAndUserId(request.accountId(), userId)
            .orElseThrow(() -> new ResourceNotFoundException("Account", request.accountId()));
        Category category = categoryRepository.findByIdAndUserIdOrDefault(request.categoryId(), userId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));

        RecurringTransaction recurring = RecurringTransaction.builder()
            .user(user)
            .account(account)
            .category(category)
            .type(request.type())
            .amount(request.amount())
            .description(request.description())
            .frequency(request.frequency())
            .startDate(request.startDate())
            .nextDueDate(request.startDate())
            .isActive(true)
            .build();

        return toResponse(recurringRepository.save(recurring));
    }

    @Override
    public List<RecurringResponse> findByUserId(UUID userId) {
        return recurringRepository.findByUserId(userId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    public RecurringResponse findByIdAndUserId(UUID id, UUID userId) {
        RecurringTransaction recurring = recurringRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("RecurringTransaction", id));
        return toResponse(recurring);
    }

    @Override
    public RecurringResponse update(UUID id, UUID userId, RecurringRequest request) {
        RecurringTransaction recurring = recurringRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("RecurringTransaction", id));
        Account account = accountRepository.findByIdAndUserId(request.accountId(), userId)
            .orElseThrow(() -> new ResourceNotFoundException("Account", request.accountId()));
        Category category = categoryRepository.findByIdAndUserIdOrDefault(request.categoryId(), userId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));

        recurring.setAccount(account);
        recurring.setCategory(category);
        recurring.setType(request.type());
        recurring.setAmount(request.amount());
        recurring.setDescription(request.description());
        recurring.setFrequency(request.frequency());
        recurring.setStartDate(request.startDate());
        recurring.setNextDueDate(request.startDate());

        return toResponse(recurringRepository.save(recurring));
    }

    @Override
    public void delete(UUID id, UUID userId) {
        RecurringTransaction recurring = recurringRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("RecurringTransaction", id));
        recurringRepository.delete(recurring);
    }

    public static LocalDate computeNextDueDate(LocalDate current, Frequency frequency) {
        return switch (frequency) {
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
            case MONTHLY -> current.plusMonths(1);
            case YEARLY -> current.plusYears(1);
        };
    }

    private RecurringResponse toResponse(RecurringTransaction r) {
        return new RecurringResponse(
            r.getId(),
            new CategoryResponse(r.getCategory().getId(), r.getCategory().getName(), r.getCategory().isDefault()),
            new AccountResponse(r.getAccount().getId(), r.getAccount().getName()),
            r.getType(),
            r.getAmount(),
            r.getDescription(),
            r.getFrequency(),
            r.getStartDate(),
            r.getNextDueDate(),
            r.isActive()
        );
    }
}
