package br.com.pauloviniciusdeveloper.finance.account.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.pauloviniciusdeveloper.finance.account.dto.AccountRequest;
import br.com.pauloviniciusdeveloper.finance.account.dto.AccountResponse;
import br.com.pauloviniciusdeveloper.finance.account.service.AccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;

import br.com.pauloviniciusdeveloper.finance.common.security.UserPrincipal;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Endpoints for managing user accounts")
public class AccountController {
    
    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<List<AccountResponse>> findAll(
        @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        UUID userId = currentUser.getId();
        return ResponseEntity.ok(accountService.findByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> findById(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @PathVariable UUID id
    ) {
        UUID userId = currentUser.getId();
        return ResponseEntity.ok(accountService.findByIdAndUserId(id, userId));
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @Valid @RequestBody AccountRequest accountCreateRequest
    ) {
        UUID userId = currentUser.getId();

        return ResponseEntity.status(HttpStatus.CREATED).body(    
            accountService.create(accountCreateRequest, userId)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> update(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @PathVariable UUID id,
        @Valid @RequestBody AccountRequest accountUpdateRequest
    ) {
        UUID userId = currentUser.getId();
        return ResponseEntity.ok(accountService.updateByIdAndUserId(id, userId, accountUpdateRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @PathVariable UUID id
    ) {
        UUID userId = currentUser.getId();
        accountService.deleteByIdAndUserId(id, userId);
        return ResponseEntity.noContent().build();
    }
}
