package com.example.CareConnect_hcl.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CareConnect_hcl.model.entity.Diagnosis;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {
    java.util.List<Diagnosis> findByPatient_IdOrderByDiagnosedAtDesc(Long patientId);
    java.util.List<Diagnosis> findByPatient_IdIn(java.util.Collection<Long> patientIds);
    java.util.List<Diagnosis> findByDoctor_DocidOrderByDiagnosedAtDesc(Long doctorId);
    boolean existsByPatient_IdAndDoctor_Docid(Long patientId, Long doctorId);
}
