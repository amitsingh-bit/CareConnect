package com.example.CareConnect_hcl.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CareConnect_hcl.model.entity.MedicalRecord;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    java.util.List<MedicalRecord> findByPatient_IdOrderByRecordedAtDesc(Long patientId);
    java.util.List<MedicalRecord> findByPatient_IdIn(java.util.Collection<Long> patientIds);
    java.util.List<MedicalRecord> findByDoctor_DocidOrderByRecordedAtDesc(Long doctorId);
    boolean existsByPatient_IdAndDoctor_Docid(Long patientId, Long doctorId);
}
