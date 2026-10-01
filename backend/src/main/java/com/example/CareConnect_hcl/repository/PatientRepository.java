package com.example.CareConnect_hcl.repository;

import com.example.CareConnect_hcl.model.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    java.util.List<Patient> findByAssignedNurses_Id(Long nurseId);
    boolean existsByIdAndAssignedNurses_Id(Long patientId, Long nurseId);
}
