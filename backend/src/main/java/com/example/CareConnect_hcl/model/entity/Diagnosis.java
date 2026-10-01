package com.example.CareConnect_hcl.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "diagnoses")
public class Diagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medical_record_id")
    @JsonBackReference("record-diagnoses")
    private MedicalRecord medicalRecord;

    // Retained to satisfy the existing MySQL schema while records migrate to medicalRecord.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonIgnore
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    private String diagnosis;
    private String notes;
    private LocalDateTime diagnosedAt;

    public Diagnosis() {
    }

    public Diagnosis(MedicalRecord medicalRecord, Doctor doctor, String diagnosis, String notes, LocalDateTime diagnosedAt) {
        this.medicalRecord = medicalRecord;
        this.patient = medicalRecord == null ? null : medicalRecord.getPatient();
        this.doctor = doctor;
        this.diagnosis = diagnosis;
        this.notes = notes;
        this.diagnosedAt = diagnosedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public MedicalRecord getMedicalRecord() { return medicalRecord; }
    public void setMedicalRecord(MedicalRecord medicalRecord) { this.medicalRecord = medicalRecord; }
    public Long getMedicalRecordId() { return medicalRecord == null ? null : medicalRecord.getId(); }
    public Patient getPatient() { return patient; }
    public Long getPatientId() { return patient == null ? null : patient.getId(); }
    public String getPatientName() { return patient == null ? null : patient.getName(); }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Doctor getDoctor() { return doctor; }
    public Long getDoctorId() { return doctor == null ? null : doctor.getDocId(); }
    public String getDoctorName() { return doctor == null ? null : doctor.getName(); }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getDiagnosedAt() { return diagnosedAt; }
    public void setDiagnosedAt(LocalDateTime diagnosedAt) { this.diagnosedAt = diagnosedAt; }
}
