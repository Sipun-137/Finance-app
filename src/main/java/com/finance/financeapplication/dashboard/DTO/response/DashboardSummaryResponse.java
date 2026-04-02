package com.finance.financeapplication.dashboard.DTO.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardSummaryResponse {
    private SummaryResponse summary;
    private CategoryBreakdownResponse categoryBreakdown;
    private MonthlyTrendResponse monthlyTrend;
    private RecentActivityResponse recentActivity;
}
