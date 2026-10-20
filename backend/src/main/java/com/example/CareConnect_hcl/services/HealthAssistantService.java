package com.example.CareConnect_hcl.services;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.CareConnect_hcl.config.CurrentAccountService;
import com.example.CareConnect_hcl.model.dto.AssistantChatRequest;
import com.example.CareConnect_hcl.model.dto.AssistantChatResponse;

@Service
public class HealthAssistantService {
    private static final int MAX_MESSAGE = 2000;
    private static final ConcurrentHashMap<Long, ArrayDeque<Instant>> REQUESTS = new ConcurrentHashMap<>();
    private static final String NOTICE = "For personal medical decisions, contact your care team. This assistant does not diagnose or prescribe.";
    private final CurrentAccountService currentAccount;
    private final AiProvider provider;
    public HealthAssistantService(CurrentAccountService currentAccount, AiProvider provider) {
        this.currentAccount = currentAccount; this.provider = provider;
    }

    public AssistantChatResponse chat(AssistantChatRequest request) {
        var account = currentAccount.requireRole("PATIENT");
        if (request == null || request.message() == null || request.message().isBlank() || request.message().length() > MAX_MESSAGE)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a message up to 2,000 characters.");
        String language = "hi".equalsIgnoreCase(request.language()) ? "hi" : "en";
        if (isUrgent(request.message())) return new AssistantChatResponse(language.equals("hi")
                ? "आपके बताए लक्षणों में तुरंत चिकित्सा सहायता की आवश्यकता हो सकती है। अभी स्थानीय आपातकालीन सेवा से संपर्क करें या निकटतम आपातकालीन विभाग जाएँ।"
                : "These symptoms may need urgent medical attention. Contact your local emergency service or go to the nearest emergency department now.", true, NOTICE);
        enforceRateLimit(account.getId());
        List<AiProvider.ChatMessage> history = new ArrayList<>();
        if (request.history() != null) {
            if (request.history().size() > 20) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conversation history is too long.");
            request.history().forEach(turn -> {
                if (turn == null || turn.content() == null || turn.content().length() > MAX_MESSAGE ||
                        !("user".equals(turn.role()) || "assistant".equals(turn.role())))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conversation history is invalid.");
                history.add(new AiProvider.ChatMessage(turn.role(), turn.content()));
            });
        }
        history.add(new AiProvider.ChatMessage("user", request.message().trim()));
        return new AssistantChatResponse(provider.complete(language, history), false, NOTICE);
    }

    private void enforceRateLimit(Long accountId) {
        Instant now = Instant.now();
        ArrayDeque<Instant> recent = REQUESTS.computeIfAbsent(accountId, ignored -> new ArrayDeque<>());
        synchronized (recent) {
            while (!recent.isEmpty() && recent.peekFirst().isBefore(now.minusSeconds(60))) recent.removeFirst();
            if (recent.size() >= 10) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Please wait before sending another message.");
            recent.addLast(now);
        }
    }

    private boolean isUrgent(String input) {
        String text = input.toLowerCase(Locale.ROOT);
        return List.of("chest pain", "can't breathe", "cannot breathe", "difficulty breathing", "stroke", "unconscious",
                "severe bleeding", "suicid", "सीने में दर्द", "सांस नहीं", "साँस नहीं", "बेहोश", "आत्महत्या")
                .stream().anyMatch(text::contains);
    }
}
