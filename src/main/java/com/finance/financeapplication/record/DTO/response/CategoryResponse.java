package com.finance.financeapplication.record.DTO.response;


import com.finance.financeapplication.common.enums.RecordType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CategoryResponse {
    private String id;
    private String name;
    private RecordType type;
    private String color;
    private LocalDateTime createdAt;
}
