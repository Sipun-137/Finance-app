package com.finance.financeapplication.auth.DTO;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogFilterDTO {
    private String userId;
    private String action;
    private String resource;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}
