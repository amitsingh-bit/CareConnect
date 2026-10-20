package com.example.CareConnect_hcl.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.CareConnect_hcl.model.entity.VideoConsultation;

public interface VideoConsultationRepository extends JpaRepository<VideoConsultation, Long> {
    Optional<VideoConsultation> findByAppointment_Id(Long appointmentId);
}
