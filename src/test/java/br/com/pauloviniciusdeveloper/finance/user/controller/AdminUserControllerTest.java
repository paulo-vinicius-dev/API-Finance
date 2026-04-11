package br.com.pauloviniciusdeveloper.finance.user.controller;

import br.com.pauloviniciusdeveloper.finance.common.exception.ResourceNotFoundException;
import br.com.pauloviniciusdeveloper.finance.common.security.JwtAuthenticationConverter;
import br.com.pauloviniciusdeveloper.finance.common.security.UserPrincipal;
import br.com.pauloviniciusdeveloper.finance.user.dto.UserAdminUpdateRequest;
import br.com.pauloviniciusdeveloper.finance.user.dto.UserResponse;
import br.com.pauloviniciusdeveloper.finance.user.entity.Role;
import br.com.pauloviniciusdeveloper.finance.user.entity.User;
import br.com.pauloviniciusdeveloper.finance.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@ActiveProfiles("test")
@Import(AdminUserControllerTest.TestSecurityConfig.class)
class AdminUserControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    private UserPrincipal adminPrincipal() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("admin@test.com")
                .fullName("Admin User")
                .hashedPassword("hash")
                .roles(Set.of(Role.ADMIN))
                .isActive(true)
                .build();
        return new UserPrincipal(user);
    }

    private UserPrincipal userPrincipal() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("user@test.com")
                .fullName("Regular User")
                .hashedPassword("hash")
                .roles(Set.of(Role.USER))
                .isActive(true)
                .build();
        return new UserPrincipal(user);
    }

    private UserResponse mockResponse(UUID id) {
        return UserResponse.builder()
                .id(id)
                .fullName("Test User")
                .email("test@test.com")
                .roles(Set.of(Role.USER))
                .isActive(true)
                .build();
    }

    // ─── GET /api/v1/admin/users ─────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/admin/users")
    class ListTests {

        @Test
        @DisplayName("deve retornar 200 com página de usuários para ADMIN")
        void shouldReturn200_forAdmin() throws Exception {
            UUID id = UUID.randomUUID();
            var page = new PageImpl<>(List.of(mockResponse(id)));

            given(userService.findAll(any(), any(Pageable.class))).willReturn(page);

            mockMvc.perform(get("/api/v1/admin/users").with(user(adminPrincipal())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(id.toString()));
        }

        @Test
        @DisplayName("deve retornar 200 ao usar parâmetro search")
        void shouldReturn200_withSearchParam() throws Exception {
            var page = new PageImpl<>(List.of(mockResponse(UUID.randomUUID())));

            given(userService.findAll(eq("test"), any(Pageable.class))).willReturn(page);

            mockMvc.perform(get("/api/v1/admin/users")
                            .param("search", "test")
                            .with(user(adminPrincipal())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("deve retornar 403 para usuário sem role ADMIN")
        void shouldReturn403_forNonAdmin() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users").with(user(userPrincipal())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("deve retornar 401 sem autenticação")
        void shouldReturn401_whenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── GET /api/v1/admin/users/{id} ────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/admin/users/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("deve retornar 200 com dados do usuário para ADMIN")
        void shouldReturn200_forAdmin() throws Exception {
            UUID id = UUID.randomUUID();
            given(userService.findById(id)).willReturn(mockResponse(id));

            mockMvc.perform(get("/api/v1/admin/users/{id}", id).with(user(adminPrincipal())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }

        @Test
        @DisplayName("deve retornar 404 quando usuário não existe")
        void shouldReturn404_whenNotFound() throws Exception {
            UUID id = UUID.randomUUID();
            given(userService.findById(id)).willThrow(new ResourceNotFoundException("User", id));

            mockMvc.perform(get("/api/v1/admin/users/{id}", id).with(user(adminPrincipal())))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("deve retornar 403 para usuário sem role ADMIN")
        void shouldReturn403_forNonAdmin() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users/{id}", UUID.randomUUID())
                            .with(user(userPrincipal())))
                    .andExpect(status().isForbidden());
        }
    }

    // ─── PATCH /api/v1/admin/users/{id} ──────────────────────────────────────

    @Nested
    @DisplayName("PATCH /api/v1/admin/users/{id}")
    class UpdateTests {

        @Test
        @DisplayName("deve retornar 200 ao atualizar usuário como ADMIN")
        void shouldReturn200_whenUpdating() throws Exception {
            UUID id = UUID.randomUUID();
            var request = new UserAdminUpdateRequest(false, Set.of(Role.USER));
            var response = UserResponse.builder()
                    .id(id).fullName("Test User").email("test@test.com")
                    .roles(Set.of(Role.USER)).isActive(false).build();

            given(userService.adminUpdate(eq(id), any(UserAdminUpdateRequest.class)))
                    .willReturn(response);

            mockMvc.perform(patch("/api/v1/admin/users/{id}", id)
                            .with(user(adminPrincipal())).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive").value(false));
        }

        @Test
        @DisplayName("deve retornar 422 quando body é inválido")
        void shouldReturn422_whenBodyInvalid() throws Exception {
            // isActive null + empty roles → validation error
            String invalidBody = "{\"isActive\": null, \"roles\": []}";

            mockMvc.perform(patch("/api/v1/admin/users/{id}", UUID.randomUUID())
                            .with(user(adminPrincipal())).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.fieldErrors").isArray());
        }

        @Test
        @DisplayName("deve retornar 404 quando usuário não existe")
        void shouldReturn404_whenNotFound() throws Exception {
            UUID id = UUID.randomUUID();
            var request = new UserAdminUpdateRequest(true, Set.of(Role.USER));

            given(userService.adminUpdate(eq(id), any()))
                    .willThrow(new ResourceNotFoundException("User", id));

            mockMvc.perform(patch("/api/v1/admin/users/{id}", id)
                            .with(user(adminPrincipal())).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("deve retornar 403 para usuário sem role ADMIN")
        void shouldReturn403_forNonAdmin() throws Exception {
            var request = new UserAdminUpdateRequest(true, Set.of(Role.USER));

            mockMvc.perform(patch("/api/v1/admin/users/{id}", UUID.randomUUID())
                            .with(user(userPrincipal())).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    // ─── DELETE /api/v1/admin/users/{id} ─────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/v1/admin/users/{id}")
    class DeleteTests {

        @Test
        @DisplayName("deve retornar 204 ao excluir usuário como ADMIN")
        void shouldReturn204_whenDeleting() throws Exception {
            UUID id = UUID.randomUUID();

            mockMvc.perform(delete("/api/v1/admin/users/{id}", id)
                            .with(user(adminPrincipal())).with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("deve retornar 404 quando usuário não existe")
        void shouldReturn404_whenNotFound() throws Exception {
            UUID id = UUID.randomUUID();

            doThrow(new ResourceNotFoundException("User", id))
                    .when(userService).adminDelete(id);

            mockMvc.perform(delete("/api/v1/admin/users/{id}", id)
                            .with(user(adminPrincipal())).with(csrf()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("deve retornar 403 para usuário sem role ADMIN")
        void shouldReturn403_forNonAdmin() throws Exception {
            mockMvc.perform(delete("/api/v1/admin/users/{id}", UUID.randomUUID())
                            .with(user(userPrincipal())).with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }
}
