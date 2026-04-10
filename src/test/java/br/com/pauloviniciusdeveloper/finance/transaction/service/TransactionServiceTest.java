package br.com.pauloviniciusdeveloper.finance.transaction.service;

import br.com.pauloviniciusdeveloper.finance.account.entity.Account;
import br.com.pauloviniciusdeveloper.finance.account.repository.AccountRepository;
import br.com.pauloviniciusdeveloper.finance.category.entity.Category;
import br.com.pauloviniciusdeveloper.finance.category.repository.CategoryRepository;
import br.com.pauloviniciusdeveloper.finance.common.exception.ResourceNotFoundException;
import br.com.pauloviniciusdeveloper.finance.transaction.dto.TransactionRequest;
import br.com.pauloviniciusdeveloper.finance.transaction.entity.Transaction;
import br.com.pauloviniciusdeveloper.finance.transaction.entity.TransactionType;
import br.com.pauloviniciusdeveloper.finance.transaction.repository.TransactionRepository;
import br.com.pauloviniciusdeveloper.finance.user.entity.User;
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
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User mockUser(UUID userId) {
        return User.builder().id(userId).fullName("Test").email("t@t.com")
                .hashedPassword("hash").isActive(true).build();
    }

    private Account mockAccount(UUID accountId, UUID userId) {
        User user = mockUser(userId);
        return Account.builder().id(accountId).name("Conta").user(user).build();
    }

    private Category mockCategory(UUID categoryId) {
        return Category.builder().id(categoryId).name("Alimentação").isDefault(false).build();
    }

    private Transaction mockTransaction(UUID id, Account account, Category category) {
        return Transaction.builder()
                .id(id)
                .description("Supermercado")
                .amount(BigDecimal.valueOf(150))
                .date(LocalDate.of(2025, 3, 10))
                .type(TransactionType.EXPENSE)
                .account(account)
                .category(category)
                .build();
    }

    private TransactionRequest mockRequest(UUID accountId, UUID categoryId) {
        return TransactionRequest.builder()
                .accountId(accountId)
                .categoryId(categoryId)
                .type(TransactionType.EXPENSE)
                .amount(BigDecimal.valueOf(150))
                .date(LocalDate.of(2025, 3, 10))
                .description("Supermercado")
                .build();
    }

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("deve criar transação com sucesso")
        void shouldCreate() {
            UUID userId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            Account account = mockAccount(accountId, userId);
            Category category = mockCategory(categoryId);
            TransactionRequest request = mockRequest(accountId, categoryId);
            Transaction saved = mockTransaction(UUID.randomUUID(), account, category);

            given(accountRepository.findByIdAndUserId(accountId, userId)).willReturn(Optional.of(account));
            given(categoryRepository.findByIdAndUserIdOrDefault(categoryId, userId)).willReturn(Optional.of(category));
            given(transactionRepository.save(any(Transaction.class))).willReturn(saved);

            var result = transactionService.create(request, userId);

            assertThat(result).isNotNull();
            assertThat(result.description()).isEqualTo("Supermercado");
            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando conta não existe")
        void shouldThrow_whenAccountNotFound() {
            UUID userId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            TransactionRequest request = mockRequest(accountId, categoryId);

            given(accountRepository.findByIdAndUserId(accountId, userId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.create(request, userId))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando categoria não existe")
        void shouldThrow_whenCategoryNotFound() {
            UUID userId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            Account account = mockAccount(accountId, userId);
            TransactionRequest request = mockRequest(accountId, categoryId);

            given(accountRepository.findByIdAndUserId(accountId, userId)).willReturn(Optional.of(account));
            given(categoryRepository.findByIdAndUserIdOrDefault(categoryId, userId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.create(request, userId))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(transactionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findByUserId")
    class FindByUserIdTests {

        @Test
        @DisplayName("deve retornar lista de transações do usuário")
        void shouldReturnList() {
            UUID userId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            Account account = mockAccount(accountId, userId);
            Category category = mockCategory(UUID.randomUUID());
            Transaction tx = mockTransaction(UUID.randomUUID(), account, category);

            given(transactionRepository.findByAccountUserId(userId)).willReturn(List.of(tx));

            var result = transactionService.findByUserId(userId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).description()).isEqualTo("Supermercado");
        }
    }

    @Nested
    @DisplayName("deleteByIdAndUserId")
    class DeleteTests {

        @Test
        @DisplayName("deve excluir transação com sucesso")
        void shouldDelete() {
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Account account = mockAccount(UUID.randomUUID(), userId);
            Category category = mockCategory(UUID.randomUUID());
            Transaction tx = mockTransaction(id, account, category);

            given(transactionRepository.findByIdAndAccountUserId(id, userId)).willReturn(Optional.of(tx));

            transactionService.deleteByIdAndUserId(id, userId);

            verify(transactionRepository).delete(tx);
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando transação não existe")
        void shouldThrow_whenNotFound() {
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            given(transactionRepository.findByIdAndAccountUserId(id, userId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.deleteByIdAndUserId(id, userId))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(transactionRepository, never()).delete(any());
        }
    }
}
