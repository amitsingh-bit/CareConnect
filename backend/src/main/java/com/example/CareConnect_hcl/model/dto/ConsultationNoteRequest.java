package com.example.CareConnect_hcl.model.dto;

import java.time.LocalDate;

public record ConsultationNoteRequest(String chiefComplaint, String history, String examination, String assessment,
        String diagnosis, String plan, String followUp, LocalDate followUpDate) {}
