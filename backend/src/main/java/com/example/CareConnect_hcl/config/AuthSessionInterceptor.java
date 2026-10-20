package com.example.CareConnect_hcl.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.example.CareConnect_hcl.services.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.CareConnect_hcl.model.entity.Account;
import com.example.CareConnect_hcl.repository.AccountRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthSessionInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(AuthSessionInterceptor.class);
    private static final String ACCOUNT_ID_SESSION_KEY = "careconnect.accountId";
    private static final String CSRF_SESSION_KEY = "careconnect.csrf";
    private static final String ROLE_REQUEST_KEY = "careconnect.auditRole";
    private static final String CORRELATION_REQUEST_KEY = "careconnect.correlationId";
    private static final Pattern RESOURCE_ID_PATTERN = Pattern.compile("/(\\d+)(?:/|$)");
    private final boolean csrfEnabled;
    private final AccountRepository accountRepository;
    private final AuditLogService auditLogService;

    public AuthSessionInterceptor(AccountRepository accountRepository, AuditLogService auditLogService,
            @Value("${careconnect.security.csrf.enabled:true}") boolean csrfEnabled) {
        this.accountRepository = accountRepository;
        this.auditLogService = auditLogService;
        this.csrfEnabled = csrfEnabled;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!request.getRequestURI().contains("/api/")) return;
        Object accountId = request.getSession(false) == null ? null : request.getSession(false).getAttribute(ACCOUNT_ID_SESSION_KEY);
        if (!(accountId instanceof Long)) accountId = request.getAttribute(ACCOUNT_ID_SESSION_KEY);
        Long id = accountId instanceof Long value ? value : null;
        String path = request.getRequestURI().substring(request.getContextPath().length());
        Account account = request.getAttribute(CurrentAccountService.REQUEST_ATTRIBUTE) instanceof Account current ? current : null;
        try {
            auditLogService.record(id, account == null ? (String) request.getAttribute(ROLE_REQUEST_KEY) : account.getRole(),
                    request.getMethod(), action(request.getMethod(), path, response.getStatus()), resourceType(path), resourceId(path),
                    (String) request.getAttribute(CORRELATION_REQUEST_KEY), path.substring(0, Math.min(path.length(), 255)), response.getStatus());
        } catch (RuntimeException auditFailure) {
            log.error("Could not persist API audit event for {} {}", request.getMethod(), path, auditFailure);
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String correlationId = UUID.randomUUID().toString();
        request.setAttribute(CORRELATION_REQUEST_KEY, correlationId);
        response.setHeader("X-Correlation-ID", correlationId);
        if (path.equals("/api/auth/login") || path.equals("/api/auth/signup") || path.equals("/api/auth/csrf")) return true;
        var session = request.getSession(false);
        if (csrfEnabled && isMutation(request.getMethod())) {
            Object expected = session == null ? null : session.getAttribute(CSRF_SESSION_KEY);
            String supplied = request.getHeader("X-XSRF-TOKEN");
            if (!(expected instanceof String token) || supplied == null ||
                    !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
                recordDenied(request, path, HttpServletResponse.SC_FORBIDDEN);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"CSRF token is missing or invalid\"}");
                return false;
            }
        }
        Object sessionAccountId = session == null ? null : session.getAttribute(ACCOUNT_ID_SESSION_KEY);
        if (sessionAccountId instanceof Long accountId) {
            Account account = accountRepository.findById(accountId).orElse(null);
            if (account != null) {
                if (!isAllowed(account.getRole(), request.getMethod(), path)) {
                    request.setAttribute(ACCOUNT_ID_SESSION_KEY, accountId);
                    request.setAttribute(ROLE_REQUEST_KEY, account.getRole());
                    recordDenied(request, path, HttpServletResponse.SC_FORBIDDEN);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"Your account does not have access to this resource\"}");
                    return false;
                }
                request.setAttribute(CurrentAccountService.REQUEST_ATTRIBUTE, account);
                request.setAttribute(ROLE_REQUEST_KEY, account.getRole());
                return true;
            }
        }
        recordDenied(request, path, HttpServletResponse.SC_UNAUTHORIZED);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"Please sign in\"}");
        return false;
    }

    private void recordDenied(HttpServletRequest request, String path, int status) {
        Object accountId = request.getAttribute(ACCOUNT_ID_SESSION_KEY);
        if (!(accountId instanceof Long)) {
            var session = request.getSession(false);
            accountId = session == null ? null : session.getAttribute(ACCOUNT_ID_SESSION_KEY);
        }
        String role = request.getAttribute(ROLE_REQUEST_KEY) instanceof String value ? value : null;
        try { auditLogService.record(accountId instanceof Long id ? id : null, role, request.getMethod(),
                action(request.getMethod(), path, status), resourceType(path), resourceId(path),
                (String) request.getAttribute(CORRELATION_REQUEST_KEY), path.substring(0, Math.min(path.length(), 255)), status); }
        catch (RuntimeException auditFailure) { log.error("Could not persist denied API audit event for {} {}", request.getMethod(), path, auditFailure); }
    }

    private static String action(String method, String path, int status) {
        String lower = path.toLowerCase();
        if (lower.equals("/api/auth/login")) return status >= 400 ? "LOGIN_FAILED" : "LOGIN";
        if (lower.equals("/api/auth/logout")) return "LOGOUT";
        if (status >= 400) return "DENIED";
        if (lower.contains("/assistant/")) return "AI_CHAT";
        if (lower.contains("/video/")) return "VIDEO_JOIN";
        return switch (method.toUpperCase()) { case "GET", "HEAD" -> "READ"; case "POST" -> "CREATE"; case "PUT", "PATCH" -> "UPDATE"; case "DELETE" -> "DELETE"; default -> "REQUEST"; };
    }
    private static String resourceType(String path) {
        String[] parts = path.split("/");
        if (parts.length < 3) return "API";
        if (parts.length > 4 && "appointments".equals(parts[2])) return parts[4].replaceAll("[^a-zA-Z_-]", "");
        return parts[2].replaceAll("[^a-zA-Z_-]", "");
    }
    private static String resourceId(String path) {
        Matcher matcher = RESOURCE_ID_PATTERN.matcher(path);
        return matcher.find() ? matcher.group(1) : null;
    }

    private boolean isMutation(String method) {
        return !HttpMethod.GET.matches(method) && !HttpMethod.HEAD.matches(method) && !HttpMethod.OPTIONS.matches(method);
    }

    private boolean isAllowed(String role, String method, String path) {
        String normalizedRole = role == null ? "" : role.toUpperCase();
        String verb = method.toUpperCase();
        boolean read = HttpMethod.GET.matches(verb);
        boolean write = HttpMethod.POST.matches(verb) || HttpMethod.PUT.matches(verb) || HttpMethod.PATCH.matches(verb);
        boolean delete = HttpMethod.DELETE.matches(verb);
        boolean admin = "ADMIN".equals(normalizedRole);

        if (path.startsWith("/api/auth/")) return true;
        if (path.startsWith("/api/assistant")) return normalizedRole.equals("PATIENT") && write;
        if (path.startsWith("/api/appointments")) return read || (write && (admin || normalizedRole.equals("DOCTOR") || normalizedRole.equals("PATIENT"))) || (delete && admin);
        if (path.startsWith("/api/patients")) {
            if (path.contains("/nurses")) return admin;
            return read || (write && (admin || normalizedRole.equals("PATIENT"))) || (delete && admin);
        }
        if (path.startsWith("/api/doctors")) return read || admin;
        if (path.startsWith("/api/medical-records")) return read && !admin || write && normalizedRole.equals("DOCTOR");
        if (path.startsWith("/api/prescriptions")) return read && (normalizedRole.equals("PATIENT") || normalizedRole.equals("DOCTOR")) || write && normalizedRole.equals("DOCTOR");
        if (path.startsWith("/api/lab-reports")) return read && !admin || write && normalizedRole.equals("DOCTOR");
        if (path.startsWith("/api/diagnoses")) return read && !admin || write && normalizedRole.equals("DOCTOR");
        if (path.startsWith("/api/medications")) return read && (admin || normalizedRole.equals("DOCTOR")) || ((write || delete) && admin);
        if (path.startsWith("/api/nurses") || path.startsWith("/api/admins")) return admin;
        if (path.startsWith("/api/vitals")) return read && !admin || write && (normalizedRole.equals("DOCTOR") || normalizedRole.equals("NURSE"));
        if (path.startsWith("/api/notifications")) return read;
        return false;
    }
}
