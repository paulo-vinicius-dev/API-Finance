package br.com.pauloviniciusdeveloper.finance.recurring.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.pauloviniciusdeveloper.finance.common.security.UserPrincipal;
import br.com.pauloviniciusdeveloper.finance.recurring.dto.RecurringRequest;
import br.com.pauloviniciusdeveloper.finance.recurring.dto.RecurringResponse;
import br.com.pauloviniciusdeveloper.finance.recurring.service.RecurringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/recurring")
@RequiredArgsConstructor
@Tag(name = "Recurring Transactions", description = "Movimentações recorrentes")
public class RecurringController {

    private final RecurringService recurringService;

    @GetMapping
    @Operation(summary = "Listar regras de movimentações recorrentes")
    public ResponseEntity<List<RecurringResponse>> findAll(
        @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(recurringService.findByUserId(currentUser.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar movimentação recorrente por ID")
    public ResponseEntity<RecurringResponse> findById(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(recurringService.findByIdAndUserId(id, currentUser.getId()));
    }

    @PostMapping
    @Operation(summary = "Criar regra de movimentação recorrente")
    public ResponseEntity<RecurringResponse> create(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @Valid @RequestBody RecurringRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recurringService.create(request, currentUser.getId()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar regra de movimentação recorrente")
    public ResponseEntity<RecurringResponse> update(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @PathVariable UUID id,
        @Valid @RequestBody RecurringRequest request
    ) {
        return ResponseEntity.ok(recurringService.update(id, currentUser.getId(), request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir movimentação recorrente")
    public ResponseEntity<Void> delete(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @PathVariable UUID id
    ) {
        recurringService.delete(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
