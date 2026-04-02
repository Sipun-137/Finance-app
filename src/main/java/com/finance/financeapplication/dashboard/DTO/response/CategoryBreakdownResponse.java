package com.finance.financeapplication.dashboard.DTO.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CategoryBreakdownResponse {
    private List<CategoryBreakdownItem> incomeBreakdown;
    private List<CategoryBreakdownItem> expenseBreakdown;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
}
