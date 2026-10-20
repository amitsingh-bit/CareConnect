package com.example.CareConnect_hcl.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.example.CareConnect_hcl.config.CurrentAccountService;
import com.example.CareConnect_hcl.model.dto.VideoJoinResponse;
import com.example.CareConnect_hcl.model.entity.Appointment;
import com.example.CareConnect_hcl.model.entity.VideoConsultation;
import com.example.CareConnect_hcl.repository.AppointmentRepository;
import com.example.CareConnect_hcl.repository.VideoConsultationRepository;

@Service
public class VideoConsultationService {
    private final CurrentAccountService currentAccount;
    private final AppointmentRepository appointments;
    private final VideoConsultationRepository consultations;
    private final VideoProvider provider;
    public VideoConsultationService(CurrentAccountService currentAccount, AppointmentRepository appointments,
            VideoConsultationRepository consultations, VideoProvider provider) {
        this.currentAccount = currentAccount; this.appointments = appointments; this.consultations = consultations; this.provider = provider;
    }
    @Transactional
    public VideoJoinResponse join(Long appointmentId) {
        var account = currentAccount.requireRole("PATIENT", "DOCTOR");
        Appointment appointment = appointments.findById(appointmentId).orElseThrow(VideoConsultationService::notFound);
        boolean isDoctor = "DOCTOR".equalsIgnoreCase(account.getRole());
        boolean ownsAppointment = isDoctor ? account.getProfileId().equals(appointment.getDoctorId()) : account.getProfileId().equals(appointment.getPatientId());
        if (!ownsAppointment) throw notFound();
        if (appointment.getAppointmentDateTime() == null || "CANCELLED".equalsIgnoreCase(appointment.getStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This appointment is not eligible for a video consultation.");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime starts = appointment.getAppointmentDateTime();
        if (now.isBefore(starts.minusMinutes(15)) || now.isAfter(starts.plusMinutes(120)))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Video consultation access opens 15 minutes before the appointment and closes 2 hours after its scheduled start.");
        if (!provider.isConfigured()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Video consultations are not configured. Contact the service administrator.");
        ZoneId zone = ZoneId.systemDefault();
        long exp = starts.plusMinutes(120).atZone(zone).toEpochSecond();
        VideoConsultation consultation = consultations.findByAppointment_Id(appointmentId).orElse(null);
        if (consultation == null) {
            VideoProvider.Room room = provider.createRoom(exp);
            consultation = consultations.save(new VideoConsultation(appointment, room.name(), room.url(), now));
        }
        long nbf = starts.minusMinutes(15).atZone(zone).toEpochSecond();
        String token = provider.createParticipantToken(consultation.getRoomName(), account.getDisplayName(),
                "account-" + account.getId(), isDoctor, nbf, exp);
        return new VideoJoinResponse(consultation.getRoomUrl() + "?t=" + token, consultation.getStatus(), starts);
    }
    private static ResponseStatusException notFound() { return new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"); }
}
