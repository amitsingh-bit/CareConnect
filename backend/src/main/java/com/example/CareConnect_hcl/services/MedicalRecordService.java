package com.example.CareConnect_hcl.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.CareConnect_hcl.model.entity.MedicalRecord;
import com.example.CareConnect_hcl.model.entity.Account;
import com.example.CareConnect_hcl.model.entity.Doctor;
import com.example.CareConnect_hcl.config.CurrentAccountService;
import com.example.CareConnect_hcl.repository.MedicalRecordRepository;
import com.example.CareConnect_hcl.repository.DoctorRepository;
import com.example.CareConnect_hcl.repository.PatientRepository;

@Service
public class MedicalRecordService {
    private final MedicalRecordRepository repository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final CurrentAccountService currentAccount;
    private final PatientAccessService patientAccess;

    public MedicalRecordService(MedicalRecordRepository repository, PatientRepository patientRepository, DoctorRepository doctorRepository,
            CurrentAccountService currentAccount, PatientAccessService patientAccess) {
        this.repository = repository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.currentAccount = currentAccount;
        this.patientAccess = patientAccess;
    }
    public List<MedicalRecord> getAll() {
        Account account = currentAccount.requireAccount();
        return switch (account.getRole().toUpperCase()) {
            case "ADMIN" -> repository.findAll();
            case "PATIENT" -> repository.findByPatient_IdOrderByRecordedAtDesc(account.getProfileId());
            case "DOCTOR", "NURSE" -> {
                var patientIds = patientAccess.accessiblePatientIdList(account);
                yield patientIds.isEmpty() ? List.of() : repository.findByPatient_IdIn(patientIds);
            }
            default -> throw forbidden();
        };
    }
    public MedicalRecord getById(Long id) {
        MedicalRecord record = repository.findById(id).orElseThrow(() -> notFound());
        patientAccess.requirePatientAccess(record.getPatientId());
        return record;
    }
    public MedicalRecord create(MedicalRecord record) {
        Account account = currentAccount.requireRole("ADMIN", "DOCTOR");
        record.setId(null);
        if ("DOCTOR".equalsIgnoreCase(account.getRole())) {
            record.setDoctor(doctorRepository.findById(account.getProfileId()).orElseThrow(() -> forbidden()));
            if (record.getPatient() != null) patientAccess.requireDoctorOwnsPatient(record.getPatient().getId());
        }
        attachRelations(record);
        return repository.save(record);
    }
    public MedicalRecord update(Long id, MedicalRecord details) {
        MedicalRecord record = getById(id);
        Account account = currentAccount.requireRole("ADMIN", "DOCTOR");
        if ("ADMIN".equalsIgnoreCase(account.getRole())) {
            record.setPatient(details.getPatient());
            record.setDoctor(details.getDoctor());
        } else {
            if (details.getPatient() != null && details.getPatient().getId() != null
                    && !details.getPatient().getId().equals(record.getPatientId())) throw forbidden();
            record.setDoctor(doctorRepository.findById(account.getProfileId()).orElseThrow(() -> forbidden()));
        }
        record.setDiagnosis(details.getDiagnosis());
        record.setTreatment(details.getTreatment());
        record.setNotes(details.getNotes());
        record.setRecordedAt(details.getRecordedAt());
        attachRelations(record);
        return repository.save(record);
    }
    public void delete(Long id) { currentAccount.requireRole("ADMIN"); repository.delete(getById(id)); }

    private void attachRelations(MedicalRecord record) {
        if (record.getPatient() == null || record.getPatient().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "patient.id is required");
        }
        record.setPatient(patientRepository.findById(record.getPatient().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found")));
        if (record.getDoctor() != null) {
            if (record.getDoctor().getDocId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "doctor.docId is required when doctor is provided");
            }
            record.setDoctor(doctorRepository.findById(record.getDoctor().getDocId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found")));
        }
    }

    private static ResponseStatusException forbidden() { return new ResponseStatusException(HttpStatus.FORBIDDEN, "Your account does not have access to this operation"); }
    private static ResponseStatusException notFound() { return new ResponseStatusException(HttpStatus.NOT_FOUND, "Medical record not found"); }
}
