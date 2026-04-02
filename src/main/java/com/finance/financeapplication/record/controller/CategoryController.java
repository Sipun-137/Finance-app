package com.finance.financeapplication.record.controller;


import com.finance.financeapplication.common.DTO.ApiResponse;
import com.finance.financeapplication.common.enums.RecordType;
import com.finance.financeapplication.record.DTO.request.CreateCategoryRequest;
import com.finance.financeapplication.record.DTO.request.UpdateCategoryRequest;
import com.finance.financeapplication.record.DTO.response.CategoryResponse;
import com.finance.financeapplication.record.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
@Tag(name = "Category APIs", description = "Category related APIs")
public class CategoryController {

    private final CategoryService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Create Category", description = "Create a new category.")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        CategoryResponse response = service.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<CategoryResponse>builder()
                        .success(true)
                        .message("Category created successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Get All Categories", description = "Get all categories.")
    public ResponseEntity<ApiResponse<Iterable<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> categories = service.findAllCategories();
        return ResponseEntity.ok(
                ApiResponse.<Iterable<CategoryResponse>>builder()
                        .success(true)
                        .message("Categories retrieved successfully")
                        .data(categories)
                        .build()
        );
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Get Categories by Type", description = "Get categories by type.")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getByType(
            @PathVariable RecordType type
    ) {
        List<CategoryResponse> categories = service.findByType(type);

        return ResponseEntity.ok(
                ApiResponse.<List<CategoryResponse>>builder()
                        .success(true)
                        .message("Categories fetched by type: " + type)
                        .data(categories)
                        .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Get Category by ID", description = "Get a category by ID.")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(
            @PathVariable String id
    ) {
        CategoryResponse category = service.findById(id);

        return ResponseEntity.ok(
                ApiResponse.<CategoryResponse>builder()
                        .success(true)
                        .message("Category retrieved successfully")
                        .data(category)
                        .build()
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Update Category", description = "Update an existing category.")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable String id,
            @RequestBody UpdateCategoryRequest request
    ) {
        CategoryResponse updated = service.updateCategory(id, request);

        return ResponseEntity.ok(
                ApiResponse.<CategoryResponse>builder()
                        .success(true)
                        .message("Category updated successfully")
                        .data(updated)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Delete Category", description = "Delete a category.")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable String id
    ) {
        service.deleteCategory(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Category deleted successfully")
                        .build()
        );
    }


}
