package com.finance.financeapplication.record.DTO.request;

import com.finance.financeapplication.common.enums.RecordType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRecordRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 13, fraction = 2, message = "Amount format: up to 13 digits, 2 decimal places")
    private BigDecimal amount;

    @NotNull(message = "Type is required — INCOME or EXPENSE")
    private RecordType type;

    @NotBlank(message = "Category ID is required")
    private String categoryId;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Record date is required")
    @PastOrPresent(message = "Record date cannot be in the future")
    private LocalDate recordDate;
}
