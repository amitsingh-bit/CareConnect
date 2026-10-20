package com.example.CareConnect_hcl.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.CareConnect_hcl.model.dto.AccountResponse;
import com.example.CareConnect_hcl.model.dto.LoginRequest;
import com.example.CareConnect_hcl.model.dto.SignupRequest;
import com.example.CareConnect_hcl.services.AuthService;
import com.example.CareConnect_hcl.services.LoginRateLimiter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String ACCOUNT_ID_SESSION_KEY = "careconnect.accountId";
    private final AuthService authService;
    private final LoginRateLimiter loginRateLimiter;
    @Value("${server.servlet.session.cookie.secure:false}")
    private boolean secureCookie;

    public AuthController(AuthService authService, LoginRateLimiter loginRateLimiter) {
        this.authService = authService;
        this.loginRateLimiter = loginRateLimiter;
    }

    @PostMapping("/signup")
    public AccountResponse signup(@RequestBody SignupRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        AccountResponse account = authService.register(request);
        startSession(httpRequest, account);
        issueCsrf(httpRequest, response);
        return account;
    }

    @PostMapping("/login")
    public AccountResponse login(@RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        if (request == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required");
        String email = request.email();
        String remoteAddress = httpRequest.getRemoteAddr();
        loginRateLimiter.check(remoteAddress, email);
        AccountResponse account = authService.login(request.email(), request.password());
        loginRateLimiter.succeeded(remoteAddress, email);
        startSession(httpRequest, account);
        issueCsrf(httpRequest, response);
        return account;
    }

    @GetMapping("/csrf")
    public void csrf(HttpServletRequest request, HttpServletResponse response) {
        issueCsrf(request, response);
    }

    @GetMapping("/me")
    public AccountResponse me(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(ACCOUNT_ID_SESSION_KEY) == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in");
        }
        return authService.getAccount((Long) session.getAttribute(ACCOUNT_ID_SESSION_KEY));
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object accountId = session.getAttribute(ACCOUNT_ID_SESSION_KEY);
            if (accountId instanceof Long id) request.setAttribute(ACCOUNT_ID_SESSION_KEY, id);
            session.invalidate();
        }
    }

    private void startSession(HttpServletRequest request, AccountResponse account) {
        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute(ACCOUNT_ID_SESSION_KEY, account.id());
    }

    private void issueCsrf(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);
        String token = UUID.randomUUID().toString();
        session.setAttribute("careconnect.csrf", token);
        String cookie = "XSRF-TOKEN=" + token + "; Path=/; SameSite=Lax" + (secureCookie ? "; Secure" : "");
        response.addHeader("Set-Cookie", cookie);
    }
}
