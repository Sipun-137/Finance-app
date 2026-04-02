package com.finance.financeapplication.record.DTO.request;

import com.finance.financeapplication.common.enums.RecordType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RecordFilterRequest {

    private RecordType type;
    private String categoryId;
    private LocalDate from;
    private LocalDate to;
    private String sortBy = "recordDate";
    private String sortDir = "desc";
}
