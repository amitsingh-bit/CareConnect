package com.example.CareConnect_hcl.services;

import java.util.Locale;
import java.util.regex.Pattern;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.CareConnect_hcl.model.dto.AccountResponse;
import com.example.CareConnect_hcl.model.dto.SignupRequest;
import com.example.CareConnect_hcl.model.entity.Account;
import com.example.CareConnect_hcl.model.entity.Admin;
import com.example.CareConnect_hcl.model.entity.Doctor;
import com.example.CareConnect_hcl.model.entity.Nurse;
import com.example.CareConnect_hcl.model.entity.Patient;
import com.example.CareConnect_hcl.repository.AccountRepository;
import com.example.CareConnect_hcl.repository.AdminRepository;
import com.example.CareConnect_hcl.repository.DoctorRepository;
import com.example.CareConnect_hcl.repository.NurseRepository;
import com.example.CareConnect_hcl.repository.PatientRepository;

@Service
public class AuthService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final AccountRepository accountRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AdminRepository adminRepository;
    private final NurseRepository nurseRepository;
    private final PasswordHasher passwordHasher;

    @Value("${careconnect.auth.admin-signup-code:}")
    private String adminSignupCode;

    public AuthService(AccountRepository accountRepository, PatientRepository patientRepository,
            DoctorRepository doctorRepository, AdminRepository adminRepository, NurseRepository nurseRepository,
            PasswordHasher passwordHasher) {
        this.accountRepository = accountRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.adminRepository = adminRepository;
        this.nurseRepository = nurseRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public AccountResponse register(SignupRequest request) {
        String name = clean(request.name());
        String email = clean(request.email()).toLowerCase(Locale.ROOT);
        String role = clean(request.role()).toUpperCase(Locale.ROOT);
        String password = request.password();

        if (name.isBlank()) throw badRequest("Name is required");
        if (!EMAIL_PATTERN.matcher(email).matches()) throw badRequest("Enter a valid email address");
        if (password == null || password.length() < 8) throw badRequest("Password must be at least 8 characters");
        if (accountRepository.existsByEmailIgnoreCase(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");

        Long profileId;
        switch (role) {
            case "PATIENT" -> {
                if (request.age() == null || request.age() < 0 || request.age() > 130) throw badRequest("Enter a valid age");
                Patient patient = new Patient(name, request.age(), defaultValue(request.disease(), "Not specified"));
                patient.setEmail(email);
                Patient saved = patientRepository.save(patient);
                profileId = saved.getId();
            }
            case "DOCTOR" -> {
                if (request.experience() == null || request.experience() < 0) throw badRequest("Enter years of experience");
                String department = clean(request.department());
                if (department.isBlank()) throw badRequest("Department is required");
                Doctor saved = doctorRepository.save(new Doctor(name, request.experience(), department));
                profileId = saved.getDocId();
            }
            case "ADMIN" -> {
                String submittedCode = request.adminSignupCode() == null ? "" : request.adminSignupCode();
                if (adminSignupCode.isBlank() || !MessageDigest.isEqual(
                        adminSignupCode.getBytes(StandardCharsets.UTF_8), submittedCode.getBytes(StandardCharsets.UTF_8))) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator signup requires a valid invitation code");
                }
                Admin saved = adminRepository.save(new Admin(name, email));
                profileId = saved.getId();
            }
            case "NURSE" -> {
                String department = clean(request.department());
                if (department.isBlank()) throw badRequest("Department is required");
                Nurse saved = nurseRepository.save(new Nurse(name, department, clean(request.phoneNumber())));
                profileId = saved.getId();
            }
            default -> throw badRequest("Choose patient, doctor, admin, or nurse");
        }

        Account account = accountRepository.save(new Account(email, passwordHasher.hash(password), role, name, profileId));
        return toResponse(account);
    }

    public AccountResponse login(String emailInput, String password) {
        String email = clean(emailInput).toLowerCase(Locale.ROOT);
        Account account = accountRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect"));
        if (password == null || !passwordHasher.matches(password, account.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect");
        }
        return toResponse(account);
    }

    public AccountResponse getAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session is no longer valid"));
        return toResponse(account);
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(account.getId(), account.getProfileId(), account.getDisplayName(),
                account.getEmail(), account.getRole());
    }

    private static String clean(String value) { return value == null ? "" : value.trim(); }
    private static String defaultValue(String value, String fallback) { String cleaned = clean(value); return cleaned.isBlank() ? fallback : cleaned; }
    private static ResponseStatusException badRequest(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
}
