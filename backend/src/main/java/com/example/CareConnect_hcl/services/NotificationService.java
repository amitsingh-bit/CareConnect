package com.example.CareConnect_hcl.services;

import java.util.List;
import org.springframework.stereotype.Service;
import com.example.CareConnect_hcl.config.CurrentAccountService;
import com.example.CareConnect_hcl.model.entity.Notification;
import com.example.CareConnect_hcl.repository.NotificationRepository;

@Service
public class NotificationService {
    private final NotificationRepository repository;
    private final CurrentAccountService currentAccount;

    public NotificationService(NotificationRepository repository, CurrentAccountService currentAccount) {
        this.repository = repository;
        this.currentAccount = currentAccount;
    }

    public List<Notification> getMyNotifications() {
        var account = currentAccount.requireRole("ADMIN", "DOCTOR", "NURSE", "PATIENT");
        return repository.findByAccount_IdOrderByCreatedAtDesc(account.getId());
    }
}
