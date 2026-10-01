package com.example.CareConnect_hcl.model.entity;

import java.time.LocalDate;

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
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medical_record_id")
    @JsonBackReference("record-prescriptions")
    private MedicalRecord medicalRecord;

    // Retained to satisfy the existing MySQL schema while prescriptions migrate to medicalRecord.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonIgnore
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id")
    private Medication medication;

    private String medicationName;
    private String dosage;
    private String frequency;
    private String duration;
    private String instructions;
    private LocalDate prescribedDate;
    private String status;

    public Prescription() {
    }

    public Prescription(MedicalRecord medicalRecord, Doctor doctor, Medication medication, String dosage, String frequency,
            String duration, String instructions, LocalDate prescribedDate) {
        this.medicalRecord = medicalRecord;
        this.patient = medicalRecord == null ? null : medicalRecord.getPatient();
        this.doctor = doctor;
        this.medication = medication;
        this.medicationName = medication == null ? null : medication.getName();
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.instructions = instructions;
        this.prescribedDate = prescribedDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MedicalRecord getMedicalRecord() { return medicalRecord; }

    public void setMedicalRecord(MedicalRecord medicalRecord) { this.medicalRecord = medicalRecord; }

    public Long getMedicalRecordId() { return medicalRecord == null ? null : medicalRecord.getId(); }

    public Patient getPatient() { return patient; }

    public Long getPatientId() { return patient == null ? null : patient.getId(); }

    public String getPatientName() { return patient == null ? null : patient.getName(); }

    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getDoctor() { return doctor; }

    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public Long getDoctorId() { return doctor == null ? null : doctor.getDocId(); }

    public String getDoctorName() { return doctor == null ? null : doctor.getName(); }

    public Medication getMedication() { return medication; }

    public Long getMedicationId() { return medication == null ? null : medication.getId(); }

    public void setMedication(Medication medication) {
        this.medication = medication;
        this.medicationName = medication == null ? null : medication.getName();
    }

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public LocalDate getPrescribedDate() {
        return prescribedDate;
    }

    public void setPrescribedDate(LocalDate prescribedDate) {
        this.prescribedDate = prescribedDate;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
