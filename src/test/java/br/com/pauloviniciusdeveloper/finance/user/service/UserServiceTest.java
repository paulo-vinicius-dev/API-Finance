package br.com.pauloviniciusdeveloper.finance.user.service;

import br.com.pauloviniciusdeveloper.finance.account.repository.AccountRepository;
import br.com.pauloviniciusdeveloper.finance.budget.repository.BudgetRepository;
import br.com.pauloviniciusdeveloper.finance.category.repository.CategoryRepository;
import br.com.pauloviniciusdeveloper.finance.common.exception.ResourceNotFoundException;
import br.com.pauloviniciusdeveloper.finance.recurring.repository.RecurringRepository;
import br.com.pauloviniciusdeveloper.finance.transaction.repository.TransactionRepository;
import br.com.pauloviniciusdeveloper.finance.user.dto.UserAdminUpdateRequest;
import br.com.pauloviniciusdeveloper.finance.user.entity.Role;
import br.com.pauloviniciusdeveloper.finance.user.entity.User;
import br.com.pauloviniciusdeveloper.finance.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private RecurringRepository recurringRepository;
    @Mock private BudgetRepository budgetRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser(UUID id) {
        return User.builder()
                .id(id)
                .fullName("Test User")
                .email("test@test.com")
                .hashedPassword("hash")
                .roles(Set.of(Role.USER))
                .isActive(true)
                .build();
    }

    // ─── findById ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findById")
    class FindByIdTests {

        @Test
        @DisplayName("deve retornar UserResponse quando usuário existe")
        void shouldReturnUser_whenExists() {
            UUID id = UUID.randomUUID();
            given(userRepository.findById(id)).willReturn(Optional.of(mockUser(id)));

            var result = userService.findById(id);

            assertThat(result.id()).isEqualTo(id);
            assertThat(result.email()).isEqualTo("test@test.com");
        }

        @Test
        @DisplayName("deve lançar UsernameNotFoundException quando usuário não existe")
        void shouldThrow_whenNotFound() {
            UUID id = UUID.randomUUID();
            given(userRepository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findById(id))
                    .isInstanceOf(UsernameNotFoundException.class);
        }
    }

    // ─── findByEmail ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByEmail")
    class FindByEmailTests {

        @Test
        @DisplayName("deve retornar UserResponse quando e-mail existe")
        void shouldReturnUser_whenEmailExists() {
            UUID id = UUID.randomUUID();
            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(mockUser(id)));

            var result = userService.findByEmail("test@test.com");

            assertThat(result.email()).isEqualTo("test@test.com");
        }

        @Test
        @DisplayName("deve lançar UsernameNotFoundException quando e-mail não existe")
        void shouldThrow_whenEmailNotFound() {
            given(userRepository.findByEmail("none@test.com")).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findByEmail("none@test.com"))
                    .isInstanceOf(UsernameNotFoundException.class);
        }
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findAll")
    class FindAllTests {

        @Test
        @DisplayName("deve retornar página de todos os usuários quando search é nulo")
        void shouldReturnAllUsers_whenSearchIsNull() {
            UUID id = UUID.randomUUID();
            var pageable = PageRequest.of(0, 20);
            var page = new PageImpl<>(List.of(mockUser(id)));

            given(userRepository.findAll(pageable)).willReturn(page);

            var result = userService.findAll(null, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(userRepository).findAll(pageable);
            verify(userRepository, never()).searchByNameOrEmail(any(), any());
        }

        @Test
        @DisplayName("deve usar busca quando search está preenchido")
        void shouldSearchUsers_whenSearchProvided() {
            UUID id = UUID.randomUUID();
            var pageable = PageRequest.of(0, 20);
            var page = new PageImpl<>(List.of(mockUser(id)));

            given(userRepository.searchByNameOrEmail("test", pageable)).willReturn(page);

            var result = userService.findAll("test", pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(userRepository).searchByNameOrEmail("test", pageable);
            verify(userRepository, never()).findAll(pageable);
        }

        @Test
        @DisplayName("deve usar busca com search em branco tratado como nulo")
        void shouldReturnAll_whenSearchIsBlank() {
            UUID id = UUID.randomUUID();
            var pageable = PageRequest.of(0, 20);
            var page = new PageImpl<>(List.of(mockUser(id)));

            given(userRepository.findAll(pageable)).willReturn(page);

            var result = userService.findAll("   ", pageable);

            verify(userRepository).findAll(pageable);
        }
    }

    // ─── adminUpdate ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("adminUpdate")
    class AdminUpdateTests {

        @Test
        @DisplayName("deve atualizar isActive e roles do usuário")
        void shouldUpdateUser() {
            UUID id = UUID.randomUUID();
            User user = mockUser(id);
            var request = new UserAdminUpdateRequest(false, Set.of(Role.ADMIN));

            given(userRepository.findById(id)).willReturn(Optional.of(user));
            given(userRepository.save(user)).willReturn(user);

            var result = userService.adminUpdate(id, request);

            assertThat(result).isNotNull();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando usuário não existe")
        void shouldThrow_whenNotFound() {
            UUID id = UUID.randomUUID();
            given(userRepository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.adminUpdate(id, new UserAdminUpdateRequest(true, Set.of(Role.USER))))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(userRepository, never()).save(any());
        }
    }

    // ─── selfDelete ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("selfDelete")
    class SelfDeleteTests {

        @Test
        @DisplayName("deve excluir a própria conta e todos os dados relacionados")
        void shouldDeleteAllUserData() {
            UUID id = UUID.randomUUID();
            given(userRepository.existsById(id)).willReturn(true);

            userService.selfDelete(id);

            verify(transactionRepository).deleteByAccountUserId(id);
            verify(recurringRepository).deleteByUserId(id);
            verify(budgetRepository).deleteByUserId(id);
            verify(accountRepository).deleteByUserId(id);
            verify(categoryRepository).deleteByUserId(id);
            verify(userRepository).deleteById(id);
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando usuário não existe")
        void shouldThrow_whenNotFound() {
            UUID id = UUID.randomUUID();
            given(userRepository.existsById(id)).willReturn(false);

            assertThatThrownBy(() -> userService.selfDelete(id))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(userRepository, never()).deleteById(any());
        }
    }

    // ─── adminDelete ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("adminDelete")
    class AdminDeleteTests {

        @Test
        @DisplayName("deve excluir usuário e todos os dados relacionados em cascata")
        void shouldDeleteAllUserData() {
            UUID id = UUID.randomUUID();
            given(userRepository.existsById(id)).willReturn(true);

            userService.adminDelete(id);

            verify(transactionRepository).deleteByAccountUserId(id);
            verify(recurringRepository).deleteByUserId(id);
            verify(budgetRepository).deleteByUserId(id);
            verify(accountRepository).deleteByUserId(id);
            verify(categoryRepository).deleteByUserId(id);
            verify(userRepository).deleteById(id);
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando usuário não existe")
        void shouldThrow_whenNotFound() {
            UUID id = UUID.randomUUID();
            given(userRepository.existsById(id)).willReturn(false);

            assertThatThrownBy(() -> userService.adminDelete(id))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(userRepository, never()).deleteById(any());
        }
    }
}
