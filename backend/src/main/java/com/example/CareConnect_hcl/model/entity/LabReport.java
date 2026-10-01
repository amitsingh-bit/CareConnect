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
@Table(name = "lab_reports")
public class LabReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medical_record_id")
    @JsonBackReference("record-lab-reports")
    private MedicalRecord medicalRecord;

    // Retained to satisfy the existing MySQL schema while reports migrate to medicalRecord.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonIgnore
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    private String testName;
    private String result;
    private String referenceRange;
    private LocalDate reportDate;
    private String status;

    public LabReport() {
    }

    public LabReport(MedicalRecord medicalRecord, Doctor doctor, String testName, String result,
            String referenceRange, LocalDate reportDate) {
        this.medicalRecord = medicalRecord;
        this.patient = medicalRecord == null ? null : medicalRecord.getPatient();
        this.doctor = doctor;
        this.testName = testName;
        this.result = result;
        this.referenceRange = referenceRange;
        this.reportDate = reportDate;
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
    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getReferenceRange() { return referenceRange; }
    public void setReferenceRange(String referenceRange) { this.referenceRange = referenceRange; }
    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
