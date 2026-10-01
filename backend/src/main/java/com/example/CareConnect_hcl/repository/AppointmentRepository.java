package com.example.CareConnect_hcl.repository;

import com.example.CareConnect_hcl.model.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    java.util.List<Appointment> findByPatient_IdOrderByAppointmentDateTimeDesc(Long patientId);
    java.util.List<Appointment> findByPatient_IdIn(java.util.Collection<Long> patientIds);
    java.util.List<Appointment> findByDoctor_DocidOrderByAppointmentDateTimeAsc(Long doctorId);
    boolean existsByPatient_IdAndDoctor_Docid(Long patientId, Long doctorId);
}
