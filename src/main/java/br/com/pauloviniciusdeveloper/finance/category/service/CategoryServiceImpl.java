package br.com.pauloviniciusdeveloper.finance.category.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.pauloviniciusdeveloper.finance.budget.repository.BudgetRepository;
import br.com.pauloviniciusdeveloper.finance.category.dto.CategoryRequest;
import br.com.pauloviniciusdeveloper.finance.category.dto.CategoryResponse;
import br.com.pauloviniciusdeveloper.finance.category.entity.Category;
import br.com.pauloviniciusdeveloper.finance.category.mapper.CategoryMapper;
import br.com.pauloviniciusdeveloper.finance.category.repository.CategoryRepository;
import br.com.pauloviniciusdeveloper.finance.common.exception.ResourceNotFoundException;
import br.com.pauloviniciusdeveloper.finance.recurring.repository.RecurringRepository;
import br.com.pauloviniciusdeveloper.finance.transaction.repository.TransactionRepository;
import br.com.pauloviniciusdeveloper.finance.user.entity.User;
import br.com.pauloviniciusdeveloper.finance.user.mapper.UserMapper;
import br.com.pauloviniciusdeveloper.finance.user.service.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final RecurringRepository recurringRepository;
    private final BudgetRepository budgetRepository;
    private final UserService userService;

    @Override
    public CategoryResponse findByIdAndUserIdOrDefault(UUID id, UUID userId) {
        Category category = categoryRepository.findByIdAndUserIdOrDefault(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        return CategoryMapper.toResponse(category);
    }

    @Override
    public List<CategoryResponse> findByUserId(UUID userId) {
        return categoryRepository.findByUserIdOrDefault(userId).stream()
            .map(CategoryMapper::toResponse)
            .toList();
    }

    @Override
    public CategoryResponse create(CategoryRequest categoryRequest, UUID userId) {
        User user = UserMapper.toEntity(userService.findById(userId));
        Category category = CategoryMapper.toEntity(categoryRequest);
        category.setUser(user);
        Category savedCategory = categoryRepository.save(category);
        return CategoryMapper.toResponse(savedCategory);
    }

    @Override
    public CategoryResponse updateByIdAndUserId(UUID id, UUID userId, CategoryRequest categoryRequest) {
        Category existingCategory = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        if (existingCategory.isDefault()) {
            throw new IllegalArgumentException("Default categories cannot be updated");
        }

        Category category = categoryRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        category.setName(categoryRequest.name());
        return CategoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteByIdAndUserId(UUID id, UUID userId) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        if (category.isDefault()) {
            throw new IllegalArgumentException("Default categories cannot be deleted");
        }

        categoryRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        transactionRepository.deleteByCategoryId(id);
        recurringRepository.deleteByCategoryId(id);
        budgetRepository.deleteByCategoryId(id);
        categoryRepository.deleteById(id);
    }
}
