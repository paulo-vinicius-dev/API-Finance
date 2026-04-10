package br.com.pauloviniciusdeveloper.finance.transaction.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
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

import br.com.pauloviniciusdeveloper.finance.common.security.UserPrincipal;
import br.com.pauloviniciusdeveloper.finance.transaction.dto.TransactionRequest;
import br.com.pauloviniciusdeveloper.finance.transaction.dto.TransactionResponse;
import br.com.pauloviniciusdeveloper.finance.transaction.entity.TransactionType;
import br.com.pauloviniciusdeveloper.finance.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction", description = "Endpoints para gerenciamento de transações")
public class TransactionController {
    
    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "Listar transações com filtros opcionais")
    public ResponseEntity<List<TransactionResponse>> findAll(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) UUID categoryId,
        @RequestParam(required = false) TransactionType type,
        @RequestParam(required = false) UUID accountId
    ) {
        UUID userId = currentUser.getId();
        if (startDate == null && endDate == null && categoryId == null && type == null && accountId == null) {
            return ResponseEntity.ok(transactionService.findByUserId(userId));
        }
        return ResponseEntity.ok(transactionService.findByUserIdWithFilters(userId, startDate, endDate, categoryId, type, accountId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @PathVariable UUID id
    ) {
        UUID userId = currentUser.getId();
        return ResponseEntity.ok(transactionService.findByIdAndUserId(id, userId));
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @Valid @RequestBody TransactionRequest transactionRequest
    ) {
        UUID userId = currentUser.getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.create(transactionRequest, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @PathVariable UUID id,
        @Valid @RequestBody TransactionRequest transactionRequest
    ) {
        UUID userId = currentUser.getId();
        return ResponseEntity.ok(transactionService.updateByIdAndUserId(id, userId, transactionRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @PathVariable UUID id
    ) {
        UUID userId = currentUser.getId();
        transactionService.deleteByIdAndUserId(id, userId);
        return ResponseEntity.noContent().build();
    }
}
