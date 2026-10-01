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

import com.example.CareConnect_hcl.model.entity.Medication;
import com.example.CareConnect_hcl.services.MedicationService;

@RestController
@RequestMapping("/api/medications")
@CrossOrigin
public class MedicationController {
    private final MedicationService service;
    public MedicationController(MedicationService service) { this.service = service; }
    @GetMapping public List<Medication> getAll() { return service.getAll(); }
    @GetMapping("/{id}") public Medication getById(@PathVariable Long id) { return service.getById(id); }
    @PostMapping public Medication create(@RequestBody Medication medication) { return service.create(medication); }
    @PutMapping("/{id}") public Medication update(@PathVariable Long id, @RequestBody Medication medication) { return service.update(id, medication); }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { service.delete(id); }
}
