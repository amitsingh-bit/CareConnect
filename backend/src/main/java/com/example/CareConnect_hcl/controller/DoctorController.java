package com.example.CareConnect_hcl.controller;

import com.example.CareConnect_hcl.model.entity.Doctor;
import com.example.CareConnect_hcl.services.DoctorService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin
public class DoctorController {

    private final DoctorService service;

    public DoctorController(DoctorService service) {
        this.service = service;
    }

    @GetMapping
    public List<Doctor> getAll() {
        return service.getAll();
    }

    @GetMapping("/directory")
    public List<Doctor> getDirectory() {
        return service.getDirectory();
    }

    @GetMapping("/{id}")
    public Doctor getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public Doctor save(@RequestBody Doctor doctor) {
        return service.save(doctor);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
