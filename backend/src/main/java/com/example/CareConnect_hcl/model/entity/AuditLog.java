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
    @Column(name = "actor_role", length = 20) private String actorRole;
    @Column(length = 50) private String action;
    @Column(name = "resource_type", length = 50) private String resourceType;
    @Column(name = "resource_id", length = 80) private String resourceId;
    @Column(name = "correlation_id", length = 36) private String correlationId;
    @Column(nullable = false, length = 10) private String method;
    @Column(nullable = false, length = 255) private String path;
    @Column(nullable = false) private int status;
    @Column(name = "occurred_at", nullable = false) private Instant occurredAt;

    protected AuditLog() {}
    public AuditLog(Long accountId, String method, String path, int status, Instant occurredAt) {
        this(accountId, null, method, "REQUEST", "API", null, null, path, status, occurredAt);
    }
    public AuditLog(Long accountId, String actorRole, String method, String action, String resourceType,
            String resourceId, String correlationId, String path, int status, Instant occurredAt) {
        this.accountId = accountId; this.actorRole = actorRole; this.method = method; this.action = action;
        this.resourceType = resourceType; this.resourceId = resourceId;
        this.correlationId = correlationId == null ? java.util.UUID.randomUUID().toString() : correlationId;
        this.path = path; this.status = status; this.occurredAt = occurredAt;
    }
    public Long getId() { return id; }
    public Long getAccountId() { return accountId; }
    public String getActorRole() { return actorRole; }
    public String getMethod() { return method; }
    public String getAction() { return action; }
    public String getResourceType() { return resourceType; }
    public String getResourceId() { return resourceId; }
    public String getCorrelationId() { return correlationId; }
    public String getPath() { return path; }
    public int getStatus() { return status; }
    public Instant getOccurredAt() { return occurredAt; }
}
