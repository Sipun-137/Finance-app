package com.finance.financeapplication.audit.DTO;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogFilterDTO {
    private String userId;
    private String action;
    private String resource;
    private LocalDate fromDate;
    private LocalDate toDate;
}
