package com.example.CareConnect_hcl.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class OpenAiCompatibleProvider implements AiProvider {
    private static final String POLICY = "You are CareConnect's health information and navigation assistant. "
            + "Answer general health education and CareConnect navigation questions in the requested language. "
            + "Do not diagnose, prescribe, recommend dosage changes, or tell a person to stop treatment. "
            + "Do not present uncertain conclusions as facts. Explain medical terms in plain language and recommend discussing personal results with a licensed clinician. "
            + "For urgent symptoms, tell the person to seek urgent medical evaluation now. You do not have access to the user's medical records. "
            + "Do not claim to have consulted a source unless you actually cite a configured source. Keep answers concise.";
    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final ObjectMapper mapper;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public OpenAiCompatibleProvider(@Value("${careconnect.ai.api-key:}") String apiKey,
            @Value("${careconnect.ai.model:}") String model,
            @Value("${careconnect.ai.base-url:https://api.openai.com/v1}") String baseUrl,
            ObjectMapper mapper) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model == null ? "" : model.trim();
        this.baseUrl = baseUrl == null ? "" : baseUrl.replaceAll("/+$", "");
        this.mapper = mapper;
    }

    @Override public boolean isConfigured() { return !apiKey.isBlank() && !model.isBlank() && !baseUrl.isBlank(); }

    @Override public String complete(String language, List<ChatMessage> history) {
        if (!isConfigured()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "The health assistant is not configured. Please contact the service administrator.");
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", POLICY + ("hi".equals(language)
                ? " Respond in Hindi." : " Respond in English.")));
        history.stream().skip(Math.max(0, history.size() - 12)).forEach(turn ->
                messages.add(Map.of("role", turn.role(), "content", turn.content())));
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", model);
            payload.put("messages", messages);
            payload.put("temperature", 0.2);
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/chat/completions"))
                    .timeout(Duration.ofSeconds(35)).header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload))).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300)
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "The configured AI provider is temporarily unavailable.");
            JsonNode body = mapper.readTree(response.body());
            String answer = body.path("choices").path(0).path("message").path("content").asText("").trim();
            if (answer.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "The AI provider returned an empty response.");
            return answer;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "The AI request was interrupted.");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not reach the configured AI provider.");
        }
    }
}
