package com.example.CareConnect_hcl.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.CareConnect_hcl.model.entity.Nurse;
import com.example.CareConnect_hcl.config.CurrentAccountService;
import com.example.CareConnect_hcl.repository.NurseRepository;

@Service
public class NurseService {
    private final NurseRepository repository;
    private final CurrentAccountService currentAccount;

    public NurseService(NurseRepository repository, CurrentAccountService currentAccount) { this.repository = repository; this.currentAccount = currentAccount; }
    public List<Nurse> getAll() { currentAccount.requireRole("ADMIN"); return repository.findAll(); }
    public Nurse getById(Long id) { currentAccount.requireRole("ADMIN"); return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nurse not found")); }
    public Nurse create(Nurse nurse) { currentAccount.requireRole("ADMIN"); nurse.setId(null); return repository.save(nurse); }
    public Nurse update(Long id, Nurse details) {
        currentAccount.requireRole("ADMIN");
        Nurse nurse = getById(id);
        nurse.setName(details.getName());
        nurse.setDepartment(details.getDepartment());
        nurse.setPhoneNumber(details.getPhoneNumber());
        return repository.save(nurse);
    }
    public void delete(Long id) { currentAccount.requireRole("ADMIN"); repository.delete(getById(id)); }
}
