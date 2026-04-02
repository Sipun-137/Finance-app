package com.finance.financeapplication.dashboard.DTO.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MonthlyTrendResponse {
    private List<MonthlyTrendItem> trends;
    private String from;                   // e.g. "2025-01"
    private String to;                     // e.g. "2025-06"
}
