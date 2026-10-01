package com.example.CareConnect_hcl.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.CareConnect_hcl.config.CurrentAccountService;
import com.example.CareConnect_hcl.model.entity.Account;
import com.example.CareConnect_hcl.model.entity.Diagnosis;
import com.example.CareConnect_hcl.repository.DiagnosisRepository;
import com.example.CareConnect_hcl.repository.DoctorRepository;
import com.example.CareConnect_hcl.repository.MedicalRecordRepository;

@Service
public class DiagnosisService {
    private final DiagnosisRepository repository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final DoctorRepository doctorRepository;
    private final CurrentAccountService currentAccount;
    private final PatientAccessService patientAccess;

    public DiagnosisService(DiagnosisRepository repository, MedicalRecordRepository medicalRecordRepository, DoctorRepository doctorRepository,
            CurrentAccountService currentAccount, PatientAccessService patientAccess) {
        this.repository = repository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.doctorRepository = doctorRepository;
        this.currentAccount = currentAccount;
        this.patientAccess = patientAccess;
    }
    public List<Diagnosis> getAll() {
        Account account = currentAccount.requireRole("ADMIN", "DOCTOR", "PATIENT", "NURSE");
        if ("ADMIN".equalsIgnoreCase(account.getRole())) return repository.findAll();
        var patientIds = patientAccess.accessiblePatientIdList(account);
        return patientIds.isEmpty() ? List.of() : repository.findByPatient_IdIn(patientIds);
    }
    public Diagnosis getById(Long id) {
        Diagnosis diagnosis = repository.findById(id).orElseThrow(() -> notFound());
        patientAccess.requirePatientAccess(diagnosis.getPatientId());
        return diagnosis;
    }
    public Diagnosis create(Diagnosis diagnosis) {
        Account account = currentAccount.requireRole("ADMIN", "DOCTOR");
        diagnosis.setId(null);
        attachRelations(diagnosis);
        if ("DOCTOR".equalsIgnoreCase(account.getRole())) {
            patientAccess.requireDoctorOwnsPatient(diagnosis.getPatientId());
            diagnosis.setDoctor(doctorRepository.findById(account.getProfileId()).orElseThrow(() -> forbidden()));
        }
        return repository.save(diagnosis);
    }
    public Diagnosis update(Long id, Diagnosis details) {
        Diagnosis diagnosis = getById(id);
        Account account = currentAccount.requireRole("ADMIN", "DOCTOR");
        diagnosis.setMedicalRecord(details.getMedicalRecord());
        diagnosis.setDoctor(details.getDoctor());
        diagnosis.setDiagnosis(details.getDiagnosis());
        diagnosis.setNotes(details.getNotes());
        diagnosis.setDiagnosedAt(details.getDiagnosedAt());
        attachRelations(diagnosis);
        if ("DOCTOR".equalsIgnoreCase(account.getRole())) {
            patientAccess.requireDoctorOwnsPatient(diagnosis.getPatientId());
            diagnosis.setDoctor(doctorRepository.findById(account.getProfileId()).orElseThrow(() -> forbidden()));
        }
        return repository.save(diagnosis);
    }
    public void delete(Long id) { currentAccount.requireRole("ADMIN"); repository.delete(getById(id)); }

    private void attachRelations(Diagnosis diagnosis) {
        if (diagnosis.getMedicalRecord() == null || diagnosis.getMedicalRecord().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "medicalRecord.id is required");
        }
        diagnosis.setMedicalRecord(medicalRecordRepository.findById(diagnosis.getMedicalRecord().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medical record not found")));
        diagnosis.setPatient(diagnosis.getMedicalRecord().getPatient());
        if (diagnosis.getDoctor() != null) {
            if (diagnosis.getDoctor().getDocId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "doctor.docId is required when doctor is provided");
            }
            diagnosis.setDoctor(doctorRepository.findById(diagnosis.getDoctor().getDocId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found")));
        }
    }

    private static ResponseStatusException forbidden() { return new ResponseStatusException(HttpStatus.FORBIDDEN, "Your account does not have access to this operation"); }
    private static ResponseStatusException notFound() { return new ResponseStatusException(HttpStatus.NOT_FOUND, "Diagnosis not found"); }
}
