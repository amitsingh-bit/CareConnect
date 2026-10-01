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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String ACCOUNT_ID_SESSION_KEY = "careconnect.accountId";
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public AccountResponse signup(@RequestBody SignupRequest request, HttpServletRequest httpRequest) {
        AccountResponse account = authService.register(request);
        startSession(httpRequest, account);
        return account;
    }

    @PostMapping("/login")
    public AccountResponse login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        AccountResponse account = authService.login(request.email(), request.password());
        startSession(httpRequest, account);
        return account;
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
        if (session != null) session.invalidate();
    }

    private void startSession(HttpServletRequest request, AccountResponse account) {
        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute(ACCOUNT_ID_SESSION_KEY, account.id());
    }
}
