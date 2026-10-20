package com.example.CareConnect_hcl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.example.CareConnect_hcl.model.entity.ConsultationNoteVersion;

public interface ConsultationNoteVersionRepository extends JpaRepository<ConsultationNoteVersion, Long> {
    List<ConsultationNoteVersion> findAllByNote_IdOrderByAmendedAtDesc(Long noteId);
}
