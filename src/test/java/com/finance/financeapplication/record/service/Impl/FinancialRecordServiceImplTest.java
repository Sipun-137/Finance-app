package com.finance.financeapplication.record.service.Impl;

import com.finance.financeapplication.common.DTO.PagedResponse;
import com.finance.financeapplication.common.enums.RecordType;
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
import com.finance.financeapplication.user.model.User;
import com.finance.financeapplication.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialRecordServiceImplTest {

    @Mock
    private FinancialRecordRepository recordRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private UserService userService;

    @InjectMocks
    private FinancialRecordServiceImpl financialRecordService;

    @Test
    void createRecord_whenValidRequest_shouldSaveAndReturnResponse() {
        String userId = "user-1";
        User user = User.builder().id(userId).name("Test User").email("user@example.com").build();
        Category category = buildCategory("cat-1", "Salary", RecordType.INCOME, "#00AA11");
        CreateRecordRequest request = new CreateRecordRequest(
                new BigDecimal("1200.50"),
                RecordType.INCOME,
                "cat-1",
                "Monthly salary",
                LocalDate.of(2026, 4, 1)
        );

        when(userService.findEntityById(userId)).thenReturn(user);
        when(categoryService.findCategoryEntityById("cat-1")).thenReturn(category);
        when(recordRepository.save(any(FinancialRecord.class))).thenAnswer(invocation -> {
            FinancialRecord saved = invocation.getArgument(0);
            saved.setId("rec-1");
            return saved;
        });

        RecordResponse response = financialRecordService.createRecord(userId, request);

        assertEquals("rec-1", response.getId());
        assertEquals(new BigDecimal("1200.50"), response.getAmount());
        assertEquals("cat-1", response.getCategoryId());
        assertEquals("Salary", response.getCategoryName());
        assertEquals(RecordType.INCOME, response.getType());

        ArgumentCaptor<FinancialRecord> captor = ArgumentCaptor.forClass(FinancialRecord.class);
        verify(recordRepository).save(captor.capture());
        assertEquals(userId, captor.getValue().getUser().getId());
        assertEquals("cat-1", captor.getValue().getCategory().getId());
        assertFalse(captor.getValue().getIsDeleted());
    }

    @Test
    void createRecord_whenCategoryTypeMismatch_shouldThrowBadRequest() {
        String userId = "user-1";
        User user = User.builder().id(userId).build();
        Category expenseCategory = buildCategory("cat-2", "Food", RecordType.EXPENSE, "#FF0000");
        CreateRecordRequest request = new CreateRecordRequest(
                new BigDecimal("100"),
                RecordType.INCOME,
                "cat-2",
                "Wrong type sample",
                LocalDate.now()
        );

        when(userService.findEntityById(userId)).thenReturn(user);
        when(categoryService.findCategoryEntityById("cat-2")).thenReturn(expenseCategory);

        assertThrows(BadRequestException.class, () -> financialRecordService.createRecord(userId, request));
        verify(recordRepository, never()).save(any(FinancialRecord.class));
    }

    @Test
    void findById_whenRecordExists_shouldReturnResponse() {
        FinancialRecord record = buildRecord(
                "rec-1",
                "user-1",
                buildCategory("cat-1", "Salary", RecordType.INCOME, "#00AA11"),
                new BigDecimal("900.00"),
                RecordType.INCOME,
                "April",
                LocalDate.of(2026, 4, 1)
        );

        when(recordRepository.findByIdAndUserId("rec-1", "user-1")).thenReturn(Optional.of(record));

        RecordResponse response = financialRecordService.findById("rec-1", "user-1");

        assertEquals("rec-1", response.getId());
        assertEquals("Salary", response.getCategoryName());
        assertEquals(RecordType.INCOME, response.getType());
    }

    @Test
    void findById_whenRecordMissing_shouldThrowResourceNotFound() {
        when(recordRepository.findByIdAndUserId("missing", "user-1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> financialRecordService.findById("missing", "user-1"));
    }

    @Test
    void findAllRecords_shouldMapPageIntoPagedResponse() {
        RecordFilterRequest filter = new RecordFilterRequest();
        Pageable pageable = PageRequest.of(0, 2);

        FinancialRecord one = buildRecord(
                "rec-1", "user-1",
                buildCategory("cat-1", "Salary", RecordType.INCOME, "#00AA11"),
                new BigDecimal("1200"), RecordType.INCOME,
                "Salary", LocalDate.of(2026, 4, 1)
        );
        FinancialRecord two = buildRecord(
                "rec-2", "user-1",
                buildCategory("cat-2", "Food", RecordType.EXPENSE, "#FF6600"),
                new BigDecimal("300"), RecordType.EXPENSE,
                "Groceries", LocalDate.of(2026, 4, 2)
        );

        Page<FinancialRecord> page = new PageImpl<>(List.of(one, two), pageable, 2);
        when(recordRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        PagedResponse<RecordResponse> response = financialRecordService.findAllRecords("user-1", filter, pageable);

        assertEquals(2, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(2, response.getSize());
        assertEquals(2, response.getTotalElements());
        assertTrue(response.isLast());
    }

    @Test
    void updateRecord_whenRecordMissing_shouldThrowResourceNotFound() {
        when(recordRepository.findByIdAndUserId("rec-404", "user-1")).thenReturn(Optional.empty());

        UpdateRecordRequest request = new UpdateRecordRequest();
        assertThrows(ResourceNotFoundException.class,
                () -> financialRecordService.updateRecord("rec-404", "user-1", request));
    }

    @Test
    void updateRecord_whenCategoryTypeMismatch_shouldThrowBadRequest() {
        FinancialRecord existing = buildRecord(
                "rec-1",
                "user-1",
                buildCategory("cat-old", "Salary", RecordType.INCOME, "#00AA11"),
                new BigDecimal("1500"),
                RecordType.INCOME,
                "Initial",
                LocalDate.of(2026, 4, 1)
        );

        UpdateRecordRequest request = new UpdateRecordRequest(
                null,
                RecordType.INCOME,
                "cat-expense",
                null,
                null
        );

        Category expenseCategory = buildCategory("cat-expense", "Food", RecordType.EXPENSE, "#FF0000");

        when(recordRepository.findByIdAndUserId("rec-1", "user-1")).thenReturn(Optional.of(existing));
        when(categoryService.findCategoryEntityById("cat-expense")).thenReturn(expenseCategory);

        assertThrows(BadRequestException.class,
                () -> financialRecordService.updateRecord("rec-1", "user-1", request));
    }

    @Test
    void updateRecord_whenValidFieldsProvided_shouldUpdateAndReturnResponse() {
        Category oldCategory = buildCategory("cat-old", "Salary", RecordType.INCOME, "#00AA11");
        Category newCategory = buildCategory("cat-new", "Bonus", RecordType.INCOME, "#0088AA");

        FinancialRecord existing = buildRecord(
                "rec-1",
                "user-1",
                oldCategory,
                new BigDecimal("1500"),
                RecordType.INCOME,
                "Initial",
                LocalDate.of(2026, 4, 1)
        );

        UpdateRecordRequest request = new UpdateRecordRequest(
                new BigDecimal("1800"),
                RecordType.INCOME,
                "cat-new",
                "Updated description",
                LocalDate.of(2026, 4, 3)
        );

        when(recordRepository.findByIdAndUserId("rec-1", "user-1")).thenReturn(Optional.of(existing));
        when(categoryService.findCategoryEntityById("cat-new")).thenReturn(newCategory);

        RecordResponse response = financialRecordService.updateRecord("rec-1", "user-1", request);

        assertEquals(new BigDecimal("1800"), response.getAmount());
        assertEquals("cat-new", response.getCategoryId());
        assertEquals("Bonus", response.getCategoryName());
        assertEquals("Updated description", response.getDescription());
        assertEquals(LocalDate.of(2026, 4, 3), response.getRecordDate());
    }

    @Test
    void deleteRecord_whenFound_shouldSoftDelete() {
        FinancialRecord record = buildRecord(
                "rec-1",
                "user-1",
                buildCategory("cat-1", "Food", RecordType.EXPENSE, "#FF6600"),
                new BigDecimal("300"),
                RecordType.EXPENSE,
                "Lunch",
                LocalDate.now()
        );
        record.setIsDeleted(false);

        when(recordRepository.findByIdAndUserId("rec-1", "user-1")).thenReturn(Optional.of(record));

        financialRecordService.deleteRecord("rec-1", "user-1");

        assertTrue(record.getIsDeleted());
    }

    @Test
    void deleteRecord_whenMissing_shouldThrowResourceNotFound() {
        when(recordRepository.findByIdAndUserId("rec-404", "user-1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> financialRecordService.deleteRecord("rec-404", "user-1"));
    }

    @Test
    void findRecentRecords_shouldUseLimitAndMapResponses() {
        FinancialRecord record = buildRecord(
                "rec-1",
                "user-1",
                buildCategory("cat-1", "Food", RecordType.EXPENSE, "#FF6600"),
                new BigDecimal("250"),
                RecordType.EXPENSE,
                "Snacks",
                LocalDate.of(2026, 4, 2)
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(recordRepository.findRecentByUserId(eq("user-1"), any(Pageable.class)))
                .thenReturn(List.of(record));

        List<RecordResponse> result = financialRecordService.findRecentRecords("user-1", 5);

        verify(recordRepository).findRecentByUserId(eq("user-1"), pageableCaptor.capture());
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(5, pageableCaptor.getValue().getPageSize());
        assertEquals(1, result.size());
        assertEquals("rec-1", result.get(0).getId());
    }

    private Category buildCategory(String id, String name, RecordType type, String color) {
        return Category.builder()
                .id(id)
                .name(name)
                .type(type)
                .color(color)
                .build();
    }

    private FinancialRecord buildRecord(
            String recordId,
            String userId,
            Category category,
            BigDecimal amount,
            RecordType type,
            String description,
            LocalDate recordDate
    ) {
        User user = User.builder().id(userId).build();
        return FinancialRecord.builder()
                .id(recordId)
                .user(user)
                .category(category)
                .amount(amount)
                .type(type)
                .description(description)
                .recordDate(recordDate)
                .isDeleted(false)
                .build();
    }
}
