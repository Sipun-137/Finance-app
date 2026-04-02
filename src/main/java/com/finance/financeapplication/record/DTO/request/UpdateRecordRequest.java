package com.finance.financeapplication.record.DTO.request;

import com.finance.financeapplication.common.enums.RecordType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecordRequest {

    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 13, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    private RecordType type;

    private String categoryId;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @PastOrPresent(message = "Record date cannot be in the future")
    private LocalDate recordDate;

}
