package com.example.CareConnect_hcl.model.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer age;
    private String disease;
    private String gender;
    private String email;
    private String phone;
    private String bloodGroup;
    private String address;
    private String emergencyContact;

    @OneToMany(mappedBy = "patient", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonManagedReference("patient-appointments")
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonManagedReference("patient-medical-records")
    private List<MedicalRecord> medicalRecords = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "patient_nurse_assignments", joinColumns = @JoinColumn(name = "patient_id"), inverseJoinColumns = @JoinColumn(name = "nurse_id"))
    private Set<Nurse> assignedNurses = new LinkedHashSet<>();

    public Patient() {
    }

    public Patient(String name, int age, String disease) {
        this.name = name;
        this.age = age;
        this.disease = disease;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getDisease() {
        return disease;
    }

    public void setDisease(String disease) {
        this.disease = disease;
    }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public List<Appointment> getAppointments() { return appointments; }
    public void setAppointments(List<Appointment> appointments) {
        this.appointments.clear();
        if (appointments != null) {
            appointments.forEach(appointment -> {
                appointment.setPatient(this);
                this.appointments.add(appointment);
            });
        }
    }

    public List<MedicalRecord> getMedicalRecords() { return medicalRecords; }
    public void setMedicalRecords(List<MedicalRecord> medicalRecords) {
        this.medicalRecords.clear();
        if (medicalRecords != null) {
            medicalRecords.forEach(record -> {
                record.setPatient(this);
                this.medicalRecords.add(record);
            });
        }
    }

    @JsonIgnore
    public Set<Nurse> getAssignedNurses() { return assignedNurses; }
    public void setAssignedNurses(Set<Nurse> assignedNurses) { this.assignedNurses = assignedNurses == null ? new LinkedHashSet<>() : new LinkedHashSet<>(assignedNurses); }
    public void assignNurse(Nurse nurse) { assignedNurses.add(nurse); }
    public void removeNurse(Nurse nurse) { assignedNurses.remove(nurse); }
}
