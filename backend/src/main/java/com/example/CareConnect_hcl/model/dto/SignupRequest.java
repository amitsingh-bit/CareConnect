package com.example.CareConnect_hcl.model.dto;

public record SignupRequest(
        String role,
        String name,
        String email,
        String password,
        Integer age,
        String disease,
        Integer experience,
        String department,
        String phoneNumber,
        String adminSignupCode) {
}
