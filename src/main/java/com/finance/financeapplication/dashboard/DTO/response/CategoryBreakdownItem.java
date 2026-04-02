package com.finance.financeapplication.dashboard.DTO.response;

import com.finance.financeapplication.common.enums.RecordType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CategoryBreakdownItem {
    private String categoryName;
    private String categoryColor;
    private RecordType type;               // INCOME or EXPENSE
    private BigDecimal total;
    private double percentage;             // share of total income or total expense
}
