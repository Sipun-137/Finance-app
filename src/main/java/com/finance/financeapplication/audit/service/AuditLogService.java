package com.finance.financeapplication.audit.service;

import com.finance.financeapplication.audit.model.AuditLog;
import com.finance.financeapplication.audit.repo.AuditLogRepository;
import com.finance.financeapplication.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}