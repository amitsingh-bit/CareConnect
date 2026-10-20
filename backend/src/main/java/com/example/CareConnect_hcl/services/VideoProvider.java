package com.example.CareConnect_hcl.services;

public interface VideoProvider {
    record Room(String name, String url) {}
    boolean isConfigured();
    Room createRoom(long expiresAtEpochSeconds);
    String createParticipantToken(String roomName, String displayName, String userId, boolean owner, long notBeforeEpochSeconds, long expiresAtEpochSeconds);
}
