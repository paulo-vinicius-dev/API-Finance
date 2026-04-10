package br.com.pauloviniciusdeveloper.finance.budget.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.pauloviniciusdeveloper.finance.budget.dto.BudgetRequest;
import br.com.pauloviniciusdeveloper.finance.budget.dto.BudgetResponse;
import br.com.pauloviniciusdeveloper.finance.budget.service.BudgetService;
import br.com.pauloviniciusdeveloper.finance.common.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
@Tag(name = "Budgets", description = "Orçamentos por categoria")
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    @Operation(summary = "Listar orçamentos, com filtro opcional por mês e ano")
    public ResponseEntity<List<BudgetResponse>> findAll(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) Integer year
    ) {
        return ResponseEntity.ok(budgetService.findByUserId(currentUser.getId(), month, year));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar orçamento por ID")
    public ResponseEntity<BudgetResponse> findById(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(budgetService.findByIdAndUserId(id, currentUser.getId()));
    }

    @PostMapping
    @Operation(summary = "Criar orçamento para uma categoria e período")
    public ResponseEntity<BudgetResponse> create(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @Valid @RequestBody BudgetRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetService.create(request, currentUser.getId()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar orçamento")
    public ResponseEntity<BudgetResponse> update(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @PathVariable UUID id,
        @Valid @RequestBody BudgetRequest request
    ) {
        return ResponseEntity.ok(budgetService.update(id, currentUser.getId(), request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir orçamento")
    public ResponseEntity<Void> delete(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @PathVariable UUID id
    ) {
        budgetService.delete(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
