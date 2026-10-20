package com.example.CareConnect_hcl.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ConsultationNoteVersionResponse(Long id, Long amendedByDoctorId, LocalDateTime amendedAt,
        String chiefComplaint, String history, String examination, String assessment, String diagnosis,
        String plan, String followUp, LocalDate followUpDate) {}
