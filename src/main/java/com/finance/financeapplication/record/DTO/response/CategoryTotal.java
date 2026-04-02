package com.finance.financeapplication.record.DTO.response;

import com.finance.financeapplication.common.enums.RecordType;

import java.math.BigDecimal;

public interface CategoryTotal {
    String getCategoryName();
    RecordType getType();
    BigDecimal getTotal();
}
