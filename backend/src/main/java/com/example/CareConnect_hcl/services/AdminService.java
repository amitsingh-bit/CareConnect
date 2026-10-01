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

@Service
public class AdminService {
    private final AdminRepository repository;
    private final CurrentAccountService currentAccount;
    private final AccountRepository accounts;

    public AdminService(AdminRepository repository, CurrentAccountService currentAccount, AccountRepository accounts) { this.repository = repository; this.currentAccount = currentAccount; this.accounts = accounts; }
    public List<Admin> getAll() { currentAccount.requireRole("ADMIN"); return repository.findAll(); }
    public List<AccountResponse> getUsers() {
        currentAccount.requireRole("ADMIN");
        return accounts.findAll().stream().map(account -> new AccountResponse(account.getId(), account.getProfileId(),
                account.getDisplayName(), account.getEmail(), account.getRole())).toList();
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
