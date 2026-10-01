package com.example.CareConnect_hcl.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CareConnect_hcl.model.entity.Nurse;

public interface NurseRepository extends JpaRepository<Nurse, Long> {
}
