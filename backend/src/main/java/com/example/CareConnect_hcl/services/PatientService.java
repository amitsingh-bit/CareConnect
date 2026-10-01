package com.example.CareConnect_hcl.services;

import com.example.CareConnect_hcl.model.entity.Patient;
import com.example.CareConnect_hcl.model.entity.Nurse;
import com.example.CareConnect_hcl.model.entity.Account;
import com.example.CareConnect_hcl.config.CurrentAccountService;
import com.example.CareConnect_hcl.repository.PatientRepository;
import com.example.CareConnect_hcl.repository.NurseRepository;
import com.example.CareConnect_hcl.services.PatientAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final NurseRepository nurseRepository;
    private final CurrentAccountService currentAccount;
    private final PatientAccessService patientAccess;

    public PatientService(PatientRepository patientRepository, NurseRepository nurseRepository,
            CurrentAccountService currentAccount, PatientAccessService patientAccess) {
        this.patientRepository = patientRepository;
        this.nurseRepository = nurseRepository;
        this.currentAccount = currentAccount;
        this.patientAccess = patientAccess;
    }
    public Patient savePatient(Patient patient) {
        currentAccount.requireRole("ADMIN");
        return patientRepository.save(patient);
    }
    public List<Patient> getAllPatients() {
        Account account = currentAccount.requireAccount();
        if ("ADMIN".equalsIgnoreCase(account.getRole())) return patientRepository.findAll();
        return patientRepository.findAllById(patientAccess.accessiblePatientIdList(account));
    }
    public Patient getPatientById(Long id) {
        patientAccess.requirePatientAccess(id);
        return patientRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
    }
    public Patient updatePatient(Long id, Patient patientDetails) {
        currentAccount.requireRole("ADMIN", "PATIENT");
        patientAccess.requirePatientAccess(id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        patient.setName(patientDetails.getName());
        patient.setAge(patientDetails.getAge());
        patient.setDisease(patientDetails.getDisease());
        patient.setGender(patientDetails.getGender());
        patient.setEmail(patientDetails.getEmail());
        patient.setPhone(patientDetails.getPhone());
        patient.setBloodGroup(patientDetails.getBloodGroup());
        patient.setAddress(patientDetails.getAddress());
        patient.setEmergencyContact(patientDetails.getEmergencyContact());

        return patientRepository.save(patient);
    }
    public void deletePatient(Long id) {
        currentAccount.requireRole("ADMIN");

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        patientRepository.delete(patient);
    }

    public Patient assignNurse(Long patientId, Long nurseId) {
        currentAccount.requireRole("ADMIN");
        Patient patient = patientRepository.findById(patientId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        Nurse nurse = nurseRepository.findById(nurseId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nurse not found"));
        patient.assignNurse(nurse);
        return patientRepository.save(patient);
    }

    public Patient unassignNurse(Long patientId, Long nurseId) {
        currentAccount.requireRole("ADMIN");
        Patient patient = patientRepository.findById(patientId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        Nurse nurse = nurseRepository.findById(nurseId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nurse not found"));
        patient.removeNurse(nurse);
        return patientRepository.save(patient);
    }

    @Transactional(readOnly = true)
    public List<Nurse> getAssignedNurses(Long patientId) {
        currentAccount.requireRole("ADMIN");
        Patient patient = patientRepository.findById(patientId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        return List.copyOf(patient.getAssignedNurses());
    }
}
