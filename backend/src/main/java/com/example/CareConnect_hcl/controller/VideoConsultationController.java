package com.example.CareConnect_hcl.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.CareConnect_hcl.model.dto.VideoJoinResponse;
import com.example.CareConnect_hcl.services.VideoConsultationService;

@RestController
@RequestMapping("/api/appointments/{appointmentId}/video")
public class VideoConsultationController {
    private final VideoConsultationService service;
    public VideoConsultationController(VideoConsultationService service) { this.service = service; }
    @PostMapping("/join") public VideoJoinResponse join(@PathVariable Long appointmentId) { return service.join(appointmentId); }
}
