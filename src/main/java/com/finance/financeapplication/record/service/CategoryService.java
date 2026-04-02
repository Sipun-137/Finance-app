package com.finance.financeapplication.record.service;

import com.finance.financeapplication.common.enums.RecordType;
import com.finance.financeapplication.record.DTO.request.CreateCategoryRequest;
import com.finance.financeapplication.record.DTO.request.UpdateCategoryRequest;
import com.finance.financeapplication.record.DTO.response.CategoryResponse;
import com.finance.financeapplication.record.model.Category;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CreateCategoryRequest request);

    List<CategoryResponse> findAllCategories();

    List<CategoryResponse> findByType(RecordType type);

    CategoryResponse findById(String id);

    CategoryResponse updateCategory(String id, UpdateCategoryRequest request);

    void deleteCategory(String id);

    // Internal use — returns raw entity for FinancialRecordService
    Category findCategoryEntityById(String id);
}
