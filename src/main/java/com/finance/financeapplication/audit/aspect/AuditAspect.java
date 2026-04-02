package com.finance.financeapplication.audit.aspect;

import com.finance.financeapplication.audit.annotation.Auditable;
import com.finance.financeapplication.audit.service.AuditLogService;
import com.finance.financeapplication.auth.model.UserPrincipal;
import com.finance.financeapplication.common.DTO.ApiResponse;
import com.finance.financeapplication.user.model.User;
import com.finance.financeapplication.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
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
    private final UserService userService;

    @AfterReturning(
            pointcut = "@annotation(auditable)",
            returning = "result"
    )
    public void auditAfterReturning(JoinPoint joinPoint,
                                    Auditable auditable,
                                    Object result) {
        try {
            User currentUser;

            if (auditable.preAuth()) {
                currentUser = extractUserFromResult(result);
            } else {
                currentUser = getCurrentUser();
            }

            String resourceId = extractResourceId(result, currentUser, auditable);
            String meta = buildMeta(joinPoint, "SUCCESS", null);

            auditLogService.log(
                    currentUser,
                    auditable.action(),
                    auditable.resource(),
                    resourceId,
                    meta
            );

        } catch (Exception e) {
            // Audit failure must NEVER break the main flow
            log.error("Audit logging failed for action [{}]: {}",
                    auditable.action(), e.getMessage());
        }
    }

    @AfterThrowing(
            pointcut = "@annotation(auditable)",
            throwing = "ex"
    )
    public void auditAfterThrowing(JoinPoint joinPoint,
                                   Auditable auditable,
                                   Exception ex) {
        try {
            User currentUser = auditable.preAuth() ? null : getCurrentUser();
            String meta = buildMeta(joinPoint, "FAILED", ex.getMessage());

            auditLogService.log(
                    currentUser,
                    auditable.action() + "_FAILED",  // e.g. "USER_LOGIN_FAILED"
                    auditable.resource(),
                    null,
                    meta
            );
        } catch (Exception e) {
            log.error("Audit (failure path) logging failed: {}", e.getMessage());
        }
    }


    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getUser();
        }
        return null;
    }


    @SuppressWarnings("unchecked")
    private User extractUserFromResult(Object result) {
        log.info("Extracting user from login result: {}", result);

        Object unwrapped = unwrap(result);
        try {
            if (unwrapped instanceof ApiResponse<?> apiResponse) {
                Object data = apiResponse.getData();
                if (data instanceof Map<?, ?> map) {
                    String email = (String) map.get("email");
                    if (email != null) {
                        return userService.findByEmail(email);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not extract user from login result: {}", e.getMessage());
        }
        return null;
    }

    private Object unwrap(Object result) {
        if (result instanceof ResponseEntity<?> responseEntity) {
            return responseEntity.getBody();
        }
        return result;
    }

    private String extractResourceId(Object result, User currentUser, Auditable auditable) {
        // Strategy 1: unwrap ApiResponse and call getId() on the inner data
        if (result instanceof ApiResponse<?> apiResponse && apiResponse.getData() != null) {
            Object data = apiResponse.getData();
            try {
                var method = data.getClass().getMethod("getId");
                Object id = method.invoke(data);
                if (id != null) return id.toString();
            } catch (Exception ignored) {
            }

            // Inner data is a Map (login response) — try "id" key
            if (data instanceof Map<?, ?> map && map.containsKey("id")) {
                Object id = map.get("id");
                return id != null ? id.toString() : null;
            }
        }

        if (result != null) {
            try {
                var method = result.getClass().getMethod("getId");
                Object id = method.invoke(result);
                if (id != null) return id.toString();
            } catch (Exception ignored) {
            }
        }
        if (currentUser != null) {
            return currentUser.getId().toString();
        }

        return null;
    }


    private String buildMeta(JoinPoint joinPoint, String status, String failureReason) {
        try {
            Map<String, Object> meta = new HashMap<>();
            meta.put("ip", getClientIp());
            meta.put("method", joinPoint.getSignature().getName());
            meta.put("status", status);

            if (failureReason != null) {
                meta.put("reason", failureReason);
            }

            return objectMapper.writeValueAsString(meta);
        } catch (Exception e) {
            return null;
        }
    }


    private String getClientIp() {
        // X-Forwarded-For is set by load balancers/proxies
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim(); // first IP is the real client
        }
        return request.getRemoteAddr();
    }
}