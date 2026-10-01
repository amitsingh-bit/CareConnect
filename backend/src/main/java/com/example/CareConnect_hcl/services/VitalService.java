package com.example.CareConnect_hcl.services;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.example.CareConnect_hcl.config.CurrentAccountService;
import com.example.CareConnect_hcl.model.entity.Account;
import com.example.CareConnect_hcl.model.entity.Vital;
import com.example.CareConnect_hcl.repository.PatientRepository;
import com.example.CareConnect_hcl.repository.VitalRepository;

@Service
public class VitalService {
    private final VitalRepository repository;
    private final PatientRepository patients;
    private final CurrentAccountService currentAccount;
    private final PatientAccessService patientAccess;

    public VitalService(VitalRepository repository, PatientRepository patients, CurrentAccountService currentAccount,
            PatientAccessService patientAccess) {
        this.repository = repository;
        this.patients = patients;
        this.currentAccount = currentAccount;
        this.patientAccess = patientAccess;
    }

    public List<Vital> getAll() {
        Account account = currentAccount.requireRole("ADMIN", "DOCTOR", "NURSE", "PATIENT");
        if ("ADMIN".equalsIgnoreCase(account.getRole())) return repository.findAll();
        List<Long> patientIds = patientAccess.accessiblePatientIdList(account);
        return patientIds.isEmpty() ? List.of() : repository.findByPatient_IdInOrderByRecordedAtDesc(patientIds);
    }

    public Vital getById(Long id) {
        Vital vital = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vitals not found"));
        patientAccess.requirePatientAccess(vital.getPatientId());
        return vital;
    }

    public Vital create(Vital vital) {
        Account account = currentAccount.requireRole("ADMIN", "DOCTOR", "NURSE");
        if (vital.getPatient() == null || vital.getPatient().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "patient.id is required");
        }
        Long patientId = vital.getPatient().getId();
        if ("DOCTOR".equalsIgnoreCase(account.getRole())) patientAccess.requireDoctorOwnsPatient(patientId);
        if ("NURSE".equalsIgnoreCase(account.getRole()) && !patientAccess.isAssignedNurse(patientId, account.getProfileId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient record not found");
        }
        vital.setId(null);
        vital.setPatient(patients.findById(patientId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found")));
        return repository.save(vital);
    }
}
