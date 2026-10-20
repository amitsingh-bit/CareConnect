package com.example.CareConnect_hcl.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CareConnect_hcl.model.dto.AssistantChatRequest;
import com.example.CareConnect_hcl.model.dto.AssistantChatResponse;
import com.example.CareConnect_hcl.services.HealthAssistantService;

@RestController
@RequestMapping("/api/assistant")
public class HealthAssistantController {
    private final HealthAssistantService service;
    public HealthAssistantController(HealthAssistantService service) { this.service = service; }
    @PostMapping("/chat") public AssistantChatResponse chat(@RequestBody AssistantChatRequest request) { return service.chat(request); }
}
