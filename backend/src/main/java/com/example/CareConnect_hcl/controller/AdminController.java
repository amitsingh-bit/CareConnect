package com.example.CareConnect_hcl.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CareConnect_hcl.model.entity.Admin;
import com.example.CareConnect_hcl.model.dto.AccountResponse;
import com.example.CareConnect_hcl.services.AdminService;

@RestController
@RequestMapping("/api/admins")
@CrossOrigin
public class AdminController {
    private final AdminService service;
    public AdminController(AdminService service) { this.service = service; }
    @GetMapping public List<Admin> getAll() { return service.getAll(); }
    @GetMapping("/users") public List<AccountResponse> getUsers() { return service.getUsers(); }
    @GetMapping("/{id}") public Admin getById(@PathVariable Long id) { return service.getById(id); }
    @PostMapping public Admin create(@RequestBody Admin admin) { return service.create(admin); }
    @PutMapping("/{id}") public Admin update(@PathVariable Long id, @RequestBody Admin admin) { return service.update(id, admin); }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { service.delete(id); }
}
