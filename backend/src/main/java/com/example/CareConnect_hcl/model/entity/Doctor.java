package com.example.CareConnect_hcl.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "doctor")
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long docid;
    private String name;
    private Integer experience;
    private String department;
    public Doctor() {
    }
    public Doctor(String name, Integer experience, String department) {
        this.name = name;
        this.experience = experience;
        this.department = department;
    }
    public Long getDocId() {
        return docid;
    }
    public void setDocId(Long docid) {
        this.docid = docid;
    }
    public String getName() {
        return name;
    }
    public Integer getExperience() {
        return experience;
    }
    public String getDepartment() {
        return department;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setExperience(Integer experience) {
        this.experience = experience;
    }
    public void setDepartment(String department) {
        this.department = department;
    }
}
