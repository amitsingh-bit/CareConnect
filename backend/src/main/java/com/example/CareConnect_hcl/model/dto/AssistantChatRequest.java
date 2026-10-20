package com.example.CareConnect_hcl.model.dto;

import java.util.List;

public record AssistantChatRequest(String message, String language, List<Turn> history) {
    public record Turn(String role, String content) {}
}
