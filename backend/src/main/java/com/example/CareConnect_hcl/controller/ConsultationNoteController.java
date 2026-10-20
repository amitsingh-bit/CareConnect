package com.example.CareConnect_hcl.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.CareConnect_hcl.model.dto.ConsultationNoteRequest;
import com.example.CareConnect_hcl.model.dto.ConsultationNoteResponse;
import com.example.CareConnect_hcl.model.dto.ConsultationNoteVersionResponse;
import com.example.CareConnect_hcl.services.ConsultationNoteService;

@RestController
@RequestMapping("/api/appointments/{appointmentId}/consultation-note")
public class ConsultationNoteController {
    private final ConsultationNoteService service;
    public ConsultationNoteController(ConsultationNoteService service) { this.service = service; }
    @GetMapping public ConsultationNoteResponse get(@PathVariable Long appointmentId) { return service.get(appointmentId); }
    @GetMapping("/versions") public java.util.List<ConsultationNoteVersionResponse> versions(@PathVariable Long appointmentId) { return service.getVersions(appointmentId); }
    @PutMapping public ConsultationNoteResponse save(@PathVariable Long appointmentId, @RequestBody ConsultationNoteRequest request) { return service.save(appointmentId, request); }
}
