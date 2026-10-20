package com.example.CareConnect_hcl.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "consultation_note_versions", indexes = @Index(name = "idx_note_version_note_time", columnList = "note_id, amended_at"))
public class ConsultationNoteVersion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "note_id", nullable = false) private ConsultationNote note;
    @Column(nullable = false) private Long amendedByDoctorId;
    @Column(nullable = false) private LocalDateTime amendedAt;
    @Column(length = 3000) private String chiefComplaint;
    @Column(length = 5000) private String history;
    @Column(length = 5000) private String examination;
    @Column(length = 5000) private String assessment;
    @Column(length = 3000) private String diagnosis;
    @Column(length = 5000) private String plan;
    @Column(length = 2000) private String followUp;
    private LocalDate followUpDate;

    protected ConsultationNoteVersion() {}
    public ConsultationNoteVersion(ConsultationNote note, Long doctorId, LocalDateTime amendedAt) {
        this.note = note; this.amendedByDoctorId = doctorId; this.amendedAt = amendedAt;
        this.chiefComplaint = note.getChiefComplaint(); this.history = note.getHistory(); this.examination = note.getExamination();
        this.assessment = note.getAssessment(); this.diagnosis = note.getDiagnosis(); this.plan = note.getPlan();
        this.followUp = note.getFollowUp(); this.followUpDate = note.getFollowUpDate();
    }
    public Long getId() { return id; }
    public Long getAmendedByDoctorId() { return amendedByDoctorId; }
    public LocalDateTime getAmendedAt() { return amendedAt; }
    public String getChiefComplaint() { return chiefComplaint; }
    public String getHistory() { return history; }
    public String getExamination() { return examination; }
    public String getAssessment() { return assessment; }
    public String getDiagnosis() { return diagnosis; }
    public String getPlan() { return plan; }
    public String getFollowUp() { return followUp; }
    public LocalDate getFollowUpDate() { return followUpDate; }
}
