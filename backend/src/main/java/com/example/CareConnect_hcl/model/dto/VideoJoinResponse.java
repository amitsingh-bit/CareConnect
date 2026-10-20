package com.example.CareConnect_hcl.model.dto;

import java.time.LocalDateTime;

public record VideoJoinResponse(String joinUrl, String status, LocalDateTime appointmentDateTime) {}
