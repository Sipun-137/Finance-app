package com.finance.financeapplication.dashboard.service.Impl;

import com.finance.financeapplication.common.enums.RecordType;
import com.finance.financeapplication.dashboard.DTO.mapper.DashboardMapper;
import com.finance.financeapplication.dashboard.DTO.response.*;
import com.finance.financeapplication.dashboard.service.DashboardService;
import com.finance.financeapplication.record.DTO.response.CategoryTotal;
import com.finance.financeapplication.record.DTO.response.MonthlyTrend;
import com.finance.financeapplication.record.model.FinancialRecord;
import com.finance.financeapplication.record.repo.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final FinancialRecordRepository recordRepository;
    private final DashboardMapper dashboardMapper;

    private static final DateTimeFormatter PERIOD_FORMAT =
            DateTimeFormatter.ofPattern("MMM yyyy");

    private static final DateTimeFormatter MONTH_LABEL_FORMAT =
            DateTimeFormatter.ofPattern("MMM yyyy");

    private static final int DEFAULT_RECENT_LIMIT = 10;

    @Override
    public DashboardSummaryResponse getOverview(String userId, LocalDate from, LocalDate to) {

        log.debug("Dashboard overview — user [{}] {} to {}", userId, from, to);

        LocalDate resolvedFrom = resolveFrom(from);
        LocalDate resolvedTo   = resolveTo(to);

        BigDecimal totalIncome   = getIncome(userId, resolvedFrom, resolvedTo);
        BigDecimal totalExpense  = getExpense(userId, resolvedFrom, resolvedTo);
        long totalRecords        = recordRepository.countByUserId(userId);

        List<CategoryTotal>   categoryTotals = recordRepository.getCategoryTotals(userId);
        List<Object[]>        rawTrends      = recordRepository.getMonthlyTrends(userId, resolvedFrom, resolvedTo);
        List<FinancialRecord> recentRecords  = recordRepository
                .findRecentByUserId(userId, PageRequest.of(0, DEFAULT_RECENT_LIMIT));

        return DashboardSummaryResponse.builder()
                .summary(dashboardMapper.toSummaryResponse(
                        totalIncome, totalExpense, totalRecords,
                        buildPeriodLabel(resolvedFrom, resolvedTo)))
                .categoryBreakdown(dashboardMapper.toCategoryBreakdown(
                        categoryTotals, totalIncome, totalExpense))
                .monthlyTrend(mapMonthlyTrends(rawTrends, resolvedFrom, resolvedTo))
                .recentActivity(dashboardMapper.toRecentActivityResponse(recentRecords))
                .build();
    }

    @Override
    public SummaryResponse getSummary(String userId, LocalDate from, LocalDate to) {
        LocalDate resolvedFrom = resolveFrom(from);
        LocalDate resolvedTo = resolveTo(to);

        BigDecimal totalIncome = getIncome(userId, resolvedFrom, resolvedTo);
        BigDecimal totalExpense = getExpense(userId, resolvedFrom, resolvedTo);
        long totalRecords = recordRepository.countByUserId(userId);

        log.debug("Summary for user [{}] — income: {}, expense: {}", userId, totalIncome, totalExpense);

        return dashboardMapper.toSummaryResponse(
                totalIncome,
                totalExpense,
                totalRecords,
                buildPeriodLabel(resolvedFrom, resolvedTo)
        );
    }

    @Override
    public CategoryBreakdownResponse getCategoryBreakdown(String userId, LocalDate from, LocalDate to) {
        LocalDate resolvedFrom = resolveFrom(from);
        LocalDate resolvedTo = resolveTo(to);

        // Fetch totals and income/expense separately for percentage calculation
        List<CategoryTotal> totals = recordRepository.getCategoryTotals(userId);
        BigDecimal totalIncome = getIncome(userId, resolvedFrom, resolvedTo);
        BigDecimal totalExpense = getExpense(userId, resolvedFrom, resolvedTo);

        return dashboardMapper.toCategoryBreakdown(totals, totalIncome, totalExpense);
    }

    @Override
    public MonthlyTrendResponse getMonthlyTrends(String userId, LocalDate from, LocalDate to) {
        LocalDate resolvedFrom = from != null ? from : LocalDate.now().minusMonths(6).withDayOfMonth(1);
        LocalDate resolvedTo   = to   != null ? to   : LocalDate.now();

        List<Object[]> rawTrends = recordRepository.getMonthlyTrends(userId, resolvedFrom, resolvedTo);
        return mapMonthlyTrends(rawTrends, resolvedFrom, resolvedTo);
    }

    @Override
    public RecentActivityResponse getRecentActivity(String userId, int limit) {
        int safeLimit = Math.min(limit, 50);
        List<FinancialRecord> records = recordRepository
                .findRecentByUserId(userId, PageRequest.of(0, safeLimit));

        return dashboardMapper.toRecentActivityResponse(records);
    }


    private BigDecimal getIncome(String userId, LocalDate from, LocalDate to) {
        return (from != null && to != null)
                ? recordRepository.sumIncomeByUserIdAndDateRange(userId, from, to)
                : recordRepository.sumIncomeByUserId(userId);
    }

    private BigDecimal getExpense(String userId, LocalDate from, LocalDate to) {
        return (from != null && to != null)
                ? recordRepository.sumExpenseByUserIdAndDateRange(userId, from, to)
                : recordRepository.sumExpenseByUserId(userId);
    }

    private LocalDate resolveFrom(LocalDate from) {
        return from != null ? from : LocalDate.now().withDayOfMonth(1);
    }

    private LocalDate resolveTo(LocalDate to) {
        return to != null ? to : LocalDate.now();
    }

    private String buildPeriodLabel(LocalDate from, LocalDate to) {
        return from.format(PERIOD_FORMAT) + " – " + to.format(PERIOD_FORMAT);
    }


    private MonthlyTrendResponse mapMonthlyTrends(List<Object[]> rows,
                                                  LocalDate from,
                                                  LocalDate to) {
        // LinkedHashMap preserves insertion order — months stay chronological
        Map<String, MonthlyTrendItem.MonthlyTrendItemBuilder> grouped = new LinkedHashMap<>();

        for (Object[] row : rows) {
            int year   = ((Number) row[0]).intValue();
            int month  = ((Number) row[1]).intValue();
            RecordType type  = RecordType.valueOf((String) row[2]);
            BigDecimal total = (BigDecimal) row[3];

            String key = year + "-" + String.format("%02d", month);

            // Initialize this month's entry with zeros if not yet seen
            grouped.putIfAbsent(key, MonthlyTrendItem.builder()
                    .year(year)
                    .month(month)
                    .monthLabel(YearMonth.of(year, month)
                            .atDay(1).format(MONTH_LABEL_FORMAT))
                    .income(BigDecimal.ZERO)
                    .expense(BigDecimal.ZERO));

            // Set income or expense depending on this row's type
            MonthlyTrendItem.MonthlyTrendItemBuilder builder = grouped.get(key);
            if (type == RecordType.INCOME) {
                builder.income(total);
            } else {
                builder.expense(total);
            }
        }

        // Finalize each month — calculate net = income - expense
        List<MonthlyTrendItem> items = new ArrayList<>();
        for (MonthlyTrendItem.MonthlyTrendItemBuilder b : grouped.values()) {
            MonthlyTrendItem item = b.build();
            items.add(MonthlyTrendItem.builder()
                    .year(item.getYear())
                    .month(item.getMonth())
                    .monthLabel(item.getMonthLabel())
                    .income(item.getIncome())
                    .expense(item.getExpense())
                    .net(item.getIncome().subtract(item.getExpense()))
                    .build());
        }

        return MonthlyTrendResponse.builder()
                .trends(items)
                .from(from.toString())
                .to(to.toString())
                .build();
    }
}
