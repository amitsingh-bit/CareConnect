package com.example.CareConnect_hcl.model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "medical_records")
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonBackReference("patient-medical-records")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    private String diagnosis;
    private String treatment;
    private String notes;
    private LocalDateTime recordedAt;

    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("record-diagnoses")
    private List<Diagnosis> diagnoses = new ArrayList<>();

    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("record-lab-reports")
    private List<LabReport> labReports = new ArrayList<>();

    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("record-prescriptions")
    private List<Prescription> prescriptions = new ArrayList<>();

    public MedicalRecord() {
    }

    public MedicalRecord(Patient patient, Doctor doctor, String diagnosis, String treatment,
            String notes, LocalDateTime recordedAt) {
        this.patient = patient;
        this.doctor = doctor;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.notes = notes;
        this.recordedAt = recordedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Long getPatientId() { return patient == null ? null : patient.getId(); }
    public String getPatientName() { return patient == null ? null : patient.getName(); }
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public Long getDoctorId() { return doctor == null ? null : doctor.getDocId(); }
    public String getDoctorName() { return doctor == null ? null : doctor.getName(); }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
    public List<Diagnosis> getDiagnoses() { return diagnoses; }
    public void setDiagnoses(List<Diagnosis> diagnoses) {
        this.diagnoses.clear();
        if (diagnoses != null) diagnoses.forEach(this::addDiagnosis);
    }
    public void addDiagnosis(Diagnosis diagnosis) {
        diagnoses.add(diagnosis);
        diagnosis.setMedicalRecord(this);
    }
    public List<LabReport> getLabReports() { return labReports; }
    public void setLabReports(List<LabReport> labReports) {
        this.labReports.clear();
        if (labReports != null) labReports.forEach(this::addLabReport);
    }
    public void addLabReport(LabReport labReport) {
        labReports.add(labReport);
        labReport.setMedicalRecord(this);
    }
    public List<Prescription> getPrescriptions() { return prescriptions; }
    public void setPrescriptions(List<Prescription> prescriptions) {
        this.prescriptions.clear();
        if (prescriptions != null) prescriptions.forEach(this::addPrescription);
    }
    public void addPrescription(Prescription prescription) {
        prescriptions.add(prescription);
        prescription.setMedicalRecord(this);
    }
}
