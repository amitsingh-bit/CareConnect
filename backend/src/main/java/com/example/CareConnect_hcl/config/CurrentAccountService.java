package com.example.CareConnect_hcl.config;

import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.server.ResponseStatusException;

import com.example.CareConnect_hcl.model.entity.Account;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class CurrentAccountService {
    public static final String REQUEST_ATTRIBUTE = CurrentAccountService.class.getName() + ".account";
    private final HttpServletRequest request;

    public CurrentAccountService(HttpServletRequest request) {
        this.request = request;
    }

    public Account requireAccount() {
        Object account = request.getAttribute(REQUEST_ATTRIBUTE);
        if (account instanceof Account current) return current;
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null && attributes.getAttribute(REQUEST_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST) instanceof Account current) return current;
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in");
    }

    public Account requireRole(String... roles) {
        Account account = requireAccount();
        if (Arrays.stream(roles).noneMatch(role -> role.equalsIgnoreCase(account.getRole()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your account does not have access to this resource");
        }
        return account;
    }

    public Long profileId(String role) {
        Account account = requireRole(role);
        if (account.getProfileId() == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account profile is not linked");
        return account.getProfileId();
    }
}
