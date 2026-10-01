package com.example.CareConnect_hcl.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.CareConnect_hcl.model.entity.Vital;

public interface VitalRepository extends JpaRepository<Vital, Long> {
    List<Vital> findByPatient_IdInOrderByRecordedAtDesc(Collection<Long> patientIds);
    List<Vital> findByPatient_IdOrderByRecordedAtDesc(Long patientId);
}
