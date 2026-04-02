package com.finance.financeapplication.record.DTO.response;

import com.finance.financeapplication.common.enums.RecordType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class RecordResponse {
    private String id;
    private BigDecimal amount;
    private RecordType type;
    private String categoryId;
    private String categoryName;
    private String categoryColor;
    private String description;
    private LocalDate recordDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
