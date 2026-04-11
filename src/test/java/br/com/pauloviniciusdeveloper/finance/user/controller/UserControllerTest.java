package br.com.pauloviniciusdeveloper.finance.user.controller;

import br.com.pauloviniciusdeveloper.finance.common.exception.ResourceNotFoundException;
import br.com.pauloviniciusdeveloper.finance.common.security.JwtAuthenticationConverter;
import br.com.pauloviniciusdeveloper.finance.common.security.UserPrincipal;
import br.com.pauloviniciusdeveloper.finance.user.dto.UserResponse;
import br.com.pauloviniciusdeveloper.finance.user.entity.Role;
import br.com.pauloviniciusdeveloper.finance.user.entity.User;
import br.com.pauloviniciusdeveloper.finance.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    private UserPrincipal mockPrincipal(UUID id) {
        User user = User.builder()
                .id(id)
                .email("user@test.com")
                .fullName("Test User")
                .hashedPassword("hash")
                .roles(Set.of(Role.USER))
                .isActive(true)
                .build();
        return new UserPrincipal(user);
    }

    private UserResponse mockUserResponse(UUID id) {
        return UserResponse.builder()
                .id(id)
                .fullName("Test User")
                .email("user@test.com")
                .roles(Set.of(Role.USER))
                .isActive(true)
                .build();
    }

    // ─── GET /me ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/users/me — deve retornar 200 com dados do usuário autenticado")
    void shouldReturn200_withCurrentUser() throws Exception {
        UUID id = UUID.randomUUID();
        UserPrincipal principal = mockPrincipal(id);

        given(userService.findById(id)).willReturn(mockUserResponse(id));

        mockMvc.perform(get("/api/v1/users/me").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"))
                .andExpect(jsonPath("$.hashedPassword").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/users/me — deve retornar 401 sem autenticação")
    void shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    // ─── DELETE /me ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/v1/users/me — deve retornar 204 ao excluir a própria conta")
    void shouldReturn204_whenDeletingSelf() throws Exception {
        UUID id = UUID.randomUUID();
        UserPrincipal principal = mockPrincipal(id);

        mockMvc.perform(delete("/api/v1/users/me").with(user(principal)).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/users/me — deve retornar 404 quando usuário não existe")
    void shouldReturn404_whenUserNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        UserPrincipal principal = mockPrincipal(id);

        doThrow(new ResourceNotFoundException("User", id))
                .when(userService).selfDelete(id);

        mockMvc.perform(delete("/api/v1/users/me").with(user(principal)).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/users/me — deve retornar 401 sem autenticação")
    void shouldReturn401_whenNotAuthenticated_delete() throws Exception {
        mockMvc.perform(delete("/api/v1/users/me").with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
