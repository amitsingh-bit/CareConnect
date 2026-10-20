package com.example.CareConnect_hcl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import com.example.CareConnect_hcl.config.CurrentAccountService;
import com.example.CareConnect_hcl.model.entity.Account;
import com.example.CareConnect_hcl.model.entity.Appointment;
import com.example.CareConnect_hcl.model.entity.Doctor;
import com.example.CareConnect_hcl.model.entity.Patient;
import com.example.CareConnect_hcl.repository.AppointmentRepository;
import com.example.CareConnect_hcl.repository.VideoConsultationRepository;
import com.example.CareConnect_hcl.services.VideoConsultationService;
import com.example.CareConnect_hcl.services.VideoProvider;

class VideoConsultationServiceTest {
    private AppointmentRepository appointments;
    private RecordingProvider provider;
    private VideoConsultationService service;
    private Appointment appointment;
    @BeforeEach void setUp() {
        provider = new RecordingProvider();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(CurrentAccountService.REQUEST_ATTRIBUTE, new Account("patient@example.invalid", "hash", "PATIENT", "Patient", 1L));
        appointment = new Appointment(); Patient patient = new Patient(); patient.setId(2L); appointment.setPatient(patient);
        Doctor doctor = new Doctor(); doctor.setDocId(3L); appointment.setDoctor(doctor);
        appointment.setAppointmentDateTime(LocalDateTime.now()); appointment.setStatus("SCHEDULED");
        appointments = (AppointmentRepository) Proxy.newProxyInstance(AppointmentRepository.class.getClassLoader(), new Class<?>[] { AppointmentRepository.class },
                (proxy, method, args) -> method.getName().equals("findById") ? Optional.of(appointment) : null);
        VideoConsultationRepository consultations = (VideoConsultationRepository) Proxy.newProxyInstance(VideoConsultationRepository.class.getClassLoader(), new Class<?>[] { VideoConsultationRepository.class }, (proxy, method, args) -> null);
        service = new VideoConsultationService(new CurrentAccountService(request), appointments, consultations, provider);
    }
    @Test void patientCannotRequestAnotherPatientsRoomToken() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.join(8L));
        assertEquals(404, error.getStatusCode().value()); assertEquals(0, provider.roomCalls);
    }
    @Test void cancelledAppointmentCannotProvisionRoom() {
        Patient patient = new Patient(); patient.setId(1L); appointment.setPatient(patient); appointment.setStatus("CANCELLED");
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.join(8L));
        assertEquals(409, error.getStatusCode().value()); assertEquals(0, provider.roomCalls);
    }
    private static final class RecordingProvider implements VideoProvider {
        int roomCalls;
        public boolean isConfigured() { return true; }
        public Room createRoom(long expiresAt) { roomCalls++; return new Room("room", "https://example.daily.co/room"); }
        public String createParticipantToken(String room, String name, String userId, boolean owner, long nbf, long exp) { return "token"; }
    }
}
