package com.finance.financeapplication.record.service.Impl;

import com.finance.financeapplication.common.DTO.PagedResponse;
import com.finance.financeapplication.exception.common.BadRequestException;
import com.finance.financeapplication.exception.common.ResourceNotFoundException;
import com.finance.financeapplication.record.DTO.request.CreateRecordRequest;
import com.finance.financeapplication.record.DTO.request.RecordFilterRequest;
import com.finance.financeapplication.record.DTO.request.UpdateRecordRequest;
import com.finance.financeapplication.record.DTO.response.RecordResponse;
import com.finance.financeapplication.record.model.Category;
import com.finance.financeapplication.record.model.FinancialRecord;
import com.finance.financeapplication.record.repo.FinancialRecordRepository;
import com.finance.financeapplication.record.service.CategoryService;
import com.finance.financeapplication.record.service.FinancialRecordService;
import com.finance.financeapplication.record.specification.RecordSpecification;
import com.finance.financeapplication.user.model.User;
import com.finance.financeapplication.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FinancialRecordServiceImpl implements FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final CategoryService categoryService;
    private final UserService userService;

    @Override
    public RecordResponse createRecord(String userId, CreateRecordRequest request) {
        User user = userService.findEntityById(userId);
        Category category = categoryService.findCategoryEntityById(request.getCategoryId());

        if (!category.getType().equals(request.getType())) {
            throw new BadRequestException(
                    "Record type [" + request.getType() + "] does not match " +
                            "category type [" + category.getType() + "] for category: " + category.getName());
        }
        FinancialRecord record = FinancialRecord.builder()
                .user(user)
                .category(category)
                .amount(request.getAmount())
                .type(request.getType())
                .description(request.getDescription())
                .recordDate(request.getRecordDate())
                .isDeleted(false)
                .build();

        FinancialRecord saved = recordRepository.save(record);
        log.info("Created record [{}] for user [{}] — {} {}",
                saved.getId(), userId, saved.getType(), saved.getAmount());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RecordResponse findById(String recordId, String userId) {
        FinancialRecord record = recordRepository.findByIdAndUserId(recordId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Record not found with id: " + recordId));
        return toResponse(record);
    }

    @Override
    public PagedResponse<RecordResponse> findAllRecords(String userId, RecordFilterRequest filter,Pageable pageable) {

        Page<FinancialRecord> data = recordRepository.findAll(RecordSpecification.filter(filter, userId), pageable);
        return PagedResponse.<RecordResponse>builder()
                .content(data.getContent().stream().map(this::toResponse).toList())
                .page(data.getNumber())
                .size(data.getSize())
                .totalElements(data.getTotalElements())
                .totalPages(data.getTotalPages())
                .last(data.isLast())
                .build();
    }

    @Override
    public RecordResponse updateRecord(String recordId, String userId, UpdateRecordRequest request) {
        FinancialRecord record = recordRepository.findByIdAndUserId(recordId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Record not found with id: " + recordId));

        // Partial update — only change fields that are actually sent (not null)
        if (request.getAmount() != null) {
            record.setAmount(request.getAmount());
        }

        if (request.getType() != null) {
            record.setType(request.getType());
        }

        if (request.getCategoryId() != null) {
            Category newCategory = categoryService.findCategoryEntityById(request.getCategoryId());

            // Re-validate type consistency after category change
            var effectiveType = request.getType() != null ? request.getType() : record.getType();
            if (!newCategory.getType().equals(effectiveType)) {
                throw new BadRequestException(
                        "Record type [" + effectiveType + "] does not match " +
                                "category type [" + newCategory.getType() + "]");
            }

            record.setCategory(newCategory);
        }

        if (request.getDescription() != null) {
            record.setDescription(request.getDescription());
        }

        if (request.getRecordDate() != null) {
            record.setRecordDate(request.getRecordDate());
        }

        // @Transactional handles UPDATE automatically — entity is dirty-checked
        log.info("Updated record [{}] for user [{}]", recordId, userId);
        return toResponse(record);
    }

    @Override
    public void deleteRecord(String recordId, String userId) {
        FinancialRecord record = recordRepository.findByIdAndUserId(recordId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Record not found with id: " + recordId));
        record.setIsDeleted(true);
        log.info("Soft deleted record [{}] for user [{}]", recordId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecordResponse> findRecentRecords(String userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit); // first page, N results
        return recordRepository.findRecentByUserId(userId, pageable)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    //-----------------mapper method--------------------------

    private RecordResponse toResponse(FinancialRecord record) {
        return RecordResponse.builder()
                .id(record.getId())
                .amount(record.getAmount())
                .type(record.getType())
                .categoryId(record.getCategory().getId())
                .categoryName(record.getCategory().getName())   // denormalized — avoids extra call
                .categoryColor(record.getCategory().getColor())
                .description(record.getDescription())
                .recordDate(record.getRecordDate())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
