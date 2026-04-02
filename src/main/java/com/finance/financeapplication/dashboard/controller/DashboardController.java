package com.finance.financeapplication.dashboard.controller;

import com.finance.financeapplication.audit.annotation.Auditable;
import com.finance.financeapplication.auth.model.UserPrincipal;
import com.finance.financeapplication.common.DTO.ApiResponse;
import com.finance.financeapplication.dashboard.DTO.response.*;
import com.finance.financeapplication.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    private String getUserId(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId();
    }

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
    @Auditable(action = "VIEW_DASHBOARD_OVERVIEW", resource = "dashboard")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getOverview(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        DashboardSummaryResponse overview = dashboardService.getOverview(
                getUserId(authentication), from, to);

        return ResponseEntity.ok(ApiResponse.success("Dashboard overview fetched", overview));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
    @Auditable(action = "VIEW_DASHBOARD_SUMMARY", resource = "dashboard")
    public ResponseEntity<ApiResponse<SummaryResponse>> getSummary(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        SummaryResponse summary = dashboardService.getSummary(getUserId(authentication), from, to);
        return ResponseEntity.ok(ApiResponse.success("Summary fetched", summary));
    }

    @GetMapping("/category-breakdown")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Auditable(action = "VIEW_CATEGORY_BREAKDOWN", resource = "dashboard")
    public ResponseEntity<ApiResponse<CategoryBreakdownResponse>> getCategoryBreakdown(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        CategoryBreakdownResponse breakdown = dashboardService
                .getCategoryBreakdown(getUserId(authentication), from, to);

        return ResponseEntity.ok(ApiResponse.success("Category breakdown fetched", breakdown));
    }

    @GetMapping("/monthly-trends")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Auditable(action = "VIEW_MONTHLY_TREND", resource = "dashboard")
    public ResponseEntity<ApiResponse<MonthlyTrendResponse>> getMonthlyTrends(
           Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        MonthlyTrendResponse trends = dashboardService
                .getMonthlyTrends(getUserId(authentication), from, to);

        return ResponseEntity.ok(ApiResponse.success("Monthly trends fetched", trends));
    }

    @GetMapping("/recent-activity")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
    @Auditable(action = "VIEW_RECENT_ACTIVITY", resource = "dashboard")
    public ResponseEntity<ApiResponse<RecentActivityResponse>> getRecentActivity(
            Authentication authentication,
            @RequestParam(defaultValue = "10") int limit) {

        RecentActivityResponse activity = dashboardService
                .getRecentActivity(getUserId(authentication), limit);

        return ResponseEntity.ok(ApiResponse.success("Recent activity fetched", activity));
    }
}
