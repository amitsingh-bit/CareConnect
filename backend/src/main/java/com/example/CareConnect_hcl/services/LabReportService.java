package com.example.CareConnect_hcl.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.CareConnect_hcl.config.CurrentAccountService;
import com.example.CareConnect_hcl.model.entity.Account;
import com.example.CareConnect_hcl.model.entity.LabReport;
import com.example.CareConnect_hcl.repository.LabReportRepository;
import com.example.CareConnect_hcl.repository.DoctorRepository;
import com.example.CareConnect_hcl.repository.MedicalRecordRepository;

@Service
public class LabReportService {
    private final LabReportRepository repository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final DoctorRepository doctorRepository;
    private final CurrentAccountService currentAccount;
    private final PatientAccessService patientAccess;

    public LabReportService(LabReportRepository repository, MedicalRecordRepository medicalRecordRepository, DoctorRepository doctorRepository,
            CurrentAccountService currentAccount, PatientAccessService patientAccess) {
        this.repository = repository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.doctorRepository = doctorRepository;
        this.currentAccount = currentAccount;
        this.patientAccess = patientAccess;
    }
    public List<LabReport> getAll() {
        Account account = currentAccount.requireAccount();
        return switch (account.getRole().toUpperCase()) {
            case "ADMIN" -> repository.findAll();
            case "PATIENT" -> repository.findByPatient_IdOrderByReportDateDesc(account.getProfileId());
            case "DOCTOR", "NURSE" -> {
                var patientIds = patientAccess.accessiblePatientIdList(account);
                yield patientIds.isEmpty() ? List.of() : repository.findByPatient_IdIn(patientIds);
            }
            default -> throw forbidden();
        };
    }
    public LabReport getById(Long id) {
        LabReport report = repository.findById(id).orElseThrow(() -> notFound());
        patientAccess.requirePatientAccess(report.getPatientId());
        return report;
    }
    public LabReport create(LabReport report) {
        Account account = currentAccount.requireRole("ADMIN", "DOCTOR");
        report.setId(null);
        attachRelations(report);
        if ("DOCTOR".equalsIgnoreCase(account.getRole())) {
            patientAccess.requireDoctorOwnsPatient(report.getPatientId());
            report.setDoctor(doctorRepository.findById(account.getProfileId()).orElseThrow(() -> forbidden()));
        }
        return repository.save(report);
    }
    public LabReport update(Long id, LabReport details) {
        LabReport report = getById(id);
        Account account = currentAccount.requireRole("ADMIN", "DOCTOR");
        report.setMedicalRecord(details.getMedicalRecord());
        report.setDoctor(details.getDoctor());
        report.setTestName(details.getTestName());
        report.setResult(details.getResult());
        report.setReferenceRange(details.getReferenceRange());
        report.setReportDate(details.getReportDate());
        report.setStatus(details.getStatus());
        attachRelations(report);
        if ("DOCTOR".equalsIgnoreCase(account.getRole())) {
            patientAccess.requireDoctorOwnsPatient(report.getPatientId());
            report.setDoctor(doctorRepository.findById(account.getProfileId()).orElseThrow(() -> forbidden()));
        }
        return repository.save(report);
    }
    public void delete(Long id) { currentAccount.requireRole("ADMIN"); repository.delete(getById(id)); }

    private void attachRelations(LabReport report) {
        if (report.getMedicalRecord() == null || report.getMedicalRecord().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "medicalRecord.id is required");
        }
        report.setMedicalRecord(medicalRecordRepository.findById(report.getMedicalRecord().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medical record not found")));
        report.setPatient(report.getMedicalRecord().getPatient());
        if (report.getDoctor() != null) {
            if (report.getDoctor().getDocId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "doctor.docId is required when doctor is provided");
            }
            report.setDoctor(doctorRepository.findById(report.getDoctor().getDocId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found")));
        }
    }

    private static ResponseStatusException forbidden() { return new ResponseStatusException(HttpStatus.FORBIDDEN, "Your account does not have access to this operation"); }
    private static ResponseStatusException notFound() { return new ResponseStatusException(HttpStatus.NOT_FOUND, "Lab report not found"); }
}
