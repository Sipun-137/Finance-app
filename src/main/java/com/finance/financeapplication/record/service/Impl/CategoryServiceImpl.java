package com.finance.financeapplication.record.service.Impl;

import com.finance.financeapplication.audit.annotation.Auditable;
import com.finance.financeapplication.common.enums.RecordType;
import com.finance.financeapplication.exception.common.BadRequestException;
import com.finance.financeapplication.exception.common.ResourceNotFoundException;
import com.finance.financeapplication.record.DTO.request.CreateCategoryRequest;
import com.finance.financeapplication.record.DTO.request.UpdateCategoryRequest;
import com.finance.financeapplication.record.DTO.response.CategoryResponse;
import com.finance.financeapplication.record.model.Category;
import com.finance.financeapplication.record.repo.CategoryRepository;
import com.finance.financeapplication.record.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Auditable(action = "Create Category", resource = "Category")
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException(
                    "Category already exists with name: " + request.getName());
        }
        Category category = Category.builder()
                .name(request.getName())
                .type(request.getType())
                .color(request.getColor())
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Created category: {} [{}]", saved.getName(), saved.getType());
        return toResponse(saved);
    }

    @Override
    @Auditable(action = "Find All Categories", resource = "Category")
    @Transactional(readOnly = true)
    public List<CategoryResponse> findAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Auditable(action = "Find All Categories By Type", resource = "Category")
    public List<CategoryResponse> findByType(RecordType type) {
        return categoryRepository.findByType(type)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Auditable(action = "Find Category By Id", resource = "Category")
    public CategoryResponse findById(String id) {
        return toResponse(getOrThrow(id));
    }

    @Override
    @Auditable(action = "Update Category", resource = "Category")
    public CategoryResponse updateCategory(String id, UpdateCategoryRequest request) {
        Category category = getOrThrow(id);

        if (request.getName() != null && !request.getName().isBlank()) {
            // Guard: new name must not clash with an existing category
            if (!category.getName().equals(request.getName())
                    && categoryRepository.existsByName(request.getName())) {
                throw new BadRequestException(
                        "Another category already has name: " + request.getName());
            }
            category.setName(request.getName());
        }

        if (request.getColor() != null) {
            category.setColor(request.getColor());
        }

        // @Transactional handles the UPDATE automatically — no explicit save needed
        log.info("Updated category: {}", category.getId());
        return toResponse(category);
    }

    @Override
    @Auditable(action = "Delete Category", resource = "Category")
    public void deleteCategory(String id) {
        try{
            if(categoryRepository.existsById(id)) {
                categoryRepository.deleteById(id);
                log.info("Deleted category: {}", id);
            } else {
                throw new ResourceNotFoundException("Category not found with id: " + id);
            }
        }catch (DataIntegrityViolationException e){
            throw new BadRequestException("Cannot delete category with records");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Category findCategoryEntityById(String id) {
        return getOrThrow(id);
    }


    //-----------------helper methods--------------------------
    private Category getOrThrow(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + id));
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .color(category.getColor())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
