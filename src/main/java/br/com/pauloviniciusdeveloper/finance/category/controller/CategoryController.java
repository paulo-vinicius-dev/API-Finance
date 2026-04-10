package br.com.pauloviniciusdeveloper.finance.category.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.pauloviniciusdeveloper.finance.category.dto.CategoryRequest;
import br.com.pauloviniciusdeveloper.finance.category.dto.CategoryResponse;
import br.com.pauloviniciusdeveloper.finance.category.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.pauloviniciusdeveloper.finance.common.security.UserPrincipal;

import java.util.UUID;



@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "Endpoints para gerenciamento de categorias")
public class CategoryController {
 
    private final CategoryService categoryService;
    
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAll(
        @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        UUID userId = currentUser.getId();
        return ResponseEntity.ok(categoryService.findByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @PathVariable UUID id
    ) {
        UUID userId = currentUser.getId();
        return ResponseEntity.ok(categoryService.findByIdAndUserIdOrDefault(id, userId));
    }

    @PostMapping()
    public ResponseEntity<CategoryResponse> create(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @Valid @RequestBody CategoryRequest entity
    ) {
        UUID userId = currentUser.getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(entity, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @PathVariable UUID id,
        @Valid @RequestBody CategoryRequest categoryRequest
    ) {
        UUID userId = currentUser.getId();
        return ResponseEntity.ok(categoryService.updateByIdAndUserId(id, userId, categoryRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @PathVariable UUID id
    ) {
        UUID userId = currentUser.getId();
        categoryService.deleteByIdAndUserId(id, userId);
        return ResponseEntity.noContent().build();
    }
}
