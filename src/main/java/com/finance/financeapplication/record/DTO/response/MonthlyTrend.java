package com.finance.financeapplication.record.DTO.response;

import com.finance.financeapplication.common.enums.RecordType;

import java.math.BigDecimal;

public interface MonthlyTrend {
    Integer getYear();
    Integer getMonth();
    RecordType getType();
    BigDecimal getTotal();
}
