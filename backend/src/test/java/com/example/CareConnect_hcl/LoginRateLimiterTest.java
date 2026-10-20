package com.example.CareConnect_hcl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import com.example.CareConnect_hcl.services.LoginRateLimiter;

class LoginRateLimiterTest {
    @Test
    void limitsRepeatedAttemptsAndAllowsRetryAfterSuccessfulAuthentication() {
        LoginRateLimiter limiter = new LoginRateLimiter();
        for (int i = 0; i < 5; i++) limiter.check("192.0.2.1", "patient@example.invalid");
        assertThrows(ResponseStatusException.class, () -> limiter.check("192.0.2.1", "patient@example.invalid"));
        limiter.succeeded("192.0.2.1", "patient@example.invalid");
        limiter.check("192.0.2.1", "patient@example.invalid");
    }

    @Test
    void separatesAddressAndAccountBuckets() {
        LoginRateLimiter limiter = new LoginRateLimiter();
        for (int i = 0; i < 5; i++) limiter.check("192.0.2.1", "patient@example.invalid");
        limiter.check("192.0.2.2", "patient@example.invalid");
        limiter.check("192.0.2.1", "other@example.invalid");
    }
}
