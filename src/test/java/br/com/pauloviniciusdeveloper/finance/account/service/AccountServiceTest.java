package br.com.pauloviniciusdeveloper.finance.account.service;

import br.com.pauloviniciusdeveloper.finance.account.dto.AccountRequest;
import br.com.pauloviniciusdeveloper.finance.account.entity.Account;
import br.com.pauloviniciusdeveloper.finance.account.repository.AccountRepository;
import br.com.pauloviniciusdeveloper.finance.common.exception.ResourceNotFoundException;
import br.com.pauloviniciusdeveloper.finance.recurring.repository.RecurringRepository;
import br.com.pauloviniciusdeveloper.finance.transaction.repository.TransactionRepository;
import br.com.pauloviniciusdeveloper.finance.user.dto.UserResponse;
import br.com.pauloviniciusdeveloper.finance.user.entity.Role;
import br.com.pauloviniciusdeveloper.finance.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RecurringRepository recurringRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AccountServiceImp accountService;

    private UserResponse mockUserResponse(UUID userId) {
        return UserResponse.builder()
                .id(userId)
                .fullName("Test User")
                .email("test@test.com")
                .roles(Set.of(Role.USER))
                .isActive(true)
                .build();
    }

    private Account mockAccount(UUID id, UUID userId) {
        return Account.builder()
                .id(id)
                .name("Conta Corrente")
                .build();
    }

    @Nested
    @DisplayName("findByUserId")
    class FindByUserIdTests {

        @Test
        @DisplayName("deve retornar lista de contas do usuário")
        void shouldReturnAccountList() {
            UUID userId = UUID.randomUUID();
            Account account = mockAccount(UUID.randomUUID(), userId);
            given(accountRepository.findByUserId(userId)).willReturn(List.of(account));

            var result = accountService.findByUserId(userId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("Conta Corrente");
        }

        @Test
        @DisplayName("deve retornar lista vazia quando usuário não tem contas")
        void shouldReturnEmptyList() {
            UUID userId = UUID.randomUUID();
            given(accountRepository.findByUserId(userId)).willReturn(List.of());

            var result = accountService.findByUserId(userId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("deve criar conta com sucesso")
        void shouldCreateAccount() {
            UUID userId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            AccountRequest request = new AccountRequest("Carteira");
            UserResponse userResponse = mockUserResponse(userId);

            Account savedAccount = Account.builder().id(accountId).name("Carteira").build();

            given(userService.findById(userId)).willReturn(userResponse);
            given(accountRepository.save(any(Account.class))).willReturn(savedAccount);

            var result = accountService.create(request, userId);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Carteira");
            assertThat(result.id()).isEqualTo(accountId);
            verify(accountRepository).save(any(Account.class));
        }
    }

    @Nested
    @DisplayName("deleteByIdAndUserId")
    class DeleteTests {

        @Test
        @DisplayName("deve excluir conta e dados relacionados em cascata")
        void shouldDeleteAccount() {
            UUID accountId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Account account = mockAccount(accountId, userId);

            given(accountRepository.findByIdAndUserId(accountId, userId)).willReturn(Optional.of(account));

            accountService.deleteByIdAndUserId(accountId, userId);

            verify(transactionRepository).deleteByAccountId(accountId);
            verify(recurringRepository).deleteByAccountId(accountId);
            verify(accountRepository).deleteById(accountId);
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando conta não existe")
        void shouldThrowNotFound_whenAccountDoesNotExist() {
            UUID accountId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            given(accountRepository.findByIdAndUserId(accountId, userId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.deleteByIdAndUserId(accountId, userId))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(accountRepository, never()).deleteById(any());
        }
    }
}
