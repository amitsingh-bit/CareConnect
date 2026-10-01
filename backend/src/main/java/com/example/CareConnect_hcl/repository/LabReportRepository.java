package com.example.CareConnect_hcl.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CareConnect_hcl.model.entity.LabReport;

public interface LabReportRepository extends JpaRepository<LabReport, Long> {
    java.util.List<LabReport> findByPatient_IdOrderByReportDateDesc(Long patientId);
    java.util.List<LabReport> findByPatient_IdIn(java.util.Collection<Long> patientIds);
    java.util.List<LabReport> findByDoctor_DocidOrderByReportDateDesc(Long doctorId);
    boolean existsByPatient_IdAndDoctor_Docid(Long patientId, Long doctorId);
}
