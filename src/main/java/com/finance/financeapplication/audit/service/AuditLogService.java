package com.finance.financeapplication.audit.service;

import com.finance.financeapplication.audit.DTO.AuditResponseDTO;
import com.finance.financeapplication.audit.model.AuditLog;
import com.finance.financeapplication.audit.repo.AuditLogRepository;
import com.finance.financeapplication.audit.specification.AuditSpecification;
import com.finance.financeapplication.auth.DTO.AuditLogFilterDTO;
import com.finance.financeapplication.common.DTO.PagedResponse;
import com.finance.financeapplication.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(User user, String action, String resource,
                    String resourceId, String meta) {
        AuditLog log = AuditLog.builder()
                .user(user)
                .action(action)
                .resource(resource)
                .resourceId(resourceId)
                .meta(meta)
                .build();

        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditResponseDTO> filter(Pageable pageable, AuditLogFilterDTO filterDTO){

        Page<AuditLog> logs = auditLogRepository.findAll(AuditSpecification.filter(filterDTO),pageable);

        return PagedResponse.<AuditResponseDTO>builder()
                .content(logs.getContent().stream()
                        .map(item-> AuditResponseDTO.builder()
                                .id(item.getId())
                                .userId(item.getUser().getId())
                                .userName(item.getUser().getName())
                                .action(item.getAction())
                                .resource(item.getResource())
                                .resourceId(item.getResourceId())
                                .meta(item.getMeta())
                                .createdAt(item.getCreatedAt())
                                .build())
                        .toList())
                .page(logs.getNumber())
                .size(logs.getSize())
                .totalElements(logs.getTotalElements())
                .totalPages(logs.getTotalPages())
                .last(logs.isLast())
                .build();
    }
}