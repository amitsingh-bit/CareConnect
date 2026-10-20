package com.example.CareConnect_hcl.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.CareConnect_hcl.model.entity.ConsultationNote;

public interface ConsultationNoteRepository extends JpaRepository<ConsultationNote, Long> {
    Optional<ConsultationNote> findByAppointment_Id(Long appointmentId);
}
