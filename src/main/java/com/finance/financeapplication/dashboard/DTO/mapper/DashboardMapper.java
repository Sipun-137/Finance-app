package com.finance.financeapplication.dashboard.DTO.mapper;

import com.finance.financeapplication.common.enums.RecordType;
import com.finance.financeapplication.dashboard.DTO.response.*;
import com.finance.financeapplication.record.DTO.response.CategoryTotal;
import com.finance.financeapplication.record.DTO.response.MonthlyTrend;
import com.finance.financeapplication.record.model.FinancialRecord;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DashboardMapper {
    private static final DateTimeFormatter RECORD_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    private static final DateTimeFormatter MONTH_LABEL_FORMAT =
            DateTimeFormatter.ofPattern("MMM yyyy");

    public SummaryResponse toSummaryResponse(BigDecimal totalIncome,
                                             BigDecimal totalExpense,
                                             long totalRecords,
                                             String periodLabel) {
        BigDecimal net = totalIncome.subtract(totalExpense);
        return SummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netBalance(net)
                .totalRecords(totalRecords)
                .periodLabel(periodLabel)
                .build();
    }

    public CategoryBreakdownResponse toCategoryBreakdown(List<CategoryTotal> totals,
                                                         BigDecimal totalIncome,
                                                         BigDecimal totalExpense) {
        List<CategoryBreakdownItem> incomeItems = totals.stream()
                .filter(t -> t.getType() == RecordType.INCOME)
                .map(t -> toCategoryBreakdownItem(t, totalIncome))
                .collect(Collectors.toList());

        List<CategoryBreakdownItem> expenseItems = totals.stream()
                .filter(t -> t.getType() == RecordType.EXPENSE)
                .map(t -> toCategoryBreakdownItem(t, totalExpense))
                .collect(Collectors.toList());

        return CategoryBreakdownResponse.builder()
                .incomeBreakdown(incomeItems)
                .expenseBreakdown(expenseItems)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .build();
    }

    private CategoryBreakdownItem toCategoryBreakdownItem(CategoryTotal total,
                                                          BigDecimal groupTotal) {
        double percentage = 0.0;
        if (groupTotal.compareTo(BigDecimal.ZERO) > 0) {
            percentage = total.getTotal()
                    .divide(groupTotal, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        return CategoryBreakdownItem.builder()
                .categoryName(total.getCategoryName())
                .type(total.getType())
                .total(total.getTotal())
                .percentage(Math.round(percentage * 100.0) / 100.0) //
                .build();
    }

    public MonthlyTrendResponse toMonthlyTrendResponse(List<MonthlyTrend> trends,
                                                       String from,
                                                       String to) {

        Map<String, MonthlyTrendItem.MonthlyTrendItemBuilder> grouped =
                new java.util.LinkedHashMap<>();

        for (MonthlyTrend trend : trends) {
            String key = trend.getYear() + "-" + String.format("%02d", trend.getMonth());

            grouped.putIfAbsent(key, MonthlyTrendItem.builder()
                    .year(trend.getYear())
                    .month(trend.getMonth())
                    .monthLabel(buildMonthLabel(trend.getYear(), trend.getMonth()))
                    .income(BigDecimal.ZERO)
                    .expense(BigDecimal.ZERO));

            MonthlyTrendItem.MonthlyTrendItemBuilder builder = grouped.get(key);

            if (trend.getType() == RecordType.INCOME) {
                builder.income(trend.getTotal());
            } else {
                builder.expense(trend.getTotal());
            }
        }


        List<MonthlyTrendItem> items = grouped.values().stream()
                .map(builder -> {
                    MonthlyTrendItem item = builder.build();

                    return MonthlyTrendItem.builder()
                            .year(item.getYear())
                            .month(item.getMonth())
                            .monthLabel(item.getMonthLabel())
                            .income(item.getIncome())
                            .expense(item.getExpense())
                            .net(item.getIncome().subtract(item.getExpense()))
                            .build();
                })
                .collect(Collectors.toList());

        return MonthlyTrendResponse.builder()
                .trends(items)
                .from(from)
                .to(to)
                .build();
    }

    private String buildMonthLabel(int year, int month) {
        return java.time.YearMonth.of(year, month)
                .atDay(1)
                .format(MONTH_LABEL_FORMAT);
    }

    public RecentActivityResponse toRecentActivityResponse(List<FinancialRecord> records) {
        List<RecentActivityItem> activities = records.stream()
                .map(this::toRecentActivityItem)
                .collect(Collectors.toList());

        return RecentActivityResponse.builder()
                .activities(activities)
                .count(activities.size())
                .build();
    }

    private RecentActivityItem toRecentActivityItem(FinancialRecord record) {
        return RecentActivityItem.builder()
                .id(record.getId())
                .categoryName(record.getCategory().getName())
                .categoryColor(record.getCategory().getColor())
                .type(record.getType())
                .amount(record.getAmount())
                .description(record.getDescription())
                .recordDate(record.getRecordDate().format(RECORD_DATE_FORMAT))
                .build();
    }
}
