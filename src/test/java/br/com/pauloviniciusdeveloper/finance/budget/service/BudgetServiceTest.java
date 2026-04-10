package br.com.pauloviniciusdeveloper.finance.budget.service;

import br.com.pauloviniciusdeveloper.finance.budget.dto.BudgetRequest;
import br.com.pauloviniciusdeveloper.finance.budget.entity.Budget;
import br.com.pauloviniciusdeveloper.finance.budget.entity.BudgetStatus;
import br.com.pauloviniciusdeveloper.finance.budget.repository.BudgetRepository;
import br.com.pauloviniciusdeveloper.finance.category.entity.Category;
import br.com.pauloviniciusdeveloper.finance.category.repository.CategoryRepository;
import br.com.pauloviniciusdeveloper.finance.common.exception.ConflictException;
import br.com.pauloviniciusdeveloper.finance.common.exception.ResourceNotFoundException;
import br.com.pauloviniciusdeveloper.finance.transaction.entity.TransactionType;
import br.com.pauloviniciusdeveloper.finance.transaction.repository.TransactionRepository;
import br.com.pauloviniciusdeveloper.finance.user.entity.User;
import br.com.pauloviniciusdeveloper.finance.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private User mockUser(UUID userId) {
        return User.builder().id(userId).fullName("Test").email("t@t.com")
                .hashedPassword("hash").isActive(true).build();
    }

    private Category mockCategory(UUID id) {
        return Category.builder().id(id).name("Alimentação").isDefault(false).build();
    }

    private Budget mockBudget(UUID id, UUID userId, Category category, BigDecimal limit, int month, int year) {
        User user = mockUser(userId);
        return Budget.builder()
                .id(id)
                .user(user)
                .category(category)
                .limitAmount(limit)
                .month(month)
                .year(year)
                .build();
    }

    private BudgetRequest mockRequest(UUID categoryId, BigDecimal limit, int month, int year) {
        return new BudgetRequest(categoryId, limit, month, year);
    }

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("deve criar orçamento com sucesso")
        void shouldCreate() {
            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            User user = mockUser(userId);
            Category category = mockCategory(categoryId);
            BudgetRequest request = mockRequest(categoryId, BigDecimal.valueOf(500), 4, 2025);

            Budget savedBudget = mockBudget(UUID.randomUUID(), userId, category, BigDecimal.valueOf(500), 4, 2025);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(categoryRepository.findByIdAndUserIdOrDefault(categoryId, userId)).willReturn(Optional.of(category));
            given(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(userId, categoryId, 4, 2025))
                    .willReturn(Optional.empty());
            given(budgetRepository.save(any(Budget.class))).willReturn(savedBudget);
            given(transactionRepository.sumAmountByUserIdAndTypeAndCategoryAndYearAndMonth(
                    userId, TransactionType.EXPENSE, categoryId, 2025, 4))
                    .willReturn(BigDecimal.ZERO);

            var result = budgetService.create(request, userId);

            assertThat(result).isNotNull();
            assertThat(result.limitAmount()).isEqualByComparingTo(BigDecimal.valueOf(500));
            verify(budgetRepository).save(any(Budget.class));
        }

        @Test
        @DisplayName("deve lançar ConflictException quando já existe orçamento no período")
        void shouldThrowConflict_whenDuplicate() {
            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            User user = mockUser(userId);
            Category category = mockCategory(categoryId);
            Budget existing = mockBudget(UUID.randomUUID(), userId, category, BigDecimal.valueOf(500), 4, 2025);
            BudgetRequest request = mockRequest(categoryId, BigDecimal.valueOf(600), 4, 2025);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(categoryRepository.findByIdAndUserIdOrDefault(categoryId, userId)).willReturn(Optional.of(category));
            given(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(userId, categoryId, 4, 2025))
                    .willReturn(Optional.of(existing));

            assertThatThrownBy(() -> budgetService.create(request, userId))
                    .isInstanceOf(ConflictException.class);

            verify(budgetRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findByIdAndUserId — status calculation")
    class StatusTests {

        @Test
        @DisplayName("deve retornar status ON_TRACK quando gasto é menor que 80%")
        void shouldReturnOnTrack() {
            UUID budgetId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            Category category = mockCategory(categoryId);
            Budget budget = mockBudget(budgetId, userId, category, BigDecimal.valueOf(1000), 4, 2025);

            given(budgetRepository.findByIdAndUserId(budgetId, userId)).willReturn(Optional.of(budget));
            given(transactionRepository.sumAmountByUserIdAndTypeAndCategoryAndYearAndMonth(
                    userId, TransactionType.EXPENSE, categoryId, 2025, 4))
                    .willReturn(BigDecimal.valueOf(700)); // 70%

            var result = budgetService.findByIdAndUserId(budgetId, userId);

            assertThat(result.status()).isEqualTo(BudgetStatus.ON_TRACK);
            assertThat(result.usagePercentage()).isEqualByComparingTo(BigDecimal.valueOf(70.00));
        }

        @Test
        @DisplayName("deve retornar status WARNING quando gasto está entre 80% e 99%")
        void shouldReturnWarning() {
            UUID budgetId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            Category category = mockCategory(categoryId);
            Budget budget = mockBudget(budgetId, userId, category, BigDecimal.valueOf(1000), 4, 2025);

            given(budgetRepository.findByIdAndUserId(budgetId, userId)).willReturn(Optional.of(budget));
            given(transactionRepository.sumAmountByUserIdAndTypeAndCategoryAndYearAndMonth(
                    userId, TransactionType.EXPENSE, categoryId, 2025, 4))
                    .willReturn(BigDecimal.valueOf(850)); // 85%

            var result = budgetService.findByIdAndUserId(budgetId, userId);

            assertThat(result.status()).isEqualTo(BudgetStatus.WARNING);
        }

        @Test
        @DisplayName("deve retornar status EXCEEDED quando gasto atinge 100%")
        void shouldReturnExceeded() {
            UUID budgetId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            Category category = mockCategory(categoryId);
            Budget budget = mockBudget(budgetId, userId, category, BigDecimal.valueOf(1000), 4, 2025);

            given(budgetRepository.findByIdAndUserId(budgetId, userId)).willReturn(Optional.of(budget));
            given(transactionRepository.sumAmountByUserIdAndTypeAndCategoryAndYearAndMonth(
                    userId, TransactionType.EXPENSE, categoryId, 2025, 4))
                    .willReturn(BigDecimal.valueOf(1100)); // 110%

            var result = budgetService.findByIdAndUserId(budgetId, userId);

            assertThat(result.status()).isEqualTo(BudgetStatus.EXCEEDED);
        }

        @Test
        @DisplayName("deve retornar status ON_TRACK quando gasto é zero")
        void shouldReturnOnTrack_whenNoSpending() {
            UUID budgetId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            Category category = mockCategory(categoryId);
            Budget budget = mockBudget(budgetId, userId, category, BigDecimal.valueOf(1000), 4, 2025);

            given(budgetRepository.findByIdAndUserId(budgetId, userId)).willReturn(Optional.of(budget));
            given(transactionRepository.sumAmountByUserIdAndTypeAndCategoryAndYearAndMonth(
                    userId, TransactionType.EXPENSE, categoryId, 2025, 4))
                    .willReturn(null); // repository returns null when no transactions

            var result = budgetService.findByIdAndUserId(budgetId, userId);

            assertThat(result.status()).isEqualTo(BudgetStatus.ON_TRACK);
            assertThat(result.usagePercentage()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando orçamento não existe")
        void shouldThrowNotFound() {
            UUID budgetId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            given(budgetRepository.findByIdAndUserId(budgetId, userId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> budgetService.findByIdAndUserId(budgetId, userId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("update")
    class UpdateTests {

        @Test
        @DisplayName("deve lançar ConflictException ao mudar categoria para um período com orçamento existente")
        void shouldThrowConflict_whenChangingToDuplicatePeriod() {
            UUID budgetId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID originalCategoryId = UUID.randomUUID();
            UUID newCategoryId = UUID.randomUUID();
            Category originalCategory = mockCategory(originalCategoryId);
            Category newCategory = mockCategory(newCategoryId);
            Budget existing = mockBudget(budgetId, userId, originalCategory, BigDecimal.valueOf(500), 4, 2025);
            BudgetRequest request = mockRequest(newCategoryId, BigDecimal.valueOf(600), 4, 2025);

            Budget conflicting = mockBudget(UUID.randomUUID(), userId, newCategory, BigDecimal.valueOf(400), 4, 2025);

            given(budgetRepository.findByIdAndUserId(budgetId, userId)).willReturn(Optional.of(existing));
            given(categoryRepository.findByIdAndUserIdOrDefault(newCategoryId, userId)).willReturn(Optional.of(newCategory));
            given(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(userId, newCategoryId, 4, 2025))
                    .willReturn(Optional.of(conflicting));

            assertThatThrownBy(() -> budgetService.update(budgetId, userId, request))
                    .isInstanceOf(ConflictException.class);
        }
    }

    @Nested
    @DisplayName("findByUserId")
    class FindByUserIdTests {

        @Test
        @DisplayName("deve retornar lista de orçamentos do usuário")
        void shouldReturnList() {
            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            Category category = mockCategory(categoryId);
            Budget budget = mockBudget(UUID.randomUUID(), userId, category, BigDecimal.valueOf(500), 4, 2025);

            given(budgetRepository.findByUserId(userId)).willReturn(List.of(budget));
            given(transactionRepository.sumAmountByUserIdAndTypeAndCategoryAndYearAndMonth(
                    any(), any(), any(), anyInt(), anyInt()))
                    .willReturn(BigDecimal.ZERO);

            var result = budgetService.findByUserId(userId, null, null);

            assertThat(result).hasSize(1);
        }
    }
}
