package com.finance.financeapplication.record.repo;

import com.finance.financeapplication.common.enums.RecordType;
import com.finance.financeapplication.record.DTO.response.CategoryTotal;
import com.finance.financeapplication.record.DTO.response.MonthlyTrend;
import com.finance.financeapplication.record.model.FinancialRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialRecordRepository extends
        JpaRepository<FinancialRecord, String>,
        JpaSpecificationExecutor<FinancialRecord> {

    Optional<FinancialRecord> findByIdAndUserId(String id, String userId);

    // All records for a user — paginated
    Page<FinancialRecord> findByUserId(String userId, Pageable pageable);

    // ── Filtered queries ──────────────────────────────────────────────────────

    // Filter by type (INCOME / EXPENSE) for a user
    Page<FinancialRecord> findByUserIdAndType(String userId, RecordType type, Pageable pageable);

    // Filter by category for a user
    Page<FinancialRecord> findByUserIdAndCategoryId(String userId, String categoryId, Pageable pageable);

    // Filter by date range — used in dashboard date pickers
    Page<FinancialRecord> findByUserIdAndRecordDateBetween(
            String userId, LocalDate from, LocalDate to, Pageable pageable);

    // ── Dashboard aggregate queries ───────────────────────────────────────────

    // Total income for a user — null safe with COALESCE
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r " +
            "WHERE r.user.id = :userId AND r.type = 'INCOME'")
    BigDecimal sumIncomeByUserId(@Param("userId") String userId);

    // Total expense for a user
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r " +
            "WHERE r.user.id = :userId AND r.type = 'EXPENSE'")
    BigDecimal sumExpenseByUserId(@Param("userId") String userId);

    // Total income within a date range — for period summaries
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r " +
            "WHERE r.user.id = :userId AND r.type = 'INCOME' " +
            "AND r.recordDate BETWEEN :from AND :to")
    BigDecimal sumIncomeByUserIdAndDateRange(
            @Param("userId") String userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    // Total expense within a date range
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r " +
            "WHERE r.user.id = :userId AND r.type = 'EXPENSE' " +
            "AND r.recordDate BETWEEN :from AND :to")
    BigDecimal sumExpenseByUserIdAndDateRange(
            @Param("userId") String userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    // Category-wise totals — powers the dashboard breakdown chart
    // Returns a list of CategoryTotal projections: { categoryName, type, total }
    @Query("SELECT r.category.name AS categoryName, r.type AS type, " +
            "SUM(r.amount) AS total " +
            "FROM FinancialRecord r WHERE r.user.id = :userId " +
            "GROUP BY r.category.name, r.type " +
            "ORDER BY total DESC")
    List<CategoryTotal> getCategoryTotals(@Param("userId") String userId);

    // Monthly trend — groups by year + month, returns income and expense per month
    // Used to render the monthly trend line chart on the dashboard
    @Query("SELECT FUNCTION('YEAR', r.recordDate) AS year, " +
            "FUNCTION('MONTH', r.recordDate) AS month, " +
            "r.type AS type, SUM(r.amount) AS total " +
            "FROM FinancialRecord r " +
            "WHERE r.user.id = :userId " +
            "AND r.recordDate BETWEEN :from AND :to " +
            "GROUP BY FUNCTION('YEAR', r.recordDate), FUNCTION('MONTH', r.recordDate), r.type " +
            "ORDER BY FUNCTION('YEAR', r.recordDate) ASC, FUNCTION('MONTH', r.recordDate) ASC")
    List<MonthlyTrend> getMonthlyTrends(
            @Param("userId") String userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

//    // Recent N records for a user — used in the dashboard activity feed
    @Query("SELECT r FROM FinancialRecord r WHERE r.user.id = :userId " +
            "ORDER BY r.recordDate DESC, r.createdAt DESC")
    List<FinancialRecord> findRecentByUserId(@Param("userId") String userId, Pageable pageable);

    // Count all records for a user — useful for pagination metadata
    long countByUserId(String userId);
}
