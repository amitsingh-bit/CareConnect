package com.example.CareConnect_hcl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import com.example.CareConnect_hcl.config.CurrentAccountService;
import com.example.CareConnect_hcl.model.dto.AssistantChatRequest;
import com.example.CareConnect_hcl.model.entity.Account;
import com.example.CareConnect_hcl.services.AiProvider;
import com.example.CareConnect_hcl.services.HealthAssistantService;

class HealthAssistantServiceTest {
    private RecordingProvider provider;
    private HealthAssistantService service;
    @BeforeEach void setUp() {
        provider = new RecordingProvider();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(CurrentAccountService.REQUEST_ATTRIBUTE, new Account("patient@example.invalid", "hash", "PATIENT", "Patient", 1L));
        service = new HealthAssistantService(new CurrentAccountService(request), provider);
    }
    @Test void urgentSymptomsReturnImmediateGuidanceWithoutCallingProvider() {
        var response = service.chat(new AssistantChatRequest("I have chest pain", "en", List.of()));
        assertTrue(response.urgent()); assertTrue(response.reply().contains("urgent medical attention")); assertEquals(0, provider.calls);
    }
    @Test void ordinaryQuestionUsesConfiguredProviderAndKeepsSafetyNotice() {
        provider.reply = "सामान्य जानकारी";
        var response = service.chat(new AssistantChatRequest("लैब रिपोर्ट क्या है?", "hi", List.of()));
        assertEquals("सामान्य जानकारी", response.reply()); assertTrue(response.safetyNotice().contains("does not diagnose"));
        assertTrue(!response.urgent()); assertEquals(1, provider.calls);
    }
    private static final class RecordingProvider implements AiProvider {
        int calls; String reply = "";
        public boolean isConfigured() { return true; }
        public String complete(String language, List<AiProvider.ChatMessage> history) { calls++; return reply; }
    }
}
