package com.example.CareConnect_hcl.model.dto;

import java.time.LocalDateTime;

public record ConsultationNoteResponse(Long id, Long appointmentId, Long patientId, String patientName,
        Long doctorId, String doctorName, String chiefComplaint, String history, String examination, String assessment,
        String diagnosis, String plan, String followUp, java.time.LocalDate followUpDate,
        LocalDateTime createdAt, LocalDateTime updatedAt) {}
