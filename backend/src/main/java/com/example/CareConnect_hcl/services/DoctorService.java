package com.example.CareConnect_hcl.services;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.CareConnect_hcl.config.CurrentAccountService;
import com.example.CareConnect_hcl.model.entity.Account;
import com.example.CareConnect_hcl.model.entity.Doctor;
import com.example.CareConnect_hcl.repository.AppointmentRepository;
import com.example.CareConnect_hcl.repository.DiagnosisRepository;
import com.example.CareConnect_hcl.repository.DoctorRepository;
import com.example.CareConnect_hcl.repository.LabReportRepository;
import com.example.CareConnect_hcl.repository.MedicalRecordRepository;
import com.example.CareConnect_hcl.repository.PrescriptionRepository;

@Service
public class DoctorService {
    private final DoctorRepository repository;
    private final CurrentAccountService currentAccount;
    private final PatientAccessService patientAccess;
    private final AppointmentRepository appointments;
    private final MedicalRecordRepository records;
    private final PrescriptionRepository prescriptions;
    private final LabReportRepository reports;
    private final DiagnosisRepository diagnoses;

    public DoctorService(DoctorRepository repository, CurrentAccountService currentAccount, PatientAccessService patientAccess,
            AppointmentRepository appointments, MedicalRecordRepository records, PrescriptionRepository prescriptions,
            LabReportRepository reports, DiagnosisRepository diagnoses) {
        this.repository = repository;
        this.currentAccount = currentAccount;
        this.patientAccess = patientAccess;
        this.appointments = appointments;
        this.records = records;
        this.prescriptions = prescriptions;
        this.reports = reports;
        this.diagnoses = diagnoses;
    }

    public List<Doctor> getAll() {
        Account account = currentAccount.requireRole("PATIENT", "DOCTOR", "NURSE", "ADMIN");
        if ("ADMIN".equalsIgnoreCase(account.getRole())) return repository.findAll();
        if ("DOCTOR".equalsIgnoreCase(account.getRole()) && account.getProfileId() != null) {
            Set<Long> ids = associatedDoctorIds(patientAccess.accessiblePatientIdList(account));
            ids.add(account.getProfileId());
            return repository.findAllById(ids);
        }
        return repository.findAllById(associatedDoctorIds(patientAccess.accessiblePatientIdList(account)));
    }

    /** Directory data is non-patient PHI and is needed to request a new appointment. */
    public List<Doctor> getDirectory() {
        currentAccount.requireRole("PATIENT", "DOCTOR", "NURSE", "ADMIN");
        return repository.findAll();
    }

    public Doctor getById(Long id) {
        return getAll().stream().filter(doctor -> id.equals(doctor.getDocId())).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
    }

    public Doctor save(Doctor doctor) {
        currentAccount.requireRole("ADMIN");
        return repository.save(doctor);
    }

    public void delete(Long id) {
        currentAccount.requireRole("ADMIN");
        repository.deleteById(id);
    }

    private Set<Long> associatedDoctorIds(Collection<Long> patientIds) {
        Set<Long> ids = new LinkedHashSet<>();
        if (patientIds.isEmpty()) return ids;
        appointments.findByPatient_IdIn(patientIds).forEach(item -> add(ids, item.getDoctorId()));
        records.findByPatient_IdIn(patientIds).forEach(item -> add(ids, item.getDoctorId()));
        prescriptions.findByPatient_IdIn(patientIds).forEach(item -> add(ids, item.getDoctorId()));
        reports.findByPatient_IdIn(patientIds).forEach(item -> add(ids, item.getDoctorId()));
        diagnoses.findByPatient_IdIn(patientIds).forEach(item -> add(ids, item.getDoctorId()));
        return ids;
    }

    private void add(Set<Long> ids, Long id) { if (id != null) ids.add(id); }
}
