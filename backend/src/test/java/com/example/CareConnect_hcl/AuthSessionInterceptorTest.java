package com.example.CareConnect_hcl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import com.example.CareConnect_hcl.config.AuthSessionInterceptor;
import com.example.CareConnect_hcl.config.CurrentAccountService;
import com.example.CareConnect_hcl.model.entity.Account;
import com.example.CareConnect_hcl.repository.AccountRepository;

class AuthSessionInterceptorTest {
    private static final String SESSION_KEY = "careconnect.accountId";
    private AccountRepository accounts;
    private AuthSessionInterceptor interceptor;

    @BeforeEach
    void setUp() {
        accounts = org.mockito.Mockito.mock(AccountRepository.class);
        interceptor = new AuthSessionInterceptor(accounts);
    }

    @Test
    void appliesRoleBasedEndpointRules() throws Exception {
        assertAllowed("PATIENT", "GET", "/api/appointments");
        assertForbidden("PATIENT", "GET", "/api/admins");
        assertAllowed("DOCTOR", "POST", "/api/diagnoses");
        assertForbidden("NURSE", "GET", "/api/prescriptions");
        assertAllowed("NURSE", "GET", "/api/lab-reports");
        assertAllowed("ADMIN", "GET", "/api/admins");
    }

    @Test
    void rejectsRequestsWithoutAnAuthenticatedSession() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/patients");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
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
        when(accounts.findById(1L)).thenReturn(Optional.of(account));
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SESSION_KEY, 1L);
        request.setSession(session);
        return request;
    }
}
