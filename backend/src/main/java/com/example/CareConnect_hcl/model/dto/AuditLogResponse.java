package com.example.CareConnect_hcl.model.dto;

import java.time.Instant;

public record AuditLogResponse(Long id, Long accountId, String actorRole, String action, String resourceType,
        String resourceId, String method, String path, int status, String correlationId, Instant occurredAt) {}
