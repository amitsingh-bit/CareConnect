package com.example.CareConnect_hcl.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.http.HttpMethod;

import com.example.CareConnect_hcl.model.entity.Account;
import com.example.CareConnect_hcl.repository.AccountRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthSessionInterceptor implements HandlerInterceptor {
    private static final String ACCOUNT_ID_SESSION_KEY = "careconnect.accountId";
    private final AccountRepository accountRepository;

    public AuthSessionInterceptor(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        var session = request.getSession(false);
        Object sessionAccountId = session == null ? null : session.getAttribute(ACCOUNT_ID_SESSION_KEY);
        if (sessionAccountId instanceof Long accountId) {
            Account account = accountRepository.findById(accountId).orElse(null);
            if (account != null) {
                String path = request.getRequestURI().substring(request.getContextPath().length());
                if (!isAllowed(account.getRole(), request.getMethod(), path)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"Your account does not have access to this resource\"}");
                    return false;
                }
                request.setAttribute(CurrentAccountService.REQUEST_ATTRIBUTE, account);
                return true;
            }
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"Please sign in\"}");
        return false;
    }

    private boolean isAllowed(String role, String method, String path) {
        String normalizedRole = role == null ? "" : role.toUpperCase();
        String verb = method.toUpperCase();
        boolean read = HttpMethod.GET.matches(verb);
        boolean write = HttpMethod.POST.matches(verb) || HttpMethod.PUT.matches(verb) || HttpMethod.PATCH.matches(verb);
        boolean delete = HttpMethod.DELETE.matches(verb);
        boolean admin = "ADMIN".equals(normalizedRole);

        if (path.startsWith("/api/auth/")) return true;
        if (path.startsWith("/api/appointments")) return read || (write && (admin || normalizedRole.equals("DOCTOR") || normalizedRole.equals("PATIENT"))) || (delete && admin);
        if (path.startsWith("/api/patients")) {
            if (path.contains("/nurses")) return admin;
            return read || (write && (admin || normalizedRole.equals("PATIENT"))) || (delete && admin);
        }
        if (path.startsWith("/api/doctors")) return read || admin;
        if (path.startsWith("/api/medical-records")) return read || ((write || delete) && (admin || normalizedRole.equals("DOCTOR")));
        if (path.startsWith("/api/prescriptions")) return read || (write && (admin || normalizedRole.equals("DOCTOR"))) || (delete && admin);
        if (path.startsWith("/api/lab-reports")) return read || ((write || delete) && (admin || normalizedRole.equals("DOCTOR")));
        if (path.startsWith("/api/diagnoses")) return admin || normalizedRole.equals("DOCTOR") || (read && (normalizedRole.equals("PATIENT") || normalizedRole.equals("NURSE")));
        if (path.startsWith("/api/medications")) return read && (admin || normalizedRole.equals("DOCTOR")) || ((write || delete) && admin);
        if (path.startsWith("/api/nurses") || path.startsWith("/api/admins")) return admin;
        if (path.startsWith("/api/vitals")) return read || (write && (admin || normalizedRole.equals("DOCTOR") || normalizedRole.equals("NURSE"))) || (delete && admin);
        if (path.startsWith("/api/notifications")) return read;
        return false;
    }
}
