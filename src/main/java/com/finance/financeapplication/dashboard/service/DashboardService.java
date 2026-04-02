package com.finance.financeapplication.dashboard.service;

import com.finance.financeapplication.dashboard.DTO.response.*;

import java.time.LocalDate;

public interface DashboardService {

    DashboardSummaryResponse getOverview(String userId, LocalDate from, LocalDate to);

    SummaryResponse getSummary(String userId, LocalDate from, LocalDate to);

    CategoryBreakdownResponse getCategoryBreakdown(String userId, LocalDate from, LocalDate to);

    MonthlyTrendResponse getMonthlyTrends(String userId, LocalDate from, LocalDate to);

    RecentActivityResponse getRecentActivity(String userId, int limit);
}

