package com.example.CareConnect_hcl.services;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.CareConnect_hcl.config.CurrentAccountService;
import com.example.CareConnect_hcl.model.entity.Account;
import com.example.CareConnect_hcl.repository.AppointmentRepository;
import com.example.CareConnect_hcl.repository.DiagnosisRepository;
import com.example.CareConnect_hcl.repository.LabReportRepository;
import com.example.CareConnect_hcl.repository.MedicalRecordRepository;
import com.example.CareConnect_hcl.repository.PatientRepository;
import com.example.CareConnect_hcl.repository.PrescriptionRepository;

@Service
public class PatientAccessService {
    private final CurrentAccountService currentAccount;
    private final PatientRepository patients;
    private final AppointmentRepository appointments;
    private final MedicalRecordRepository records;
    private final PrescriptionRepository prescriptions;
    private final LabReportRepository reports;
    private final DiagnosisRepository diagnoses;

    public PatientAccessService(CurrentAccountService currentAccount, PatientRepository patients,
            AppointmentRepository appointments, MedicalRecordRepository records,
            PrescriptionRepository prescriptions, LabReportRepository reports, DiagnosisRepository diagnoses) {
        this.currentAccount = currentAccount;
        this.patients = patients;
        this.appointments = appointments;
        this.records = records;
        this.prescriptions = prescriptions;
        this.reports = reports;
        this.diagnoses = diagnoses;
    }

    public Set<Long> accessiblePatientIds(Account account) {
        Set<Long> ids = new LinkedHashSet<>();
        switch (account.getRole().toUpperCase()) {
            case "ADMIN" -> patients.findAll().forEach(patient -> ids.add(patient.getId()));
            case "PATIENT" -> { if (account.getProfileId() != null) ids.add(account.getProfileId()); }
            case "NURSE" -> { if (account.getProfileId() != null) patients.findByAssignedNurses_Id(account.getProfileId()).forEach(patient -> ids.add(patient.getId())); }
            case "DOCTOR" -> {
                Long doctorId = account.getProfileId();
                appointments.findByDoctor_DocidOrderByAppointmentDateTimeAsc(doctorId).forEach(item -> ids.add(item.getPatientId()));
                records.findByDoctor_DocidOrderByRecordedAtDesc(doctorId).forEach(item -> ids.add(item.getPatientId()));
                prescriptions.findByDoctor_DocidOrderByPrescribedDateDesc(doctorId).forEach(item -> ids.add(item.getPatientId()));
                reports.findByDoctor_DocidOrderByReportDateDesc(doctorId).forEach(item -> ids.add(item.getPatientId()));
                diagnoses.findByDoctor_DocidOrderByDiagnosedAtDesc(doctorId).forEach(item -> ids.add(item.getPatientId()));
                ids.remove(null);
            }
            default -> throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unknown account role");
        }
        return ids;
    }

    public List<Long> accessiblePatientIdList(Account account) {
        return List.copyOf(accessiblePatientIds(account));
    }

    public boolean canAccessPatient(Account account, Long patientId) {
        if (patientId == null) return false;
        return switch (account.getRole().toUpperCase()) {
            case "ADMIN" -> true;
            case "PATIENT" -> patientId.equals(account.getProfileId());
            case "NURSE" -> account.getProfileId() != null && patients.existsByIdAndAssignedNurses_Id(patientId, account.getProfileId());
            case "DOCTOR" -> account.getProfileId() != null && isPatientLinkedToDoctor(patientId, account.getProfileId());
            default -> false;
        };
    }

    public void requirePatientAccess(Long patientId) {
        Account account = currentAccount.requireAccount();
        if (!canAccessPatient(account, patientId)) {
            // Return not-found so callers cannot distinguish a hidden record from a missing one.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient record not found");
        }
    }

    public boolean isPatientLinkedToDoctor(Long patientId, Long doctorId) {
        if (patientId == null || doctorId == null) return false;
        return appointments.existsByPatient_IdAndDoctor_Docid(patientId, doctorId)
                || records.existsByPatient_IdAndDoctor_Docid(patientId, doctorId)
                || prescriptions.existsByPatient_IdAndDoctor_Docid(patientId, doctorId)
                || reports.existsByPatient_IdAndDoctor_Docid(patientId, doctorId)
                || diagnoses.existsByPatient_IdAndDoctor_Docid(patientId, doctorId);
    }

    public void requireDoctorOwnsPatient(Long patientId) {
        Account account = currentAccount.requireRole("DOCTOR");
        if (!isPatientLinkedToDoctor(patientId, account.getProfileId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient record not found");
        }
    }

    public boolean isAssignedNurse(Long patientId, Long nurseId) {
        return patientId != null && nurseId != null && patients.existsByIdAndAssignedNurses_Id(patientId, nurseId);
    }
}
