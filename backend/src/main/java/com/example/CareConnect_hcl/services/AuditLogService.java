package com.example.CareConnect_hcl.services;

import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.example.CareConnect_hcl.model.entity.AuditLog;
import com.example.CareConnect_hcl.repository.AuditLogRepository;

@Service
public class AuditLogService {
    private final AuditLogRepository repository;
    public AuditLogService(AuditLogRepository repository) { this.repository = repository; }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long accountId, String method, String path, int status) {
        repository.save(new AuditLog(accountId, method, path, status, Instant.now()));
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long accountId, String actorRole, String method, String action, String resourceType,
            String resourceId, String correlationId, String path, int status) {
        repository.save(new AuditLog(accountId, actorRole, method, action, resourceType, resourceId,
                correlationId, path, status, Instant.now()));
    }
}
