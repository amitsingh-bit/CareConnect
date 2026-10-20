package com.example.CareConnect_hcl.model.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "video_consultations", uniqueConstraints = @UniqueConstraint(name = "uk_video_appointment", columnNames = "appointment_id"))
public class VideoConsultation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "appointment_id", nullable = false, unique = true) private Appointment appointment;
    @Column(nullable = false, length = 80) private String roomName;
    @Column(nullable = false, length = 500) private String roomUrl;
    @Column(nullable = false, length = 20) private String status;
    @Column(nullable = false) private LocalDateTime createdAt;
    @Column(nullable = false) private LocalDateTime updatedAt;
    protected VideoConsultation() {}
    public VideoConsultation(Appointment appointment, String roomName, String roomUrl, LocalDateTime now) {
        this.appointment = appointment; this.roomName = roomName; this.roomUrl = roomUrl; this.status = "READY"; this.createdAt = now; this.updatedAt = now;
    }
    public Long getId() { return id; }
    public Appointment getAppointment() { return appointment; }
    public String getRoomName() { return roomName; }
    public String getRoomUrl() { return roomUrl; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
