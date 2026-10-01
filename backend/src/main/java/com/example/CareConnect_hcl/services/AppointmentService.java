package com.example.CareConnect_hcl.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.CareConnect_hcl.config.CurrentAccountService;
import com.example.CareConnect_hcl.model.entity.Account;
import com.example.CareConnect_hcl.model.entity.Appointment;
import com.example.CareConnect_hcl.model.entity.Patient;
import com.example.CareConnect_hcl.repository.AppointmentRepository;
import com.example.CareConnect_hcl.repository.DoctorRepository;
import com.example.CareConnect_hcl.repository.PatientRepository;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final CurrentAccountService currentAccount;
    private final PatientAccessService patientAccess;

    public AppointmentService(AppointmentRepository appointmentRepository, PatientRepository patientRepository,
            DoctorRepository doctorRepository, CurrentAccountService currentAccount, PatientAccessService patientAccess) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.currentAccount = currentAccount;
        this.patientAccess = patientAccess;
    }

    public List<Appointment> getAll() {
        Account account = currentAccount.requireAccount();
        return switch (account.getRole().toUpperCase()) {
            case "ADMIN" -> appointmentRepository.findAll();
            case "PATIENT" -> appointmentRepository.findByPatient_IdOrderByAppointmentDateTimeDesc(account.getProfileId());
            case "DOCTOR" -> appointmentRepository.findByDoctor_DocidOrderByAppointmentDateTimeAsc(account.getProfileId());
            case "NURSE" -> {
                var patientIds = patientAccess.accessiblePatientIdList(account);
                yield patientIds.isEmpty() ? List.of() : appointmentRepository.findByPatient_IdIn(patientIds);
            }
            default -> throw forbidden();
        };
    }

    public Appointment getById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
        assertVisible(appointment);
        return appointment;
    }

    @Transactional
    public Appointment create(Appointment appointment) {
        Account account = currentAccount.requireRole("ADMIN", "DOCTOR", "PATIENT");
        appointment.setId(null);
        boolean creatingPatient = appointment.getPatient() != null && appointment.getPatient().getId() == null;
        if (creatingPatient) {
            if (!"ADMIN".equalsIgnoreCase(account.getRole()) && !"DOCTOR".equalsIgnoreCase(account.getRole())) throw forbidden();
            Patient newPatient = appointment.getPatient();
            if (newPatient.getName() == null || newPatient.getName().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient name is required");
            }
            appointment.setPatient(patientRepository.save(newPatient));
        }
        attachRelations(appointment);
        if ("PATIENT".equalsIgnoreCase(account.getRole()) && !account.getProfileId().equals(appointment.getPatientId())) {
            throw forbidden();
        }
        if ("DOCTOR".equalsIgnoreCase(account.getRole())) {
            if (!account.getProfileId().equals(appointment.getDoctorId())) throw forbidden();
            if (!creatingPatient) patientAccess.requireDoctorOwnsPatient(appointment.getPatientId());
        }
        return appointmentRepository.save(appointment);
    }

    public Appointment update(Long id, Appointment details) {
        Appointment appointment = getById(id);
        Account account = currentAccount.requireRole("ADMIN", "DOCTOR");
        if ("ADMIN".equalsIgnoreCase(account.getRole())) {
            appointment.setPatient(details.getPatient());
            appointment.setDoctor(details.getDoctor());
        } else if (details.getPatient() != null && details.getPatient().getId() != null) {
            patientAccess.requireDoctorOwnsPatient(details.getPatient().getId());
        }
        appointment.setAppointmentDateTime(details.getAppointmentDateTime());
        appointment.setStatus(details.getStatus());
        appointment.setReason(details.getReason());
        attachRelations(appointment);
        return appointmentRepository.save(appointment);
    }

    public void delete(Long id) {
        currentAccount.requireRole("ADMIN");
        Appointment appointment = getById(id);
        appointmentRepository.delete(appointment);
    }

    private void attachRelations(Appointment appointment) {
        if (appointment.getPatient() == null || appointment.getPatient().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "patient.id is required");
        }
        appointment.setPatient(patientRepository.findById(appointment.getPatient().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found")));
        if (appointment.getDoctor() == null || appointment.getDoctor().getDocId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "doctor.docId is required");
        }
        appointment.setDoctor(doctorRepository.findById(appointment.getDoctor().getDocId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found")));
    }

    private void assertVisible(Appointment appointment) {
        Account account = currentAccount.requireAccount();
        if ("ADMIN".equalsIgnoreCase(account.getRole())) return;
        if ("PATIENT".equalsIgnoreCase(account.getRole()) || "NURSE".equalsIgnoreCase(account.getRole())) {
            patientAccess.requirePatientAccess(appointment.getPatientId());
        } else if ("DOCTOR".equalsIgnoreCase(account.getRole())) {
            if (!account.getProfileId().equals(appointment.getDoctorId())) throw notFound();
        } else throw forbidden();
    }

    private static ResponseStatusException forbidden() { return new ResponseStatusException(HttpStatus.FORBIDDEN, "Your account does not have access to this operation"); }
    private static ResponseStatusException notFound() { return new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"); }
}
