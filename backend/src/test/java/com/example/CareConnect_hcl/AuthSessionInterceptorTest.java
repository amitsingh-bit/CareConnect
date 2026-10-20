package com.example.CareConnect_hcl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Optional;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import com.example.CareConnect_hcl.config.AuthSessionInterceptor;
import com.example.CareConnect_hcl.config.CurrentAccountService;
import com.example.CareConnect_hcl.model.entity.Account;
import com.example.CareConnect_hcl.repository.AccountRepository;
import com.example.CareConnect_hcl.repository.AuditLogRepository;
import com.example.CareConnect_hcl.services.AuditLogService;

class AuthSessionInterceptorTest {
    private static final String SESSION_KEY = "careconnect.accountId";
    private AccountRepository accounts;
    private AuthSessionInterceptor interceptor;
    private Account currentAccount;
    private AuditLogService auditLogs;
    private java.util.List<com.example.CareConnect_hcl.model.entity.AuditLog> recorded;

    @BeforeEach
    void setUp() {
        accounts = (AccountRepository) Proxy.newProxyInstance(AccountRepository.class.getClassLoader(),
                new Class<?>[] { AccountRepository.class }, (proxy, method, args) -> {
                    if (method.getName().equals("findById")) return Optional.ofNullable(currentAccount);
                    if (method.getName().equals("toString")) return "TestAccountRepository";
                    return null;
                });
        recorded = new ArrayList<>();
        AuditLogRepository auditRepository = (AuditLogRepository) Proxy.newProxyInstance(AuditLogRepository.class.getClassLoader(),
                new Class<?>[] { AuditLogRepository.class }, (proxy, method, args) -> {
                    if (method.getName().equals("save")) { recorded.add((com.example.CareConnect_hcl.model.entity.AuditLog) args[0]); return args[0]; }
                    if (method.getName().equals("toString")) return "TestAuditLogRepository";
                    return null;
                });
        auditLogs = new AuditLogService(auditRepository);
        interceptor = new AuthSessionInterceptor(accounts, auditLogs, true);
    }

    @Test
    void appliesRoleBasedEndpointRules() throws Exception {
        assertAllowed("PATIENT", "GET", "/api/appointments");
        assertForbidden("PATIENT", "GET", "/api/admins");
        assertAllowed("DOCTOR", "POST", "/api/diagnoses");
        assertForbidden("NURSE", "GET", "/api/prescriptions");
        assertAllowed("NURSE", "GET", "/api/lab-reports");
        assertAllowed("ADMIN", "GET", "/api/admins");
        assertForbidden("ADMIN", "GET", "/api/medical-records");
        assertForbidden("ADMIN", "GET", "/api/diagnoses");
        assertForbidden("ADMIN", "GET", "/api/prescriptions");
        assertForbidden("ADMIN", "GET", "/api/lab-reports");
        assertForbidden("ADMIN", "GET", "/api/vitals");
    }

    @Test
    void rejectsRequestsWithoutAnAuthenticatedSession() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/patients");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
        org.junit.jupiter.api.Assertions.assertNotNull(response.getHeader("X-Correlation-ID"));
        assertEquals(1, recorded.size());
    }

    @Test
    void rejectsStateChangingRequestsWithoutCsrfToken() throws Exception {
        MockHttpServletRequest request = request("ADMIN", "POST", "/api/vitals");
        request.removeHeader("X-XSRF-TOKEN");
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(403, response.getStatus());
        assertEquals(1, recorded.size());
    }

    private void assertAllowed(String role, String method, String path) throws Exception {
        MockHttpServletRequest request = request(role, method, path);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()), role + " should access " + path);
        assertEquals(role, ((Account) request.getAttribute(CurrentAccountService.REQUEST_ATTRIBUTE)).getRole());
    }

    private void assertForbidden(String role, String method, String path) throws Exception {
        MockHttpServletRequest request = request(role, method, path);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()), role + " should not access " + path);
        assertEquals(403, response.getStatus());
    }

    private MockHttpServletRequest request(String role, String method, String path) {
        Account account = new Account("test@example.invalid", "hash", role, "Test", 1L);
        currentAccount = account;
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SESSION_KEY, 1L);
        session.setAttribute("careconnect.csrf", "test-csrf");
        request.setSession(session);
        if (!method.equals("GET")) request.addHeader("X-XSRF-TOKEN", "test-csrf");
        return request;
    }
}
