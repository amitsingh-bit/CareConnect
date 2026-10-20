package com.example.CareConnect_hcl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.example.CareConnect_hcl.model.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop200ByOrderByOccurredAtDesc();
}
