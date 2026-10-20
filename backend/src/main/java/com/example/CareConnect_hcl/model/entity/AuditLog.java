package com.example.CareConnect_hcl.model.entity;

import java.time.Instant;
import jakarta.persistence.*;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_account_time", columnList = "account_id, occurred_at"),
        @Index(name = "idx_audit_time", columnList = "occurred_at")
})
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "account_id") private Long accountId;
    @Column(nullable = false, length = 10) private String method;
    @Column(nullable = false, length = 255) private String path;
    @Column(nullable = false) private int status;
    @Column(name = "occurred_at", nullable = false) private Instant occurredAt;

    protected AuditLog() {}
    public AuditLog(Long accountId, String method, String path, int status, Instant occurredAt) {
        this.accountId = accountId; this.method = method; this.path = path; this.status = status; this.occurredAt = occurredAt;
    }
    public Long getId() { return id; }
}
