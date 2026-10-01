package com.example.CareConnect_hcl.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.CareConnect_hcl.model.entity.Medication;
import com.example.CareConnect_hcl.config.CurrentAccountService;
import com.example.CareConnect_hcl.repository.MedicationRepository;

@Service
public class MedicationService {
    private final MedicationRepository repository;
    private final CurrentAccountService currentAccount;

    public MedicationService(MedicationRepository repository, CurrentAccountService currentAccount) { this.repository = repository; this.currentAccount = currentAccount; }
    public List<Medication> getAll() { currentAccount.requireRole("DOCTOR", "ADMIN"); return repository.findAll(); }
    public Medication getById(Long id) { currentAccount.requireRole("DOCTOR", "ADMIN"); return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication not found")); }
    public Medication create(Medication medication) { currentAccount.requireRole("ADMIN"); medication.setId(null); return repository.save(medication); }
    public Medication update(Long id, Medication details) {
        currentAccount.requireRole("ADMIN");
        Medication medication = getById(id);
        medication.setName(details.getName());
        medication.setStrength(details.getStrength());
        medication.setForm(details.getForm());
        return repository.save(medication);
    }
    public void delete(Long id) { currentAccount.requireRole("ADMIN"); repository.delete(getById(id)); }
}
