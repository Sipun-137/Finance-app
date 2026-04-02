package com.finance.financeapplication.audit.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditResponseDTO {

    private String id;

    private String userId;
    private String userName;

    private String action;
    private String resource;
    private String resourceId;
    private String meta;
    private LocalDateTime createdAt;

}
