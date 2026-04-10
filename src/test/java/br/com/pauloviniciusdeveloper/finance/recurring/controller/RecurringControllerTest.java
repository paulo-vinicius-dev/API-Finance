package br.com.pauloviniciusdeveloper.finance.recurring.controller;

import br.com.pauloviniciusdeveloper.finance.account.dto.AccountResponse;
import br.com.pauloviniciusdeveloper.finance.category.dto.CategoryResponse;
import br.com.pauloviniciusdeveloper.finance.common.exception.ResourceNotFoundException;
import br.com.pauloviniciusdeveloper.finance.common.security.JwtAuthenticationConverter;
import br.com.pauloviniciusdeveloper.finance.common.security.UserPrincipal;
import br.com.pauloviniciusdeveloper.finance.recurring.dto.RecurringRequest;
import br.com.pauloviniciusdeveloper.finance.recurring.dto.RecurringResponse;
import br.com.pauloviniciusdeveloper.finance.recurring.entity.Frequency;
import br.com.pauloviniciusdeveloper.finance.recurring.service.RecurringService;
import br.com.pauloviniciusdeveloper.finance.transaction.entity.TransactionType;
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

import java.math.BigDecimal;
import java.time.LocalDate;
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

@WebMvcTest(RecurringController.class)
@ActiveProfiles("test")
class RecurringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private RecurringService recurringService;

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

    private RecurringResponse mockResponse() {
        return new RecurringResponse(
                UUID.randomUUID(),
                new CategoryResponse(UUID.randomUUID(), "Alimentação", false),
                new AccountResponse(UUID.randomUUID(), "Conta Corrente"),
                TransactionType.EXPENSE,
                BigDecimal.valueOf(200),
                "Mensalidade",
                Frequency.MONTHLY,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 2, 1),
                true
        );
    }

    @Test
    @DisplayName("GET /api/v1/recurring — deve retornar 200 com lista")
    void shouldReturn200_withList() throws Exception {
        UserPrincipal principal = mockPrincipal();
        given(recurringService.findByUserId(principal.getId())).willReturn(List.of(mockResponse()));

        mockMvc.perform(get("/api/v1/recurring").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].frequency").value("MONTHLY"));
    }

    @Test
    @DisplayName("POST /api/v1/recurring — deve retornar 201 ao criar")
    void shouldReturn201_whenCreating() throws Exception {
        UserPrincipal principal = mockPrincipal();
        RecurringRequest request = new RecurringRequest(
                UUID.randomUUID(), UUID.randomUUID(), TransactionType.EXPENSE,
                BigDecimal.valueOf(200), "Mensalidade", Frequency.MONTHLY,
                LocalDate.of(2025, 1, 1)
        );

        given(recurringService.create(any(RecurringRequest.class), eq(principal.getId())))
                .willReturn(mockResponse());

        mockMvc.perform(post("/api/v1/recurring")
                        .with(user(principal)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    @Test
    @DisplayName("DELETE /api/v1/recurring/{id} — deve retornar 204 ao excluir")
    void shouldReturn204_whenDeleting() throws Exception {
        UserPrincipal principal = mockPrincipal();
        UUID recurringId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/recurring/{id}", recurringId)
                        .with(user(principal)).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/recurring/{id} — deve retornar 404 quando não existe")
    void shouldReturn404_whenNotFound() throws Exception {
        UserPrincipal principal = mockPrincipal();
        UUID recurringId = UUID.randomUUID();

        doThrow(new ResourceNotFoundException("RecurringTransaction", recurringId))
                .when(recurringService).delete(eq(recurringId), eq(principal.getId()));

        mockMvc.perform(delete("/api/v1/recurring/{id}", recurringId)
                        .with(user(principal)).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
