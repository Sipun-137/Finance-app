package com.finance.financeapplication.audit.controller;


import com.finance.financeapplication.audit.DTO.AuditResponseDTO;
import com.finance.financeapplication.audit.service.AuditLogService;
import com.finance.financeapplication.auth.DTO.AuditLogFilterDTO;
import com.finance.financeapplication.common.DTO.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;


    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public PagedResponse<AuditResponseDTO> filterAuditLogs(
            @PageableDefault Pageable pageable,
            AuditLogFilterDTO filterDTO
    ) {
        return auditLogService.filter(pageable, filterDTO);
    }

}
