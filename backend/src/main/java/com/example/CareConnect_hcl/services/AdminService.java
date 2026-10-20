package com.example.CareConnect_hcl.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.CareConnect_hcl.model.entity.Admin;
import com.example.CareConnect_hcl.model.dto.AccountResponse;
import com.example.CareConnect_hcl.config.CurrentAccountService;
import com.example.CareConnect_hcl.repository.AdminRepository;
import com.example.CareConnect_hcl.repository.AccountRepository;
import com.example.CareConnect_hcl.repository.AuditLogRepository;
import com.example.CareConnect_hcl.model.dto.AuditLogResponse;

@Service
public class AdminService {
    private final AdminRepository repository;
    private final CurrentAccountService currentAccount;
    private final AccountRepository accounts;
    private final AuditLogRepository auditLogs;

    public AdminService(AdminRepository repository, CurrentAccountService currentAccount, AccountRepository accounts, AuditLogRepository auditLogs) { this.repository = repository; this.currentAccount = currentAccount; this.accounts = accounts; this.auditLogs = auditLogs; }
    public List<Admin> getAll() { currentAccount.requireRole("ADMIN"); return repository.findAll(); }
    public List<AccountResponse> getUsers() {
        currentAccount.requireRole("ADMIN");
        return accounts.findAll().stream().map(account -> new AccountResponse(account.getId(), account.getProfileId(),
                account.getDisplayName(), account.getEmail(), account.getRole())).toList();
    }
    public List<AuditLogResponse> getAuditLogs() {
        currentAccount.requireRole("ADMIN");
        return auditLogs.findTop200ByOrderByOccurredAtDesc().stream().map(log -> new AuditLogResponse(log.getId(),
                log.getAccountId(), log.getActorRole(), log.getAction(), log.getResourceType(), log.getResourceId(),
                log.getMethod(), log.getPath(), log.getStatus(), log.getCorrelationId(), log.getOccurredAt())).toList();
    }
    public Admin getById(Long id) { currentAccount.requireRole("ADMIN"); return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found")); }
    public Admin create(Admin admin) { currentAccount.requireRole("ADMIN"); admin.setId(null); return repository.save(admin); }
    public Admin update(Long id, Admin details) {
        currentAccount.requireRole("ADMIN");
        Admin admin = getById(id);
        admin.setName(details.getName());
        admin.setEmail(details.getEmail());
        return repository.save(admin);
    }
    public void delete(Long id) { currentAccount.requireRole("ADMIN"); repository.delete(getById(id)); }
}
