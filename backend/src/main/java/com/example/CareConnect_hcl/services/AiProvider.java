package com.example.CareConnect_hcl.services;

import java.util.List;

public interface AiProvider {
    String complete(String language, List<ChatMessage> messages);
    boolean isConfigured();
    record ChatMessage(String role, String content) {}
}
