package br.com.pauloviniciusdeveloper.finance.recurring.service;

import br.com.pauloviniciusdeveloper.finance.account.entity.Account;
import br.com.pauloviniciusdeveloper.finance.account.repository.AccountRepository;
import br.com.pauloviniciusdeveloper.finance.category.entity.Category;
import br.com.pauloviniciusdeveloper.finance.category.repository.CategoryRepository;
import br.com.pauloviniciusdeveloper.finance.common.exception.ResourceNotFoundException;
import br.com.pauloviniciusdeveloper.finance.recurring.dto.RecurringRequest;
import br.com.pauloviniciusdeveloper.finance.recurring.entity.Frequency;
import br.com.pauloviniciusdeveloper.finance.recurring.entity.RecurringTransaction;
import br.com.pauloviniciusdeveloper.finance.recurring.repository.RecurringRepository;
import br.com.pauloviniciusdeveloper.finance.transaction.entity.TransactionType;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringServiceTest {

    @Mock
    private RecurringRepository recurringRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RecurringServiceImpl recurringService;

    private User mockUser(UUID userId) {
        return User.builder().id(userId).fullName("Test").email("t@t.com")
                .hashedPassword("hash").isActive(true).build();
    }

    private Account mockAccount(UUID accountId, UUID userId) {
        User user = mockUser(userId);
        return Account.builder().id(accountId).name("Conta").build();
    }

    private Category mockCategory(UUID categoryId) {
        return Category.builder().id(categoryId).name("Alimentação").isDefault(false).build();
    }

    private RecurringTransaction mockRecurring(UUID id, UUID userId, Account account, Category category) {
        User user = mockUser(userId);
        return RecurringTransaction.builder()
                .id(id)
                .user(user)
                .account(account)
                .category(category)
                .type(TransactionType.EXPENSE)
                .amount(BigDecimal.valueOf(100))
                .frequency(Frequency.MONTHLY)
                .startDate(LocalDate.of(2025, 1, 1))
                .nextDueDate(LocalDate.of(2025, 2, 1))
                .isActive(true)
                .build();
    }

    private RecurringRequest mockRequest(UUID accountId, UUID categoryId) {
        return new RecurringRequest(
                categoryId, accountId, TransactionType.EXPENSE,
                BigDecimal.valueOf(200), "Mensalidade", Frequency.MONTHLY,
                LocalDate.of(2025, 3, 1)
        );
    }

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("deve criar transação recorrente com sucesso")
        void shouldCreate() {
            UUID userId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            User user = mockUser(userId);
            Account account = mockAccount(accountId, userId);
            Category category = mockCategory(categoryId);
            RecurringRequest request = mockRequest(accountId, categoryId);

            RecurringTransaction saved = mockRecurring(UUID.randomUUID(), userId, account, category);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(accountRepository.findByIdAndUserId(accountId, userId)).willReturn(Optional.of(account));
            given(categoryRepository.findByIdAndUserIdOrDefault(categoryId, userId)).willReturn(Optional.of(category));
            given(recurringRepository.save(any(RecurringTransaction.class))).willReturn(saved);

            var result = recurringService.create(request, userId);

            assertThat(result).isNotNull();
            assertThat(result.type()).isEqualTo(TransactionType.EXPENSE);
            verify(recurringRepository).save(any(RecurringTransaction.class));
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando usuário não existe")
        void shouldThrow_whenUserNotFound() {
            UUID userId = UUID.randomUUID();
            RecurringRequest request = mockRequest(UUID.randomUUID(), UUID.randomUUID());

            given(userRepository.findById(userId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> recurringService.create(request, userId))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(recurringRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando conta não existe")
        void shouldThrow_whenAccountNotFound() {
            UUID userId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            User user = mockUser(userId);
            RecurringRequest request = mockRequest(accountId, categoryId);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(accountRepository.findByIdAndUserId(accountId, userId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> recurringService.create(request, userId))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(recurringRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findByUserId")
    class FindByUserIdTests {

        @Test
        @DisplayName("deve retornar lista de transações recorrentes do usuário")
        void shouldReturnList() {
            UUID userId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            Account account = mockAccount(accountId, userId);
            Category category = mockCategory(categoryId);
            RecurringTransaction recurring = mockRecurring(UUID.randomUUID(), userId, account, category);

            given(recurringRepository.findByUserId(userId)).willReturn(List.of(recurring));

            var result = recurringService.findByUserId(userId);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("update")
    class UpdateTests {

        @Test
        @DisplayName("deve atualizar transação recorrente com sucesso")
        void shouldUpdate() {
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            Account account = mockAccount(accountId, userId);
            Category category = mockCategory(categoryId);
            RecurringTransaction existing = mockRecurring(id, userId, account, category);
            RecurringRequest request = mockRequest(accountId, categoryId);

            given(recurringRepository.findByIdAndUserId(id, userId)).willReturn(Optional.of(existing));
            given(accountRepository.findByIdAndUserId(accountId, userId)).willReturn(Optional.of(account));
            given(categoryRepository.findByIdAndUserIdOrDefault(categoryId, userId)).willReturn(Optional.of(category));
            given(recurringRepository.save(existing)).willReturn(existing);

            var result = recurringService.update(id, userId, request);

            assertThat(result).isNotNull();
            verify(recurringRepository).save(existing);
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando transação recorrente não existe")
        void shouldThrow_whenNotFound() {
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            RecurringRequest request = mockRequest(UUID.randomUUID(), UUID.randomUUID());

            given(recurringRepository.findByIdAndUserId(id, userId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> recurringService.update(id, userId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTests {

        @Test
        @DisplayName("deve excluir transação recorrente com sucesso")
        void shouldDelete() {
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            Account account = mockAccount(accountId, userId);
            Category category = mockCategory(categoryId);
            RecurringTransaction recurring = mockRecurring(id, userId, account, category);

            given(recurringRepository.findByIdAndUserId(id, userId)).willReturn(Optional.of(recurring));

            recurringService.delete(id, userId);

            verify(recurringRepository).delete(recurring);
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando transação recorrente não existe")
        void shouldThrow_whenNotFound() {
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            given(recurringRepository.findByIdAndUserId(id, userId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> recurringService.delete(id, userId))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(recurringRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("computeNextDueDate")
    class ComputeNextDueDateTests {

        @Test
        @DisplayName("deve calcular próxima data para frequência diária")
        void shouldComputeDaily() {
            LocalDate date = LocalDate.of(2025, 1, 15);
            assertThat(RecurringServiceImpl.computeNextDueDate(date, Frequency.DAILY))
                    .isEqualTo(LocalDate.of(2025, 1, 16));
        }

        @Test
        @DisplayName("deve calcular próxima data para frequência semanal")
        void shouldComputeWeekly() {
            LocalDate date = LocalDate.of(2025, 1, 15);
            assertThat(RecurringServiceImpl.computeNextDueDate(date, Frequency.WEEKLY))
                    .isEqualTo(LocalDate.of(2025, 1, 22));
        }

        @Test
        @DisplayName("deve calcular próxima data para frequência mensal")
        void shouldComputeMonthly() {
            LocalDate date = LocalDate.of(2025, 1, 15);
            assertThat(RecurringServiceImpl.computeNextDueDate(date, Frequency.MONTHLY))
                    .isEqualTo(LocalDate.of(2025, 2, 15));
        }

        @Test
        @DisplayName("deve calcular próxima data para frequência anual")
        void shouldComputeYearly() {
            LocalDate date = LocalDate.of(2025, 1, 15);
            assertThat(RecurringServiceImpl.computeNextDueDate(date, Frequency.YEARLY))
                    .isEqualTo(LocalDate.of(2026, 1, 15));
        }
    }
}
