package br.com.pauloviniciusdeveloper.finance.category.mapper;

import br.com.pauloviniciusdeveloper.finance.category.dto.CategoryResponse;
import br.com.pauloviniciusdeveloper.finance.category.dto.CategoryRequest;
import br.com.pauloviniciusdeveloper.finance.category.entity.Category;

public class CategoryMapper {
    
    public static CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
            .id(category.getId())
            .name(category.getName())
            .isDefault(category.isDefault())
            .build();
        }

    public static Category toEntity(CategoryRequest categoryRequest) {
        return Category.builder()
            .name(categoryRequest.name())
            .build();
    }

    public static Category toEntity(CategoryResponse categoryResponse) {
        return Category.builder()
            .id(categoryResponse.id())
            .name(categoryResponse.name())
            .isDefault(categoryResponse.isDefault())
            .build();
    }
}
