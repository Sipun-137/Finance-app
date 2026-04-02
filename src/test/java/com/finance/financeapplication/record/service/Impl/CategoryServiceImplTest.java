package com.finance.financeapplication.record.service.Impl;

import com.finance.financeapplication.common.enums.RecordType;
import com.finance.financeapplication.exception.common.BadRequestException;
import com.finance.financeapplication.exception.common.ResourceNotFoundException;
import com.finance.financeapplication.record.DTO.request.CreateCategoryRequest;
import com.finance.financeapplication.record.DTO.request.UpdateCategoryRequest;
import com.finance.financeapplication.record.DTO.response.CategoryResponse;
import com.finance.financeapplication.record.model.Category;
import com.finance.financeapplication.record.repo.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createCategory_whenValidRequest_shouldSaveAndReturnResponse() {
        CreateCategoryRequest request = new CreateCategoryRequest("Salary", RecordType.INCOME, "#00AA11");

        when(categoryRepository.existsByName("Salary")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId("cat-1");
            return category;
        });

        CategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals("cat-1", response.getId());
        assertEquals("Salary", response.getName());
        assertEquals(RecordType.INCOME, response.getType());
        assertEquals("#00AA11", response.getColor());

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertEquals("Salary", captor.getValue().getName());
        assertEquals(RecordType.INCOME, captor.getValue().getType());
    }

    @Test
    void createCategory_whenNameAlreadyExists_shouldThrowBadRequest() {
        CreateCategoryRequest request = new CreateCategoryRequest("Salary", RecordType.INCOME, "#00AA11");
        when(categoryRepository.existsByName("Salary")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> categoryService.createCategory(request));

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_whenDuplicateName_shouldThrowBadRequest() {
        Category existing = Category.builder()
                .id("cat-1")
                .name("Food")
                .type(RecordType.EXPENSE)
                .color("#AA0000")
                .build();

        UpdateCategoryRequest request = new UpdateCategoryRequest("Travel", "#112233");

        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByName("Travel")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> categoryService.updateCategory("cat-1", request));
    }

    @Test
    void updateCategory_whenValidRequest_shouldUpdateAndReturnResponse() {
        Category existing = Category.builder()
                .id("cat-1")
                .name("Food")
                .type(RecordType.EXPENSE)
                .color("#AA0000")
                .build();

        UpdateCategoryRequest request = new UpdateCategoryRequest("Groceries", "#22BB44");

        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByName("Groceries")).thenReturn(false);

        CategoryResponse response = categoryService.updateCategory("cat-1", request);

        assertEquals("cat-1", response.getId());
        assertEquals("Groceries", response.getName());
        assertEquals("#22BB44", response.getColor());
    }

    @Test
    void deleteCategory_whenCategoryNotFound_shouldThrowResourceNotFound() {
        when(categoryRepository.existsById("cat-404")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory("cat-404"));
    }

    @Test
    void deleteCategory_whenDataIntegrityViolation_shouldThrowBadRequest() {
        when(categoryRepository.existsById("cat-1")).thenReturn(true);
        doThrow(new DataIntegrityViolationException("fk violation"))
                .when(categoryRepository).deleteById("cat-1");

        assertThrows(BadRequestException.class, () -> categoryService.deleteCategory("cat-1"));
    }

    @Test
    void findByType_shouldMapEntitiesToResponse() {
        Category income = Category.builder()
                .id("cat-1")
                .name("Salary")
                .type(RecordType.INCOME)
                .color("#00AA11")
                .build();

        when(categoryRepository.findByType(RecordType.INCOME)).thenReturn(List.of(income));

        List<CategoryResponse> result = categoryService.findByType(RecordType.INCOME);

        assertEquals(1, result.size());
        assertEquals("Salary", result.getFirst().getName());
        assertEquals(RecordType.INCOME, result.getFirst().getType());
    }
}
