package com.example.CareConnect_hcl.services;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.example.CareConnect_hcl.config.CurrentAccountService;
import com.example.CareConnect_hcl.model.dto.ConsultationNoteRequest;
import com.example.CareConnect_hcl.model.dto.ConsultationNoteResponse;
import com.example.CareConnect_hcl.model.entity.Appointment;
import com.example.CareConnect_hcl.model.entity.ConsultationNote;
import com.example.CareConnect_hcl.repository.AppointmentRepository;
import com.example.CareConnect_hcl.repository.ConsultationNoteRepository;
import com.example.CareConnect_hcl.repository.ConsultationNoteVersionRepository;
import com.example.CareConnect_hcl.model.entity.ConsultationNoteVersion;

@Service
public class ConsultationNoteService {
    private final CurrentAccountService currentAccount;
    private final AppointmentRepository appointments;
    private final ConsultationNoteRepository notes;
    private final PatientAccessService patientAccess;
    private final ConsultationNoteVersionRepository versions;
    public ConsultationNoteService(CurrentAccountService currentAccount, AppointmentRepository appointments,
            ConsultationNoteRepository notes, PatientAccessService patientAccess, ConsultationNoteVersionRepository versions) {
        this.currentAccount = currentAccount; this.appointments = appointments; this.notes = notes; this.patientAccess = patientAccess; this.versions = versions;
    }

    @Transactional(readOnly = true)
    public ConsultationNoteResponse get(Long appointmentId) {
        Appointment appointment = appointment(appointmentId);
        authorizeRead(appointment);
        ConsultationNote note = notes.findByAppointment_Id(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consultation note not found"));
        return response(note);
    }

    @Transactional
    public ConsultationNoteResponse save(Long appointmentId, ConsultationNoteRequest request) {
        var doctor = currentAccount.requireRole("DOCTOR");
        Appointment appointment = appointment(appointmentId);
        if (appointment.getDoctorId() == null || !appointment.getDoctorId().equals(doctor.getProfileId())) notFound();
        if ("CANCELLED".equalsIgnoreCase(appointment.getStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A cancelled appointment cannot have a consultation note.");
        if (request == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Consultation note content is required.");
        length(request.chiefComplaint(), 3000); length(request.history(), 5000); length(request.examination(), 5000); length(request.assessment(), 5000);
        length(request.diagnosis(), 3000);
        length(request.plan(), 5000); length(request.followUp(), 2000);
        if (request.followUpDate() != null && request.followUpDate().isBefore(appointment.getAppointmentDateTime().toLocalDate()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Follow-up date cannot be before the appointment date.");
        LocalDateTime now = LocalDateTime.now();
        ConsultationNote note = notes.findByAppointment_Id(appointmentId).orElseGet(() -> {
            ConsultationNote created = new ConsultationNote(); created.setAppointment(appointment); created.setCreatedAt(LocalDateTime.now()); return created;
        });
        if (note.getId() != null && changed(note, request)) versions.save(new ConsultationNoteVersion(note, doctor.getProfileId(), now));
        note.setChiefComplaint(clean(request.chiefComplaint()));
        note.setHistory(clean(request.history())); note.setExamination(clean(request.examination()));
        note.setAssessment(clean(request.assessment())); note.setDiagnosis(clean(request.diagnosis()));
        note.setPlan(clean(request.plan())); note.setFollowUp(clean(request.followUp())); note.setFollowUpDate(request.followUpDate());
        note.setUpdatedAt(now);
        return response(notes.save(note));
    }

    private Appointment appointment(Long id) {
        return appointments.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }
    private void authorizeRead(Appointment appointment) {
        var account = currentAccount.requireAccount();
        switch (account.getRole().toUpperCase()) {
            case "ADMIN" -> { }
            case "PATIENT" -> { if (!appointment.getPatientId().equals(account.getProfileId())) notFound(); }
            case "DOCTOR" -> { if (!appointment.getDoctorId().equals(account.getProfileId())) notFound(); }
            case "NURSE" -> patientAccess.requirePatientAccess(appointment.getPatientId());
            default -> notFound();
        }
    }
    private static ConsultationNoteResponse response(ConsultationNote note) {
        Appointment appointment = note.getAppointment();
        return new ConsultationNoteResponse(note.getId(), appointment.getId(), appointment.getPatientId(),
                appointment.getPatientName(), appointment.getDoctorId(), appointment.getDoctorName(), note.getChiefComplaint(),
                note.getHistory(), note.getExamination(), note.getAssessment(), note.getDiagnosis(), note.getPlan(), note.getFollowUp(), note.getFollowUpDate(),
                note.getCreatedAt(), note.getUpdatedAt());
    }
    private static boolean changed(ConsultationNote note, ConsultationNoteRequest request) {
        return !java.util.Objects.equals(note.getChiefComplaint(), clean(request.chiefComplaint()))
                || !java.util.Objects.equals(note.getHistory(), clean(request.history()))
                || !java.util.Objects.equals(note.getExamination(), clean(request.examination()))
                || !java.util.Objects.equals(note.getAssessment(), clean(request.assessment()))
                || !java.util.Objects.equals(note.getDiagnosis(), clean(request.diagnosis()))
                || !java.util.Objects.equals(note.getPlan(), clean(request.plan()))
                || !java.util.Objects.equals(note.getFollowUp(), clean(request.followUp()))
                || !java.util.Objects.equals(note.getFollowUpDate(), request.followUpDate());
    }
    private static String clean(String value) { return value == null ? "" : value.trim(); }
    private static void length(String value, int max) { if (value != null && value.length() > max) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A consultation note field is too long."); }
    private static void notFound() { throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Consultation note not found"); }
}
