package com.finance.financeapplication.audit.model;

import com.finance.financeapplication.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // e.g. "CREATE_RECORD", "DELETE_USER", "LOGIN"
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    // e.g. "financial_records", "users"
    @Column(name = "resource", length = 50)
    private String resource;

    // UUID of the affected row, if applicable
    @Column(name = "resource_id")
    private String resourceId;

    // Extra context: IP address, old value, etc. stored as JSON string
    @Column(name = "meta", columnDefinition = "TEXT")
    private String meta;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
