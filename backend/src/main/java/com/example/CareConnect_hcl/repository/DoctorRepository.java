package com.example.CareConnect_hcl.repository;

import com.example.CareConnect_hcl.model.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}