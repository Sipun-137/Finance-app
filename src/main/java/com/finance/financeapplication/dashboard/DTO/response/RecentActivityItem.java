package com.finance.financeapplication.dashboard.DTO.response;

import com.finance.financeapplication.common.enums.RecordType;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
public class RecentActivityItem {
    private String id;
    private String categoryName;
    private String categoryColor;
    private RecordType type;
    private BigDecimal amount;
    private String description;
    private String recordDate;             // formatted as "dd MMM yyyy"
}