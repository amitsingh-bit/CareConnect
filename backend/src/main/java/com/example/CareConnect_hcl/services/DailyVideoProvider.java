package com.example.CareConnect_hcl.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class DailyVideoProvider implements VideoProvider {
    private final String apiKey;
    private final ObjectMapper mapper;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
    public DailyVideoProvider(@Value("${careconnect.video.daily-api-key:}") String apiKey, ObjectMapper mapper) {
        this.apiKey = apiKey == null ? "" : apiKey.trim(); this.mapper = mapper;
    }
    @Override public boolean isConfigured() { return !apiKey.isBlank(); }
    @Override public Room createRoom(long expiresAt) {
        JsonNode response = post("/rooms", Map.of("privacy", "private", "properties", Map.of("exp", expiresAt, "enable_prejoin_ui", true, "enable_recording", false)));
        String name = response.path("name").asText(""); String url = response.path("url").asText("");
        if (name.isBlank() || url.isBlank()) throw unavailable();
        return new Room(name, url);
    }
    @Override public String createParticipantToken(String roomName, String displayName, String userId, boolean owner, long nbf, long exp) {
        JsonNode response = post("/meeting-tokens", Map.of("properties", Map.of("room_name", roomName, "user_name", displayName,
                "user_id", userId, "is_owner", owner, "nbf", nbf, "exp", exp, "eject_at_token_exp", true)));
        String token = response.path("token").asText("");
        if (token.isBlank()) throw unavailable();
        return token;
    }
    private JsonNode post(String endpoint, Object payload) {
        if (!isConfigured()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Video consultations are not configured. Contact the service administrator.");
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.daily.co/v1" + endpoint)).timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + apiKey).header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload))).build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (result.statusCode() < 200 || result.statusCode() >= 300) throw unavailable();
            return mapper.readTree(result.body());
        } catch (ResponseStatusException error) { throw error; }
        catch (InterruptedException error) { Thread.currentThread().interrupt(); throw unavailable(); }
        catch (Exception error) { throw unavailable(); }
    }
    private static ResponseStatusException unavailable() { return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "The video provider is temporarily unavailable."); }
}
