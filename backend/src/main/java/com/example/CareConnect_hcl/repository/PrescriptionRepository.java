package com.example.CareConnect_hcl.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CareConnect_hcl.model.entity.Prescription;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    java.util.List<Prescription> findByPatient_IdOrderByPrescribedDateDesc(Long patientId);
    java.util.List<Prescription> findByPatient_IdIn(java.util.Collection<Long> patientIds);
    java.util.List<Prescription> findByDoctor_DocidOrderByPrescribedDateDesc(Long doctorId);
    boolean existsByPatient_IdAndDoctor_Docid(Long patientId, Long doctorId);
}
