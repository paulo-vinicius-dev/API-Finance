package br.com.pauloviniciusdeveloper.finance.category.service;

import java.util.List;
import java.util.UUID;

import br.com.pauloviniciusdeveloper.finance.category.dto.CategoryRequest;
import br.com.pauloviniciusdeveloper.finance.category.dto.CategoryResponse;

public interface CategoryService {
    CategoryResponse findByIdAndUserIdOrDefault(UUID id, UUID userId);
    List<CategoryResponse> findByUserId(UUID userId);
    CategoryResponse create(CategoryRequest categoryRequest, UUID userId);
    CategoryResponse updateByIdAndUserId(UUID id, UUID userId, CategoryRequest categoryRequest);
    void deleteByIdAndUserId(UUID id, UUID userId);
}
