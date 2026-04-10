package br.com.pauloviniciusdeveloper.finance.account.controller;

import br.com.pauloviniciusdeveloper.finance.account.dto.AccountRequest;
import br.com.pauloviniciusdeveloper.finance.account.dto.AccountResponse;
import br.com.pauloviniciusdeveloper.finance.account.service.AccountService;
import br.com.pauloviniciusdeveloper.finance.common.exception.ConflictException;
import br.com.pauloviniciusdeveloper.finance.common.exception.ResourceNotFoundException;
import br.com.pauloviniciusdeveloper.finance.common.security.JwtAuthenticationConverter;
import br.com.pauloviniciusdeveloper.finance.common.security.UserPrincipal;
import br.com.pauloviniciusdeveloper.finance.user.entity.Role;
import br.com.pauloviniciusdeveloper.finance.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
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

@WebMvcTest(AccountController.class)
@ActiveProfiles("test")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    private UserPrincipal mockPrincipal() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@test.com")
                .fullName("Test User")
                .hashedPassword("hash")
                .roles(Set.of(Role.USER))
                .isActive(true)
                .build();
        return new UserPrincipal(user);
    }

    @Test
    @DisplayName("GET /api/v1/accounts — deve retornar 200 com lista de contas")
    void shouldReturn200_withAccountList() throws Exception {
        UserPrincipal principal = mockPrincipal();
        AccountResponse response = AccountResponse.builder()
                .id(UUID.randomUUID()).name("Conta Corrente").build();

        given(accountService.findByUserId(principal.getId())).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/accounts").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Conta Corrente"));
    }

    @Test
    @DisplayName("POST /api/v1/accounts — deve retornar 201 com conta criada")
    void shouldReturn201_whenCreating() throws Exception {
        UserPrincipal principal = mockPrincipal();
        AccountRequest request = new AccountRequest("Poupança");
        AccountResponse response = AccountResponse.builder()
                .id(UUID.randomUUID()).name("Poupança").build();

        given(accountService.create(any(AccountRequest.class), eq(principal.getId()))).willReturn(response);

        mockMvc.perform(post("/api/v1/accounts")
                        .with(user(principal)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Poupança"));
    }

    @Test
    @DisplayName("POST /api/v1/accounts — deve retornar 422 quando nome está em branco")
    void shouldReturn422_whenNameIsBlank() throws Exception {
        UserPrincipal principal = mockPrincipal();
        AccountRequest request = new AccountRequest("");

        mockMvc.perform(post("/api/v1/accounts")
                        .with(user(principal)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    @DisplayName("DELETE /api/v1/accounts/{id} — deve retornar 204 ao excluir")
    void shouldReturn204_whenDeleting() throws Exception {
        UserPrincipal principal = mockPrincipal();
        UUID accountId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/accounts/{id}", accountId)
                        .with(user(principal)).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/accounts/{id} — deve retornar 409 quando conta possui transações recorrentes")
    void shouldReturn409_whenAccountHasRecurringTransactions() throws Exception {
        UserPrincipal principal = mockPrincipal();
        UUID accountId = UUID.randomUUID();

        doThrow(new ConflictException("Não é possível excluir a conta pois ela possui transações recorrentes vinculadas."))
                .when(accountService).deleteByIdAndUserId(eq(accountId), eq(principal.getId()));

        mockMvc.perform(delete("/api/v1/accounts/{id}", accountId)
                        .with(user(principal)).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("transações recorrentes")));
    }

    @Test
    @DisplayName("DELETE /api/v1/accounts/{id} — deve retornar 404 quando conta não existe")
    void shouldReturn404_whenAccountNotFound() throws Exception {
        UserPrincipal principal = mockPrincipal();
        UUID accountId = UUID.randomUUID();

        doThrow(new ResourceNotFoundException("Account", accountId))
                .when(accountService).deleteByIdAndUserId(eq(accountId), eq(principal.getId()));

        mockMvc.perform(delete("/api/v1/accounts/{id}", accountId)
                        .with(user(principal)).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/accounts — deve retornar 401 sem autenticação")
    void shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isUnauthorized());
    }
}
