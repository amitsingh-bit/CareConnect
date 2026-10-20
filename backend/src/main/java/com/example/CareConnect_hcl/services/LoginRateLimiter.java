package com.example.CareConnect_hcl.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Process-local login throttling keyed by remote address and a one-way email digest. */
@Component
public class LoginRateLimiter {
    private static final int MAX_ATTEMPTS = 5;
    private static final int MAX_BUCKETS = 10_000;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public void check(String remoteAddress, String email) {
        String key = key(remoteAddress, email);
        Bucket bucket = buckets.get(key);
        if (bucket == null) {
            if (buckets.size() >= MAX_BUCKETS) {
                Instant cutoff = Instant.now().minus(WINDOW);
                buckets.entrySet().removeIf(entry -> entry.getValue().expired(cutoff));
                if (buckets.size() >= MAX_BUCKETS) {
                    throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                            "Sign-in is temporarily busy. Please try again later.");
                }
            }
            bucket = buckets.computeIfAbsent(key, ignored -> new Bucket());
        }
        synchronized (bucket) {
            bucket.prune(Instant.now());
            if (bucket.attempts.size() >= MAX_ATTEMPTS) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Too many sign-in attempts. Please try again later.");
            }
            bucket.attempts.addLast(Instant.now());
        }
    }

    public void succeeded(String remoteAddress, String email) {
        buckets.remove(key(remoteAddress, email));
    }

    private static String key(String remoteAddress, String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase(java.util.Locale.ROOT);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(normalized.getBytes(StandardCharsets.UTF_8));
            return (remoteAddress == null ? "unknown" : remoteAddress) + ":" + java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    private static final class Bucket {
        private final ArrayDeque<Instant> attempts = new ArrayDeque<>();
        private synchronized boolean expired(Instant cutoff) { return attempts.isEmpty() || attempts.peekLast().isBefore(cutoff); }
        private void prune(Instant now) {
            while (!attempts.isEmpty() && attempts.peekFirst().isBefore(now.minus(WINDOW))) attempts.removeFirst();
        }
    }
}
