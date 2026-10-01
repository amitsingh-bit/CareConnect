package com.example.CareConnect_hcl.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.CareConnect_hcl.model.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByAccount_IdOrderByCreatedAtDesc(Long accountId);
}
