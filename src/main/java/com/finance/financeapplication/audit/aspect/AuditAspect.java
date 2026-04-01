package com.finance.financeapplication.audit.aspect;

import com.finance.financeapplication.audit.annotation.Auditable;
import com.finance.financeapplication.audit.service.AuditLogService;
import com.finance.financeapplication.auth.model.UserPrincipal;
import com.finance.financeapplication.user.model.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogService auditLogService;
    private final HttpServletRequest request;
    private final ObjectMapper objectMapper;

    @AfterReturning(
            pointcut = "@annotation(auditable)",
            returning = "result"
    )
    public void auditAfterReturning(JoinPoint joinPoint,
                                    Auditable auditable,
                                    Object result) {
        try {
            User currentUser = getCurrentUser(); // from SecurityContext
            String resourceId = extractResourceId(result);
            String meta = buildMeta(joinPoint, result);

            auditLogService.log(
                    currentUser,
                    auditable.action(),
                    auditable.resource(),
                    resourceId,
                    meta
            );
        } catch (Exception e) {
            // Never let audit failure break the main flow
            log.error("Audit logging failed", e);
        }
    }

    // ── Helpers ────────────────────────────────────────────────

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof UserDetails ud) {
            // Cast to your custom UserDetails that holds the User entity
            return ((UserPrincipal) ud).getUser();
        }
        return null;
    }

    private String extractResourceId(Object result) {
        if (result == null) return null;
        try {
            // Works if your returned DTO/Entity has getId()
            var method = result.getClass().getMethod("getId");
            Object id = method.invoke(result);
            return id != null ? id.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String buildMeta(JoinPoint joinPoint, Object result) {
        try {
            Map<String, Object> meta = new HashMap<>();
            meta.put("ip", request.getRemoteAddr());
            meta.put("method", joinPoint.getSignature().getName());
            return objectMapper.writeValueAsString(meta);
        } catch (Exception e) {
            return null;
        }
    }
}