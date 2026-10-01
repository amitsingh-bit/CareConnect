package com.example.CareConnect_hcl.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.CareConnect_hcl.config.CurrentAccountService;
import com.example.CareConnect_hcl.model.entity.Account;
import com.example.CareConnect_hcl.model.entity.Prescription;
import com.example.CareConnect_hcl.repository.PrescriptionRepository;
import com.example.CareConnect_hcl.repository.MedicationRepository;
import com.example.CareConnect_hcl.repository.MedicalRecordRepository;
import com.example.CareConnect_hcl.repository.DoctorRepository;

@Service
public class PrescriptionService {
    private final PrescriptionRepository repository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicationRepository medicationRepository;
    private final DoctorRepository doctorRepository;
    private final CurrentAccountService currentAccount;
    private final PatientAccessService patientAccess;

    public PrescriptionService(PrescriptionRepository repository, MedicalRecordRepository medicalRecordRepository,
            MedicationRepository medicationRepository, DoctorRepository doctorRepository,
            CurrentAccountService currentAccount, PatientAccessService patientAccess) {
        this.repository = repository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.medicationRepository = medicationRepository;
        this.doctorRepository = doctorRepository;
        this.currentAccount = currentAccount;
        this.patientAccess = patientAccess;
    }
    public List<Prescription> getAll() {
        Account account = currentAccount.requireRole("ADMIN", "DOCTOR", "PATIENT", "NURSE");
        return switch (account.getRole().toUpperCase()) {
            case "ADMIN" -> repository.findAll();
            case "PATIENT" -> repository.findByPatient_IdOrderByPrescribedDateDesc(account.getProfileId());
            case "DOCTOR" -> {
                List<Long> patientIds = patientAccess.accessiblePatientIdList(account);
                yield patientIds.isEmpty() ? List.of() : repository.findByPatient_IdIn(patientIds);
            }
            case "NURSE" -> {
                List<Long> patientIds = patientAccess.accessiblePatientIdList(account);
                yield patientIds.isEmpty() ? List.of() : repository.findByPatient_IdIn(patientIds);
            }
            default -> throw forbidden();
        };
    }
    public Prescription getById(Long id) {
        Prescription prescription = repository.findById(id).orElseThrow(() -> notFound());
        patientAccess.requirePatientAccess(prescription.getPatientId());
        return prescription;
    }
    public Prescription create(Prescription prescription) {
        Account account = currentAccount.requireRole("ADMIN", "DOCTOR");
        prescription.setId(null);
        attachRelations(prescription);
        if ("DOCTOR".equalsIgnoreCase(account.getRole())) {
            patientAccess.requireDoctorOwnsPatient(prescription.getPatientId());
            prescription.setDoctor(doctorRepository.findById(account.getProfileId()).orElseThrow(() -> forbidden()));
        }
        return repository.save(prescription);
    }
    public Prescription update(Long id, Prescription details) {
        Prescription prescription = getById(id);
        Account account = currentAccount.requireRole("ADMIN", "DOCTOR");
        prescription.setMedicalRecord(details.getMedicalRecord());
        prescription.setDoctor(details.getDoctor());
        prescription.setMedication(details.getMedication());
        prescription.setDosage(details.getDosage());
        prescription.setFrequency(details.getFrequency());
        prescription.setDuration(details.getDuration());
        prescription.setInstructions(details.getInstructions());
        prescription.setPrescribedDate(details.getPrescribedDate());
        prescription.setStatus(details.getStatus());
        attachRelations(prescription);
        if ("DOCTOR".equalsIgnoreCase(account.getRole())) {
            patientAccess.requireDoctorOwnsPatient(prescription.getPatientId());
            prescription.setDoctor(doctorRepository.findById(account.getProfileId()).orElseThrow(() -> forbidden()));
        }
        return repository.save(prescription);
    }
    public void delete(Long id) { currentAccount.requireRole("ADMIN"); repository.delete(getById(id)); }

    private void attachRelations(Prescription prescription) {
        if (prescription.getMedicalRecord() == null || prescription.getMedicalRecord().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "medicalRecord.id is required");
        }
        prescription.setMedicalRecord(medicalRecordRepository.findById(prescription.getMedicalRecord().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medical record not found")));
        prescription.setPatient(prescription.getMedicalRecord().getPatient());
        if (prescription.getMedication() == null || prescription.getMedication().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "medication.id is required");
        }
        prescription.setMedication(medicationRepository.findById(prescription.getMedication().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication not found")));
        if (prescription.getDoctor() != null) {
            if (prescription.getDoctor().getDocId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "doctor.docId is required when doctor is provided");
            }
            prescription.setDoctor(doctorRepository.findById(prescription.getDoctor().getDocId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found")));
        }
    }

    private static ResponseStatusException forbidden() { return new ResponseStatusException(HttpStatus.FORBIDDEN, "Your account does not have access to this operation"); }
    private static ResponseStatusException notFound() { return new ResponseStatusException(HttpStatus.NOT_FOUND, "Prescription not found"); }
}
