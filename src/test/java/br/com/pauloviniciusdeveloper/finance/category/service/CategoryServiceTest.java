package br.com.pauloviniciusdeveloper.finance.category.service;

import br.com.pauloviniciusdeveloper.finance.budget.repository.BudgetRepository;
import br.com.pauloviniciusdeveloper.finance.category.dto.CategoryRequest;
import br.com.pauloviniciusdeveloper.finance.category.entity.Category;
import br.com.pauloviniciusdeveloper.finance.category.repository.CategoryRepository;
import br.com.pauloviniciusdeveloper.finance.common.exception.ResourceNotFoundException;
import br.com.pauloviniciusdeveloper.finance.recurring.repository.RecurringRepository;
import br.com.pauloviniciusdeveloper.finance.transaction.repository.TransactionRepository;
import br.com.pauloviniciusdeveloper.finance.user.dto.UserResponse;
import br.com.pauloviniciusdeveloper.finance.user.entity.Role;
import br.com.pauloviniciusdeveloper.finance.user.entity.User;
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
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private RecurringRepository recurringRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private UserResponse mockUserResponse(UUID userId) {
        return UserResponse.builder()
                .id(userId)
                .fullName("Test User")
                .email("test@test.com")
                .roles(Set.of(Role.USER))
                .isActive(true)
                .build();
    }

    private User mockUser(UUID userId) {
        return User.builder().id(userId).fullName("Test").email("test@test.com")
                .hashedPassword("hash").isActive(true).build();
    }

    private Category mockCategory(UUID id, boolean isDefault, User user) {
        return Category.builder().id(id).name("Alimentação").isDefault(isDefault).user(user).build();
    }

    @Nested
    @DisplayName("findByUserId")
    class FindByUserIdTests {

        @Test
        @DisplayName("deve retornar categorias do usuário e categorias padrão")
        void shouldReturnUserAndDefaultCategories() {
            UUID userId = UUID.randomUUID();
            User user = mockUser(userId);
            Category userCategory = mockCategory(UUID.randomUUID(), false, user);
            Category defaultCategory = mockCategory(UUID.randomUUID(), true, null);

            given(categoryRepository.findByUserIdOrDefault(userId)).willReturn(List.of(userCategory, defaultCategory));

            var result = categoryService.findByUserId(userId);

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("deve criar categoria com sucesso")
        void shouldCreate() {
            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            UserResponse userResponse = mockUserResponse(userId);
            User user = mockUser(userId);
            Category saved = mockCategory(categoryId, false, user);
            CategoryRequest request = new CategoryRequest("Nova Categoria");

            given(userService.findById(userId)).willReturn(userResponse);
            given(categoryRepository.save(any(Category.class))).willReturn(saved);

            var result = categoryService.create(request, userId);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Alimentação");
            verify(categoryRepository).save(any(Category.class));
        }
    }

    @Nested
    @DisplayName("updateByIdAndUserId")
    class UpdateTests {

        @Test
        @DisplayName("deve lançar IllegalArgumentException ao tentar atualizar categoria padrão")
        void shouldThrow_whenUpdatingDefaultCategory() {
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Category defaultCategory = mockCategory(id, true, null);
            CategoryRequest request = new CategoryRequest("Novo Nome");

            given(categoryRepository.findById(id)).willReturn(Optional.of(defaultCategory));

            assertThatThrownBy(() -> categoryService.updateByIdAndUserId(id, userId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Default categories cannot be updated");

            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando categoria não existe")
        void shouldThrow_whenNotFound() {
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            CategoryRequest request = new CategoryRequest("Novo Nome");

            given(categoryRepository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.updateByIdAndUserId(id, userId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteByIdAndUserId")
    class DeleteTests {

        @Test
        @DisplayName("deve excluir categoria e dados relacionados em cascata")
        void shouldDelete() {
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            User user = mockUser(userId);
            Category category = mockCategory(id, false, user);

            given(categoryRepository.findById(id)).willReturn(Optional.of(category));
            given(categoryRepository.findByIdAndUserId(id, userId)).willReturn(Optional.of(category));

            categoryService.deleteByIdAndUserId(id, userId);

            verify(transactionRepository).deleteByCategoryId(id);
            verify(recurringRepository).deleteByCategoryId(id);
            verify(budgetRepository).deleteByCategoryId(id);
            verify(categoryRepository).deleteById(id);
        }

        @Test
        @DisplayName("deve lançar IllegalArgumentException ao tentar excluir categoria padrão")
        void shouldThrow_whenDeletingDefaultCategory() {
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Category defaultCategory = mockCategory(id, true, null);

            given(categoryRepository.findById(id)).willReturn(Optional.of(defaultCategory));

            assertThatThrownBy(() -> categoryService.deleteByIdAndUserId(id, userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Default categories cannot be deleted");

            verify(categoryRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando categoria não existe")
        void shouldThrow_whenNotFound() {
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            given(categoryRepository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.deleteByIdAndUserId(id, userId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando categoria não pertence ao usuário")
        void shouldThrow_whenCategoryBelongsToOtherUser() {
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();
            User otherUser = mockUser(otherUserId);
            Category otherUserCategory = mockCategory(id, false, otherUser);

            given(categoryRepository.findById(id)).willReturn(Optional.of(otherUserCategory));

            assertThatThrownBy(() -> categoryService.deleteByIdAndUserId(id, userId))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(categoryRepository, never()).delete(any());
        }
    }
}
